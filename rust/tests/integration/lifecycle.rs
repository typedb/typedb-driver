/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

use std::{
    sync::{
        Arc,
        atomic::{AtomicBool, Ordering},
    },
    thread,
    time::Duration,
};

use serial_test::serial;
use typedb_driver::{Addresses, Credentials, DriverOptions, DriverTlsConfig, TransactionType, TypeDBDriver};

const WATCHDOG: Duration = Duration::from_secs(60);

async fn new_driver() -> TypeDBDriver {
    TypeDBDriver::new(
        Addresses::try_from_address_str(TypeDBDriver::DEFAULT_ADDRESS).unwrap(),
        Credentials::new("admin", "password"),
        DriverOptions::new(DriverTlsConfig::disabled()),
    )
    .await
    .unwrap()
}

/// Each test owns its own database: some of them deliberately leave a transaction open, which would
/// otherwise stop a later test from deleting a shared one.
async fn reset_database(driver: &TypeDBDriver, database: &str) {
    if driver.databases().contains(database).await.unwrap() {
        driver.databases().get(database).await.unwrap().delete().await.unwrap();
    }
    driver.databases().create(database).await.unwrap();
}

/// Leaves the server as the test found it. Other suites assert that no databases exist at all, so a
/// leftover here fails them rather than this one. Retried: a database whose transaction was left open
/// only becomes deletable once the server notices that its connection is gone.
async fn drop_database(database: &str) {
    let driver = new_driver().await;
    for attempt in 0..20 {
        if !driver.databases().contains(database).await.unwrap_or(false) {
            return;
        }
        if let Ok(handle) = driver.databases().get(database).await
            && handle.delete().await.is_ok()
        {
            return;
        }
        thread::sleep(Duration::from_millis(50 * (attempt + 1)));
    }
    panic!("could not clean up database '{database}'");
}

/// Runs `body` to completion, aborting the whole process if it deadlocks. A plain test timeout would
/// not do: a deadlocked driver drop hangs the test binary without failing it.
fn with_watchdog<F: std::future::Future<Output = ()>>(name: &'static str, body: F) {
    let finished = Arc::new(AtomicBool::new(false));
    let watchdog = {
        let finished = finished.clone();
        thread::spawn(move || {
            let deadline = std::time::Instant::now() + WATCHDOG;
            while std::time::Instant::now() < deadline {
                if finished.load(Ordering::SeqCst) {
                    return;
                }
                thread::sleep(Duration::from_millis(50));
            }
            eprintln!("DEADLOCK: '{name}' did not finish within {WATCHDOG:?}");
            std::process::abort();
        })
    };
    async_std::task::block_on(body);
    finished.store(true, Ordering::SeqCst);
    watchdog.join().unwrap();
}

#[test]
#[serial]
fn drop_after_successful_work() {
    const DB: &str = "lifecycle_drop_after_successful_work";
    with_watchdog("drop_after_successful_work", async {
        let driver = new_driver().await;
        reset_database(&driver, DB).await;
        let transaction = driver.transaction(DB, TransactionType::Schema).await.unwrap();
        transaction.query("define entity person;").await.unwrap();
        transaction.commit().await.unwrap();
        drop(driver);
        drop_database(DB).await;
    });
}

#[test]
#[serial]
fn drop_with_open_transaction() {
    const DB: &str = "lifecycle_drop_with_open_transaction";
    with_watchdog("drop_with_open_transaction", async {
        let driver = new_driver().await;
        reset_database(&driver, DB).await;
        let transaction = driver.transaction(DB, TransactionType::Write).await.unwrap();
        // Dropped in the opposite order to the usual one: the driver goes first, while the
        // transaction still holds a reference to the shared runtime.
        drop(driver);
        drop(transaction);
        drop_database(DB).await;
    });
}

#[test]
#[serial]
fn force_close_then_drop() {
    const DB: &str = "lifecycle_force_close_then_drop";
    with_watchdog("force_close_then_drop", async {
        let driver = new_driver().await;
        reset_database(&driver, DB).await;
        let transaction = driver.transaction(DB, TransactionType::Read).await.unwrap();
        driver.force_close().unwrap();
        assert!(!driver.is_open());
        drop(transaction);
        drop(driver);
        drop_database(DB).await;
    });
}

#[test]
#[serial]
fn drop_after_failed_query() {
    const DB: &str = "lifecycle_drop_after_failed_query";
    with_watchdog("drop_after_failed_query", async {
        let driver = new_driver().await;
        reset_database(&driver, DB).await;
        let transaction = driver.transaction(DB, TransactionType::Read).await.unwrap();
        assert!(transaction.query("this is not typeql").await.is_err());
        drop(transaction);
        drop(driver);
        drop_database(DB).await;
    });
}

#[test]
#[serial]
fn drop_after_failed_connection() {
    with_watchdog("drop_after_failed_connection", async {
        let result = TypeDBDriver::new(
            Addresses::try_from_address_str("127.0.0.1:1").unwrap(),
            Credentials::new("admin", "password"),
            DriverOptions::new(DriverTlsConfig::disabled()),
        )
        .await;
        assert!(result.is_err());
    });
}

/// Waits for `flag`, so that a callback which never runs fails the test instead of hanging it.
fn await_flag(flag: &AtomicBool, what: &str) {
    for _ in 0..100 {
        if flag.load(Ordering::SeqCst) {
            return;
        }
        thread::sleep(Duration::from_millis(50));
    }
    panic!("{what} did not happen within 5s");
}

/// Close callbacks are dispatched to a thread that shutdown has to stop in the right order: too early
/// and a transaction closing against a driver being dropped would never hear back.
#[test]
#[serial]
fn close_callback_runs_when_driver_is_dropped() {
    const DB: &str = "lifecycle_close_callback_runs_when_driver_is_dropped";
    with_watchdog("close_callback_runs_when_driver_is_dropped", async {
        let driver = new_driver().await;
        reset_database(&driver, DB).await;
        let transaction = driver.transaction(DB, TransactionType::Read).await.unwrap();
        let callback_ran = Arc::new(AtomicBool::new(false));
        let flag = callback_ran.clone();
        transaction.on_close(move |_| flag.store(true, Ordering::SeqCst)).await.unwrap();
        drop(transaction);
        await_flag(&callback_ran, "close callback");
        drop(driver);
        drop_database(DB).await;
    });
}

/// A close callback that owns the driver: running it releases a reference from the very thread that
/// dispatches callbacks.
#[test]
#[serial]
fn drop_driver_from_within_its_own_close_callback() {
    const DB: &str = "lifecycle_drop_driver_from_within_its_own_close_callback";
    with_watchdog("drop_driver_from_within_its_own_close_callback", async {
        let setup = new_driver().await;
        reset_database(&setup, DB).await;
        drop(setup);

        let driver = new_driver().await;
        let transaction = driver.transaction(DB, TransactionType::Read).await.unwrap();
        let callback_ran = Arc::new(AtomicBool::new(false));
        let flag = callback_ran.clone();
        transaction
            .on_close(move |_| {
                drop(driver);
                flag.store(true, Ordering::SeqCst);
            })
            .await
            .unwrap();
        drop(transaction);
        await_flag(&callback_ran, "close callback owning the driver");
        drop_database(DB).await;
    });
}

/// Export and import hold their own references to the shared runtime, through a separate transmitter
/// from the one transactions use.
#[test]
#[serial]
fn drop_after_export_and_import() {
    const DB: &str = "lifecycle_drop_after_export_and_import";
    const IMPORTED: &str = "lifecycle_drop_after_export_and_import_copy";
    with_watchdog("drop_after_export_and_import", async {
        let driver = new_driver().await;
        reset_database(&driver, DB).await;
        let transaction = driver.transaction(DB, TransactionType::Schema).await.unwrap();
        transaction.query("define entity person;").await.unwrap();
        transaction.commit().await.unwrap();

        let directory = std::env::temp_dir().join(format!("typedb-lifecycle-{}", std::process::id()));
        std::fs::create_dir_all(&directory).unwrap();
        let schema_file = directory.join("schema.tql");
        let data_file = directory.join("data.typedb");
        let _ = std::fs::remove_file(&schema_file);
        let _ = std::fs::remove_file(&data_file);

        driver.databases().get(DB).await.unwrap().export_to_file(&schema_file, &data_file).await.unwrap();
        if driver.databases().contains(IMPORTED).await.unwrap() {
            driver.databases().get(IMPORTED).await.unwrap().delete().await.unwrap();
        }
        let schema = std::fs::read_to_string(&schema_file).unwrap();
        driver.databases().import_from_file(IMPORTED, schema, &data_file).await.unwrap();
        assert!(driver.databases().contains(IMPORTED).await.unwrap());

        driver.databases().get(IMPORTED).await.unwrap().delete().await.unwrap();
        drop(driver);
        let _ = std::fs::remove_dir_all(&directory);
        drop_database(DB).await;
    });
}

/// Counts the OS threads of the current process, where that can be done cheaply enough to observe a
/// thread that is only briefly still alive. Anything slower (spawning `ps`, say) takes long enough
/// for a leaked thread to finish on its own, which would make the assertion below meaningless.
fn thread_count() -> Option<usize> {
    std::fs::read_dir("/proc/self/task").ok().map(Iterator::count)
}

/// Dropping a driver must not leave its threads running: teardown that outlives the drop is exactly
/// what races process exit, which is only observable as a crash under a sanitiser. Asserted here
/// directly instead - on platforms where the measurement is sharp enough to mean anything.
#[test]
#[serial]
fn threads_are_gone_once_drop_returns() {
    const DB: &str = "lifecycle_threads_are_gone_once_drop_returns";
    if thread_count().is_none() {
        eprintln!("SKIPPED: no /proc, cannot count threads precisely enough");
        return;
    }
    with_watchdog("threads_are_gone_once_drop_returns", async {
        let setup = new_driver().await;
        reset_database(&setup, DB).await;
        drop(setup);

        let baseline = thread_count().unwrap();
        for _ in 0..5 {
            let driver = new_driver().await;
            let transaction = driver.transaction(DB, TransactionType::Read).await.unwrap();
            transaction.query("match $t sub $_;").await.unwrap();
            drop(transaction);
            drop(driver);
            let after_drop = thread_count().unwrap();
            assert!(
                after_drop <= baseline,
                "driver threads outlived drop: {after_drop} threads against a baseline of {baseline}"
            );
        }
        drop_database(DB).await;
    });
}

#[test]
#[serial]
fn repeated_open_and_close_cycles() {
    const DB: &str = "lifecycle_repeated_open_and_close_cycles";
    with_watchdog("repeated_open_and_close_cycles", async {
        let setup = new_driver().await;
        reset_database(&setup, DB).await;
        drop(setup);
        for _ in 0..25 {
            let driver = new_driver().await;
            let transaction = driver.transaction(DB, TransactionType::Read).await.unwrap();
            transaction.query("match $t sub $_;").await.unwrap();
            drop(transaction);
            drop(driver);
        }
        drop_database(DB).await;
    });
}

/// Dropping a driver from inside a Tokio context must not panic: teardown has to stay off the
/// caller's thread, because dropping a runtime from an asynchronous context aborts.
#[test]
#[serial]
fn drop_inside_tokio_runtime() {
    const DB: &str = "lifecycle_drop_inside_tokio_runtime";
    let finished = Arc::new(AtomicBool::new(false));
    let watchdog = {
        let finished = finished.clone();
        thread::spawn(move || {
            let deadline = std::time::Instant::now() + WATCHDOG;
            while std::time::Instant::now() < deadline {
                if finished.load(Ordering::SeqCst) {
                    return;
                }
                thread::sleep(Duration::from_millis(50));
            }
            eprintln!("DEADLOCK: 'drop_inside_tokio_runtime' did not finish");
            std::process::abort();
        })
    };
    let runtime = tokio::runtime::Builder::new_multi_thread().enable_all().build().unwrap();
    runtime.block_on(async {
        let driver = new_driver().await;
        reset_database(&driver, DB).await;
        let transaction = driver.transaction(DB, TransactionType::Read).await.unwrap();
        drop(transaction);
        drop(driver);
        drop_database(DB).await;
    });
    finished.store(true, Ordering::SeqCst);
    watchdog.join().unwrap();
}
