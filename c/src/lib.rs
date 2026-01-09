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

mod analyze;
mod answer;
mod common;
mod concept;
mod connection;
mod database;
mod database_manager;
mod error;
mod iterator;
mod memory;
mod promise;
mod query_options;
mod transaction;
mod transaction_options;
mod user;
mod user_manager;

use tracing_subscriber::{fmt as tracing_fmt, fmt, layer::SubscriberExt, util::SubscriberInitExt, EnvFilter, Layer};
use tracing_subscriber::fmt::SubscriberBuilder;

/// Enables logging in the TypeDB driver.
///
///  This function sets up tracing with the following priority:
///  1. TYPEDB_DRIVER_LOG environment variable (if set). Use TYPEDB_DRIVER_CLIB_LOG to see memory exchanges
///  2. RUST_LOG environment variable (if set)
///  3. Default level (INFO)
///
///  The logging is initialized only once using a static flag to prevent
///  multiple initializations in applications that create multiple drivers.
#[no_mangle]
pub extern "C" fn init_logging() {
    const ENV_RUST_LOG: &str = "RUST_LOG";
    const ENV_TYPEDB_DRIVER_LOG: &str = "TYPEDB_DRIVER_LOG";
    const ENV_TYPEDB_DRIVER_CLIB_LOG: &str = "TYPEDB_DRIVER_CLIB_LOG";
    const TYPEDB_DRIVER_TARGET: &str = "typedb_driver";
    const TYPEDB_DRIVER_CLIB_TARGET: &str = "typedb_driver_clib";

    use std::sync::Once;
    static INIT: Once = Once::new();
    INIT.call_once(|| {
        // build a single directive string ordered by precedence (Left = Lowest, Right = Highest).

        // 1. Start with defaults.
        let mut directives = format!("{}={}", TYPEDB_DRIVER_TARGET, "info");
        directives.push_str(&format!(",{}=info", TYPEDB_DRIVER_CLIB_TARGET));

        // 2. Append RUST_LOG.
        // If RUST_LOG contains "typedb_driver=debug", it appears after default, so overrides it
        if let Ok(rust_log) = std::env::var(ENV_RUST_LOG) {
            directives.push_str(",");
            directives.push_str(&rust_log);
        }

        // 3. Append specific overrides, if they exist
        if let Ok(level) = std::env::var(ENV_TYPEDB_DRIVER_LOG) {
            directives.push_str(&format!(",{}={}", TYPEDB_DRIVER_TARGET, level));
        }
        if let Ok(level) = std::env::var(ENV_TYPEDB_DRIVER_CLIB_LOG) {
            directives.push_str(&format!(",{}={}", TYPEDB_DRIVER_CLIB_TARGET, level));
        }

        // 4. Build the filter
        // parse_lossy ensures that if the user makes a typo in RUST_LOG the defaults still load.
        let env_filter = EnvFilter::builder().parse_lossy(directives);

        if let Err(e) = tracing_subscriber::registry()
            .with(env_filter)
            .with(fmt::layer().with_target(false))
            .try_init()
        {
            eprintln!("Failed to initialize logging: {}", e);
        }
    });
}
