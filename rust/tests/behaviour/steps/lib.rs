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

#![deny(unused_must_use)]
#![deny(elided_lifetimes_in_paths)]

use std::{
    collections::{HashSet, VecDeque},
    error::Error,
    fmt,
    fmt::Formatter,
    iter, mem,
    path::{Path, PathBuf},
};

use cucumber::{gherkin::Feature, StatsWriter, World};
use futures::{
    future::Either,
    stream::{self, StreamExt},
};
use itertools::Itertools;
use tokio::time::{sleep, Duration};
use typedb_driver::{
    answer::{ConceptDocument, ConceptRow, QueryAnswer, QueryType},
    BoxStream, Credential, Options, Result as TypeDBResult, Transaction, TypeDBDriver,
};

use crate::params::QueryAnswerType;

mod connection;
mod params;
mod query;
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

#[derive(World)]
pub struct Context {
    pub is_cloud: bool,
    pub tls_root_ca: PathBuf,
    pub transaction_options: Options,
    pub driver: Option<TypeDBDriver>,
    pub transactions: VecDeque<Transaction>,
    pub answer: Option<QueryAnswer>,
    pub answer_type: Option<QueryAnswerType>,
    pub answer_query_type: Option<QueryType>,
    pub collected_rows: Option<Vec<ConceptRow>>,
    pub collected_documents: Option<Vec<ConceptDocument>>,
    pub concurrent_answers: Vec<QueryAnswer>,
    pub concurrent_rows_streams: Option<Vec<BoxStream<'static, TypeDBResult<ConceptRow>>>>,
}

impl fmt::Debug for Context {
    fn fmt(&self, f: &mut Formatter<'_>) -> fmt::Result {
        f.debug_struct("Context")
            .field("is_cloud", &self.is_cloud)
            .field("tls_root_ca", &self.tls_root_ca)
            .field("transaction_options", &self.transaction_options)
            .field("driver", &self.driver)
            .field("transactions", &self.transactions)
            .field("answer", &self.answer)
            .field("answer_type", &self.answer_type)
            .field("answer_query_type", &self.answer_query_type)
            .field("collected_rows", &self.collected_rows)
            .field("collected_documents", &self.collected_documents)
            .field("concurrent_answers", &self.concurrent_answers)
            .field(
                "concurrent_rows_streams",
                &self.concurrent_rows_streams.as_ref().map(|streams| format!("{} streams", streams.len())),
            )
            .finish()
    }
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
        self.cleanup_concurrent_answers().await;
        self.reset_driver();
        Ok(())
    }

    pub fn all_databases(&self) -> HashSet<String> {
        self.driver
            .as_ref()
            .unwrap()
            .databases()
            .all()
            .unwrap()
            .into_iter()
            .map(|db| db.name().to_owned())
            .collect::<HashSet<_>>()
    }

    pub async fn cleanup_databases(&mut self) {
        if self.driver.is_none() || !self.driver.as_ref().unwrap().is_open() {
            self.create_default_driver(Some(Self::ADMIN_USERNAME), Some(Self::ADMIN_PASSWORD)).await.unwrap();
        }

        for db in self.driver.as_ref().unwrap().databases().all().unwrap() {
            db.delete().unwrap()
        }
    }

    pub async fn cleanup_transactions(&mut self) {
        while let Some(transaction) = self.try_take_transaction() {
            transaction.force_close();
        }
    }

    pub async fn cleanup_users(&mut self) {
        let driver = self.driver.as_ref().unwrap();
        if self.driver.is_none() || !driver.is_open() {
            return;
        }

        for user in driver.users().all().unwrap() {
            if user.username == Context::ADMIN_USERNAME {
                continue;
            }
            driver.users().delete(user.username).ok();
        }
    }

    pub async fn cleanup_answers(&mut self) {
        self.answer = None;
        self.answer_type = None;
        self.answer_query_type = None;
        self.collected_rows = None;
        self.collected_documents = None;
    }

    pub async fn cleanup_concurrent_answers(&mut self) {
        self.concurrent_answers = Vec::new();
        self.concurrent_rows_streams = None;
    }

    pub fn transaction_opt(&self) -> Option<&Transaction> {
        self.transactions.front()
    }

    pub fn transaction(&self) -> &Transaction {
        self.transactions.front().unwrap()
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
        let answer = answer?;
        self.answer_query_type = Some(answer.get_query_type());
        self.answer_type = Some(match &answer {
            QueryAnswer::Ok(_) => QueryAnswerType::Ok,
            QueryAnswer::ConceptRowStream(_, _) => QueryAnswerType::ConceptRows,
            QueryAnswer::ConceptDocumentStream(_, _) => QueryAnswerType::ConceptDocuments,
        });
        self.answer = Some(answer);
        Ok(())
    }

    pub fn set_concurrent_answers(&mut self, answers: Vec<QueryAnswer>) {
        self.concurrent_answers = answers;
    }

    pub async fn unwrap_answer_if_needed(&mut self) {
        if self.collected_rows.is_none() && self.collected_documents.is_none() {
            match self.answer_type.expect("Nothing to unwrap: no answer") {
                QueryAnswerType::Ok => panic!("Nothing to unwrap: cannot unwrap Ok"),
                QueryAnswerType::ConceptRows => self.unwrap_answer_into_rows().await,
                QueryAnswerType::ConceptDocuments => self.unwrap_answer_into_documents().await,
            }
        }
    }

    pub async fn unwrap_concurrent_answers_into_rows_streams_if_neeed(&mut self) {
        if self.concurrent_rows_streams.is_none() {
            self.unwrap_concurrent_answers_into_rows_streams().await
        }
    }

    pub async fn unwrap_answer_into_rows(&mut self) {
        self.collected_rows =
            Some(self.answer.take().unwrap().into_rows().map(|result| result.unwrap()).collect::<Vec<_>>());
    }

    pub async fn unwrap_concurrent_answers_into_rows_streams(&mut self) {
        self.concurrent_rows_streams =
            Some(self.concurrent_answers.drain(..).map(|answer| answer.into_rows()).collect());
    }

    pub async fn unwrap_answer_into_documents(&mut self) {
        self.collected_documents =
            Some(self.answer.take().unwrap().into_documents().map(|result| result.unwrap()).collect::<Vec<_>>());
    }

    pub async fn get_answer(&self) -> Option<&QueryAnswer> {
        self.answer.as_ref()
    }

    pub async fn get_answer_query_type(&self) -> Option<QueryType> {
        self.answer_query_type
    }

    pub async fn get_answer_type(&self) -> Option<QueryAnswerType> {
        self.answer_type
    }

    pub async fn try_get_collected_rows(&mut self) -> Option<&Vec<ConceptRow>> {
        self.unwrap_answer_if_needed().await;
        self.collected_rows.as_ref()
    }

    pub async fn get_collected_rows(&mut self) -> &Vec<ConceptRow> {
        self.try_get_collected_rows().await.unwrap()
    }

    pub async fn try_get_collected_documents(&mut self) -> Option<&Vec<ConceptDocument>> {
        self.unwrap_answer_if_needed().await;
        self.collected_documents.as_ref()
    }

    pub async fn get_collected_documents(&mut self) -> &Vec<ConceptDocument> {
        self.try_get_collected_documents().await.unwrap()
    }

    pub async fn get_collected_answer_row_index(&mut self, index: usize) -> &ConceptRow {
        self.unwrap_answer_if_needed().await;
        self.collected_rows.as_ref().unwrap().get(index).unwrap()
    }

    pub async fn try_get_concurrent_rows_streams(
        &mut self,
    ) -> Option<&mut Vec<BoxStream<'static, TypeDBResult<ConceptRow>>>> {
        self.unwrap_concurrent_answers_into_rows_streams_if_neeed().await;
        self.concurrent_rows_streams.as_mut()
    }

    pub async fn get_concurrent_rows_streams(&mut self) -> &mut Vec<BoxStream<'static, TypeDBResult<ConceptRow>>> {
        self.try_get_concurrent_rows_streams().await.unwrap()
    }

    async fn create_default_driver(&mut self, username: Option<&str>, password: Option<&str>) -> TypeDBResult {
        match self.is_cloud {
            false => self.create_core_driver(Self::DEFAULT_CORE_ADDRESS, username, password).await,
            true => self.create_cloud_driver(&Self::DEFAULT_CLOUD_ADDRESSES, username, password).await,
        }
    }

    async fn create_core_driver(
        &mut self,
        address: &str,
        _username: Option<&str>,
        _password: Option<&str>,
    ) -> TypeDBResult {
        assert!(!self.is_cloud);
        let driver = TypeDBDriver::new_core(address)?;
        self.driver = Some(driver);
        Ok(())
    }

    async fn create_cloud_driver(
        &mut self,
        addresses: &[&str],
        username: Option<&str>,
        password: Option<&str>,
    ) -> TypeDBResult {
        assert!(self.is_cloud);
        let driver = TypeDBDriver::new_cloud(
            addresses,
            Credential::with_tls(
                username.expect("Username is required for cloud connection"),
                password.expect("Password is required for cloud connection"),
                Some(&self.tls_root_ca),
            )
            .unwrap(),
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
            answer: None,
            answer_type: None,
            answer_query_type: None,
            collected_rows: None,
            collected_documents: None,
            concurrent_answers: Vec::new(),
            concurrent_rows_streams: None,
        }
    }
}

// Most of the drivers are error-driven, while the Rust driver returns Option::None in many cases instead.
// These "fake" errors allow us to emulate error messages for generalised driver BDDs,
// at least verifying that the same error is produced by the same actions.
#[derive(Debug, Clone)]
pub enum BehaviourTestOptionalError {
    VariableDoesNotExist(String),
    InvalidConceptConversion,
    InvalidValueCasting(String),
}

impl fmt::Display for BehaviourTestOptionalError {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        match self {
            Self::VariableDoesNotExist(var) => write!(f, "The variable '{}' does not exist.", var),
            Self::InvalidConceptConversion => write!(f, "Invalid concept conversion."),
            Self::InvalidValueCasting(type_) => write!(f, "Invalid value casting to '{}'.", type_),
        }
    }
}

impl Error for BehaviourTestOptionalError {
    fn source(&self) -> Option<&(dyn Error + 'static)> {
        match self {
            Self::VariableDoesNotExist(_) => None,
            Self::InvalidConceptConversion => None,
            Self::InvalidValueCasting(_) => None,
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
