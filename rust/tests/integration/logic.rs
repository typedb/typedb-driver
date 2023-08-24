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

use std::{collections::HashMap, default::Default};

use futures::TryStreamExt;
use serial_test::serial;
use typedb_client::{
    answer::{ConceptMap, Explainable},
    concept::{Attribute, Concept, Value},
    logic::Explanation,
    transaction::concept::api::ThingAPI,
    Connection, DatabaseManager, Options, Result as TypeDBResult, Session,
    SessionType::{Data, Schema},
    Transaction,
    TransactionType::{Read, Write},
};

use super::common;
use crate::test_for_each_arg;

test_for_each_arg! {
    {
        core => common::new_core_connection().unwrap(),
        cluster => common::new_cluster_connection().unwrap(),
    }

    async fn test_disjunction_explainable(connection: Connection) -> TypeDBResult {
        let schema = r#"define
            person sub entity,
                owns name,
                plays friendship:friend,
                plays marriage:husband,
                plays marriage:wife;
            name sub attribute, value string;
            friendship sub relation,
                relates friend;
            marriage sub relation,
                relates husband, relates wife;"#;
        common::create_test_database_with_schema(connection.clone(), schema).await?;

        let databases = DatabaseManager::new(connection);
        {
            let session = Session::new(databases.get(common::TEST_DATABASE).await?, Schema).await?;
            let transaction = session.transaction(Write).await?;
            transaction.logic().put_rule(
                "marriage-is-friendship".to_string(),
                typeql_lang::parse_pattern("{ $x isa person; $y isa person; (husband: $x, wife: $y) isa marriage; }")?
                    .into_conjunction(),
                typeql_lang::parse_variable("(friend: $x, friend: $y) isa friendship")?,
            ).await?;
            transaction.commit().await?;
        }

        let session = Session::new(databases.get(common::TEST_DATABASE).await?, Data).await?;
        let transaction = session.transaction(Write).await?;
        let data = r#"insert $x isa person, has name 'Zack';
            $y isa person, has name 'Yasmin';
            (husband: $x, wife: $y) isa marriage;"#;
        let _ = transaction.query().insert(data)?;
        transaction.commit().await?;

        let with_inference_and_explanation = Options::new().infer(true).explain(true);
        let transaction = session.transaction_with_options(Read, with_inference_and_explanation).await?;
        let answer_stream = transaction.query().match_(
            r#"match $p1 isa person;
            { (friend: $p1, friend: $p2) isa friendship;}
            or { $p1 has name 'Zack'; };"#,
        )?;
        let answers = answer_stream.try_collect::<Vec<ConceptMap>>().await?;

        assert_eq!(3, answers.len());

        for answer in answers {
            if answer.map.contains_key("p2") {
                assert_eq!(3, answer.map.len());
                assert!(!answer.explainables.is_empty());
                assert_explanations_count_and_projection_match(&answer, 1, &transaction).await?;
            } else {
                assert_eq!(2, answer.map.len());
                assert!(answer.explainables.is_empty());
            }
        }

        Ok(())
    }

    async fn test_relation_explainable(connection: Connection) -> TypeDBResult {
        let schema = r#"define
            person sub entity,
                owns name,
                plays friendship:friend,
                plays marriage:husband,
                plays marriage:wife;
            name sub attribute, value string;
            friendship sub relation,
                relates friend;
            marriage sub relation,
                relates husband, relates wife;"#;
        common::create_test_database_with_schema(connection.clone(), schema).await?;

        let databases = DatabaseManager::new(connection);
        {
            let session = Session::new(databases.get(common::TEST_DATABASE).await?, Schema).await?;
            let transaction = session.transaction(Write).await?;
            transaction.logic().put_rule(
                "marriage-is-friendship".to_string(),
                typeql_lang::parse_pattern("{ $x isa person; $y isa person; (husband: $x, wife: $y) isa marriage; }")?
                    .into_conjunction(),
                typeql_lang::parse_variable("(friend: $x, friend: $y) isa friendship")?,
            ).await?;
            transaction.commit().await?;
        }

        let session = Session::new(databases.get(common::TEST_DATABASE).await?, Data).await?;
        let transaction = session.transaction(Write).await?;
        let data = r#"insert $x isa person, has name 'Zack';
            $y isa person, has name 'Yasmin';
            (husband: $x, wife: $y) isa marriage;"#;
        let _ = transaction.query().insert(data)?;
        transaction.commit().await?;

        let with_inference_and_explanation = Options::new().infer(true).explain(true);
        let transaction = session.transaction_with_options(Read, with_inference_and_explanation).await?;
        let answer_stream = transaction.query().match_(
            r#"match (friend: $p1, friend: $p2) isa friendship; $p1 has name $na;"#,
        )?;
        let answers = answer_stream.try_collect::<Vec<ConceptMap>>().await?;

        assert_eq!(2, answers.len());

        for answer in answers {
            assert!(!answer.explainables.is_empty());
            assert_explanations_count_and_projection_match(&answer, 1, &transaction).await?;
        }

        Ok(())
    }

    async fn test_relation_explainable_multiple_ways(connection: Connection) -> TypeDBResult {
        let schema = r#"define
            person sub entity,
                owns name,
                plays friendship:friend,
                plays marriage:husband,
                plays marriage:wife;
            name sub attribute, value string;
            friendship sub relation,
                relates friend;
            marriage sub relation,
                relates husband, relates wife;"#;
        common::create_test_database_with_schema(connection.clone(), schema).await?;

        let databases = DatabaseManager::new(connection);
        {
            let session = Session::new(databases.get(common::TEST_DATABASE).await?, Schema).await?;
            let transaction = session.transaction(Write).await?;
            transaction.logic().put_rule(
                "marriage-is-friendship".to_string(),
                typeql_lang::parse_pattern("{ $x isa person; $y isa person; (husband: $x, wife: $y) isa marriage; }")?
                    .into_conjunction(),
                typeql_lang::parse_variable("(friend: $x, friend: $y) isa friendship")?,
            ).await?;
            transaction.logic().put_rule(
                "everyone-is-friends".to_string(),
                typeql_lang::parse_pattern("{ $x isa person; $y isa person; not { $x is $y; }; }")?
                    .into_conjunction(),
                typeql_lang::parse_variable("(friend: $x, friend: $y) isa friendship")?,
            ).await?;
            transaction.commit().await?;
        }

        let session = Session::new(databases.get(common::TEST_DATABASE).await?, Data).await?;
        let transaction = session.transaction(Write).await?;
        let data = r#"insert $x isa person, has name 'Zack';
            $y isa person, has name 'Yasmin';
            (husband: $x, wife: $y) isa marriage;"#;
        let _ = transaction.query().insert(data)?;
        transaction.commit().await?;

        let with_inference_and_explanation = Options::new().infer(true).explain(true);
        let transaction = session.transaction_with_options(Read, with_inference_and_explanation).await?;
        let answer_stream = transaction.query().match_(
            r#"match (friend: $p1, friend: $p2) isa friendship; $p1 has name $na;"#,
        )?;
        let answers = answer_stream.try_collect::<Vec<ConceptMap>>().await?;

        assert_eq!(2, answers.len());

        for answer in answers {
            assert!(!answer.explainables.is_empty());
            assert_explanations_count_and_projection_match(&answer, 3, &transaction).await?;
        }

        Ok(())
    }

    async fn test_has_explicit_explainable_two_ways(connection: Connection) -> TypeDBResult {
        let schema = r#"define
            milk sub entity,
                owns age-in-days,
                owns is-still-good;
            age-in-days sub attribute, value long;
            is-still-good sub attribute, value boolean;"#;
        common::create_test_database_with_schema(connection.clone(), schema).await?;

        let databases = DatabaseManager::new(connection);
        {
            let session = Session::new(databases.get(common::TEST_DATABASE).await?, Schema).await?;
            let transaction = session.transaction(Write).await?;
            transaction.logic().put_rule(
                "old-milk-is-not-good".to_string(),
                typeql_lang::parse_pattern("{ $x isa milk, has age-in-days <= 10; }")?
                    .into_conjunction(),
                typeql_lang::parse_variable("$x has is-still-good true")?,
            ).await?;
            transaction.logic().put_rule(
                "all-milk-is-good".to_string(),
                typeql_lang::parse_pattern("{ $x isa milk; }")?
                    .into_conjunction(),
                typeql_lang::parse_variable("$x has is-still-good true")?,
            ).await?;
            transaction.commit().await?;
        }

        let session = Session::new(databases.get(common::TEST_DATABASE).await?, Data).await?;
        let transaction = session.transaction(Write).await?;
        let data = r#"insert $x isa milk, has age-in-days 5;"#;
        let _ = transaction.query().insert(data)?;
        let data = r#"insert $x isa milk, has age-in-days 10;"#;
        let _ = transaction.query().insert(data)?;
        let data = r#"insert $x isa milk, has age-in-days 15;"#;
        let _ = transaction.query().insert(data)?;
        transaction.commit().await?;

        let with_inference_and_explanation = Options::new().infer(true).explain(true);
        let transaction = session.transaction_with_options(Read, with_inference_and_explanation).await?;
        let answer_stream = transaction.query().match_(
            r#"match $x has is-still-good $a;"#,
        )?;
        let answers = answer_stream.try_collect::<Vec<ConceptMap>>().await?;

        assert_eq!(3, answers.len());

        let age_in_days = transaction.concept().get_attribute_type(String::from("age-in-days")).await?.unwrap();
        for answer in answers {
            assert!(!answer.explainables.is_empty());
            match answer.map.get("x").unwrap() {
                Concept::Entity(entity) => {
                    let attributes: Vec<Attribute> = entity.get_has(&transaction, vec![age_in_days.clone()], vec![])?.try_collect().await?;
                    if attributes.first().unwrap().value == Value::Long(15) {
                        assert_explanations_count_and_projection_match(&answer, 1, &transaction).await?;
                    } else {
                        assert_explanations_count_and_projection_match(&answer, 2, &transaction).await?;
                    }
                },
                _ => panic!("Incorrect Concept type: {:?}", answer.map.get("x").unwrap()),
            }
        }

        Ok(())
    }
}

async fn assert_explanations_count_and_projection_match(
    ans: &ConceptMap,
    explanations_count: usize,
    transaction: &Transaction<'_>,
) -> TypeDBResult {
    assert_explainables_in_concept_map(ans);
    let explainables = all_explainables(ans);
    assert_eq!(1, explainables.len());

    let explanations = get_explanations(explainables[0], transaction).await?;
    assert_eq!(explanations_count, explanations.len());

    assert_explanation_concepts_match_projection(ans, explanations);
    Ok(())
}

fn assert_explainables_in_concept_map(ans: &ConceptMap) {
    ans.explainables.relations.keys().for_each(|k| assert!(ans.map.contains_key(k.as_str())));
    ans.explainables.attributes.keys().for_each(|k| assert!(ans.map.contains_key(k.as_str())));
    ans.explainables
        .ownerships
        .keys()
        .for_each(|(k1, k2)| assert!(ans.map.contains_key(k1.as_str()) && ans.map.contains_key(k2.as_str())));
}

fn all_explainables(ans: &ConceptMap) -> Vec<&Explainable> {
    let explainables = &ans.explainables;
    explainables
        .attributes
        .values()
        .chain(explainables.relations.values())
        .chain(explainables.ownerships.values())
        .collect::<Vec<_>>()
}

async fn get_explanations(explainable: &Explainable, transaction: &Transaction<'_>) -> TypeDBResult<Vec<Explanation>> {
    transaction.query().explain(explainable.id)?.try_collect::<Vec<_>>().await
}

fn assert_explanation_concepts_match_projection(ans: &ConceptMap, explanations: Vec<Explanation>) {
    for explanation in explanations {
        let mapping = explanation.variable_mapping;
        let projected = apply_mapping(&mapping, ans);
        for var in projected.map.keys() {
            assert!(explanation.conclusion.map.contains_key(var));
            assert_eq!(explanation.conclusion.map.get(var), projected.map.get(var));
        }
    }
}

fn apply_mapping(mapping: &HashMap<String, Vec<String>>, complete_map: &ConceptMap) -> ConceptMap {
    let mut concepts: HashMap<String, Concept> = HashMap::new();
    for key in mapping.keys() {
        assert!(complete_map.map.contains_key(key));
        let concept = complete_map.get(key).unwrap();
        for mapped in mapping.get(key).unwrap() {
            assert!(!concepts.contains_key(mapped) || concepts.get(mapped).unwrap() == concept);
            concepts.insert(mapped.to_string(), concept.clone());
        }
    }
    ConceptMap { map: concepts, explainables: Default::default() }
}
