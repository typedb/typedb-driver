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
    ffi::c_char,
    ptr::{addr_of_mut, null_mut},
};

use paste::paste;
use typedb_driver::{
    analyze::{
        conjunction::{
            Comparator, Conjunction, ConjunctionID, Constraint, ConstraintExactness, ConstraintSpan, ConstraintVertex,
            ConstraintWithSpan, Variable,
        },
        pipeline::{Pipeline, PipelineStage, ReduceAssignment, Reducer, SortOrder, SortVariable},
        AnalyzedQuery, Fetch, Function, ReturnOperation, TypeAnnotations, VariableAnnotations,
    },
    box_stream,
    concept::{type_::Type, AttributeType, Concept, Kind, ValueType},
    BoxPromise, Promise,
};

use crate::{
    common::StringIterator,
    concept::ConceptIterator,
    error::try_release,
    iterator::{iterator_next, CIterator},
    memory::{
        borrow, free, release, release_optional, release_optional_string, release_string, string_view, take_ownership,
    },
};

// Iterators, promises & enums
pub struct AnalyzedQueryPromise(BoxPromise<'static, typedb_driver::Result<AnalyzedQuery>>);

impl AnalyzedQueryPromise {
    pub fn new(promise: impl Promise<'static, typedb_driver::Result<AnalyzedQuery>>) -> Self {
        Self(Box::new(|| Ok(promise.resolve()?)))
    }
}

pub struct VariableIterator(pub CIterator<Variable>);

pub struct ReducerIterator(pub CIterator<Reducer>);

pub struct PipelineStageIterator(pub CIterator<PipelineStage>);

pub struct SortVariableIterator(pub CIterator<SortVariable>);

pub struct ReduceAssignmentIterator(pub CIterator<ReduceAssignment>);

pub struct ConstraintWithSpanIterator(pub CIterator<ConstraintWithSpan>);
pub struct ConstraintVertexIterator(pub CIterator<ConstraintVertex>);
pub struct ConjunctionIDIterator(pub CIterator<ConjunctionID>);
pub struct FunctionIterator(pub CIterator<Function>);
pub struct VariableAnnotationsIterator(pub CIterator<VariableAnnotations>);

#[repr(C)]
enum ReturnOperationVariant {
    StreamReturn,
    SingleReturn,
    CheckReturn,
    ReduceReturn,
}

#[repr(C)]
enum PipelineStageVariant {
    Match,
    Insert,
    Put,
    Update,
    Delete,
    Select,
    Sort,
    Require,
    Offset,
    Limit,
    Distinct,
    Reduce,
}

#[repr(C)]
enum ConstraintVariant {
    Isa,
    Has,
    Links,

    Sub,
    Owns,
    Relates,
    Plays,

    FunctionCall,
    Expression,
    Is,
    Iid,
    Comparison,
    KindOf,
    Label,
    Value,

    Or,
    Not,
    Try,
}

#[repr(C)]
enum VariableAnnotationsVariant {
    ThingAnnotations,
    TypeAnnotations,
    ValueAnnotations,
}

#[repr(C)]
enum FetchVariant {
    Leaf,
    List,
    Object,
}

/// Waits for and returns the result of the Analyze request.
/// In case the operation failed, the error flag will only be set when the promise is resolved.
/// The native promise object is freed when it is resolved.
#[no_mangle]
pub extern "C" fn analyzed_query_promise_resolve(promise: *mut AnalyzedQueryPromise) -> *mut AnalyzedQuery {
    try_release(take_ownership(promise).0.resolve())
}

/// Frees the native rust <code>AnalyzePromise</code> object.
#[no_mangle]
pub extern "C" fn analyzed_query_promise_drop(promise: *mut AnalyzedQueryPromise) {
    drop(take_ownership(promise))
}

/// Returns the structure of the query pipeline in the analyzed query.
#[no_mangle]
pub extern "C" fn analyzed_query_pipeline(analyzed_query: *const AnalyzedQuery) -> *mut Pipeline {
    release(borrow(analyzed_query).query.clone())
}

/// Returns the analyzed functions in the preamble of the query.
#[no_mangle]
pub extern "C" fn analyzed_preamble(analyzed_query: *const AnalyzedQuery) -> *mut FunctionIterator {
    release(FunctionIterator(CIterator(box_stream(borrow(analyzed_query).preamble.clone().into_iter()))))
}

/// Returns the analyzed fetch
#[no_mangle]
pub extern "C" fn analyzed_fetch(analyzed_query: *const AnalyzedQuery) -> *mut Fetch {
    release_optional(borrow(analyzed_query).fetch.clone())
}

#[no_mangle]
pub extern "C" fn fetch_variant(fetch: *const Fetch) -> FetchVariant {
    match borrow(fetch) {
        Fetch::List(_) => FetchVariant::List,
        Fetch::Leaf(_) => FetchVariant::Leaf,
        Fetch::Object(_) => FetchVariant::Object,
    }
}

#[no_mangle]
pub extern "C" fn fetch_leaf_annotations(fetch: *const Fetch) -> *mut StringIterator {
    match borrow(fetch) {
        Fetch::Leaf(leaf) => {
            let iter = leaf.annotations.clone().into_iter().map(|a| Ok(a.name().to_owned()));
            release(StringIterator(CIterator(box_stream(iter))))
        }
        _ => unreachable!("Expected Fetch to be Leaf variant"),
    }
}

#[no_mangle]
pub extern "C" fn fetch_list_element(fetch: *const Fetch) -> *mut Fetch {
    match borrow(fetch) {
        Fetch::List(element) => release((**element).clone()),
        _ => unreachable!("Expected Fetch to be List variant"),
    }
}

#[no_mangle]
pub extern "C" fn fetch_object_fields(fetch: *const Fetch) -> *mut StringIterator {
    match borrow(fetch) {
        Fetch::Object(map) => {
            let iter = map.keys().cloned().collect::<Vec<_>>().clone().into_iter().map(Ok);
            release(StringIterator(CIterator(box_stream(iter))))
        }
        _ => unreachable!("Expected Fetch to be Object variant"),
    }
}

#[no_mangle]
pub extern "C" fn fetch_object_get_field(fetch: *const Fetch, field: *const c_char) -> *mut Fetch {
    match borrow(fetch) {
        Fetch::Object(map) => release_optional(map.get(string_view(field)).cloned()),
        _ => unreachable!("Expected Fetch to be Object variant"),
    }
}

// Functions
#[no_mangle]
pub extern "C" fn function_body(function: *const Function) -> *mut Pipeline {
    release(borrow(function).body.clone())
}

#[no_mangle]
pub extern "C" fn function_argument_variables(function: *const Function) -> *mut VariableIterator {
    release(VariableIterator(CIterator(box_stream(borrow(function).argument_variables.clone().into_iter()))))
}

#[no_mangle]
pub extern "C" fn function_return_operation(function: *const Function) -> *mut ReturnOperation {
    release(borrow(function).return_operation.clone())
}

#[no_mangle]
pub extern "C" fn return_operation_variant(return_operation: *const ReturnOperation) -> ReturnOperationVariant {
    match borrow(return_operation) {
        ReturnOperation::Stream { .. } => ReturnOperationVariant::StreamReturn,
        ReturnOperation::Single { .. } => ReturnOperationVariant::SingleReturn,
        ReturnOperation::Check { .. } => ReturnOperationVariant::CheckReturn,
        ReturnOperation::Reduce { .. } => ReturnOperationVariant::ReduceReturn,
    }
}

#[no_mangle]
pub extern "C" fn return_operation_stream_variables(return_operation: *const ReturnOperation) -> *mut VariableIterator {
    match borrow(return_operation) {
        ReturnOperation::Stream { variables, .. } => {
            release(VariableIterator(CIterator(box_stream(variables.iter().cloned()))))
        }
        _ => unreachable!("Expected ReturnOperation to be Stream"),
    }
}

#[no_mangle]
pub extern "C" fn return_operation_single_variables(return_operation: *const ReturnOperation) -> *mut VariableIterator {
    match borrow(return_operation) {
        ReturnOperation::Single { variables, .. } => {
            release(VariableIterator(CIterator(box_stream(variables.iter().cloned()))))
        }
        _ => unreachable!("Expected ReturnOperation to be Single"),
    }
}

#[no_mangle]
pub extern "C" fn return_operation_single_selector(return_operation: *const ReturnOperation) -> *mut c_char {
    match borrow(return_operation) {
        ReturnOperation::Single { selector, .. } => release_string(selector.clone()),
        _ => unreachable!("Expected ReturnOperation to be Single"),
    }
}

#[no_mangle]
pub extern "C" fn return_operation_reducers(return_operation: *const ReturnOperation) -> *mut ReducerIterator {
    match borrow(return_operation) {
        ReturnOperation::Reduce { reducers } => {
            release(ReducerIterator(CIterator(box_stream(reducers.iter().cloned()))))
        }
        _ => unreachable!("Expected ReturnOperation to be Reducer"),
    }
}

#[no_mangle]
pub extern "C" fn function_argument_annotations(function: *const Function) -> *mut VariableAnnotationsIterator {
    let iter = borrow(function).argument_annotations.clone().into_iter();
    release(VariableAnnotationsIterator(CIterator(box_stream(iter))))
}

#[no_mangle]
pub extern "C" fn function_return_annotations(function: *const Function) -> *mut VariableAnnotationsIterator {
    let iter = borrow(function).return_annotations.clone().into_iter();
    release(VariableAnnotationsIterator(CIterator(box_stream(iter))))
}

// TODO: Get stuff from the other branch

// Stages
#[no_mangle]
pub extern "C" fn pipeline_stages(pipeline: *const Pipeline) -> *mut PipelineStageIterator {
    release(PipelineStageIterator(CIterator(box_stream(borrow(pipeline).stages.iter().cloned()))))
}

// Stage field accessors
#[no_mangle]
pub extern "C" fn pipeline_stage_variant(stage: *const PipelineStage) -> PipelineStageVariant {
    match borrow(stage) {
        PipelineStage::Match { .. } => PipelineStageVariant::Match,
        PipelineStage::Insert { .. } => PipelineStageVariant::Insert,
        PipelineStage::Put { .. } => PipelineStageVariant::Put,
        PipelineStage::Update { .. } => PipelineStageVariant::Update,
        PipelineStage::Delete { .. } => PipelineStageVariant::Delete,
        PipelineStage::Select { .. } => PipelineStageVariant::Select,
        PipelineStage::Sort { .. } => PipelineStageVariant::Sort,
        PipelineStage::Require { .. } => PipelineStageVariant::Require,
        PipelineStage::Offset { .. } => PipelineStageVariant::Offset,
        PipelineStage::Limit { .. } => PipelineStageVariant::Limit,
        PipelineStage::Distinct => PipelineStageVariant::Distinct,
        PipelineStage::Reduce { .. } => PipelineStageVariant::Reduce,
    }
}

#[no_mangle]
pub extern "C" fn pipeline_stage_get_block(stage: *const PipelineStage) -> *mut ConjunctionID {
    match borrow(stage) {
        PipelineStage::Match { block, .. }
        | PipelineStage::Insert { block, .. }
        | PipelineStage::Put { block, .. }
        | PipelineStage::Update { block, .. }
        | PipelineStage::Delete { block, .. } => release(block.clone()),
        PipelineStage::Select { .. }
        | PipelineStage::Sort { .. }
        | PipelineStage::Require { .. }
        | PipelineStage::Offset { .. }
        | PipelineStage::Limit { .. }
        | PipelineStage::Distinct { .. }
        | PipelineStage::Reduce { .. } => unreachable!("PipelineStage {stage:?} has no block"),
    }
}

#[no_mangle]
pub extern "C" fn pipeline_stage_delete_get_deleted_variables(stage: *const PipelineStage) -> *mut VariableIterator {
    let PipelineStage::Delete { deleted_variables, .. } = borrow(stage) else { unreachable!("Expected Delete stage") };
    release(VariableIterator(CIterator(box_stream(deleted_variables.iter().cloned()))))
}

#[no_mangle]
pub extern "C" fn pipeline_stage_select_get_variables(stage: *const PipelineStage) -> *mut VariableIterator {
    let PipelineStage::Select { variables, .. } = borrow(stage) else { unreachable!("Expected Select stage") };
    release(VariableIterator(CIterator(box_stream(variables.iter().cloned()))))
}

#[no_mangle]
pub extern "C" fn pipeline_stage_require_get_variables(stage: *const PipelineStage) -> *mut VariableIterator {
    let PipelineStage::Require { variables, .. } = borrow(stage) else { unreachable!("Expected Require stage") };
    release(VariableIterator(CIterator(box_stream(variables.iter().cloned()))))
}

#[no_mangle]
pub extern "C" fn pipeline_stage_offset_get_offset(stage: *const PipelineStage) -> i64 {
    let PipelineStage::Offset { offset, .. } = borrow(stage) else { unreachable!("Expected Offset stage") };
    (*offset as i64)
}

#[no_mangle]
pub extern "C" fn pipeline_stage_limit_get_limit(stage: *const PipelineStage) -> i64 {
    let PipelineStage::Limit { limit, .. } = borrow(stage) else { unreachable!("Expected Limit stage") };
    (*limit as i64)
}

#[no_mangle]
pub extern "C" fn pipeline_stage_sort_get_sort_variables(stage: *const PipelineStage) -> *mut SortVariableIterator {
    let PipelineStage::Sort { variables, .. } = borrow(stage) else { unreachable!("Expected Sort stage") };
    release(SortVariableIterator(CIterator(box_stream(variables.iter().cloned()))))
}

#[no_mangle]
pub extern "C" fn pipeline_stage_reduce_get_groupby(stage: *const PipelineStage) -> *mut VariableIterator {
    let PipelineStage::Reduce { groupby, .. } = borrow(stage) else { unreachable!("Expected Reduce stage") };
    release(VariableIterator(CIterator(box_stream(groupby.iter().cloned()))))
}

#[no_mangle]
pub extern "C" fn pipeline_stage_reduce_get_reducer_assignments(
    stage: *const PipelineStage,
) -> *mut ReduceAssignmentIterator {
    let PipelineStage::Reduce { reducers, .. } = borrow(stage) else { unreachable!("Expected Reduce stage") };
    release(ReduceAssignmentIterator(CIterator(box_stream(reducers.iter().cloned()))))
}

#[no_mangle]
pub extern "C" fn sort_variable_get_variable(sort_variable: *const SortVariable) -> *mut Variable {
    release(borrow(sort_variable).variable.clone())
}

#[no_mangle]
pub extern "C" fn sort_variable_get_order(sort_variable: *const SortVariable) -> SortOrder {
    borrow(sort_variable).order.clone()
}

#[no_mangle]
pub extern "C" fn reduce_assignment_get_assigned(reduce_assignment: *const ReduceAssignment) -> *mut Variable {
    release(borrow(reduce_assignment).assigned.clone())
}

#[no_mangle]
pub extern "C" fn reduce_assignment_get_reducer(reduce_assignment: *const ReduceAssignment) -> *mut Reducer {
    release(borrow(reduce_assignment).reducer.clone())
}

#[no_mangle]
pub extern "C" fn reducer_get_name(reducer: *const Reducer) -> *mut c_char {
    release_string(borrow(reducer).reducer.clone())
}

#[no_mangle]
pub extern "C" fn reducer_get_arguments(reducer: *const Reducer) -> *mut VariableIterator {
    release(VariableIterator(CIterator(box_stream(borrow(reducer).arguments.iter().cloned()))))
}

#[no_mangle]
pub extern "C" fn variable_get_name(pipeline_structure: *const Pipeline, variable: *const Variable) -> *mut c_char {
    release_optional_string(borrow(pipeline_structure).variable_name(borrow(variable)).map(str::to_owned))
}

#[no_mangle]
pub extern "C" fn pipeline_get_conjunction(
    pipeline: *const Pipeline,
    conjunction_id: *const ConjunctionID,
) -> *mut Conjunction {
    release_optional(borrow(pipeline).conjunctions.get(borrow(conjunction_id).0).cloned())
}

#[no_mangle]
pub extern "C" fn conjunction_get_constraints(conjunction: *const Conjunction) -> *mut ConstraintWithSpanIterator {
    release(ConstraintWithSpanIterator(CIterator(box_stream(borrow(conjunction).constraints.clone().into_iter()))))
}

#[no_mangle]
pub extern "C" fn conjunction_get_annotated_variables(conjunction: *const Conjunction) -> *mut VariableIterator {
    let iter = borrow(conjunction).variable_annotations.keys().cloned().collect::<Vec<_>>().into_iter();
    release(VariableIterator(CIterator(box_stream(iter))))
}

#[no_mangle]
pub extern "C" fn conjunction_get_variable_annotations(
    conjunction: *const Conjunction,
    variable: *const Variable,
) -> *mut VariableAnnotations {
    release_optional(borrow(conjunction).variable_annotations.get(borrow(variable)).cloned())
}

#[no_mangle]
pub extern "C" fn variable_annotations_variant(annotations: *const VariableAnnotations) -> VariableAnnotationsVariant {
    match &borrow(annotations).types {
        TypeAnnotations::Thing(_) => VariableAnnotationsVariant::ThingAnnotations,
        TypeAnnotations::Type(_) => VariableAnnotationsVariant::TypeAnnotations,
        TypeAnnotations::Value(_) => VariableAnnotationsVariant::ValueAnnotations,
    }
}

#[no_mangle]
pub extern "C" fn variable_annotations_thing(annotations: *const VariableAnnotations) -> *mut ConceptIterator {
    match &borrow(annotations).types {
        TypeAnnotations::Thing(annotations) => release(ConceptIterator(CIterator(box_stream(
            annotations.clone().into_iter().map(|t| Ok(type_to_concept(t))),
        )))),
        _ => unreachable!("Expected variable to have thing annotations"),
    }
}
#[no_mangle]
pub extern "C" fn variable_annotations_type(annotations: *const VariableAnnotations) -> *mut ConceptIterator {
    match &borrow(annotations).types {
        TypeAnnotations::Type(annotations) => release(ConceptIterator(CIterator(box_stream(
            annotations.clone().into_iter().map(|t| Ok(type_to_concept(t))),
        )))),
        _ => unreachable!("Expected variable to have type annotations"),
    }
}

#[no_mangle]
pub extern "C" fn variable_annotations_value(annotations: *const VariableAnnotations) -> *mut StringIterator {
    match &borrow(annotations).types {
        TypeAnnotations::Value(annotations) => {
            release(StringIterator(CIterator(box_stream([Ok(annotations.name().to_owned())].into_iter()))))
        }
        _ => unreachable!("Expected variable to have value annotations"),
    }
}

#[no_mangle]
pub extern "C" fn constraint_span_begin(constraint: *const ConstraintWithSpan) -> i64 {
    borrow(constraint).span.as_ref().map_or(0, |c| c.begin) as i64
}

#[no_mangle]
pub extern "C" fn constraint_span_end(constraint: *const ConstraintWithSpan) -> i64 {
    borrow(constraint).span.as_ref().map_or(0, |c| c.end) as i64
}

#[no_mangle]
pub extern "C" fn constraint_variant(constraint: *const ConstraintWithSpan) -> ConstraintVariant {
    match &borrow(constraint).constraint {
        Constraint::Isa { .. } => ConstraintVariant::Isa,
        Constraint::Has { .. } => ConstraintVariant::Has,
        Constraint::Links { .. } => ConstraintVariant::Links,
        Constraint::Sub { .. } => ConstraintVariant::Sub,
        Constraint::Owns { .. } => ConstraintVariant::Owns,
        Constraint::Relates { .. } => ConstraintVariant::Relates,
        Constraint::Plays { .. } => ConstraintVariant::Plays,
        Constraint::FunctionCall { .. } => ConstraintVariant::FunctionCall,
        Constraint::Expression { .. } => ConstraintVariant::Expression,
        Constraint::Is { .. } => ConstraintVariant::Is,
        Constraint::Iid { .. } => ConstraintVariant::Iid,
        Constraint::Comparison { .. } => ConstraintVariant::Comparison,
        Constraint::Kind { .. } => ConstraintVariant::KindOf,
        Constraint::Label { .. } => ConstraintVariant::Label,
        Constraint::Value { .. } => ConstraintVariant::Value,
        Constraint::Or { .. } => ConstraintVariant::Or,
        Constraint::Not { .. } => ConstraintVariant::Not,
        Constraint::Try { .. } => ConstraintVariant::Try,
    }
}

#[no_mangle]
pub extern "C" fn constraint_isa_get_instance(constraint: *const ConstraintWithSpan) -> *mut ConstraintVertex {
    let Constraint::Isa { instance, .. } = &borrow(constraint).constraint else {
        unreachable!("Expected constraint to be Isa");
    };
    release(instance.clone())
}

#[no_mangle]
pub extern "C" fn constraint_isa_get_type(constraint: *const ConstraintWithSpan) -> *mut ConstraintVertex {
    let Constraint::Isa { r#type: type_, .. } = &borrow(constraint).constraint else {
        unreachable!("Expected constraint to be Isa");
    };
    release(type_.clone())
}

#[no_mangle]
pub extern "C" fn constraint_has_get_owner(constraint: *const ConstraintWithSpan) -> *mut ConstraintVertex {
    let Constraint::Has { owner, .. } = &borrow(constraint).constraint else {
        unreachable!("Expected constraint to be Has");
    };
    release(owner.clone())
}

#[no_mangle]
pub extern "C" fn constraint_has_get_attribute(constraint: *const ConstraintWithSpan) -> *mut ConstraintVertex {
    let Constraint::Has { attribute, .. } = &borrow(constraint).constraint else {
        unreachable!("Expected constraint to be Has");
    };
    release(attribute.clone())
}

#[no_mangle]
pub extern "C" fn constraint_links_get_relation(constraint: *const ConstraintWithSpan) -> *mut ConstraintVertex {
    let Constraint::Links { relation, .. } = &borrow(constraint).constraint else {
        unreachable!("Expected constraint to be Links");
    };
    release(relation.clone())
}

#[no_mangle]
pub extern "C" fn constraint_links_get_player(constraint: *const ConstraintWithSpan) -> *mut ConstraintVertex {
    let Constraint::Links { player, .. } = &borrow(constraint).constraint else {
        unreachable!("Expected constraint to be Links");
    };
    release(player.clone())
}

#[no_mangle]
pub extern "C" fn constraint_links_get_role(constraint: *const ConstraintWithSpan) -> *mut ConstraintVertex {
    let Constraint::Links { role, .. } = &borrow(constraint).constraint else {
        unreachable!("Expected constraint to be Links");
    };
    release(role.clone())
}

#[no_mangle]
pub extern "C" fn constraint_sub_get_subtype(constraint: *const ConstraintWithSpan) -> *mut ConstraintVertex {
    let Constraint::Sub { subtype, .. } = &borrow(constraint).constraint else {
        unreachable!("Expected constraint to be Sub");
    };
    release(subtype.clone())
}

#[no_mangle]
pub extern "C" fn constraint_sub_get_supertype(constraint: *const ConstraintWithSpan) -> *mut ConstraintVertex {
    let Constraint::Sub { supertype, .. } = &borrow(constraint).constraint else {
        unreachable!("Expected constraint to be Sub");
    };
    release(supertype.clone())
}

#[no_mangle]
pub extern "C" fn constraint_owns_get_owner(constraint: *const ConstraintWithSpan) -> *mut ConstraintVertex {
    let Constraint::Owns { owner, .. } = &borrow(constraint).constraint else {
        unreachable!("Expected constraint to be Owns");
    };
    release(owner.clone())
}

#[no_mangle]
pub extern "C" fn constraint_owns_get_attribute(constraint: *const ConstraintWithSpan) -> *mut ConstraintVertex {
    let Constraint::Owns { attribute, .. } = &borrow(constraint).constraint else {
        unreachable!("Expected constraint to be Owns");
    };
    release(attribute.clone())
}

#[no_mangle]
pub extern "C" fn constraint_relates_get_relation(constraint: *const ConstraintWithSpan) -> *mut ConstraintVertex {
    let Constraint::Relates { relation, .. } = &borrow(constraint).constraint else {
        unreachable!("Expected constraint to be Relates");
    };
    release(relation.clone())
}

#[no_mangle]
pub extern "C" fn constraint_relates_get_role(constraint: *const ConstraintWithSpan) -> *mut ConstraintVertex {
    let Constraint::Relates { role, .. } = &borrow(constraint).constraint else {
        unreachable!("Expected constraint to be Relates");
    };
    release(role.clone())
}

#[no_mangle]
pub extern "C" fn constraint_plays_get_player(constraint: *const ConstraintWithSpan) -> *mut ConstraintVertex {
    let Constraint::Plays { player, .. } = &borrow(constraint).constraint else {
        unreachable!("Expected constraint to be Plays");
    };
    release(player.clone())
}

#[no_mangle]
pub extern "C" fn constraint_plays_get_role(constraint: *const ConstraintWithSpan) -> *mut ConstraintVertex {
    let Constraint::Plays { role, .. } = &borrow(constraint).constraint else {
        unreachable!("Expected constraint to be Plays");
    };
    release(role.clone())
}

#[no_mangle]
pub extern "C" fn constraint_isa_get_exactness(constraint: *const ConstraintWithSpan) -> ConstraintExactness {
    let Constraint::Isa { exactness, .. } = &borrow(constraint).constraint else {
        unreachable!("Expected constraint to be Isa");
    };
    exactness.clone()
}

#[no_mangle]
pub extern "C" fn constraint_has_get_exactness(constraint: *const ConstraintWithSpan) -> ConstraintExactness {
    let Constraint::Has { exactness, .. } = &borrow(constraint).constraint else {
        unreachable!("Expected constraint to be Has");
    };
    exactness.clone()
}

#[no_mangle]
pub extern "C" fn constraint_links_get_exactness(constraint: *const ConstraintWithSpan) -> ConstraintExactness {
    let Constraint::Links { exactness, .. } = &borrow(constraint).constraint else {
        unreachable!("Expected constraint to be Links");
    };
    exactness.clone()
}

#[no_mangle]
pub extern "C" fn constraint_sub_get_exactness(constraint: *const ConstraintWithSpan) -> ConstraintExactness {
    let Constraint::Sub { exactness, .. } = &borrow(constraint).constraint else {
        unreachable!("Expected constraint to be Sub");
    };
    exactness.clone()
}

#[no_mangle]
pub extern "C" fn constraint_owns_get_exactness(constraint: *const ConstraintWithSpan) -> ConstraintExactness {
    let Constraint::Owns { exactness, .. } = &borrow(constraint).constraint else {
        unreachable!("Expected constraint to be Owns");
    };
    exactness.clone()
}

#[no_mangle]
pub extern "C" fn constraint_relates_get_exactness(constraint: *const ConstraintWithSpan) -> ConstraintExactness {
    let Constraint::Relates { exactness, .. } = &borrow(constraint).constraint else {
        unreachable!("Expected constraint to be Relates");
    };
    exactness.clone()
}

#[no_mangle]
pub extern "C" fn constraint_plays_get_exactness(constraint: *const ConstraintWithSpan) -> ConstraintExactness {
    let Constraint::Plays { exactness, .. } = &borrow(constraint).constraint else {
        unreachable!("Expected constraint to be Plays");
    };
    exactness.clone()
}

#[no_mangle]
pub extern "C" fn constraint_function_call_get_name(constraint: *const ConstraintWithSpan) -> *mut c_char {
    let Constraint::FunctionCall { name, .. } = &borrow(constraint).constraint else {
        unreachable!("Expected constraint to be FunctionCall");
    };
    release_string(name.clone())
}

#[no_mangle]
pub extern "C" fn constraint_function_call_get_assigned(
    constraint: *const ConstraintWithSpan,
) -> *mut ConstraintVertexIterator {
    let Constraint::FunctionCall { assigned, .. } = &borrow(constraint).constraint else {
        unreachable!("Expected constraint to be FunctionCall");
    };
    release(ConstraintVertexIterator(CIterator(box_stream(assigned.clone().into_iter()))))
}

#[no_mangle]
pub extern "C" fn constraint_function_call_get_arguments(
    constraint: *const ConstraintWithSpan,
) -> *mut ConstraintVertexIterator {
    let Constraint::FunctionCall { arguments, .. } = &borrow(constraint).constraint else {
        unreachable!("Expected constraint to be FunctionCall");
    };
    release(ConstraintVertexIterator(CIterator(box_stream(arguments.clone().into_iter()))))
}

#[no_mangle]
pub extern "C" fn constraint_expression_get_text(constraint: *const ConstraintWithSpan) -> *mut c_char {
    let Constraint::Expression { text, .. } = &borrow(constraint).constraint else {
        unreachable!("Expected constraint to be Expression");
    };
    release_string(text.clone())
}

#[no_mangle]
pub extern "C" fn constraint_expression_get_assigned(constraint: *const ConstraintWithSpan) -> *mut ConstraintVertex {
    let Constraint::Expression { assigned, .. } = &borrow(constraint).constraint else {
        unreachable!("Expected constraint to be Expression");
    };
    release(assigned.clone())
}

#[no_mangle]
pub extern "C" fn constraint_expression_get_arguments(
    constraint: *const ConstraintWithSpan,
) -> *mut ConstraintVertexIterator {
    let Constraint::Expression { arguments, .. } = &borrow(constraint).constraint else {
        unreachable!("Expected constraint to be Expression");
    };
    release(ConstraintVertexIterator(CIterator(box_stream(arguments.iter().cloned()))))
}

#[no_mangle]
pub extern "C" fn constraint_is_get_lhs(constraint: *const ConstraintWithSpan) -> *mut ConstraintVertex {
    let Constraint::Is { lhs, .. } = &borrow(constraint).constraint else {
        unreachable!("Expected constraint to be Is");
    };
    release(lhs.clone())
}

#[no_mangle]
pub extern "C" fn constraint_is_get_rhs(constraint: *const ConstraintWithSpan) -> *mut ConstraintVertex {
    let Constraint::Is { rhs, .. } = &borrow(constraint).constraint else {
        unreachable!("Expected constraint to be Is");
    };
    release(rhs.clone())
}

#[no_mangle]
pub extern "C" fn constraint_iid_get_variable(constraint: *const ConstraintWithSpan) -> *mut ConstraintVertex {
    let Constraint::Iid { concept, .. } = &borrow(constraint).constraint else {
        unreachable!("Expected constraint to be Iid");
    };
    release(concept.clone())
}

#[no_mangle]
pub extern "C" fn constraint_iid_get_iid(constraint: *const ConstraintWithSpan) -> *mut c_char {
    let Constraint::Iid { iid, .. } = &borrow(constraint).constraint else {
        unreachable!("Expected constraint to be Iid");
    };
    release_string(iid.to_string())
}

#[no_mangle]
pub extern "C" fn constraint_comparison_get_lhs(constraint: *const ConstraintWithSpan) -> *mut ConstraintVertex {
    let Constraint::Comparison { lhs, .. } = &borrow(constraint).constraint else {
        unreachable!("Expected constraint to be Comparison");
    };
    release(lhs.clone())
}

#[no_mangle]
pub extern "C" fn constraint_comparison_get_rhs(constraint: *const ConstraintWithSpan) -> *mut ConstraintVertex {
    let Constraint::Comparison { rhs, .. } = &borrow(constraint).constraint else {
        unreachable!("Expected constraint to be Comparison");
    };
    release(rhs.clone())
}

#[no_mangle]
pub extern "C" fn constraint_comparison_get_comparator(constraint: *const ConstraintWithSpan) -> Comparator {
    let Constraint::Comparison { comparator, .. } = &borrow(constraint).constraint else {
        unreachable!("Expected constraint to be Comparison");
    };
    comparator.clone()
}

#[no_mangle]
pub extern "C" fn comparator_get_name(comparator: Comparator) -> *mut c_char {
    release_string(comparator.symbol().to_owned())
}

#[no_mangle]
pub extern "C" fn constraint_kind_get_kind(constraint: *const ConstraintWithSpan) -> Kind {
    let Constraint::Kind { kind, .. } = &borrow(constraint).constraint else {
        unreachable!("Expected constraint to be Kind");
    };
    kind.clone()
}

#[no_mangle]
pub extern "C" fn constraint_kind_get_type(constraint: *const ConstraintWithSpan) -> *mut ConstraintVertex {
    let Constraint::Kind { r#type: type_, .. } = &borrow(constraint).constraint else {
        unreachable!("Expected constraint to be Kind");
    };
    release(type_.clone())
}

#[no_mangle]
pub extern "C" fn constraint_label_get_variable(constraint: *const ConstraintWithSpan) -> *mut ConstraintVertex {
    let Constraint::Label { r#type: type_, .. } = &borrow(constraint).constraint else {
        unreachable!("Expected constraint to be Label");
    };
    release(type_.clone())
}

#[no_mangle]
pub extern "C" fn constraint_label_get_label(constraint: *const ConstraintWithSpan) -> *mut c_char {
    let Constraint::Label { label, .. } = &borrow(constraint).constraint else {
        unreachable!("Expected constraint to be Label");
    };
    release_string(label.clone())
}

#[no_mangle]
pub extern "C" fn constraint_value_get_attribute_type(constraint: *const ConstraintWithSpan) -> *mut ConstraintVertex {
    let Constraint::Value { attribute_type, .. } = &borrow(constraint).constraint else {
        unreachable!("Expected constraint to be Value");
    };
    release(attribute_type.clone())
}

#[no_mangle]
pub extern "C" fn constraint_value_get_value_type(constraint: *const ConstraintWithSpan) -> *mut c_char {
    let Constraint::Value { value_type, .. } = &borrow(constraint).constraint else {
        unreachable!("Expected constraint to be Value");
    };
    release_string(value_type.name().to_owned())
}

#[no_mangle]
pub extern "C" fn constraint_or_get_branches(constraint: *const ConstraintWithSpan) -> *mut ConjunctionIDIterator {
    let Constraint::Or { branches, .. } = &borrow(constraint).constraint else {
        unreachable!("Expected constraint to be Or");
    };
    release(ConjunctionIDIterator(CIterator(box_stream(branches.clone().into_iter()))))
}

#[no_mangle]
pub extern "C" fn constraint_not_get_conjunction(constraint: *const ConstraintWithSpan) -> *mut ConjunctionID {
    let Constraint::Not { conjunction, .. } = &borrow(constraint).constraint else {
        unreachable!("Expected constraint to be Not");
    };
    release(conjunction.clone())
}

#[no_mangle]
pub extern "C" fn constraint_try_get_conjunction(constraint: *const ConstraintWithSpan) -> *mut ConjunctionID {
    let Constraint::Try { conjunction, .. } = &borrow(constraint).constraint else {
        unreachable!("Expected constraint to be Try");
    };
    release(conjunction.clone())
}

// ConstraintVertex accessors
#[no_mangle]
pub extern "C" fn constraint_vertex_is_variable(vertex: *const ConstraintVertex) -> bool {
    matches!(borrow(vertex), ConstraintVertex::Variable(_))
}

#[no_mangle]
pub extern "C" fn constraint_vertex_is_label(vertex: *const ConstraintVertex) -> bool {
    matches!(borrow(vertex), ConstraintVertex::Label(_))
}

#[no_mangle]
pub extern "C" fn constraint_vertex_is_value(vertex: *const ConstraintVertex) -> bool {
    matches!(borrow(vertex), ConstraintVertex::Value(_))
}

#[no_mangle]
pub extern "C" fn constraint_vertex_is_named_role(vertex: *const ConstraintVertex) -> bool {
    matches!(borrow(vertex), ConstraintVertex::NamedRole(_))
}

#[no_mangle]
pub extern "C" fn constraint_vertex_as_variable(vertex: *const ConstraintVertex) -> *mut Variable {
    match borrow(vertex) {
        ConstraintVertex::Variable(var) => release(var.clone()),
        _ => unreachable!(),
    }
}

#[no_mangle]
pub extern "C" fn constraint_vertex_as_label(vertex: *const ConstraintVertex) -> *mut Concept {
    let as_concept = match borrow(vertex) {
        ConstraintVertex::Label(t) => type_to_concept(t.clone()),
        _ => unreachable!(),
    };
    release(as_concept)
}

#[no_mangle]
pub extern "C" fn constraint_vertex_as_value(vertex: *const ConstraintVertex) -> *mut Concept {
    match borrow(vertex) {
        ConstraintVertex::Value(value) => release(Concept::Value(value.clone())),
        _ => unreachable!(),
    }
}

#[no_mangle]
pub extern "C" fn constraint_vertex_as_named_role_get_variable(vertex: *const ConstraintVertex) -> *mut Variable {
    match borrow(vertex) {
        ConstraintVertex::NamedRole(value) => release(value.variable.clone()),
        _ => unreachable!(),
    }
}

#[no_mangle]
pub extern "C" fn constraint_vertex_as_named_role_get_name(vertex: *const ConstraintVertex) -> *mut c_char {
    match borrow(vertex) {
        ConstraintVertex::NamedRole(value) => release_string(value.name.clone()),
        _ => unreachable!(),
    }
}

macro_rules! iterator_methods {
    ($($type_prefix:ident $fun_prefix:ident)*) => {
        paste! {$(
            #[doc=concat!("Forwards the <code>" , stringify!([<$type_prefix Iterator>]), "</code> and returns the next <code>", stringify!($type_prefix), "</code> if it exists, or null if there are no more elements.")]
            #[no_mangle]
            pub extern "C" fn [<$fun_prefix _iterator_next>](it: *mut [<$type_prefix Iterator>]) -> *mut $type_prefix {
                unsafe { iterator_next(addr_of_mut!((*it).0)) }
            }

            #[doc=concat!("Frees the native rust <code>", stringify!([<$type_prefix Iterator>]), "</code> object.")]
            #[no_mangle]
            pub extern "C" fn [<$fun_prefix _iterator_drop>](it: *mut [<$type_prefix Iterator>]) {
                free(it);
            }
        )*}
    };
}

iterator_methods! {
    // Use and then copy expansion.
}

#[doc = "Forwards the <code>FunctionIterator</code> and returns the next <code>Function</code> if it exists, or null if there are no more elements."]
#[no_mangle]
pub extern "C" fn function_iterator_next(it: *mut FunctionIterator) -> *mut Function {
    unsafe { iterator_next(addr_of_mut!((*it).0)) }
}
#[doc = "Frees the native rust <code>FunctionIterator</code> object."]
#[no_mangle]
pub extern "C" fn function_iterator_drop(it: *mut FunctionIterator) {
    free(it);
}

#[doc = "Forwards the <code>ConjunctionIDIterator</code> and returns the next <code>ConjunctionID</code> if it exists, or null if there are no more elements."]
#[no_mangle]
pub extern "C" fn conjunction_id_iterator_next(it: *mut ConjunctionIDIterator) -> *mut ConjunctionID {
    unsafe { iterator_next(addr_of_mut!((*it).0)) }
}

#[doc = "Frees the native rust <code>ConjunctionIDIterator</code> object."]
#[no_mangle]
pub extern "C" fn conjunction_id_iterator_drop(it: *mut ConjunctionIDIterator) {
    free(it);
}
#[doc = "Forwards the <code>ConstraintWithSpanIterator</code> and returns the next <code>ConstraintWithSpan</code> if it exists, or null if there are no more elements."]
#[no_mangle]
pub extern "C" fn constraint_with_span_iterator_next(it: *mut ConstraintWithSpanIterator) -> *mut ConstraintWithSpan {
    unsafe { iterator_next(addr_of_mut!((*it).0)) }
}

#[doc = "Frees the native rust <code>ConstraintWithSpanIterator</code> object."]
#[no_mangle]
pub extern "C" fn constraint_with_span_iterator_drop(it: *mut ConstraintWithSpanIterator) {
    free(it);
}

#[doc = "Forwards the <code>ConstraintVertexIterator</code> and returns the next <code>ConstraintVertex</code> if it exists, or null if there are no more elements."]
#[no_mangle]
pub extern "C" fn constraint_vertex_iterator_next(it: *mut ConstraintVertexIterator) -> *mut ConstraintVertex {
    unsafe { iterator_next(addr_of_mut!((*it).0)) }
}

#[doc = "Frees the native rust <code>ConstraintVertexIterator</code> object."]
#[no_mangle]
pub extern "C" fn constraint_vertex_iterator_drop(it: *mut ConstraintVertexIterator) {
    free(it);
}

#[doc = "Forwards the <code>PipelineStageIterator</code> and returns the next <code>PipelineStage</code> if it exists, or null if there are no more elements."]
#[no_mangle]
pub extern "C" fn pipeline_stage_iterator_next(it: *mut PipelineStageIterator) -> *mut PipelineStage {
    unsafe { iterator_next(addr_of_mut!((*it).0)) }
}

#[doc = "Frees the native rust <code>SortVariableIterator</code> object."]
#[no_mangle]
pub extern "C" fn sort_variable_iterator_drop(it: *mut SortVariableIterator) {
    free(it);
}

#[doc = "Forwards the <code>ReduceAssignmentIterator</code> and returns the next <code>ReduceAssignment</code> if it exists, or null if there are no more elements."]
#[no_mangle]
pub extern "C" fn reduce_assignment_iterator_next(it: *mut ReduceAssignmentIterator) -> *mut ReduceAssignment {
    unsafe { iterator_next(addr_of_mut!((*it).0)) }
}

#[doc = "Frees the native rust <code>ReduceAssignmentIterator</code> object."]
#[no_mangle]
pub extern "C" fn reduce_assignment_iterator_drop(it: *mut ReduceAssignmentIterator) {
    free(it);
}

#[doc = "Forwards the <code>ReducerIterator</code> and returns the next <code>Reducer</code> if it exists, or null if there are no more elements."]
#[no_mangle]
pub extern "C" fn reducer_iterator_next(it: *mut ReducerIterator) -> *mut Reducer {
    unsafe { iterator_next(addr_of_mut!((*it).0)) }
}

#[doc = "Frees the native rust <code>ReducerIterator</code> object."]
#[no_mangle]
pub extern "C" fn reducer_iterator_drop(it: *mut ReducerIterator) {
    free(it);
}

#[doc = "Frees the native rust <code>PipelineStageIterator</code> object."]
#[no_mangle]
pub extern "C" fn pipeline_stage_iterator_drop(it: *mut PipelineStageIterator) {
    free(it);
}

#[doc = "Forwards the <code>SortVariableIterator</code> and returns the next <code>SortVariable</code> if it exists, or null if there are no more elements."]
#[no_mangle]
pub extern "C" fn sort_variable_iterator_next(it: *mut SortVariableIterator) -> *mut SortVariable {
    unsafe { iterator_next(addr_of_mut!((*it).0)) }
}

#[doc = "Forwards the <code>VariableAnnotationsIterator</code> and returns the next <code>VariableAnnotations</code> if it exists, or null if there are no more elements."]
#[no_mangle]
pub extern "C" fn variable_annotations_iterator_next(it: *mut VariableAnnotationsIterator) -> *mut VariableAnnotations {
    unsafe { iterator_next(addr_of_mut!((*it).0)) }
}

#[doc = "Frees the native rust <code>VariableAnnotationsIterator</code> object."]
#[no_mangle]
pub extern "C" fn variable_annotations_iterator_drop(it: *mut VariableAnnotationsIterator) {
    free(it);
}

#[doc = "Forwards the <code>VariableIterator</code> and returns the next <code>Variable</code> if it exists, or null if there are no more elements."]
#[no_mangle]
pub extern "C" fn variable_iterator_next(it: *mut VariableIterator) -> *mut Variable {
    unsafe { iterator_next(addr_of_mut!((*it).0)) }
}

#[doc = "Frees the native rust <code>VariableIterator</code> object."]
#[no_mangle]
pub extern "C" fn variable_iterator_drop(it: *mut VariableIterator) {
    free(it);
}

macro_rules! drop_methods {
    ($($type_name:ident $fun_prefix:ident)*) => {
        paste! {$(
            #[doc=concat!("Frees the native rust <code>", stringify!($type_name), "</code> object.")]
            #[no_mangle]
            pub extern "C" fn [<$fun_prefix _drop>](obj: *mut $type_name) {
                free(obj);
            }
        )*}
    };
}

drop_methods! {
    // Use and then copy expansion.
}

#[doc = "Frees the native rust <code>AnalyzedQuery</code> object."]
#[no_mangle]
pub extern "C" fn analyzed_query_drop(obj: *mut AnalyzedQuery) {
    free(obj);
}

#[doc = "Frees the native rust <code>Conjunction</code> object."]
#[no_mangle]
pub extern "C" fn conjunction_drop(obj: *mut Conjunction) {
    free(obj);
}

#[doc = "Frees the native rust <code>ConjunctionID</code> object."]
#[no_mangle]
pub extern "C" fn conjunction_id_drop(obj: *mut ConjunctionID) {
    free(obj);
}

#[doc = "Frees the native rust <code>Constraint</code> object."]
#[no_mangle]
pub extern "C" fn constraint_with_span_drop(obj: *mut ConstraintWithSpan) {
    free(obj);
}

#[doc = "Frees the native rust <code>ConstraintVertex</code> object."]
#[no_mangle]
pub extern "C" fn constraint_vertex_drop(obj: *mut ConstraintVertex) {
    free(obj);
}

#[doc = "Frees the native rust <code>Fetch</code> object."]
#[no_mangle]
pub extern "C" fn fetch_drop(obj: *mut Fetch) {
    free(obj);
}

#[doc = "Frees the native rust <code>Function</code> object."]
#[no_mangle]
pub extern "C" fn function_drop(obj: *mut Function) {
    free(obj);
}

#[doc = "Frees the native rust <code>ReturnOperation</code> object."]
#[no_mangle]
pub extern "C" fn return_operation_drop(obj: *mut ReturnOperation) {
    free(obj);
}

#[doc = "Frees the native rust <code>Pipeline</code> object."]
#[no_mangle]
pub extern "C" fn pipeline_drop(obj: *mut Pipeline) {
    free(obj);
}

#[doc = "Frees the native rust <code>PipelineStage</code> object."]
#[no_mangle]
pub extern "C" fn pipeline_stage_drop(obj: *mut PipelineStage) {
    free(obj);
}

#[doc = "Frees the native rust <code>Reducer</code> object."]
#[no_mangle]
pub extern "C" fn reducer_drop(obj: *mut Reducer) {
    free(obj);
}

#[doc = "Frees the native rust <code>ReduceAssignment</code> object."]
#[no_mangle]
pub extern "C" fn reduce_assignment_drop(obj: *mut ReduceAssignment) {
    free(obj);
}

#[doc = "Frees the native rust <code>SortVariable</code> object."]
#[no_mangle]
pub extern "C" fn sort_variable_drop(obj: *mut SortVariable) {
    free(obj);
}

#[doc = "Frees the native rust <code>VariableAnnotations</code> object."]
#[no_mangle]
pub extern "C" fn variable_annotations_drop(obj: *mut VariableAnnotations) {
    free(obj);
}

#[doc = "Frees the native rust <code>Variable</code> object."]
#[no_mangle]
pub extern "C" fn variable_drop(obj: *mut Variable) {
    free(obj);
}

fn type_to_concept(type_: Type) -> Concept {
    match type_ {
        Type::EntityType(t) => Concept::EntityType(t),
        Type::RelationType(t) => Concept::RelationType(t),
        Type::AttributeType(t) => Concept::AttributeType(t),
        Type::RoleType(t) => Concept::RoleType(t),
    }
}
