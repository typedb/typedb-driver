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

mod concept;
mod connection;
mod parameter;
mod session_tracker;
mod typeql;
mod util;

use std::{
    collections::{HashMap, HashSet},
    path::PathBuf,
};

use cucumber::{StatsWriter, World};
use futures::future::try_join_all;
use tokio::time::{sleep, Duration};
use typedb_client::{
    answer::{ConceptMap, ConceptMapGroup, Numeric, NumericGroup},
    concept::{Attribute, AttributeType, Entity, EntityType, Relation, RelationType, Thing},
    logic::Rule,
    Connection, Credential, Database, DatabaseManager, Result as TypeDBResult, Transaction, UserManager,
};

use self::session_tracker::SessionTracker;

#[derive(Debug, World)]
pub struct Context {
    pub tls_root_ca: PathBuf,
    pub connection: Connection,
    pub databases: DatabaseManager,
    pub users: UserManager,
    pub session_trackers: Vec<SessionTracker>,
    pub things: HashMap<String, Option<Thing>>,
    pub answer: Vec<ConceptMap>,
    pub answer_group: Vec<ConceptMapGroup>,
    pub numeric_answer: Option<Numeric>,
    pub numeric_answer_group: Vec<NumericGroup>,
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

    pub async fn after_scenario(&mut self) -> TypeDBResult {
        sleep(Context::STEP_REATTEMPT_SLEEP).await;
        self.set_connection(Connection::new_encrypted(
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
        self.session_trackers.get(0).unwrap().transaction()
    }

    pub fn take_transaction(&mut self) -> Transaction {
        self.session_trackers.get_mut(0).unwrap().take_transaction()
    }

    pub async fn get_entity_type(&self, type_label: String) -> TypeDBResult<EntityType> {
        self.transaction().concept().get_entity_type(type_label).await.map(|entity_type| {
            assert!(entity_type.is_some());
            entity_type.unwrap()
        })
    }

    pub async fn get_relation_type(&self, type_label: String) -> TypeDBResult<RelationType> {
        self.transaction().concept().get_relation_type(type_label).await.map(|relation_type| {
            assert!(relation_type.is_some());
            relation_type.unwrap()
        })
    }

    pub async fn get_attribute_type(&self, type_label: String) -> TypeDBResult<AttributeType> {
        self.transaction().concept().get_attribute_type(type_label).await.map(|attribute_type| {
            assert!(attribute_type.is_some());
            attribute_type.unwrap()
        })
    }

    pub fn get_thing(&self, var_name: String) -> &Thing {
        assert!(&self.things.contains_key(&var_name));
        self.things.get(&var_name).unwrap().as_ref().unwrap()
    }

    pub fn get_entity(&self, var_name: String) -> &Entity {
        let thing = self.get_thing(var_name);
        assert!(matches!(thing, Thing::Entity(_)));
        let Thing::Entity(entity) = thing else { unreachable!() };
        entity
    }

    pub fn get_relation(&self, var_name: String) -> &Relation {
        let thing = self.get_thing(var_name);
        assert!(matches!(thing, Thing::Relation(_)));
        let Thing::Relation(relation) = thing else { unreachable!() };
        relation
    }

    pub fn get_attribute(&self, var_name: String) -> &Attribute {
        let thing = self.get_thing(var_name);
        assert!(matches!(thing, Thing::Attribute(_)));
        let Thing::Attribute(attribute) = thing else { unreachable!() };
        attribute
    }

    pub fn insert_thing(&mut self, var_name: String, thing: Option<Thing>) {
        self.things.insert(var_name, thing);
    }

    pub fn insert_entity(&mut self, var_name: String, entity: Option<Entity>) {
        self.insert_thing(var_name, entity.map(Thing::Entity));
    }

    pub fn insert_relation(&mut self, var_name: String, relation: Option<Relation>) {
        self.insert_thing(var_name, relation.map(Thing::Relation));
    }

    pub fn insert_attribute(&mut self, var_name: String, attribute: Option<Attribute>) {
        self.insert_thing(var_name, attribute.map(Thing::Attribute));
    }

    pub async fn get_rule(&self, label: String) -> TypeDBResult<Option<Rule>> {
        self.transaction().logic().get_rule(label).await
    }

    pub fn set_connection(&mut self, new_connection: Connection) {
        self.connection = new_connection;
        self.databases = DatabaseManager::new(self.connection.clone());
        self.users = UserManager::new(self.connection.clone());
        self.session_trackers.clear();
        self.answer.clear();
        self.answer_group.clear();
        self.numeric_answer = None;
        self.numeric_answer_group.clear();
    }
}

impl Default for Context {
    fn default() -> Self {
        let tls_root_ca = PathBuf::from(
            std::env::var("ROOT_CA").expect("ROOT_CA environment variable needs to be set for cluster tests to run"),
        );
        let connection = Connection::new_encrypted(
            &["localhost:11729", "localhost:21729", "localhost:31729"],
            Credential::with_tls(Context::ADMIN_USERNAME, Context::ADMIN_PASSWORD, Some(&tls_root_ca)).unwrap(),
        )
        .unwrap();
        let databases = DatabaseManager::new(connection.clone());
        let users = UserManager::new(connection.clone());
        Self {
            tls_root_ca,
            connection,
            databases,
            users,
            session_trackers: Vec::new(),
            things: HashMap::new(),
            answer: Vec::new(),
            answer_group: Vec::new(),
            numeric_answer: None,
            numeric_answer_group: Vec::new(),
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
