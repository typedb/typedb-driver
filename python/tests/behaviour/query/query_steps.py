# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.
import json
from concurrent.futures.thread import ThreadPoolExecutor
from datetime import datetime
from decimal import Decimal
from functools import partial
from typing import Optional

from behave import *
from hamcrest import *
from tests.behaviour.config.parameters import ConceptKind, MayError, ValueType, parse_bool, parse_list, \
    try_parse_value_type, is_or_not_reason, check_is_none
from tests.behaviour.context import Context
from tests.behaviour.util.util import list_contains_json
from typedb.api.answer.query_type import QueryType
from typedb.api.concept.concept import Concept
from typedb.api.concept.instance.attribute import Attribute
from typedb.api.concept.instance.entity import Entity
from typedb.api.concept.instance.instance import Instance
from typedb.api.concept.instance.relation import Relation
from typedb.api.concept.type.attribute_type import AttributeType
from typedb.api.concept.type.entity_type import EntityType
from typedb.api.concept.type.relation_type import RelationType
from typedb.api.concept.type.role_type import RoleType
from typedb.api.concept.type.type import Type
from typedb.api.concept.value.value import Value
from typedb.common.datetime import Datetime
from typedb.common.duration import Duration
from typedb.driver import *


@step("typeql write query{may_error:MayError}")
@step("typeql read query{may_error:MayError}")
@step("typeql schema query{may_error:MayError}")
def step_impl(context: Context, may_error: MayError):
    context.clear_answers()
    may_error.check(lambda: context.tx().query(query=context.text).resolve())


@step("get answers of typeql write query")
@step("get answers of typeql read query")
@step("get answers of typeql schema query")
def step_impl(context: Context):
    context.clear_answers()
    context.answer = context.tx().query(query=context.text).resolve()


@step("concurrently get answers of typeql write query {count:Int} times")
@step("concurrently get answers of typeql read query {count:Int} times")
@step("concurrently get answers of typeql schema query {count:Int} times")
def step_impl(context: Context, count: int):
    assert_that(count, is_(less_than_or_equal_to(context.THREAD_POOL_SIZE)))
    with ThreadPoolExecutor(max_workers=context.THREAD_POOL_SIZE) as executor:
        context.clear_concurrent_answers()
        context.concurrent_answers = []
        for i in range(count):
            context.concurrent_answers.append(
                executor.submit(partial(lambda: context.tx().query(query=context.text).resolve())))


@step("answer type {is_or_not:IsOrNot}: {answer_type}")
def step_impl(context: Context, is_or_not: bool, answer_type: str):
    if answer_type == "ok":
        assert_that(context.answer.is_ok(), is_(is_or_not), "Expected is_ok()")
    elif answer_type == "concept rows":
        assert_that(context.answer.is_concept_rows(), is_(is_or_not), "Expected is_concept_rows()")
    elif answer_type == "concept documents":
        assert_that(context.answer.is_concept_documents(), is_(is_or_not), "Expected is_concept_documents()")


def unwrap_answer_ok(context: Context):
    context.unwrapped_answer = context.answer.as_ok()


def unwrap_answer_rows(context: Context):
    context.unwrapped_answer = context.answer.as_concept_rows()


def unwrap_answer_documents(context: Context):
    context.unwrapped_answer = context.answer.as_concept_documents()


@step("answer unwraps as ok{may_error:MayError}")
def step_impl(context: Context, may_error: MayError):
    may_error.check(lambda: unwrap_answer_ok(context))


@step("answer unwraps as concept rows{may_error:MayError}")
def step_impl(context: Context, may_error: MayError):
    may_error.check(lambda: unwrap_answer_rows(context))


@step("answer unwraps as concept documents{may_error:MayError}")
def step_impl(context: Context, may_error: MayError):
    may_error.check(lambda: unwrap_answer_documents(context))


def unwrap_answer_if_needed(context: Context):
    if not context.answer:
        raise ValueError("Cannot unwrap test answer as answer is absent")

    if context.unwrapped_answer is None:
        if context.answer.is_ok():
            unwrap_answer_ok(context)
        elif context.answer.is_concept_rows():
            unwrap_answer_rows(context)
        elif context.answer.is_concept_documents():
            unwrap_answer_documents(context)
        else:
            raise ValueError(
                "Cannot unwrap answer: it should be in Ok/ConceptRows/ConceptDocuments, but appeared to be something else")


def unwrap_concurrent_answers_if_needed(context: Context):
    if context.unwrapped_concurrent_answers is None:
        if not context.concurrent_answers:
            raise ValueError("Cannot unwrap test concurrent answers as concurrent answers are absent")

        context.unwrapped_concurrent_answers = []
        for answer_future in context.concurrent_answers:
            answer = answer_future.result()
            if answer.is_ok():
                context.unwrapped_concurrent_answers.append(answer.as_ok())
            elif answer.is_concept_rows():
                context.unwrapped_concurrent_answers.append(answer.as_concept_rows())
            elif answer.is_concept_documents():
                context.unwrapped_concurrent_answers.append(answer.as_concept_documents())
            else:
                raise ValueError(
                    "Cannot unwrap answer: it should be in Ok/ConceptRows/ConceptDocuments, but appeared to be something else")


def collect_answer_if_needed(context: Context):
    unwrap_answer_if_needed(context)

    if context.collected_answer is None:
        if context.unwrapped_answer.is_concept_rows() or context.unwrapped_answer.is_concept_documents():
            context.collected_answer = list(context.unwrapped_answer)
        else:
            raise NotImplemented("Cannot collect answer")


@step("answer size is: {size:Int}")
def step_impl(context: Context, size: int):
    collect_answer_if_needed(context)
    assert_that(len(context.collected_answer), is_(size))


@step("answer query type {is_or_not:IsOrNot}: {query_type:QueryType}")
def step_impl(context: Context, is_or_not: bool, query_type: QueryType):
    answer_query_type = context.answer.query_type
    assert_that(answer_query_type == query_type, is_(is_or_not),
                is_or_not_reason(is_or_not, real=answer_query_type, expected=query_type))


@step("concurrently process {count:Int} row from answers{may_error:MayError}")
@step("concurrently process {count:Int} rows from answers{may_error:MayError}")
def step_impl(context: Context, count: int, may_error: MayError):
    answers_num = len(context.concurrent_answers)
    assert_that(answers_num, is_(less_than_or_equal_to(context.THREAD_POOL_SIZE)))

    unwrap_concurrent_answers_if_needed(context)

    with ThreadPoolExecutor(max_workers=context.THREAD_POOL_SIZE) as executor:
        futures = [
            executor.submit(lambda it=answer: [next(it) for _ in range(count)])
            for answer in context.unwrapped_concurrent_answers
        ]

        for future in futures:
            may_error.check(future.result, exception=StopIteration)


@step("answer column names are")
def step_impl(context: Context):
    collect_answer_if_needed(context)
    column_names = context.collected_answer[0].column_names()
    assert_that(sorted(list(column_names)), equal_to(sorted(parse_list(context.table))))


def get_row_get_concepts(context: Context, row_index: int) -> [Concept]:
    collect_answer_if_needed(context)
    row = context.collected_answer[row_index]
    return list(row.concepts())


def get_row_get_concept(context: Context, row_index: int, var: str, is_by_var_index: bool) -> Concept:
    collect_answer_if_needed(context)
    row = context.collected_answer[row_index]
    return row.get_index(list(context.collected_answer[0].column_names()).index(var)) if is_by_var_index \
        else row.get(var)


def get_row_get_type(context: Context, row_index: int, var: str, is_by_var_index: bool) -> Type:
    return get_row_get_concept(context, row_index, var, is_by_var_index).as_type()


def get_row_get_instance(context: Context, row_index: int, var: str, is_by_var_index: bool) -> Instance:
    return get_row_get_concept(context, row_index, var, is_by_var_index).as_instance()


def get_row_get_entity_type(context: Context, row_index: int, var: str, is_by_var_index: bool) -> EntityType:
    return get_row_get_concept(context, row_index, var, is_by_var_index).as_entity_type()


def get_row_get_relation_type(context: Context, row_index: int, var: str, is_by_var_index: bool) -> RelationType:
    return get_row_get_concept(context, row_index, var, is_by_var_index).as_relation_type()


def get_row_get_attribute_type(context: Context, row_index: int, var: str, is_by_var_index: bool) -> AttributeType:
    return get_row_get_concept(context, row_index, var, is_by_var_index).as_attribute_type()


def get_row_get_role_type(context: Context, row_index: int, var: str, is_by_var_index: bool) -> RoleType:
    return get_row_get_concept(context, row_index, var, is_by_var_index).as_role_type()


def get_row_get_entity(context: Context, row_index: int, var: str, is_by_var_index: bool) -> Entity:
    return get_row_get_concept(context, row_index, var, is_by_var_index).as_entity()


def get_row_get_relation(context: Context, row_index: int, var: str, is_by_var_index: bool) -> Relation:
    return get_row_get_concept(context, row_index, var, is_by_var_index).as_relation()


def get_row_get_attribute(context: Context, row_index: int, var: str, is_by_var_index: bool) -> Attribute:
    return get_row_get_concept(context, row_index, var, is_by_var_index).as_attribute()


def get_row_get_value(context: Context, row_index: int, var: str, is_by_var_index: bool) -> Value:
    return get_row_get_concept(context, row_index, var, is_by_var_index).as_value()


def get_row_get_concept_of_kind(context: Context, row_index: int, var: str, is_by_var_index: bool, kind: ConceptKind):
    if kind == ConceptKind.CONCEPT:
        return get_row_get_concept(context, row_index, var, is_by_var_index)
    elif kind == ConceptKind.TYPE:
        return get_row_get_type(context, row_index, var, is_by_var_index)
    elif kind == ConceptKind.INSTANCE:
        return get_row_get_instance(context, row_index, var, is_by_var_index)
    elif kind == ConceptKind.ENTITY_TYPE:
        return get_row_get_entity_type(context, row_index, var, is_by_var_index)
    elif kind == ConceptKind.RELATION_TYPE:
        return get_row_get_relation_type(context, row_index, var, is_by_var_index)
    elif kind == ConceptKind.ATTRIBUTE_TYPE:
        return get_row_get_attribute_type(context, row_index, var, is_by_var_index)
    elif kind == ConceptKind.ROLE_TYPE:
        return get_row_get_role_type(context, row_index, var, is_by_var_index)
    elif kind == ConceptKind.ENTITY:
        return get_row_get_entity(context, row_index, var, is_by_var_index)
    elif kind == ConceptKind.RELATION:
        return get_row_get_relation(context, row_index, var, is_by_var_index)
    elif kind == ConceptKind.ATTRIBUTE:
        return get_row_get_attribute(context, row_index, var, is_by_var_index)
    elif kind == ConceptKind.VALUE:
        return get_row_get_value(context, row_index, var, is_by_var_index)
    else:
        raise ValueError(f"Not all ConceptKind variants are covered! Found {kind}")


@step("answer get row({row_index:Int}) query type {is_or_not:IsOrNot}: {query_type:QueryType}")
def step_impl(context: Context, row_index: int, is_or_not: bool, query_type: QueryType):
    collect_answer_if_needed(context)
    answer_query_type = context.collected_answer[row_index].query_type
    assert_that(answer_query_type == query_type, is_(is_or_not),
                is_or_not_reason(is_or_not, real=answer_query_type, expected=query_type))


@step("answer get row({row_index:Int}) get variable{is_by_var_index:IsByVarIndex}({var:Var}){may_error:MayError}")
def step_impl(context: Context, row_index: int, is_by_var_index: bool, var: str, may_error: MayError):
    may_error.check(lambda: get_row_get_concept(context, row_index, var, is_by_var_index))


@step(
    "answer get row({row_index:Int}) get variable{is_by_var_index:IsByVarIndex}({var:Var}) as {kind:ConceptKind}{may_error:MayError}")
def step_impl(context: Context, row_index: int, is_by_var_index: bool, var: str, kind: ConceptKind,
              may_error: MayError):
    may_error.check(lambda: get_row_get_concept_of_kind(context, row_index, var, is_by_var_index, kind))


@step(
    "answer get row({row_index:Int}) get {kind:ConceptKind}{is_by_var_index:IsByVarIndex}({var:Var}) is type: {is_type:Bool}")
def step_impl(context: Context, row_index: int, kind: ConceptKind, is_by_var_index: bool, var: str, is_type: bool):
    assert_that(get_row_get_concept(context, row_index, var, is_by_var_index).is_type(), is_(is_type))


@step(
    "answer get row({row_index:Int}) get {kind:ConceptKind}{is_by_var_index:IsByVarIndex}({var:Var}) is instance: {is_instance:Bool}")
def step_impl(context: Context, row_index: int, kind: ConceptKind, is_by_var_index: bool, var: str, is_instance: bool):
    assert_that(get_row_get_concept(context, row_index, var, is_by_var_index).is_instance(), is_(is_instance))


@step(
    "answer get row({row_index:Int}) get {kind:ConceptKind}{is_by_var_index:IsByVarIndex}({var:Var}) is value: {is_value:Bool}")
def step_impl(context: Context, row_index: int, kind: ConceptKind, is_by_var_index: bool, var: str, is_value: bool):
    assert_that(get_row_get_concept(context, row_index, var, is_by_var_index).is_value(), is_(is_value))


@step(
    "answer get row({row_index:Int}) get {kind:ConceptKind}{is_by_var_index:IsByVarIndex}({var:Var}) is entity type: {is_entity_type:Bool}")
def step_impl(context: Context, row_index: int, kind: ConceptKind, is_by_var_index: bool, var: str,
              is_entity_type: bool):
    assert_that(get_row_get_concept(context, row_index, var, is_by_var_index).is_entity_type(), is_(is_entity_type))


@step(
    "answer get row({row_index:Int}) get {kind:ConceptKind}{is_by_var_index:IsByVarIndex}({var:Var}) is relation type: {is_relation_type:Bool}")
def step_impl(context: Context, row_index: int, kind: ConceptKind, is_by_var_index: bool, var: str,
              is_relation_type: bool):
    assert_that(get_row_get_concept(context, row_index, var, is_by_var_index).is_relation_type(), is_(is_relation_type))


@step(
    "answer get row({row_index:Int}) get {kind:ConceptKind}{is_by_var_index:IsByVarIndex}({var:Var}) is attribute type: {is_attribute_type:Bool}")
def step_impl(context: Context, row_index: int, kind: ConceptKind, is_by_var_index: bool, var: str,
              is_attribute_type: bool):
    assert_that(get_row_get_concept(context, row_index, var, is_by_var_index).is_attribute_type(),
                is_(is_attribute_type))


@step(
    "answer get row({row_index:Int}) get {kind:ConceptKind}{is_by_var_index:IsByVarIndex}({var:Var}) is role type: {is_role_type:Bool}")
def step_impl(context: Context, row_index: int, kind: ConceptKind, is_by_var_index: bool, var: str, is_role_type: bool):
    assert_that(get_row_get_concept(context, row_index, var, is_by_var_index).is_role_type(), is_(is_role_type))


@step(
    "answer get row({row_index:Int}) get {kind:ConceptKind}{is_by_var_index:IsByVarIndex}({var:Var}) is entity: {is_entity:Bool}")
def step_impl(context: Context, row_index: int, kind: ConceptKind, is_by_var_index: bool, var: str, is_entity: bool):
    assert_that(get_row_get_concept(context, row_index, var, is_by_var_index).is_entity(), is_(is_entity))


@step(
    "answer get row({row_index:Int}) get {kind:ConceptKind}{is_by_var_index:IsByVarIndex}({var:Var}) is relation: {is_relation:Bool}")
def step_impl(context: Context, row_index: int, kind: ConceptKind, is_by_var_index: bool, var: str, is_relation: bool):
    assert_that(get_row_get_concept(context, row_index, var, is_by_var_index).is_relation(), is_(is_relation))


@step(
    "answer get row({row_index:Int}) get {kind:ConceptKind}{is_by_var_index:IsByVarIndex}({var:Var}) is attribute: {is_attribute:Bool}")
def step_impl(context: Context, row_index: int, kind: ConceptKind, is_by_var_index: bool, var: str, is_attribute: bool):
    assert_that(get_row_get_concept(context, row_index, var, is_by_var_index).is_attribute(), is_(is_attribute))


@step(
    "answer get row({row_index:Int}) get {kind:ConceptKind}{is_by_var_index:IsByVarIndex}({var:Var}) get type is type: {is_type:Bool}")
def step_impl(context: Context, row_index: int, kind: ConceptKind, is_by_var_index: bool, var: str, is_type: bool):
    assert_that(get_row_get_concept(context, row_index, var, is_by_var_index).get_type().is_type(), is_(is_type))


@step(
    "answer get row({row_index:Int}) get {kind:ConceptKind}{is_by_var_index:IsByVarIndex}({var:Var}) get type is instance: {is_instance:Bool}")
def step_impl(context: Context, row_index: int, kind: ConceptKind, is_by_var_index: bool, var: str, is_instance: bool):
    assert_that(get_row_get_concept(context, row_index, var, is_by_var_index).get_type().is_instance(),
                is_(is_instance))


@step(
    "answer get row({row_index:Int}) get {kind:ConceptKind}{is_by_var_index:IsByVarIndex}({var:Var}) get type is value: {is_value:Bool}")
def step_impl(context: Context, row_index: int, kind: ConceptKind, is_by_var_index: bool, var: str, is_value: bool):
    assert_that(get_row_get_concept(context, row_index, var, is_by_var_index).get_type().is_value(), is_(is_value))


@step(
    "answer get row({row_index:Int}) get {kind:ConceptKind}{is_by_var_index:IsByVarIndex}({var:Var}) get type is entity type: {is_entity_type:Bool}")
def step_impl(context: Context, row_index: int, kind: ConceptKind, is_by_var_index: bool, var: str,
              is_entity_type: bool):
    assert_that(get_row_get_concept(context, row_index, var, is_by_var_index).get_type().is_entity_type(),
                is_(is_entity_type))


@step(
    "answer get row({row_index:Int}) get {kind:ConceptKind}{is_by_var_index:IsByVarIndex}({var:Var}) get type is relation type: {is_relation_type:Bool}")
def step_impl(context: Context, row_index: int, kind: ConceptKind, is_by_var_index: bool, var: str,
              is_relation_type: bool):
    assert_that(get_row_get_concept(context, row_index, var, is_by_var_index).get_type().is_relation_type(),
                is_(is_relation_type))


@step(
    "answer get row({row_index:Int}) get {kind:ConceptKind}{is_by_var_index:IsByVarIndex}({var:Var}) get type is attribute type: {is_attribute_type:Bool}")
def step_impl(context: Context, row_index: int, kind: ConceptKind, is_by_var_index: bool, var: str,
              is_attribute_type: bool):
    assert_that(get_row_get_concept(context, row_index, var, is_by_var_index).get_type().is_attribute_type(),
                is_(is_attribute_type))


@step(
    "answer get row({row_index:Int}) get {kind:ConceptKind}{is_by_var_index:IsByVarIndex}({var:Var}) get type is role type: {is_role_type:Bool}")
def step_impl(context: Context, row_index: int, kind: ConceptKind, is_by_var_index: bool, var: str, is_role_type: bool):
    assert_that(get_row_get_concept(context, row_index, var, is_by_var_index).get_type().is_role_type(),
                is_(is_role_type))


@step(
    "answer get row({row_index:Int}) get {kind:ConceptKind}{is_by_var_index:IsByVarIndex}({var:Var}) get type is entity: {is_entity:Bool}")
def step_impl(context: Context, row_index: int, kind: ConceptKind, is_by_var_index: bool, var: str, is_entity: bool):
    assert_that(get_row_get_concept(context, row_index, var, is_by_var_index).get_type().is_entity(), is_(is_entity))


@step(
    "answer get row({row_index:Int}) get {kind:ConceptKind}{is_by_var_index:IsByVarIndex}({var:Var}) get type is relation: {is_relation:Bool}")
def step_impl(context: Context, row_index: int, kind: ConceptKind, is_by_var_index: bool, var: str, is_relation: bool):
    assert_that(get_row_get_concept(context, row_index, var, is_by_var_index).get_type().is_relation(),
                is_(is_relation))


@step(
    "answer get row({row_index:Int}) get {kind:ConceptKind}{is_by_var_index:IsByVarIndex}({var:Var}) get type is attribute: {is_attribute:Bool}")
def step_impl(context: Context, row_index: int, kind: ConceptKind, is_by_var_index: bool, var: str, is_attribute: bool):
    assert_that(get_row_get_concept(context, row_index, var, is_by_var_index).get_type().is_attribute(),
                is_(is_attribute))


@step(
    "answer get row({row_index:Int}) get {kind:ConceptKind}{is_by_var_index:IsByVarIndex}({var:Var}) get label: {label}")
def step_impl(context: Context, row_index: int, kind: ConceptKind, is_by_var_index: bool, var: str, label: str):
    concept_label = get_row_get_concept_of_kind(context, row_index, var, is_by_var_index, kind).get_label()
    assert_that(concept_label, is_(label))


@step(
    "answer get row({row_index:Int}) get {kind:ConceptKind}{is_by_var_index:IsByVarIndex}({var:Var}) try get label {is_or_not:IsOrNot} none")
def step_impl(context: Context, row_index: int, kind: ConceptKind, is_by_var_index: bool, var: str,
              is_or_not: bool):
    concept_label = get_row_get_concept_of_kind(context, row_index, var, is_by_var_index, kind).try_get_label()
    check_is_none(concept_label, is_or_not)


@step(
    "answer get row({row_index:Int}) get {kind:ConceptKind}{is_by_var_index:IsByVarIndex}({var:Var}) try get label: {label}")
def step_impl(context: Context, row_index: int, kind: ConceptKind, is_by_var_index: bool, var: str, label: str):
    concept_label = get_row_get_concept_of_kind(context, row_index, var, is_by_var_index, kind).try_get_label()
    assert_that(concept_label, is_(label))


@step(
    "answer get row({row_index:Int}) get {kind:ConceptKind}{is_by_var_index:IsByVarIndex}({var:Var}) get type get label: {label}")
def step_impl(context: Context, row_index: int, kind: ConceptKind, is_by_var_index: bool, var: str, label: str):
    concept_label = get_row_get_concept_of_kind(context, row_index, var, is_by_var_index, kind).get_type().get_label()
    assert_that(concept_label, is_(label))


@step(
    "answer get row({row_index:Int}) get {kind:ConceptKind}{is_by_var_index:IsByVarIndex}({var:Var}) try get value type: {value_type:Words}")
def step_impl(context: Context, row_index: int, kind: ConceptKind, is_by_var_index: bool, var: str, value_type: str):
    concept_value_type = get_row_get_concept_of_kind(context, row_index, var, is_by_var_index,
                                                     kind).try_get_value_type()
    assert_that(concept_value_type, is_(value_type))


@step(
    "answer get row({row_index:Int}) get value{is_by_var_index:IsByVarIndex}({var:Var}) get value type: {value_type:Words}")
def step_impl(context: Context, row_index: int, is_by_var_index: bool, var: str, value_type: str):
    value_value_type = get_row_get_value(context, row_index, var, is_by_var_index).get_type()
    assert_that(value_value_type, is_(value_type))


@step(
    "answer get row({row_index:Int}) get {kind:ConceptKind}{is_by_var_index:IsByVarIndex}({var:Var}) try get value type {is_or_not:IsOrNot} none")
def step_impl(context: Context, row_index: int, kind: ConceptKind, is_by_var_index: bool, var: str,
              is_or_not: bool):
    concept_value_type = get_row_get_concept_of_kind(context, row_index, var, is_by_var_index,
                                                     kind).try_get_value_type()
    check_is_none(concept_value_type, is_or_not)


@step(
    "answer get row({row_index:Int}) get {kind:ConceptKind}{is_by_var_index:IsByVarIndex}({var:Var}) try get value type: {value_type:Words}")
def step_impl(context: Context, row_index: int, kind: ConceptKind, is_by_var_index: bool, var: str, value_type: str):
    concept_value_type = get_row_get_concept_of_kind(context, row_index, var, is_by_var_index,
                                                     kind).try_get_value_type()
    assert_that(concept_value_type, is_(value_type))


@step(
    "answer get row({row_index:Int}) get attribute{is_by_var_index:IsByVarIndex}({var:Var}) get type get value type: {value_type:Words}")
def step_impl(context: Context, row_index: int, is_by_var_index: bool, var: str, value_type: str):
    type_value_type = get_row_get_attribute(context, row_index, var, is_by_var_index).get_type().try_get_value_type()
    # can be "none" (not a value type), that's why we don't convert it to ValueType
    assert_that(type_value_type, is_(value_type))


@step(
    "answer get row({row_index:Int}) get {kind:ConceptKind}{is_by_var_index:IsByVarIndex}({var:Var}) is boolean: {is_boolean:Bool}")
def step_impl(context: Context, row_index: int, kind: ConceptKind, is_by_var_index: bool, var: str, is_boolean: bool):
    result = get_row_get_concept_of_kind(context, row_index, var, is_by_var_index, kind).is_boolean()
    assert_that(result, is_(is_boolean))


@step(
    "answer get row({row_index:Int}) get {kind:ConceptKind}{is_by_var_index:IsByVarIndex}({var:Var}) is long: {is_long:Bool}")
def step_impl(context: Context, row_index: int, kind: ConceptKind, is_by_var_index: bool, var: str, is_long: bool):
    result = get_row_get_concept_of_kind(context, row_index, var, is_by_var_index, kind).is_long()
    assert_that(result, is_(is_long))


@step(
    "answer get row({row_index:Int}) get {kind:ConceptKind}{is_by_var_index:IsByVarIndex}({var:Var}) is double: {is_double:Bool}")
def step_impl(context: Context, row_index: int, kind: ConceptKind, is_by_var_index: bool, var: str, is_double: bool):
    result = get_row_get_concept_of_kind(context, row_index, var, is_by_var_index, kind).is_double()
    assert_that(result, is_(is_double))


@step(
    "answer get row({row_index:Int}) get {kind:ConceptKind}{is_by_var_index:IsByVarIndex}({var:Var}) is decimal: {is_decimal:Bool}")
def step_impl(context: Context, row_index: int, kind: ConceptKind, is_by_var_index: bool, var: str, is_decimal: bool):
    result = get_row_get_concept_of_kind(context, row_index, var, is_by_var_index, kind).is_decimal()
    assert_that(result, is_(is_decimal))


@step(
    "answer get row({row_index:Int}) get {kind:ConceptKind}{is_by_var_index:IsByVarIndex}({var:Var}) is string: {is_string:Bool}")
def step_impl(context: Context, row_index: int, kind: ConceptKind, is_by_var_index: bool, var: str, is_string: bool):
    result = get_row_get_concept_of_kind(context, row_index, var, is_by_var_index, kind).is_string()
    assert_that(result, is_(is_string))


@step(
    "answer get row({row_index:Int}) get {kind:ConceptKind}{is_by_var_index:IsByVarIndex}({var:Var}) is date: {is_date:Bool}")
def step_impl(context: Context, row_index: int, kind: ConceptKind, is_by_var_index: bool, var: str, is_date: bool):
    result = get_row_get_concept_of_kind(context, row_index, var, is_by_var_index, kind).is_date()
    assert_that(result, is_(is_date))


@step(
    "answer get row({row_index:Int}) get {kind:ConceptKind}{is_by_var_index:IsByVarIndex}({var:Var}) is datetime: {is_datetime:Bool}")
def step_impl(context: Context, row_index: int, kind: ConceptKind, is_by_var_index: bool, var: str, is_datetime: bool):
    result = get_row_get_concept_of_kind(context, row_index, var, is_by_var_index, kind).is_datetime()
    assert_that(result, is_(is_datetime))


@step(
    "answer get row({row_index:Int}) get {kind:ConceptKind}{is_by_var_index:IsByVarIndex}({var:Var}) is datetime-tz: {is_datetime_tz:Bool}")
def step_impl(context: Context, row_index: int, kind: ConceptKind, is_by_var_index: bool, var: str,
              is_datetime_tz: bool):
    result = get_row_get_concept_of_kind(context, row_index, var, is_by_var_index, kind).is_datetime_tz()
    assert_that(result, is_(is_datetime_tz))


@step(
    "answer get row({row_index:Int}) get {kind:ConceptKind}{is_by_var_index:IsByVarIndex}({var:Var}) is duration: {is_duration:Bool}")
def step_impl(context: Context, row_index: int, kind: ConceptKind, is_by_var_index: bool, var: str, is_duration: bool):
    result = get_row_get_concept_of_kind(context, row_index, var, is_by_var_index, kind).is_duration()
    assert_that(result, is_(is_duration))


@step(
    "answer get row({row_index:Int}) get {kind:ConceptKind}{is_by_var_index:IsByVarIndex}({var:Var}) is struct: {is_struct:Bool}")
def step_impl(context: Context, row_index: int, kind: ConceptKind, is_by_var_index: bool, var: str, is_struct: bool):
    result = get_row_get_concept_of_kind(context, row_index, var, is_by_var_index, kind).is_struct()
    assert_that(result, is_(is_struct))


@step(
    "answer get row({row_index:Int}) get {kind:ConceptKind}{is_by_var_index:IsByVarIndex}({var:Var}) {contains_or_doesnt:ContainsOrDoesnt} iid")
def step_impl(context: Context, row_index: int, kind: ConceptKind, is_by_var_index: bool, var: str,
              contains_or_doesnt: bool):
    concept_iid = get_row_get_concept_of_kind(context, row_index, var, is_by_var_index, kind).get_iid()
    if contains_or_doesnt:
        assert_that(concept_iid, is_not(None))
        assert_that(concept_iid, is_not(""))
    else:
        assert_that(concept_iid, is_(None))


@step(
    "answer get row({row_index:Int}) get {kind:ConceptKind}{is_by_var_index:IsByVarIndex}({var:Var}) try get iid {is_or_not:IsOrNot} none")
def step_impl(context: Context, row_index: int, kind: ConceptKind, is_by_var_index: bool, var: str,
              is_or_not: bool):
    concept_iid = get_row_get_concept_of_kind(context, row_index, var, is_by_var_index, kind).try_get_iid()
    check_is_none(concept_iid, is_or_not)


def parse_expected_value(value: str, value_type: Optional[ValueType]):
    if value_type is None:
        value_type = ValueType.STRUCT  # consider it as a custom struct

    if value_type == ValueType.BOOLEAN:
        return parse_bool(value)
    elif value_type == ValueType.LONG:
        return int(value)
    elif value_type == ValueType.DOUBLE:
        return float(value)
    elif value_type == ValueType.DECIMAL:
        return Decimal(value)
    elif value_type == ValueType.STRING:
        return value[1:-1].replace('\\"', '"')
    elif value_type == ValueType.DATE:
        date_format = "%Y-%m-%d"
        return datetime.strptime(value, date_format).date()
    elif value_type == ValueType.DATETIME:
        return Datetime.utcfromstring(value)
    elif value_type == ValueType.DATETIME_TZ:
        if " " in value:  # IANA timezone name
            value_dt, value_tz = value.split(" ")
            return Datetime.utcfromstring(value_dt, value_tz)
        else:  # offset
            offset_start = value.rfind("+")
            if offset_start == -1:
                offset_start = value.rfind("-")
            if offset_start == -1:
                raise ValueError("No IANA or Offset values for datetime-tz")
            value_dt, value_offset = value[:offset_start], Datetime.offset_seconds_fromstring(value[offset_start:])
            return Datetime.utcfromstring(value_dt, offset_seconds=value_offset)
    elif value_type == ValueType.DURATION:
        return Duration.fromstring(value)
    elif value_type == ValueType.STRUCT:
        return value  # compare string representations
    else:
        raise ValueError(f"ValueType {value_type} is not covered by this step")


def get_by_value_type(concept, value_type: ValueType) -> Concept.VALUE:
    if value_type == ValueType.BOOLEAN:
        return concept.get_boolean()
    elif value_type == ValueType.LONG:
        return concept.get_long()
    elif value_type == ValueType.DOUBLE:
        return concept.get_double()
    elif value_type == ValueType.DECIMAL:
        return concept.get_decimal()
    elif value_type == ValueType.STRING:
        return concept.get_string()
    elif value_type == ValueType.DATE:
        return concept.get_date()
    elif value_type == ValueType.DATETIME:
        return concept.get_datetime()
    elif value_type == ValueType.DATETIME_TZ:
        return concept.get_datetime_tz()
    elif value_type == ValueType.DURATION:
        return concept.get_duration()
    elif value_type == ValueType.STRUCT:
        return str(concept.get_struct())  # compare string representations
    else:
        raise ValueError(f"ValueType {value_type} is not covered by this step")


def try_get_by_value_type(concept, value_type: ValueType) -> Optional[Concept.VALUE]:
    if value_type == ValueType.BOOLEAN:
        return concept.try_get_boolean()
    elif value_type == ValueType.LONG:
        return concept.try_get_long()
    elif value_type == ValueType.DOUBLE:
        return concept.try_get_double()
    elif value_type == ValueType.DECIMAL:
        return concept.try_get_decimal()
    elif value_type == ValueType.STRING:
        return concept.try_get_string()
    elif value_type == ValueType.DATE:
        return concept.try_get_date()
    elif value_type == ValueType.DATETIME:
        return concept.try_get_datetime()
    elif value_type == ValueType.DATETIME_TZ:
        return concept.try_get_datetime_tz()
    elif value_type == ValueType.DURATION:
        return concept.try_get_duration()
    elif value_type == ValueType.STRUCT:
        struct = concept.try_get_struct()
        return str(struct) if struct is not None else None  # compare string representations
    else:
        raise ValueError(f"ValueType {value_type} is not covered by this step")


@step(
    "answer get row({row_index:Int}) get {kind:ConceptKind}{is_by_var_index:IsByVarIndex}({var:Var}) try get value {is_or_not:IsOrNot} none")
def step_impl(context: Context, row_index: int, kind: ConceptKind, is_by_var_index: bool, var: str,
              is_or_not: bool):
    concept = get_row_get_concept_of_kind(context, row_index, var, is_by_var_index, kind)
    concept_value = concept.try_get_value()
    check_is_none(concept_value, is_or_not)


@step(
    "answer get row({row_index:Int}) get {kind:ConceptKind}{is_by_var_index:IsByVarIndex}({var:Var}) try get value {is_or_not:IsOrNot}: {value}")
def step_impl(context: Context, row_index: int, kind: ConceptKind, is_by_var_index: bool, var: str, is_or_not: bool,
              value: str):
    concept = get_row_get_concept_of_kind(context, row_index, var, is_by_var_index, kind)
    concept_value = concept.try_get_value()
    expected_value = parse_expected_value(value, try_parse_value_type(concept.try_get_value_type()))
    assert_that(concept_value == expected_value, is_(is_or_not),
                is_or_not_reason(is_or_not, real=concept_value, expected=expected_value))


@step(
    "answer get row({row_index:Int}) get attribute{is_by_var_index:IsByVarIndex}({var:Var}) get value {is_or_not:IsOrNot}: {value}")
def step_impl(context: Context, row_index: int, is_by_var_index: bool, var: str, is_or_not: bool, value: str):
    attribute = get_row_get_attribute(context, row_index, var, is_by_var_index)
    attribute_value = attribute.get_value()
    expected_value = parse_expected_value(value, try_parse_value_type(attribute.get_type().try_get_value_type()))
    assert_that(attribute_value == expected_value, is_(is_or_not),
                is_or_not_reason(is_or_not, real=attribute_value, expected=expected_value))


@step(
    "answer get row({row_index:Int}) get attribute{is_by_var_index:IsByVarIndex}({var:Var}) get {value_type:ValueType}{may_error:MayError}")
def step_impl(context: Context, row_index: int, is_by_var_index: bool, var: str, value_type: ValueType,
              may_error: MayError):
    attribute = get_row_get_attribute(context, row_index, var, is_by_var_index)
    may_error.check(lambda: get_by_value_type(attribute, value_type))


@step(
    "answer get row({row_index:Int}) get attribute{is_by_var_index:IsByVarIndex}({var:Var}) get {value_type:ValueType} {is_or_not:IsOrNot}: {value}")
def step_impl(context: Context, row_index: int, is_by_var_index: bool, var: str, value_type: ValueType, is_or_not: bool,
              value: str):
    attribute = get_row_get_attribute(context, row_index, var, is_by_var_index)
    attribute_value_as_value_type = get_by_value_type(attribute, value_type)
    expected_value = parse_expected_value(value, value_type)
    assert_that(attribute_value_as_value_type == expected_value, is_(is_or_not),
                is_or_not_reason(is_or_not, real=attribute_value_as_value_type, expected=expected_value))


@step(
    "answer get row({row_index:Int}) get {kind:ConceptKind}{is_by_var_index:IsByVarIndex}({var:Var}) try get {value_type:ValueType} {is_or_not:IsOrNot} none")
def step_impl(context: Context, row_index: int, kind: ConceptKind, is_by_var_index: bool, var: str,
              value_type: ValueType, is_or_not: bool):
    concept = get_row_get_concept_of_kind(context, row_index, var, is_by_var_index, kind)
    concept_value = try_get_by_value_type(concept, value_type)
    check_is_none(concept_value, is_or_not)


@step(
    "answer get row({row_index:Int}) get {kind:ConceptKind}{is_by_var_index:IsByVarIndex}({var:Var}) try get {value_type:ValueType} {is_or_not:IsOrNot}: {value}")
def step_impl(context: Context, row_index: int, kind: ConceptKind, is_by_var_index: bool, var: str,
              value_type: ValueType, is_or_not: bool,
              value: str):
    concept = get_row_get_concept_of_kind(context, row_index, var, is_by_var_index, kind)
    concept_value = try_get_by_value_type(concept, value_type)
    expected_value = parse_expected_value(value, value_type)
    assert_that(concept_value == expected_value, is_(is_or_not),
                is_or_not_reason(is_or_not, real=concept_value, expected=expected_value))


@step(
    "answer get row({row_index:Int}) get value{is_by_var_index:IsByVarIndex}({var:Var}) get {is_or_not:IsOrNot}: {value}")
def step_impl(context: Context, row_index: int, is_by_var_index: bool, var: str, is_or_not: bool, value: str):
    real_value = get_row_get_value(context, row_index, var, is_by_var_index)
    expected_value = parse_expected_value(value, try_parse_value_type(real_value.get_type()))
    real_value_get = real_value.get()
    assert_that(real_value_get == expected_value, is_(is_or_not),
                is_or_not_reason(is_or_not, real=real_value_get, expected=expected_value))


@step(
    "answer get row({row_index:Int}) get value{is_by_var_index:IsByVarIndex}({var:Var}) get {value_type:ValueType} {is_or_not:IsOrNot}: {value}")
def step_impl(context: Context, row_index: int, is_by_var_index: bool, var: str, value_type: ValueType, is_or_not: bool,
              value: str):
    real_value = get_row_get_value(context, row_index, var, is_by_var_index)
    real_value_as_value_type = get_by_value_type(real_value, value_type)
    expected_value = parse_expected_value(value, value_type)
    assert_that(real_value_as_value_type == expected_value, is_(is_or_not),
                is_or_not_reason(is_or_not, real=real_value_as_value_type, expected=expected_value))


@step("answer get row({row_index:Int}) get concepts size is: {size:Int}")
def step_impl(context: Context, row_index: int, size: int):
    collect_answer_if_needed(context)
    assert_that(len(get_row_get_concepts(context, row_index)), is_(size))


@step("answer {contains_or_doesnt:ContainsOrDoesnt} document")
def step_impl(context: Context, contains_or_doesnt: bool):
    collect_answer_if_needed(context)
    expected = json.loads(context.text)
    assert_that(list_contains_json(context.collected_answer, expected), is_(contains_or_doesnt),
                "expected?({}) document: {}\nin answer: {}".format(contains_or_doesnt, expected,
                                                                   context.collected_answer))
