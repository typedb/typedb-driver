/*
 * Copyright (C) 2022 Vaticle
 *
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

mod connection;
pub mod session_tracker;
mod typeql;
mod util;

use cucumber::{StatsWriter, World};
use futures::future::try_join_all;
use typedb_client::{Connection, Database, DatabaseManager, Result as TypeDBResult, Transaction};

use self::session_tracker::SessionTracker;

#[derive(Debug, World)]
pub struct Context {
    pub connection: Connection,
    pub databases: DatabaseManager,
    pub session_trackers: Vec<SessionTracker>,
}

impl Context {
    async fn test(glob: &'static str) -> bool {
        !Self::cucumber()
            .repeat_failed()
            .fail_on_skipped()
            .max_concurrent_scenarios(Some(1))
            .with_default_cli()
            .after(|_, _, _, _, context| {
                Box::pin(async {
                    context.unwrap().after_scenario().await.unwrap();
                })
            })
            .filter_run(glob, |_, _, sc| !sc.tags.iter().any(Self::is_ignore_tag))
            .await
            .execution_has_failed()
    }

    fn is_ignore_tag(t: &String) -> bool {
        t == "ignore" || t == "ignore-typedb" || t == "ignore-client-rust" || t == "ignore-typedb-client-rust"
    }

    async fn after_scenario(&self) -> TypeDBResult {
        try_join_all(self.databases.all().await.unwrap().into_iter().map(Database::delete)).await?;
        Ok(())
    }

    fn transaction(&self) -> &Transaction {
        self.session_trackers.get(0).unwrap().transaction()
    }

    pub fn take_transaction(&mut self) -> Transaction {
        self.session_trackers.get_mut(0).unwrap().take_transaction()
    }
}

impl Default for Context {
    fn default() -> Self {
        let connection = Connection::new_plaintext("0.0.0.0:1729").unwrap();
        let databases = DatabaseManager::new(connection.clone());
        Self { connection, databases, session_trackers: Vec::new() }
    }
}

#[macro_export]
macro_rules! generic_step_impl {
    {$($(#[step($pattern:expr)])+ $async:ident fn $fn_name:ident $args:tt $body:tt)+} => {
        $($(
        #[given($pattern)]
        #[when($pattern)]
        #[then($pattern)]
        )*
        $async fn $fn_name $args $body
        )*
    };
}
