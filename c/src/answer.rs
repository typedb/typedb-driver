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

use std::ffi::c_char;

use typedb_driver::{
    answer::{ConceptMap, ConceptMapGroup, Explainable, Explainables, ValueGroup},
    box_stream,
    concept::Concept,
    logic::{Explanation, Rule},
};

use super::{
    common::{StringIterator, StringPairIterator},
    concept::ConceptIterator,
    iterator::CIterator,
    memory::{borrow, free, release, release_optional, release_string, string_view},
    query::ConceptMapIterator,
};

#[no_mangle]
pub extern "C" fn concept_map_drop(concept_map: *mut ConceptMap) {
    free(concept_map);
}

#[no_mangle]
pub extern "C" fn concept_map_get_variables(concept_map: *const ConceptMap) -> *mut StringIterator {
    release(StringIterator(CIterator(box_stream(borrow(concept_map).map.clone().into_keys()))))
}

#[no_mangle]
pub extern "C" fn concept_map_get_values(concept_map: *const ConceptMap) -> *mut ConceptIterator {
    release(ConceptIterator(CIterator(box_stream(borrow(concept_map).map.clone().into_values().map(Ok)))))
}

#[no_mangle]
pub extern "C" fn concept_map_get(concept_map: *const ConceptMap, var: *const c_char) -> *mut Concept {
    release_optional(borrow(concept_map).get(string_view(var)).cloned())
}

#[no_mangle]
pub extern "C" fn concept_map_get_explainables(concept_map: *const ConceptMap) -> *mut Explainables {
    release(borrow(concept_map).explainables.clone())
}

#[no_mangle]
pub extern "C" fn concept_map_equals(lhs: *const ConceptMap, rhs: *const ConceptMap) -> bool {
    borrow(lhs) == borrow(rhs)
}

#[no_mangle]
pub extern "C" fn concept_map_to_string(concept_map: *const ConceptMap) -> *mut c_char {
    release_string(format!("{:?}", borrow(concept_map)))
}

#[no_mangle]
pub extern "C" fn explainables_drop(explainables: *mut Explainables) {
    free(explainables);
}

#[no_mangle]
pub extern "C" fn explainables_equals(lhs: *const Explainables, rhs: *const Explainables) -> bool {
    borrow(lhs) == borrow(rhs)
}

#[no_mangle]
pub extern "C" fn explainables_to_string(explainables: *const Explainables) -> *mut c_char {
    release_string(format!("{:?}", borrow(explainables)))
}

#[no_mangle]
pub extern "C" fn explainables_get_relation(explainables: *const Explainables, var: *const c_char) -> *mut Explainable {
    release_optional(borrow(explainables).relations.get(string_view(var)).cloned())
}

#[no_mangle]
pub extern "C" fn explainables_get_attribute(
    explainables: *const Explainables,
    var: *const c_char,
) -> *mut Explainable {
    release_optional(borrow(explainables).attributes.get(string_view(var)).cloned())
}

#[no_mangle]
pub extern "C" fn explainables_get_ownership(
    explainables: *const Explainables,
    owner: *const c_char,
    attribute: *const c_char,
) -> *mut Explainable {
    release_optional(
        borrow(explainables)
            .ownerships
            .get(&(string_view(owner).to_owned(), string_view(attribute).to_owned()))
            .cloned(),
    )
}

#[no_mangle]
pub extern "C" fn explainables_get_relations_keys(explainables: *const Explainables) -> *mut StringIterator {
    release(StringIterator(CIterator(box_stream(borrow(explainables).relations.clone().into_keys()))))
}

#[no_mangle]
pub extern "C" fn explainables_get_attributes_keys(explainables: *const Explainables) -> *mut StringIterator {
    release(StringIterator(CIterator(box_stream(borrow(explainables).attributes.clone().into_keys()))))
}

#[no_mangle]
pub extern "C" fn explainables_get_ownerships_keys(explainables: *const Explainables) -> *mut StringPairIterator {
    release(StringPairIterator(CIterator(box_stream(borrow(explainables).ownerships.clone().into_keys()))))
}

#[no_mangle]
pub extern "C" fn explainable_drop(explainable: *mut Explainable) {
    free(explainable);
}

#[no_mangle]
pub extern "C" fn explainable_get_id(explainable: *const Explainable) -> i64 {
    borrow(explainable).id
}

#[no_mangle]
pub extern "C" fn explainable_get_conjunction(explainable: *const Explainable) -> *mut c_char {
    release_string(borrow(explainable).conjunction.clone())
}

#[no_mangle]
pub extern "C" fn explanation_drop(explanation: *mut Explanation) {
    free(explanation);
}

#[no_mangle]
pub extern "C" fn explanation_equals(lhs: *const Explanation, rhs: *const Explanation) -> bool {
    borrow(lhs) == borrow(rhs)
}

#[no_mangle]
pub extern "C" fn explanation_to_string(explanation: *const Explanation) -> *mut c_char {
    release_string(format!("{:?}", borrow(explanation)))
}

#[no_mangle]
pub extern "C" fn explanation_get_rule(explanation: *const Explanation) -> *mut Rule {
    release(borrow(explanation).rule.clone())
}

#[no_mangle]
pub extern "C" fn explanation_get_conclusion(explanation: *const Explanation) -> *mut ConceptMap {
    release(borrow(explanation).conclusion.clone())
}

#[no_mangle]
pub extern "C" fn explanation_get_condition(explanation: *const Explanation) -> *mut ConceptMap {
    release(borrow(explanation).condition.clone())
}

#[no_mangle]
pub extern "C" fn explanation_get_mapped_variables(explanation: *const Explanation) -> *mut StringIterator {
    release(StringIterator(CIterator(box_stream(borrow(explanation).variable_mapping.keys().cloned()))))
}

#[no_mangle]
pub extern "C" fn explanation_get_mapping(explanation: *const Explanation, var: *const c_char) -> *mut StringIterator {
    release(StringIterator(CIterator(box_stream(
        borrow(explanation).variable_mapping.get(string_view(var)).into_iter().flatten().cloned(),
    ))))
}

#[no_mangle]
pub extern "C" fn concept_map_group_drop(concept_map_group: *mut ConceptMapGroup) {
    free(concept_map_group);
}

#[no_mangle]
pub extern "C" fn concept_map_group_get_owner(concept_map_group: *const ConceptMapGroup) -> *mut Concept {
    release(borrow(concept_map_group).owner.clone())
}

#[no_mangle]
pub extern "C" fn concept_map_group_get_concept_maps(
    concept_map_group: *const ConceptMapGroup,
) -> *mut ConceptMapIterator {
    release(ConceptMapIterator(CIterator(box_stream(
        borrow(concept_map_group).concept_maps.clone().into_iter().map(Ok),
    ))))
}

#[no_mangle]
pub extern "C" fn concept_map_group_to_string(concept_map_group: *const ConceptMapGroup) -> *const c_char {
    release_string(format!("{:?}", borrow(concept_map_group)))
}

#[no_mangle]
pub extern "C" fn concept_map_group_equals(lhs: *const ConceptMapGroup, rhs: *const ConceptMapGroup) -> bool {
    borrow(lhs) == borrow(rhs)
}

#[no_mangle]
pub extern "C" fn value_group_drop(value_group: *mut ValueGroup) {
    free(value_group);
}

#[no_mangle]
pub extern "C" fn value_group_to_string(value_group: *const ValueGroup) -> *const c_char {
    release_string(format!("{:?}", borrow(value_group)))
}

#[no_mangle]
pub extern "C" fn value_group_equals(lhs: *const ValueGroup, rhs: *const ValueGroup) -> bool {
    borrow(lhs) == borrow(rhs)
}

#[no_mangle]
pub extern "C" fn value_group_get_owner(value_group: *mut ValueGroup) -> *mut Concept {
    release(borrow(value_group).owner.clone())
}

#[no_mangle]
pub extern "C" fn value_group_get_value(value_group: *mut ValueGroup) -> *mut Concept {
    release(Concept::Value(borrow(value_group).value.clone()))
}
