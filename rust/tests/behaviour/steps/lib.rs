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
use std::collections::VecDeque;
use std::env::VarError;

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
use typedb_driver::answer::QueryAnswer;

mod connection;
mod query;
mod params;
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
    pub is_cloud: bool,
    pub tls_root_ca: PathBuf,
    pub transaction_options: Options,
    pub driver: Option<TypeDBDriver>,
    pub transactions: VecDeque<Transaction>,
    // pub answer: Vec<ConceptRow>,
    pub answer: Option<QueryAnswer>,
    pub fetch_answer: Option<JSON>,
    pub value_answer: Option<Option<Value>>,
}

impl Context {
    const DEFAULT_CORE_ADDRESS: &'static str = "127.0.0.1:1729";
    const DEFAULT_CLOUD_ADDRESSES: [&'static str; 3] = ["127.0.0.1:11729", "127.0.0.1:21729", "127.0.0.1:31729"];
    const DEFAULT_DATABASE: &'static str = "test";
    const ADMIN_USERNAME: &'static str = "admin";
    const ADMIN_PASSWORD: &'static str = "password";
    const STEP_REATTEMPT_SLEEP: Duration = Duration::from_millis(250);
    const STEP_REATTEMPT_LIMIT: u32 = 20;

    pub async fn test<I: AsRef<Path>>(glob: I, is_cloud: bool) -> bool {
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
            .before(move |_, _, _, context| {
                context.is_cloud = is_cloud;
                // cucumber removes the default hook before each scenario and restores it after!
                std::panic::set_hook(Box::new(move |info| println!("{}", info)));
                Box::pin(async move {})

            })
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
        self.cleanup_transactions().await;
        self.cleanup_databases().await;
        // self.cleanup_users().await;
        self.cleanup_answers().await;
        self.reset_driver();
        Ok(())
    }

    pub async fn all_databases(&self) -> HashSet<String> {
        self.driver.as_ref().unwrap().databases().all().await.unwrap().into_iter().map(|db| db.name().to_owned()).collect::<HashSet<_>>()
    }

    pub async fn cleanup_databases(&mut self) {
        if self.driver.is_none() || !self.driver.as_ref().unwrap().is_open() {
            self.create_default_driver(Some(Self::ADMIN_USERNAME), Some(Self::ADMIN_PASSWORD)).await.unwrap();
        }

        try_join_all(self.driver.as_ref().unwrap().databases().all().await.unwrap().into_iter().map(|db| db.delete())).await.unwrap();
    }

    pub async fn cleanup_transactions(&mut self) {
        while let Some(transaction) = self.try_take_transaction() {
            transaction.force_close();
        }
    }

    pub async fn cleanup_users(&mut self) {
        if self.driver.is_none() || !self.driver.as_ref().unwrap().is_open() {
            return;
        }

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

    pub async fn cleanup_answers(&mut self) {
        self.answer = None;
        self.fetch_answer = None;
        self.value_answer = None;
    }

    pub fn transaction_opt(&self) -> Option<&Transaction> {
        self.transactions.get(0)
    }

    pub fn transaction(&self) -> &Transaction {
        self.transactions.get(0).unwrap()
    }

    pub fn take_transaction(&mut self) -> Transaction {
        self.transactions.pop_front().unwrap()
    }

    pub fn try_take_transaction(&mut self) -> Option<Transaction> {
        self.transactions.pop_front()
    }

    pub fn push_transaction(&mut self, transaction: TypeDBResult<Transaction>) -> TypeDBResult {
        self.transactions.push_back(transaction?);
        Ok(())
    }

    pub async fn set_transactions(&mut self, transactions: VecDeque<Transaction>) {
        self.cleanup_transactions().await;
        self.transactions = transactions;
    }

    pub fn set_answer(&mut self, answer: TypeDBResult<QueryAnswer>) -> TypeDBResult {
        self.answer = Some(answer?);
        Ok(())
    }

    async fn create_default_driver(&mut self, username: Option<&str>, password: Option<&str>) -> TypeDBResult {
        match self.is_cloud {
            false => self.create_core_driver(Self::DEFAULT_CORE_ADDRESS, username, password).await,
            true => self.create_cloud_driver(&Self::DEFAULT_CLOUD_ADDRESSES, username, password).await,
        }
    }

    async fn create_core_driver(&mut self, address: &str, _username: Option<&str>, _password: Option<&str>) -> TypeDBResult {
        assert!(!self.is_cloud);
        let driver = TypeDBDriver::new_core(address).await?;
        self.driver = Some(driver);
        Ok(())
    }

    async fn create_cloud_driver(&mut self, addresses: &[&str], username: Option<&str>, password: Option<&str>) -> TypeDBResult {
        assert!(self.is_cloud);
        let driver = TypeDBDriver::new_cloud(
            addresses,
            Credential::with_tls(
                username.expect("Username is required for cloud connection"),
                password.expect("Password is required for cloud connection"),
                Some(&self.tls_root_ca),
            ).unwrap()
        )?;
        self.driver = Some(driver);
        Ok(())
    }

    pub fn reset_driver(&mut self) {
        if let Some(driver) = self.driver.as_ref() {
            driver.force_close().unwrap()
        }
        self.driver = None
    }
}

impl Default for Context {
    fn default() -> Self {
        let transaction_options = Options::new();
        let tls_root_ca = match std::env::var("ROOT_CA") {
            Ok(root_ca) => PathBuf::from(root_ca),
            Err(_) => PathBuf::new(),
        };
        Self {
            is_cloud: false,
            tls_root_ca,
            transaction_options,
            driver: None,
            transactions: VecDeque::new(),
            // answer: Vec::new(),
            answer: None,
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
