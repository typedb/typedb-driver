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
    collections::{HashMap, HashSet},
    iter, mem,
    path::{Path, PathBuf},
};

use cucumber::{gherkin::Feature, StatsWriter, World};
use futures::{
    future::{Either, try_join_all},
    stream::{self, StreamExt},
};
use itertools::Itertools;
use tokio::time::{Duration, sleep};

use typedb_driver::{
    answer::{ConceptRow, JSON},
    concept::{Value},
    Connection, Credential, Database, DatabaseManager, Options, Result as TypeDBResult, Transaction, UserManager,
};

use self::transaction_tracker::TransactionTracker;

mod connection;
mod driver;
mod parameter;
mod query;
mod transaction_tracker;
mod util;

#[derive(Debug, Default)]
struct SingletonParser {
    basic: cucumber::parser::Basic,
}

impl<I: AsRef<Path>> cucumber::Parser<I> for SingletonParser {
    type Cli = <cucumber::parser::Basic as cucumber::Parser<I>>::Cli;
    type Output = stream::FlatMap<
        stream::Iter<std::vec::IntoIter<Result<Feature, cucumber::parser::Error>>>,
        Either<
            stream::Iter<std::vec::IntoIter<Result<Feature, cucumber::parser::Error>>>,
            stream::Iter<iter::Once<Result<Feature, cucumber::parser::Error>>>,
        >,
        fn(
            Result<Feature, cucumber::parser::Error>,
        ) -> Either<
            stream::Iter<std::vec::IntoIter<Result<Feature, cucumber::parser::Error>>>,
            stream::Iter<iter::Once<Result<Feature, cucumber::parser::Error>>>,
        >,
    >;

    fn parse(self, input: I, cli: Self::Cli) -> Self::Output {
        self.basic.parse(input, cli).flat_map(|res| match res {
            Ok(mut feature) => {
                let scenarios = mem::take(&mut feature.scenarios);
                let singleton_features = scenarios
                    .into_iter()
                    .map(|scenario| {
                        Ok(Feature {
                            name: feature.name.clone() + " :: " + &scenario.name,
                            scenarios: vec![scenario],
                            ..feature.clone()
                        })
                    })
                    .collect_vec();
                Either::Left(stream::iter(singleton_features))
            }
            Err(err) => Either::Right(stream::iter(iter::once(Err(err)))),
        })
    }
}

#[derive(Debug, World)]
pub struct Context {
    pub tls_root_ca: PathBuf,
    pub session_options: Options,
    pub transaction_options: Options,
    pub connection: Connection,
    pub databases: DatabaseManager,
    pub users: UserManager,
    pub transaction_trackers: Vec<TransactionTracker>,
    pub answer: Vec<ConceptRow>,
    pub fetch_answer: Option<JSON>,
    pub value_answer: Option<Option<Value>>,
}

impl Context {
    const GROUP_COLUMN_NAME: &'static str = "owner";
    const VALUE_COLUMN_NAME: &'static str = "value";
    const DEFAULT_DATABASE: &'static str = "test";
    const ADMIN_USERNAME: &'static str = "admin";
    const ADMIN_PASSWORD: &'static str = "password";
    const STEP_REATTEMPT_SLEEP: Duration = Duration::from_millis(250);
    const STEP_REATTEMPT_LIMIT: u32 = 20;

    async fn test(glob: &'static str) -> bool {
        let default_panic = std::panic::take_hook();
        std::panic::set_hook(Box::new(move |info| {
            default_panic(info);
            std::process::exit(1);
        }));

        !Self::cucumber::<&str>()
            .repeat_failed()
            .fail_on_skipped()
            .max_concurrent_scenarios(Some(1))
            .with_parser(SingletonParser::default())
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
        t == "ignore" || t == "ignore-typedb-driver" || t == "ignore-typedb-driver-rust"
    }

    pub async fn after_scenario(&mut self) -> TypeDBResult {
        sleep(Context::STEP_REATTEMPT_SLEEP).await;
        self.session_options = Options::new();
        self.transaction_options = Options::new();
        self.set_connection(Connection::new_cloud(
            &["localhost:11729", "localhost:21729", "localhost:31729"],
            Credential::with_tls(Context::ADMIN_USERNAME, Context::ADMIN_PASSWORD, Some(&self.tls_root_ca))?,
        )?);
        self.cleanup_databases().await;
        self.cleanup_users().await;
        Ok(())
    }

    pub async fn all_databases(&self) -> HashSet<String> {
        self.databases.all().await.unwrap().into_iter().map(|db| db.name().to_owned()).collect::<HashSet<_>>()
    }

    pub async fn cleanup_databases(&mut self) {
        try_join_all(self.databases.all().await.unwrap().into_iter().map(Database::delete)).await.ok();
    }

    pub async fn cleanup_users(&mut self) {
        try_join_all(
            self.users
                .all()
                .await
                .unwrap()
                .into_iter()
                .filter(|user| user.username != Context::ADMIN_USERNAME)
                .map(|user| self.users.delete(user.username)),
        )
        .await
        .ok();
    }

    pub fn transaction(&self) -> &Transaction {
        self.transaction_trackers.get(0).unwrap().transaction()
    }

    pub fn take_transaction(&mut self) -> Transaction {
        self.transaction_trackers.get_mut(0).unwrap().take_transaction()
    }

    pub fn set_connection(&mut self, new_connection: Connection) {
        self.connection = new_connection;
        self.databases = DatabaseManager::new(self.connection.clone());
        self.users = UserManager::new(self.connection.clone());
        self.transaction_trackers.clear();
        self.answer.clear();
        self.fetch_answer = None;
        self.value_answer = None;
    }
}

impl Default for Context {
    fn default() -> Self {
        let tls_root_ca = PathBuf::from(
            std::env::var("ROOT_CA").expect("ROOT_CA environment variable needs to be set for cloud tests to run"),
        );
        let session_options = Options::new();
        let transaction_options = Options::new();
        let connection = Connection::new_cloud(
            &["localhost:11729", "localhost:21729", "localhost:31729"],
            Credential::with_tls(Context::ADMIN_USERNAME, Context::ADMIN_PASSWORD, Some(&tls_root_ca)).unwrap(),
        )
        .unwrap();
        let databases = DatabaseManager::new(connection.clone());
        let users = UserManager::new(connection.clone());
        Self {
            tls_root_ca,
            session_options,
            transaction_options,
            connection,
            databases,
            users,
            transaction_trackers: Vec::new(),
            answer: Vec::new(),
            fetch_answer: None,
            value_answer: None,
        }
    }
}

#[macro_export]
macro_rules! generic_step_impl {
    {$($(#[step($pattern:expr)])+ $vis:vis $async:ident fn $fn_name:ident $args:tt $(-> $res:ty)? $body:block)+} => {
        $($(
        #[given($pattern)]
        #[when($pattern)]
        #[then($pattern)]
        )*
        $vis $async fn $fn_name $args $(-> $res)? $body
        )*
    };
}

#[macro_export]
macro_rules! assert_err {
    ($expr:expr) => {{
        let res = $expr;
        assert!(res.is_err(), "{res:?}")
    }};
}
