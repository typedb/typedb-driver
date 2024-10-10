/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

#![deny(unused_must_use)]
#![deny(elided_lifetimes_in_paths)]

use std::{
    collections::{HashMap, HashSet},
    iter, mem,
    path::{Path, PathBuf},
};

use cucumber::{gherkin::Feature, StatsWriter, World};
use futures::{
    future::{try_join_all, Either},
    stream::{self, StreamExt},
};
use itertools::Itertools;
use tokio::time::{sleep, Duration};
use typedb_driver::{
    answer::{ConceptRow, JSON},
    concept::Value,
    Credential, Database, DatabaseManager, Options, Result as TypeDBResult, Transaction, TypeDBDriver, UserManager,
};

use self::transaction_tracker::TransactionTracker;

mod connection;
mod query;
mod transaction_tracker;
mod parameter;
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
    pub transaction_options: Options,
    pub driver: Option<TypeDBDriver>,
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

    pub async fn test<I: AsRef<Path>>(glob: I) -> bool {
        let default_panic = std::panic::take_hook();
        std::panic::set_hook(Box::new(move |info| {
            default_panic(info);
            std::process::exit(1);
        }));

        !Self::cucumber::<I>()
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
        self.transaction_options = Options::new();
        self.cleanup_databases().await;
        // self.cleanup_users().await;
        self.set_connection(None);
        Ok(())
    }

    pub async fn all_databases(&self) -> HashSet<String> {
        self.driver.as_ref().unwrap().databases().all().await.unwrap().into_iter().map(|db| db.name().to_owned()).collect::<HashSet<_>>()
    }

    pub async fn cleanup_databases(&mut self) {
        try_join_all(self.driver.as_ref().unwrap().databases().all().await.unwrap().into_iter().map(|db| db.delete())).await.unwrap();
    }

    pub async fn cleanup_users(&mut self) {
        try_join_all(
            self.driver.as_ref().unwrap()
                .users()
                .all()
                .await
                .unwrap()
                .into_iter()
                .filter(|user| user.username != Context::ADMIN_USERNAME)
                .map(|user| self.driver.as_ref().unwrap().users().delete(user.username)),
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

    pub fn set_connection(&mut self, new_connection: Option<TypeDBDriver>) {
        self.driver = new_connection;
        self.transaction_trackers.clear();
        self.answer.clear();
        self.fetch_answer = None;
        self.value_answer = None;
    }
}

#[cfg(not(feature = "cloud"))]
impl Default for Context {
    fn default() -> Self {
        let transaction_options = Options::new();
        Self {
            tls_root_ca: PathBuf::new(), // unused for core
            transaction_options,
            driver: None,
            transaction_trackers: Vec::new(),
            answer: Vec::new(),
            fetch_answer: None,
            value_answer: None,
        }
    }
}

#[cfg(feature = "cloud")]
impl Default for Context {
    fn default() -> Self {
        let tls_root_ca = PathBuf::from(
            std::env::var("ROOT_CA").expect("ROOT_CA environment variable needs to be set for cloud tests to run"),
        );
        let transaction_options = Options::new();
        Self {
            tls_root_ca,
            transaction_options,
            driver: None,
            transaction_trackers: Vec::new(),
            answer: Vec::new(),
            fetch_answer: None,
            value_answer: None,
        }
    }
}

#[macro_export]
macro_rules! generic_step {
    {$(#[step($pattern:expr)])+ $vis:vis $async:ident fn $fn_name:ident $args:tt $(-> $res:ty)? $body:block} => {
        #[allow(unused)]
        $vis $async fn $fn_name $args $(-> $res)? $body

        const _: () = {
            $(
            #[::cucumber::given($pattern)]
            #[::cucumber::when($pattern)]
            #[::cucumber::then($pattern)]
            )+
            $vis $async fn step $args $(-> $res)? $body
        };
    };
}

#[macro_export]
macro_rules! assert_err {
    ($expr:expr) => {{
        let res = $expr;
        assert!(res.is_err(), "{res:?}")
    }};
}
