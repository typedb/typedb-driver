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

from datetime import datetime
from decimal import Decimal
from typing import Optional

from behave import *
from hamcrest import *
from tests.behaviour.config.parameters import ConceptKind, MayError, ValueType, parse_bool, parse_list, \
    try_parse_value_type, is_or_not_reason
from tests.behaviour.context import Context
from typedb.api.answer.query_type import QueryType
from typedb.api.concept.concept import Concept
from typedb.api.concept.thing.attribute import Attribute
from typedb.api.concept.thing.entity import Entity
from typedb.api.concept.thing.relation import Relation
from typedb.api.concept.thing.thing import Thing
from typedb.api.concept.type.attribute_type import AttributeType
from typedb.api.concept.type.entity_type import EntityType
from typedb.api.concept.type.relation_type import RelationType
from typedb.api.concept.type.role_type import RoleType
from typedb.api.concept.type.thing_type import ThingType
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


@step("answer type {is_or_not:IsOrNot}: {answer_type}")
def step_impl(context: Context, is_or_not: bool, answer_type: str):
    if answer_type == "ok":
        assert_that(context.answer.is_ok(), is_(is_or_not), "Expected is_ok()")
    elif answer_type == "concept rows":
        assert_that(context.answer.is_concept_rows(), is_(is_or_not), "Expected is_concept_rows()")
    elif answer_type == "concept trees":
        assert_that(context.answer.is_concept_trees(), is_(is_or_not), "Expected is_concept_trees()")


def unwrap_answer_ok(context: Context):
    context.unwrapped_answer = context.answer.as_ok()


def unwrap_answer_rows(context: Context):
    context.unwrapped_answer = context.answer.as_concept_rows()


def unwrap_answer_trees(context: Context):
    context.unwrapped_answer = context.answer.as_concept_trees()


@step("result is a successful ok")
def step_impl(context: Context):
    unwrap_answer_ok(context)


def unwrap_answer_if_needed(context: Context):
    if not context.answer:
        raise ValueError("Cannot unwrap test answer as answer is absent")

    if context.unwrapped_answer is None:
        if context.answer.is_ok():
            unwrap_answer_ok(context)
        if context.answer.is_concept_rows():
            unwrap_answer_rows(context)
        elif context.answer.is_concept_trees():
            unwrap_answer_trees(context)
        else:
            raise ValueError(
                "Cannot unwrap answer: it should be in Ok/ConceptRows/ConceptTrees, but appeared to be something else")


def collect_answer_if_needed(context: Context):
    unwrap_answer_if_needed(context)

    if context.collected_answer is None:
        if context.unwrapped_answer.is_concept_rows():
            context.collected_answer = list(context.unwrapped_answer)
        else:
            raise NotImplemented("Cannot collect answer")


def assert_answer_size(answer, size: int):
    assert_that(len(answer), is_(size))


@step("answer size: {size:Int}")
def step_impl(context: Context, size: int):
    collect_answer_if_needed(context)
    assert_answer_size(context.collected_answer, size)


@step("answer query type {is_or_not:IsOrNot} {query_type:QueryType}")
def step_impl(context: Context, is_or_not: bool, query_type: QueryType):
    collect_answer_if_needed(context)
    answer_query_type = context.collected_answer[0].query_type
    assert_that(answer_query_type == query_type, is_(is_or_not),
                is_or_not_reason(is_or_not, real=answer_query_type, expected=query_type))


@step("answer column names are")
def step_impl(context: Context):
    collect_answer_if_needed(context)
    column_names = context.collected_answer[0].column_names()
    assert_that(sorted(list(column_names)), equal_to(sorted(parse_list(context.table))))


def get_row_get_concept(context: Context, row_index: int, var: str, is_by_var_index: bool) -> Concept:
    collect_answer_if_needed(context)
    row = context.collected_answer[row_index]
    return row.get_index(list(context.collected_answer[0].column_names()).index(var)) if is_by_var_index \
        else row.get(var)


def get_row_get_type(context: Context, row_index: int, var: str, is_by_var_index: bool) -> Type:
    return get_row_get_concept(context, row_index, var, is_by_var_index).as_type()


def get_row_get_thing_type(context: Context, row_index: int, var: str, is_by_var_index: bool) -> ThingType:
    return get_row_get_concept(context, row_index, var, is_by_var_index).as_thing_type()


def get_row_get_thing(context: Context, row_index: int, var: str, is_by_var_index: bool) -> Thing:
    return get_row_get_concept(context, row_index, var, is_by_var_index).as_thing()


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
    elif kind == ConceptKind.THING_TYPE:
        return get_row_get_thing_type(context, row_index, var, is_by_var_index)
    elif kind == ConceptKind.THING:
        return get_row_get_thing(context, row_index, var, is_by_var_index)
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


@step(
    "answer get row({row_index:Int}) get {kind:ConceptKind}{is_by_var_index:ByIndexOfVarOrNot}({var:Var}) is type: {is_type:Bool}")
def step_impl(context: Context, row_index: int, kind: ConceptKind, is_by_var_index: bool, var: str, is_type: bool):
    assert_that(get_row_get_concept(context, row_index, var, is_by_var_index).is_type(), is_(is_type))


@step(
    "answer get row({row_index:Int}) get {kind:ConceptKind}{is_by_var_index:ByIndexOfVarOrNot}({var:Var}) is thing type: {is_thing_type:Bool}")
def step_impl(context: Context, row_index: int, kind: ConceptKind, is_by_var_index: bool, var: str,
              is_thing_type: bool):
    assert_that(get_row_get_concept(context, row_index, var, is_by_var_index).is_thing_type(), is_(is_thing_type))


@step(
    "answer get row({row_index:Int}) get {kind:ConceptKind}{is_by_var_index:ByIndexOfVarOrNot}({var:Var}) is thing: {is_thing:Bool}")
def step_impl(context: Context, row_index: int, kind: ConceptKind, is_by_var_index: bool, var: str, is_thing: bool):
    assert_that(get_row_get_concept(context, row_index, var, is_by_var_index).is_thing(), is_(is_thing))


@step(
    "answer get row({row_index:Int}) get {kind:ConceptKind}{is_by_var_index:ByIndexOfVarOrNot}({var:Var}) is value: {is_value:Bool}")
def step_impl(context: Context, row_index: int, kind: ConceptKind, is_by_var_index: bool, var: str, is_value: bool):
    assert_that(get_row_get_concept(context, row_index, var, is_by_var_index).is_value(), is_(is_value))


@step(
    "answer get row({row_index:Int}) get {kind:ConceptKind}{is_by_var_index:ByIndexOfVarOrNot}({var:Var}) is entity type: {is_entity_type:Bool}")
def step_impl(context: Context, row_index: int, kind: ConceptKind, is_by_var_index: bool, var: str,
              is_entity_type: bool):
    assert_that(get_row_get_concept(context, row_index, var, is_by_var_index).is_entity_type(), is_(is_entity_type))


@step(
    "answer get row({row_index:Int}) get {kind:ConceptKind}{is_by_var_index:ByIndexOfVarOrNot}({var:Var}) is relation type: {is_relation_type:Bool}")
def step_impl(context: Context, row_index: int, kind: ConceptKind, is_by_var_index: bool, var: str,
              is_relation_type: bool):
    assert_that(get_row_get_concept(context, row_index, var, is_by_var_index).is_relation_type(), is_(is_relation_type))


@step(
    "answer get row({row_index:Int}) get {kind:ConceptKind}{is_by_var_index:ByIndexOfVarOrNot}({var:Var}) is attribute type: {is_attribute_type:Bool}")
def step_impl(context: Context, row_index: int, kind: ConceptKind, is_by_var_index: bool, var: str,
              is_attribute_type: bool):
    assert_that(get_row_get_concept(context, row_index, var, is_by_var_index).is_attribute_type(),
                is_(is_attribute_type))


@step(
    "answer get row({row_index:Int}) get {kind:ConceptKind}{is_by_var_index:ByIndexOfVarOrNot}({var:Var}) is role type: {is_role_type:Bool}")
def step_impl(context: Context, row_index: int, kind: ConceptKind, is_by_var_index: bool, var: str, is_role_type: bool):
    assert_that(get_row_get_concept(context, row_index, var, is_by_var_index).is_role_type(), is_(is_role_type))


@step(
    "answer get row({row_index:Int}) get {kind:ConceptKind}{is_by_var_index:ByIndexOfVarOrNot}({var:Var}) is entity: {is_entity:Bool}")
def step_impl(context: Context, row_index: int, kind: ConceptKind, is_by_var_index: bool, var: str, is_entity: bool):
    assert_that(get_row_get_concept(context, row_index, var, is_by_var_index).is_entity(), is_(is_entity))


@step(
    "answer get row({row_index:Int}) get {kind:ConceptKind}{is_by_var_index:ByIndexOfVarOrNot}({var:Var}) is relation: {is_relation:Bool}")
def step_impl(context: Context, row_index: int, kind: ConceptKind, is_by_var_index: bool, var: str, is_relation: bool):
    assert_that(get_row_get_concept(context, row_index, var, is_by_var_index).is_relation(), is_(is_relation))


@step(
    "answer get row({row_index:Int}) get {kind:ConceptKind}{is_by_var_index:ByIndexOfVarOrNot}({var:Var}) is attribute: {is_attribute:Bool}")
def step_impl(context: Context, row_index: int, kind: ConceptKind, is_by_var_index: bool, var: str, is_attribute: bool):
    assert_that(get_row_get_concept(context, row_index, var, is_by_var_index).is_attribute(), is_(is_attribute))


@step(
    "answer get row({row_index:Int}) get {kind:ConceptKind}{is_by_var_index:ByIndexOfVarOrNot}({var:Var}) get type is type: {is_type:Bool}")
def step_impl(context: Context, row_index: int, kind: ConceptKind, is_by_var_index: bool, var: str, is_type: bool):
    assert_that(get_row_get_concept(context, row_index, var, is_by_var_index).get_type().is_type(), is_(is_type))


@step(
    "answer get row({row_index:Int}) get {kind:ConceptKind}{is_by_var_index:ByIndexOfVarOrNot}({var:Var}) get type is thing type: {is_thing_type:Bool}")
def step_impl(context: Context, row_index: int, kind: ConceptKind, is_by_var_index: bool, var: str,
              is_thing_type: bool):
    assert_that(get_row_get_concept(context, row_index, var, is_by_var_index).get_type().is_thing_type(),
                is_(is_thing_type))


@step(
    "answer get row({row_index:Int}) get {kind:ConceptKind}{is_by_var_index:ByIndexOfVarOrNot}({var:Var}) get type is thing: {is_thing:Bool}")
def step_impl(context: Context, row_index: int, kind: ConceptKind, is_by_var_index: bool, var: str, is_thing: bool):
    assert_that(get_row_get_concept(context, row_index, var, is_by_var_index).get_type().is_thing(), is_(is_thing))


@step(
    "answer get row({row_index:Int}) get {kind:ConceptKind}{is_by_var_index:ByIndexOfVarOrNot}({var:Var}) get type is value: {is_value:Bool}")
def step_impl(context: Context, row_index: int, kind: ConceptKind, is_by_var_index: bool, var: str, is_value: bool):
    assert_that(get_row_get_concept(context, row_index, var, is_by_var_index).get_type().is_value(), is_(is_value))


@step(
    "answer get row({row_index:Int}) get {kind:ConceptKind}{is_by_var_index:ByIndexOfVarOrNot}({var:Var}) get type is entity type: {is_entity_type:Bool}")
def step_impl(context: Context, row_index: int, kind: ConceptKind, is_by_var_index: bool, var: str,
              is_entity_type: bool):
    assert_that(get_row_get_concept(context, row_index, var, is_by_var_index).get_type().is_entity_type(),
                is_(is_entity_type))


@step(
    "answer get row({row_index:Int}) get {kind:ConceptKind}{is_by_var_index:ByIndexOfVarOrNot}({var:Var}) get type is relation type: {is_relation_type:Bool}")
def step_impl(context: Context, row_index: int, kind: ConceptKind, is_by_var_index: bool, var: str,
              is_relation_type: bool):
    assert_that(get_row_get_concept(context, row_index, var, is_by_var_index).get_type().is_relation_type(),
                is_(is_relation_type))


@step(
    "answer get row({row_index:Int}) get {kind:ConceptKind}{is_by_var_index:ByIndexOfVarOrNot}({var:Var}) get type is attribute type: {is_attribute_type:Bool}")
def step_impl(context: Context, row_index: int, kind: ConceptKind, is_by_var_index: bool, var: str,
              is_attribute_type: bool):
    assert_that(get_row_get_concept(context, row_index, var, is_by_var_index).get_type().is_attribute_type(),
                is_(is_attribute_type))


@step(
    "answer get row({row_index:Int}) get {kind:ConceptKind}{is_by_var_index:ByIndexOfVarOrNot}({var:Var}) get type is role type: {is_role_type:Bool}")
def step_impl(context: Context, row_index: int, kind: ConceptKind, is_by_var_index: bool, var: str, is_role_type: bool):
    assert_that(get_row_get_concept(context, row_index, var, is_by_var_index).get_type().is_role_type(),
                is_(is_role_type))


@step(
    "answer get row({row_index:Int}) get {kind:ConceptKind}{is_by_var_index:ByIndexOfVarOrNot}({var:Var}) get type is entity: {is_entity:Bool}")
def step_impl(context: Context, row_index: int, kind: ConceptKind, is_by_var_index: bool, var: str, is_entity: bool):
    assert_that(get_row_get_concept(context, row_index, var, is_by_var_index).get_type().is_entity(), is_(is_entity))


@step(
    "answer get row({row_index:Int}) get {kind:ConceptKind}{is_by_var_index:ByIndexOfVarOrNot}({var:Var}) get type is relation: {is_relation:Bool}")
def step_impl(context: Context, row_index: int, kind: ConceptKind, is_by_var_index: bool, var: str, is_relation: bool):
    assert_that(get_row_get_concept(context, row_index, var, is_by_var_index).get_type().is_relation(),
                is_(is_relation))


@step(
    "answer get row({row_index:Int}) get {kind:ConceptKind}{is_by_var_index:ByIndexOfVarOrNot}({var:Var}) get type is attribute: {is_attribute:Bool}")
def step_impl(context: Context, row_index: int, kind: ConceptKind, is_by_var_index: bool, var: str, is_attribute: bool):
    assert_that(get_row_get_concept(context, row_index, var, is_by_var_index).get_type().is_attribute(),
                is_(is_attribute))


@step(
    "answer get row({row_index:Int}) get {kind:ConceptKind}{is_by_var_index:ByIndexOfVarOrNot}({var:Var}) get label: {label}")
def step_impl(context: Context, row_index: int, kind: ConceptKind, is_by_var_index: bool, var: str, label: str):
    concept_label = get_row_get_concept_of_kind(context, row_index, var, is_by_var_index, kind).get_label()
    assert_that(concept_label, is_(label))


@step(
    "answer get row({row_index:Int}) get {kind:ConceptKind}{is_by_var_index:ByIndexOfVarOrNot}({var:Var}) get type get label: {label}")
def step_impl(context: Context, row_index: int, kind: ConceptKind, is_by_var_index: bool, var: str, label: str):
    concept_label = get_row_get_concept_of_kind(context, row_index, var, is_by_var_index, kind).get_type().get_label()
    assert_that(concept_label, is_(label))


@step(
    "answer get row({row_index:Int}) get {kind:ConceptKind}{is_by_var_index:ByIndexOfVarOrNot}({var:Var}) get value type: {value_type:Words}")
def step_impl(context: Context, row_index: int, kind: ConceptKind, is_by_var_index: bool, var: str, value_type: str):
    concept_value_type = get_row_get_concept_of_kind(context, row_index, var, is_by_var_index, kind).get_value_type()
    # can be "none" (not a value type), that's why we don't convert it to ValueType
    assert_that(concept_value_type, is_(value_type))


@step(
    "answer get row({row_index:Int}) get attribute{is_by_var_index:ByIndexOfVarOrNot}({var:Var}) get type get value type: {value_type:Words}")
def step_impl(context: Context, row_index: int, is_by_var_index: bool, var: str, value_type: str):
    type_value_type = get_row_get_attribute(context, row_index, var, is_by_var_index).get_type().get_value_type()
    # can be "none" (not a value type), that's why we don't convert it to ValueType
    assert_that(type_value_type, is_(value_type))


@step(
    "answer get row({row_index:Int}) get {kind:ConceptKind}{is_by_var_index:ByIndexOfVarOrNot}({var:Var}) is untyped: {is_untyped:Bool}")
def step_impl(context: Context, row_index: int, kind: ConceptKind, is_by_var_index: bool, var: str, is_untyped: bool):
    result = get_row_get_concept_of_kind(context, row_index, var, is_by_var_index, kind).is_untyped()
    assert_that(result, is_(is_untyped))


@step(
    "answer get row({row_index:Int}) get {kind:ConceptKind}{is_by_var_index:ByIndexOfVarOrNot}({var:Var}) is boolean: {is_boolean:Bool}")
def step_impl(context: Context, row_index: int, kind: ConceptKind, is_by_var_index: bool, var: str, is_boolean: bool):
    result = get_row_get_concept_of_kind(context, row_index, var, is_by_var_index, kind).is_boolean()
    assert_that(result, is_(is_boolean))


@step(
    "answer get row({row_index:Int}) get {kind:ConceptKind}{is_by_var_index:ByIndexOfVarOrNot}({var:Var}) is long: {is_long:Bool}")
def step_impl(context: Context, row_index: int, kind: ConceptKind, is_by_var_index: bool, var: str, is_long: bool):
    result = get_row_get_concept_of_kind(context, row_index, var, is_by_var_index, kind).is_long()
    assert_that(result, is_(is_long))


@step(
    "answer get row({row_index:Int}) get {kind:ConceptKind}{is_by_var_index:ByIndexOfVarOrNot}({var:Var}) is double: {is_double:Bool}")
def step_impl(context: Context, row_index: int, kind: ConceptKind, is_by_var_index: bool, var: str, is_double: bool):
    result = get_row_get_concept_of_kind(context, row_index, var, is_by_var_index, kind).is_double()
    assert_that(result, is_(is_double))


@step(
    "answer get row({row_index:Int}) get {kind:ConceptKind}{is_by_var_index:ByIndexOfVarOrNot}({var:Var}) is decimal: {is_decimal:Bool}")
def step_impl(context: Context, row_index: int, kind: ConceptKind, is_by_var_index: bool, var: str, is_decimal: bool):
    result = get_row_get_concept_of_kind(context, row_index, var, is_by_var_index, kind).is_decimal()
    assert_that(result, is_(is_decimal))


@step(
    "answer get row({row_index:Int}) get {kind:ConceptKind}{is_by_var_index:ByIndexOfVarOrNot}({var:Var}) is string: {is_string:Bool}")
def step_impl(context: Context, row_index: int, kind: ConceptKind, is_by_var_index: bool, var: str, is_string: bool):
    result = get_row_get_concept_of_kind(context, row_index, var, is_by_var_index, kind).is_string()
    assert_that(result, is_(is_string))


@step(
    "answer get row({row_index:Int}) get {kind:ConceptKind}{is_by_var_index:ByIndexOfVarOrNot}({var:Var}) is date: {is_date:Bool}")
def step_impl(context: Context, row_index: int, kind: ConceptKind, is_by_var_index: bool, var: str, is_date: bool):
    result = get_row_get_concept_of_kind(context, row_index, var, is_by_var_index, kind).is_date()
    assert_that(result, is_(is_date))


@step(
    "answer get row({row_index:Int}) get {kind:ConceptKind}{is_by_var_index:ByIndexOfVarOrNot}({var:Var}) is datetime: {is_datetime:Bool}")
def step_impl(context: Context, row_index: int, kind: ConceptKind, is_by_var_index: bool, var: str, is_datetime: bool):
    result = get_row_get_concept_of_kind(context, row_index, var, is_by_var_index, kind).is_datetime()
    assert_that(result, is_(is_datetime))


@step(
    "answer get row({row_index:Int}) get {kind:ConceptKind}{is_by_var_index:ByIndexOfVarOrNot}({var:Var}) is datetime-tz: {is_datetime_tz:Bool}")
def step_impl(context: Context, row_index: int, kind: ConceptKind, is_by_var_index: bool, var: str,
              is_datetime_tz: bool):
    result = get_row_get_concept_of_kind(context, row_index, var, is_by_var_index, kind).is_datetime_tz()
    assert_that(result, is_(is_datetime_tz))


@step(
    "answer get row({row_index:Int}) get {kind:ConceptKind}{is_by_var_index:ByIndexOfVarOrNot}({var:Var}) is duration: {is_duration:Bool}")
def step_impl(context: Context, row_index: int, kind: ConceptKind, is_by_var_index: bool, var: str, is_duration: bool):
    result = get_row_get_concept_of_kind(context, row_index, var, is_by_var_index, kind).is_duration()
    assert_that(result, is_(is_duration))


@step(
    "answer get row({row_index:Int}) get {kind:ConceptKind}{is_by_var_index:ByIndexOfVarOrNot}({var:Var}) is struct: {is_struct:Bool}")
def step_impl(context: Context, row_index: int, kind: ConceptKind, is_by_var_index: bool, var: str, is_struct: bool):
    result = get_row_get_concept_of_kind(context, row_index, var, is_by_var_index, kind).is_struct()
    assert_that(result, is_(is_struct))


@step(
    "answer get row({row_index:Int}) get {kind:ConceptKind}{is_by_var_index:ByIndexOfVarOrNot}({var:Var}) get iid exists")
def step_impl(context: Context, row_index: int, kind: ConceptKind, is_by_var_index: bool, var: str):
    concept_iid = get_row_get_concept_of_kind(context, row_index, var, is_by_var_index, kind).get_iid()
    assert_that(concept_iid, is_not(None))


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
        # TODO: Offset is not supported
        value_dt, value_tz = value.split(" ")
        return Datetime.utcfromstring(value_dt, value_tz)
    elif value_type == ValueType.DURATION:
        return Duration.fromstring(value)
    elif value_type == ValueType.STRUCT:
        return value  # compare string representations
    else:
        raise ValueError(f"ValueType {value_type} is not covered by this step")


def get_as_value_type(concept, value_type: ValueType) -> Value.VALUE:
    if value_type == ValueType.BOOLEAN:
        return concept.as_boolean()
    elif value_type == ValueType.LONG:
        return concept.as_long()
    elif value_type == ValueType.DOUBLE:
        return concept.as_double()
    elif value_type == ValueType.DECIMAL:
        return concept.as_decimal()
    elif value_type == ValueType.STRING:
        return concept.as_string()
    elif value_type == ValueType.DATE:
        return concept.as_date()
    elif value_type == ValueType.DATETIME:
        return concept.as_datetime()
    elif value_type == ValueType.DATETIME_TZ:
        return concept.as_datetime_tz()
    elif value_type == ValueType.DURATION:
        return concept.as_duration()
    elif value_type == ValueType.STRUCT:
        return str(concept.as_struct())  # compare string representations
    else:
        raise ValueError(f"ValueType {value_type} is not covered by this step")


@step(
    "answer get row({row_index:Int}) get attribute{is_by_var_index:ByIndexOfVarOrNot}({var:Var}) get value {is_or_not:IsOrNot}: {value}")
def step_impl(context: Context, row_index: int, is_by_var_index: bool, var: str, is_or_not: bool, value: str):
    attribute = get_row_get_attribute(context, row_index, var, is_by_var_index)
    attribute_value = attribute.get_value()
    expected_value = parse_expected_value(value, try_parse_value_type(attribute.get_type().get_value_type()))
    assert_that(attribute_value == expected_value, is_(is_or_not),
                is_or_not_reason(is_or_not, real=attribute_value, expected=expected_value))


@step(
    "answer get row({row_index:Int}) get attribute{is_by_var_index:ByIndexOfVarOrNot}({var:Var}) as {value_type:ValueType} {is_or_not:IsOrNot}: {value}")
def step_impl(context: Context, row_index: int, is_by_var_index: bool, var: str, value_type: ValueType, is_or_not: bool,
              value: str):
    attribute = get_row_get_attribute(context, row_index, var, is_by_var_index)
    attribute_value_as_value_type = get_as_value_type(attribute, value_type)
    expected_value = parse_expected_value(value, value_type)
    assert_that(attribute_value_as_value_type == expected_value, is_(is_or_not),
                is_or_not_reason(is_or_not, real=attribute_value_as_value_type, expected=expected_value))


@step(
    "answer get row({row_index:Int}) get value{is_by_var_index:ByIndexOfVarOrNot}({var:Var}) get {is_or_not:IsOrNot}: {value}")
def step_impl(context: Context, row_index: int, is_by_var_index: bool, var: str, is_or_not: bool, value: str):
    real_value = get_row_get_value(context, row_index, var, is_by_var_index)
    expected_value = parse_expected_value(value, try_parse_value_type(real_value.get_type()))
    real_value_get = real_value.get()
    assert_that(real_value_get == expected_value, is_(is_or_not),
                is_or_not_reason(is_or_not, real=real_value_get, expected=expected_value))


@step(
    "answer get row({row_index:Int}) get value{is_by_var_index:ByIndexOfVarOrNot}({var:Var}) as {value_type} {is_or_not:IsOrNot}: {value}")
def step_impl(context: Context, row_index: int, is_by_var_index: bool, var: str, value_type: str, is_or_not: bool,
              value: str):
    real_value = get_row_get_value(context, row_index, var, is_by_var_index)
    real_value_as_value_type = get_as_value_type(real_value, value_type)
    expected_value = parse_expected_value(value, value_type)
    assert_that(real_value_as_value_type == expected_value, is_(is_or_not),
                is_or_not_reason(is_or_not, real=real_value_as_value_type, expected=expected_value))
