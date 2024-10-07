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

from behave import *
from datetime import datetime
from decimal import Decimal
from hamcrest import *
from tests.behaviour.config.parameters import ConceptKind, MayError, PredefinedValueType, parse_predefined_value_type_opt
from tests.behaviour.context import Context
from typedb.api.answer.query_type import QueryType
from typedb.api.concept.concept import Concept
from typedb.api.concept.type.attribute_type import AttributeType
from typedb.api.concept.type.entity_type import EntityType
from typedb.api.concept.type.relation_type import RelationType
from typedb.api.concept.type.role_type import RoleType
from typedb.api.concept.type.type import Type
from typedb.api.concept.type.thing_type import ThingType
from typedb.api.concept.thing.thing import Thing
from typedb.api.concept.thing.entity import Entity
from typedb.api.concept.thing.relation import Relation
from typedb.api.concept.thing.attribute import Attribute
from typedb.api.concept.value.value import Value
from typedb.common.datetime import Datetime
from typedb.common.duration import Duration
from typedb.driver import *


@step("typeql write query{may_error:MayError}")
@step("typeql read query{may_error:MayError}")
@step("typeql schema query{may_error:MayError}")
def step_impl(context: Context, may_error: MayError):
    may_error.check(lambda: context.tx().query(query=context.text).resolve())


@step("get answers of typeql write query")
@step("get answers of typeql read query")
@step("get answers of typeql schema query")
def step_impl(context: Context):
    context.answer = context.tx().query(query=context.text).resolve()


@step("answer type {is_or_not:IsOrNot} {answer_type}")
def step_impl(context: Context, is_or_not: bool, answer_type: str):
    if answer_type == "ok":
        assert_that(context.answer.is_ok(), is_(is_or_not))
    elif answer_type == "concept rows":
        assert_that(context.answer.is_concept_rows(), is_(is_or_not))
    elif answer_type == "concept trees":
        assert_that(context.answer.is_concept_trees(), is_(is_or_not))


def unwrap_answer_ok(context: Context):
    context.unwrapped_answer = context.answer.as_ok()


def unwrap_answer_rows(context: Context):
    context.unwrapped_answer = context.answer.as_concept_rows()


def unwrap_answer_trees(context: Context):
    context.unwrapped_answer = context.answer.as_concept_trees()


@step("result is a successful ok")
def step_impl(context: Context):
    unwrap_answer_ok()


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
    assert_that(context.collected_answer[0].query_type == query_type, is_(is_or_not))


@step("answer column names are")
def step_impl(context: Context):
    collect_answer_if_needed(context)
    column_names = context.collected_answer[0].column_names
    expected_names = context.table() # TODO: Parse array!
    assert_that(sorted(column_names), equal_to(sorted(expected_names)))


def get_row_get_concept(context: Context, row_index: int, var: str) -> Concept:
    collect_answer_if_needed(context)
    return context.collected_answer[row_index].get(var)


def get_row_get_type(context: Context, row_index: int, var: str) -> Type:
    return get_row_get_concept(context, row_index, var).as_type()


def get_row_get_thing_type(context: Context, row_index: int, var: str) -> ThingType:
    return get_row_get_concept(context, row_index, var).as_thing_type()


def get_row_get_thing(context: Context, row_index: int, var: str) -> Thing:
    return get_row_get_concept(context, row_index, var).as_thing()


def get_row_get_entity_type(context: Context, row_index: int, var: str) -> EntityType:
    return get_row_get_concept(context, row_index, var).as_entity_type()


def get_row_get_relation_type(context: Context, row_index: int, var: str) -> RelationType:
    return get_row_get_concept(context, row_index, var).as_relation_type()


def get_row_get_attribute_type(context: Context, row_index: int, var: str) -> AttributeType:
    return get_row_get_concept(context, row_index, var).as_attribute_type()


def get_row_get_role_type(context: Context, row_index: int, var: str) -> RoleType:
    return get_row_get_concept(context, row_index, var).as_role_type()


def get_row_get_entity(context: Context, row_index: int, var: str) -> Entity:
    return get_row_get_concept(context, row_index, var).as_entity()


def get_row_get_relation(context: Context, row_index: int, var: str) -> Relation:
    return get_row_get_concept(context, row_index, var).as_relation()


def get_row_get_attribute(context: Context, row_index: int, var: str) -> Attribute:
    return get_row_get_concept(context, row_index, var).as_attribute()


def get_row_get_value(context: Context, row_index: int, var: str) -> Value:
    return get_row_get_concept(context, row_index, var).as_value()


def get_row_get_variable_by_index_of_variable(context: Context, row_index: int, var: str) -> Concept:
    collect_answer_if_needed(context)
    row = context.collected_answer[row_index]
    header = list(row.column_names())
    return row.get_index(header.index(var))


def get_row_get_entity_type_by_index_of_variable(context: Context, row_index: int, var: str) -> EntityType:
    return get_row_get_variable_by_index_of_variable(context, row_index, var).as_entity_type()


def get_row_get_relation_type_by_index_of_variable(context: Context, row_index: int, var: str) -> RelationType:
    return get_row_get_variable_by_index_of_variable(context, row_index, var).as_relation_type()


def get_row_get_attribute_type_by_index_of_variable(context: Context, row_index: int, var: str) -> AttributeType:
    return get_row_get_variable_by_index_of_variable(context, row_index, var).as_attribute_type()


def get_row_get_role_type_by_index_of_variable(context: Context, row_index: int, var: str) -> RoleType:
    return get_row_get_variable_by_index_of_variable(context, row_index, var).as_role_type()


def get_row_get_entity_by_index_of_variable(context: Context, row_index: int, var: str) -> Entity:
    return get_row_get_variable_by_index_of_variable(context, row_index, var).as_entity()


def get_row_get_relation_by_index_of_variable(context: Context, row_index: int, var: str) -> Relation:
    return get_row_get_variable_by_index_of_variable(context, row_index, var).as_relation()


def get_row_get_attribute_by_index_of_variable(context: Context, row_index: int, var: str) -> Attribute:
    return get_row_get_variable_by_index_of_variable(context, row_index, var).as_attribute()


def get_row_get_value_by_index_of_variable(context: Context, row_index: int, var: str) -> Value:
    return get_row_get_variable_by_index_of_variable(context, row_index, var).as_value()


@step("answer get row({row_index:Int}) get variable({var:Var}) is type: {is_type:Bool}")
def step_impl(context: Context, row_index: int, var: str, is_type: bool):
    assert_that(get_row_get_concept(context, row_index, var).is_type(), is_(is_type))


@step("answer get row({row_index:Int}) get variable({var:Var}) is thing type: {is_thing_type:Bool}")
def step_impl(context: Context, row_index: int, var: str, is_thing_type: bool):
    assert_that(get_row_get_concept(context, row_index, var).is_thing_type(), is_(is_thing_type))


@step("answer get row({row_index:Int}) get variable({var:Var}) is thing: {is_thing:Bool}")
def step_impl(context: Context, row_index: int, var: str, is_thing: bool):
    assert_that(get_row_get_concept(context, row_index, var).is_thing(), is_(is_thing))


@step("answer get row({row_index:Int}) get variable({var:Var}) is value: {is_value:Bool}")
def step_impl(context: Context, row_index: int, var: str, is_value: bool):
    assert_that(get_row_get_concept(context, row_index, var).is_value(), is_(is_value))


@step("answer get row({row_index:Int}) get variable({var:Var}) is entity type: {is_entity_type:Bool}")
def step_impl(context: Context, row_index: int, var: str, is_entity_type: bool):
    assert_that(get_row_get_concept(context, row_index, var).is_entity_type(), is_(is_entity_type))


@step("answer get row({row_index:Int}) get variable({var:Var}) is relation type: {is_relation_type:Bool}")
def step_impl(context: Context, row_index: int, var: str, is_relation_type: bool):
    assert_that(get_row_get_concept(context, row_index, var).is_relation_type(), is_(is_relation_type))


@step("answer get row({row_index:Int}) get variable({var:Var}) is attribute type: {is_attribute_type:Bool}")
def step_impl(context: Context, row_index: int, var: str, is_attribute_type: bool):
    assert_that(get_row_get_concept(context, row_index, var).is_attribute_type(), is_(is_attribute_type))


@step("answer get row({row_index:Int}) get variable({var:Var}) is role type: {is_role_type:Bool}")
def step_impl(context: Context, row_index: int, var: str, is_role_type: bool):
    assert_that(get_row_get_concept(context, row_index, var).is_role_type(), is_(is_role_type))


@step("answer get row({row_index:Int}) get variable({var:Var}) is entity: {is_entity:Bool}")
def step_impl(context: Context, row_index: int, var: str, is_entity: bool):
    assert_that(get_row_get_concept(context, row_index, var).is_entity(), is_(is_entity))


@step("answer get row({row_index:Int}) get variable({var:Var}) is relation: {is_relation:Bool}")
def step_impl(context: Context, row_index: int, var: str, is_relation: bool):
    assert_that(get_row_get_concept(context, row_index, var).is_relation(), is_(is_relation))


@step("answer get row({row_index:Int}) get variable({var:Var}) is attribute: {is_attribute:Bool}")
def step_impl(context: Context, row_index: int, var: str, is_attribute: bool):
    assert_that(get_row_get_concept(context, row_index, var).is_attribute(), is_(is_attribute))


@step("answer get row({row_index:Int}) get {kind:ConceptKind}({var:Var}) get label: {label:Label}")
def step_impl(context: Context, row_index: int, kind: ConceptKind, var: str, label: str):
    if kind == ConceptKind.TYPE:
        type_label = get_row_get_type(context, row_index, var).label
    elif kind == ConceptKind.THING_TYPE:
        type_label = get_row_get_thing_type(context, row_index, var).label
    elif kind == ConceptKind.ENTITY_TYPE:
        type_label = get_row_get_entity_type(context, row_index, var).label
    elif kind == ConceptKind.RELATION_TYPE:
        type_label = get_row_get_relation_type(context, row_index, var).label
    elif kind == ConceptKind.ATTRIBUTE_TYPE:
        type_label = get_row_get_attribute_type(context, row_index, var).label
    elif kind == ConceptKind.ROLE_TYPE:
        type_label = get_row_get_role_type(context, row_index, var).label
    else:
        raise ValueError(f"ConceptKind {kind} is not covered by this step")

    assert_that(type_label, is_(label))


@step("answer get row({row_index:Int}) get {kind:ConceptKind}({var:Var}) get name: {name:Words}")
def step_impl(context: Context, row_index: int, kind: ConceptKind, var: str, name: str):
    if kind == ConceptKind.TYPE:
        type_name = get_row_get_type(context, row_index, var).label.name
    elif kind == ConceptKind.THING_TYPE:
        type_name = get_row_get_thing_type(context, row_index, var).label.name
    elif kind == ConceptKind.ENTITY_TYPE:
        type_name = get_row_get_entity_type(context, row_index, var).label.name
    elif kind == ConceptKind.RELATION_TYPE:
        type_name = get_row_get_relation_type(context, row_index, var).label.name
    elif kind == ConceptKind.ATTRIBUTE_TYPE:
        type_name = get_row_get_attribute_type(context, row_index, var).label.name
    elif kind == ConceptKind.ROLE_TYPE:
        type_name = get_row_get_role_type(context, row_index, var).label.name
    else:
        raise ValueError(f"ConceptKind {kind} is not covered by this step")

    assert_that(type_name, is_(name))


@step("answer get row({row_index:Int}) get {kind:ConceptKind}({var:Var}) get scope is none")
def step_impl(context: Context, row_index: int, kind: ConceptKind, var: str):
    if kind == ConceptKind.TYPE:
        type_scope = get_row_get_type(context, row_index, var).label.scope
    elif kind == ConceptKind.THING_TYPE:
        type_scope = get_row_get_thing_type(context, row_index, var).label.scope
    elif kind == ConceptKind.ENTITY_TYPE:
        type_scope = get_row_get_entity_type(context, row_index, var).label.scope
    elif kind == ConceptKind.RELATION_TYPE:
        type_scope = get_row_get_relation_type(context, row_index, var).label.scope
    elif kind == ConceptKind.ATTRIBUTE_TYPE:
        type_scope = get_row_get_attribute_type(context, row_index, var).label.scope
    elif kind == ConceptKind.ROLE_TYPE:
        type_scope = get_row_get_role_type(context, row_index, var).label.scope
    else:
        raise ValueError(f"ConceptKind {kind} is not covered by this step")

    assert_that(type_scope, is_(None))


@step("answer get row({row_index:Int}) get {kind:ConceptKind}({var:Var}) get scope: {scope:Words}")
def step_impl(context: Context, row_index: int, kind: ConceptKind, var: str, scope: str):
    if kind == ConceptKind.TYPE:
        type_scope = get_row_get_type(context, row_index, var).label.scope
    elif kind == ConceptKind.THING_TYPE:
        type_scope = get_row_get_thing_type(context, row_index, var).label.scope
    elif kind == ConceptKind.ENTITY_TYPE:
        type_scope = get_row_get_entity_type(context, row_index, var).label.scope
    elif kind == ConceptKind.RELATION_TYPE:
        type_scope = get_row_get_relation_type(context, row_index, var).label.scope
    elif kind == ConceptKind.ATTRIBUTE_TYPE:
        type_scope = get_row_get_attribute_type(context, row_index, var).label.scope
    elif kind == ConceptKind.ROLE_TYPE:
        type_scope = get_row_get_role_type(context, row_index, var).label.scope
    else:
        raise ValueError(f"ConceptKind {kind} is not covered by this step")

    assert_that(type_scope, is_(scope))


@step("answer get row({row_index:Int}) get {kind:ConceptKind}({var:Var}) get type get label: {label:Label}")
def step_impl(context: Context, row_index: int, kind: ConceptKind, var: str, label: str):
    if kind == ConceptKind.THING:
        type_label = get_row_get_thing(context, row_index, var).get_type().label
    elif kind == ConceptKind.ENTITY:
        type_label = get_row_get_entity(context, row_index, var).get_type().label
    elif kind == ConceptKind.RELATION:
        type_label = get_row_get_relation(context, row_index, var).get_type().label
    elif kind == ConceptKind.ATTRIBUTE:
        type_label = get_row_get_attribute(context, row_index, var).get_type().label
    else:
        raise ValueError(f"ConceptKind {kind} is not covered by this step")

    assert_that(type_label, is_(label))


@step("answer get row({row_index:Int}) get {kind:ConceptKind}({var:Var}) get type get name: {name:Words}")
def step_impl(context: Context, row_index: int, kind: ConceptKind, var: str, name: str):
    if kind == ConceptKind.THING:
        type_name = get_row_get_thing(context, row_index, var).get_type().label.name
    elif kind == ConceptKind.ENTITY:
        type_name = get_row_get_entity(context, row_index, var).get_type().label.name
    elif kind == ConceptKind.RELATION:
        type_name = get_row_get_relation(context, row_index, var).get_type().label.name
    elif kind == ConceptKind.ATTRIBUTE:
        type_name = get_row_get_attribute(context, row_index, var).get_type().label.name
    else:
        raise ValueError(f"ConceptKind {kind} is not covered by this step")

    assert_that(type_name, is_(name))


@step("answer get row({row_index:Int}) get {kind:ConceptKind}({var:Var}) get type get scope is none")
def step_impl(context: Context, row_index: int, kind: ConceptKind, var: str):
    if kind == ConceptKind.THING:
        type_scope = get_row_get_thing(context, row_index, var).get_type().label.scope
    elif kind == ConceptKind.ENTITY:
        type_scope = get_row_get_entity(context, row_index, var).get_type().label.scope
    elif kind == ConceptKind.RELATION:
        type_scope = get_row_get_relation(context, row_index, var).get_type().label.scope
    elif kind == ConceptKind.ATTRIBUTE:
        type_scope = get_row_get_attribute(context, row_index, var).get_type().label.scope
    else:
        raise ValueError(f"ConceptKind {kind} is not covered by this step")

    assert_that(type_scope, is_(None))


@step("answer get row({row_index:Int}) get {kind:ConceptKind}({var:Var}) get type get scope: {scope:Words}")
def step_impl(context: Context, row_index: int, kind: ConceptKind, var: str, scope: str):
    if kind == ConceptKind.THING:
        type_scope = get_row_get_thing(context, row_index, var).get_type().label.scope
    elif kind == ConceptKind.ENTITY:
        type_scope = get_row_get_entity(context, row_index, var).get_type().label.scope
    elif kind == ConceptKind.RELATION:
        type_scope = get_row_get_relation(context, row_index, var).get_type().label.scope
    elif kind == ConceptKind.ATTRIBUTE:
        type_scope = get_row_get_attribute(context, row_index, var).get_type().label.scope
    else:
        raise ValueError(f"ConceptKind {kind} is not covered by this step")

    assert_that(type_scope, is_(scope))


@step("answer get row({row_index:Int}) get {kind:ConceptKind}({var:Var}) get value type: {value_type:Words}")
def step_impl(context: Context, row_index: int, kind: ConceptKind, var: str, value_type: str):
    if kind == ConceptKind.ATTRIBUTE_TYPE:
        concept_value_type = get_row_get_attribute_type(context, row_index, var).get_type().get_value_type()
    elif kind == ConceptKind.VALUE:
        concept_value_type = get_row_get_value(context, row_index, var).get_type().get_value_type()
    else:
        raise ValueError(f"ConceptKind {kind} is not covered by this step")

    assert_that(concept_value_type, is_(value_type))


@step("answer get row({row_index:Int}) get attribute({var:Var}) get type get value type: {value_type:Words}")
def step_impl(context: Context, row_index: int, var: str, value_type: str):
    assert_that(get_row_get_attribute(context, row_index, var).type().get_value_type(), is_(value_type))


@step("answer get row({row_index:Int}) get {kind:ConceptKind}({var:Var}) is untyped: {is_untyped:Bool}")
def step_impl(context: Context, row_index: int, kind: ConceptKind, var: str, is_untyped: bool):
    if kind == ConceptKind.ATTRIBUTE_TYPE:
        result = get_row_get_attribute_type(context, row_index, var).is_untyped()
    elif kind == ConceptKind.ATTRIBUTE:
        result = get_row_get_attribute(context, row_index, var).is_untyped()
    elif kind == ConceptKind.VALUE:
        result = get_row_get_value(context, row_index, var).is_untyped()
    else:
        raise ValueError(f"ConceptKind {kind} is not covered by this step")

    assert_that(result, is_(is_untyped))


@step("answer get row({row_index:Int}) get {kind:ConceptKind}({var:Var}) is boolean: {is_boolean:Bool}")
def step_impl(context: Context, row_index: int, kind: ConceptKind, var: str, is_boolean: bool):
    if kind == ConceptKind.ATTRIBUTE_TYPE:
        result = get_row_get_attribute_type(context, row_index, var).is_boolean()
    elif kind == ConceptKind.ATTRIBUTE:
        result = get_row_get_attribute(context, row_index, var).is_boolean()
    elif kind == ConceptKind.VALUE:
        result = get_row_get_value(context, row_index, var).is_boolean()
    else:
        raise ValueError(f"ConceptKind {kind} is not covered by this step")

    assert_that(result, is_(is_boolean))


@step("answer get row({row_index:Int}) get {kind:ConceptKind}({var:Var}) is long: {is_long:Bool}")
def step_impl(context: Context, row_index: int, kind: ConceptKind, var: str, is_long: bool):
    if kind == ConceptKind.ATTRIBUTE_TYPE:
        result = get_row_get_attribute_type(context, row_index, var).is_long()
    elif kind == ConceptKind.ATTRIBUTE:
        result = get_row_get_attribute(context, row_index, var).is_long()
    elif kind == ConceptKind.VALUE:
        result = get_row_get_value(context, row_index, var).is_long()
    else:
        raise ValueError(f"ConceptKind {kind} is not covered by this step")

    assert_that(result, is_(is_long))


@step("answer get row({row_index:Int}) get {kind:ConceptKind}({var:Var}) is double: {is_double:Bool}")
def step_impl(context: Context, row_index: int, kind: ConceptKind, var: str, is_double: bool):
    if kind == ConceptKind.ATTRIBUTE_TYPE:
        result = get_row_get_attribute_type(context, row_index, var).is_double()
    elif kind == ConceptKind.ATTRIBUTE:
        result = get_row_get_attribute(context, row_index, var).is_double()
    elif kind == ConceptKind.VALUE:
        result = get_row_get_value(context, row_index, var).is_double()
    else:
        raise ValueError(f"ConceptKind {kind} is not covered by this step")

    assert_that(result, is_(is_double))


@step("answer get row({row_index:Int}) get {kind:ConceptKind}({var:Var}) is decimal: {is_decimal:Bool}")
def step_impl(context: Context, row_index: int, kind: ConceptKind, var: str, is_decimal: bool):
    if kind == ConceptKind.ATTRIBUTE_TYPE:
        result = get_row_get_attribute_type(context, row_index, var).is_decimal()
    elif kind == ConceptKind.ATTRIBUTE:
        result = get_row_get_attribute(context, row_index, var).is_decimal()
    elif kind == ConceptKind.VALUE:
        result = get_row_get_value(context, row_index, var).is_decimal()
    else:
        raise ValueError(f"ConceptKind {kind} is not covered by this step")

    assert_that(result, is_(is_decimal))


@step("answer get row({row_index:Int}) get {kind:ConceptKind}({var:Var}) is string: {is_string:Bool}")
def step_impl(context: Context, row_index: int, kind: ConceptKind, var: str, is_string: bool):
    if kind == ConceptKind.ATTRIBUTE_TYPE:
        result = get_row_get_attribute_type(context, row_index, var).is_string()
    elif kind == ConceptKind.ATTRIBUTE:
        result = get_row_get_attribute(context, row_index, var).is_string()
    elif kind == ConceptKind.VALUE:
        result = get_row_get_value(context, row_index, var).is_string()
    else:
        raise ValueError(f"ConceptKind {kind} is not covered by this step")

    assert_that(result, is_(is_string))


@step("answer get row({row_index:Int}) get {kind:ConceptKind}({var:Var}) is date: {is_date:Bool}")
def step_impl(context: Context, row_index: int, kind: ConceptKind, var: str, is_date: bool):
    if kind == ConceptKind.ATTRIBUTE_TYPE:
        result = get_row_get_attribute_type(context, row_index, var).is_date()
    elif kind == ConceptKind.ATTRIBUTE:
        result = get_row_get_attribute(context, row_index, var).is_date()
    elif kind == ConceptKind.VALUE:
        result = get_row_get_value(context, row_index, var).is_date()
    else:
        raise ValueError(f"ConceptKind {kind} is not covered by this step")

    assert_that(result, is_(is_date))


@step("answer get row({row_index:Int}) get {kind:ConceptKind}({var:Var}) is datetime: {is_datetime:Bool}")
def step_impl(context: Context, row_index: int, kind: ConceptKind, var: str, is_datetime: bool):
    if kind == ConceptKind.ATTRIBUTE_TYPE:
        result = get_row_get_attribute_type(context, row_index, var).is_datetime()
    elif kind == ConceptKind.ATTRIBUTE:
        result = get_row_get_attribute(context, row_index, var).is_datetime()
    elif kind == ConceptKind.VALUE:
        result = get_row_get_value(context, row_index, var).is_datetime()
    else:
        raise ValueError(f"ConceptKind {kind} is not covered by this step")

    assert_that(result, is_(is_datetime))


@step("answer get row({row_index:Int}) get {kind:ConceptKind}({var:Var}) is datetime-tz: {is_datetime_tz:Bool}")
def step_impl(context: Context, row_index: int, kind: ConceptKind, var: str, is_datetime_tz: bool):
    if kind == ConceptKind.ATTRIBUTE_TYPE:
        result = get_row_get_attribute_type(context, row_index, var).is_datetime_tz()
    elif kind == ConceptKind.ATTRIBUTE:
        result = get_row_get_attribute(context, row_index, var).is_datetime_tz()
    elif kind == ConceptKind.VALUE:
        result = get_row_get_value(context, row_index, var).is_datetime_tz()
    else:
        raise ValueError(f"ConceptKind {kind} is not covered by this step")

    assert_that(result, is_(is_datetime_tz))


@step("answer get row({row_index:Int}) get {kind:ConceptKind}({var:Var}) is duration: {is_duration:Bool}")
def step_impl(context: Context, row_index: int, kind: ConceptKind, var: str, is_duration: bool):
    if kind == ConceptKind.ATTRIBUTE_TYPE:
        result = get_row_get_attribute_type(context, row_index, var).is_duration()
    elif kind == ConceptKind.ATTRIBUTE:
        result = get_row_get_attribute(context, row_index, var).is_duration()
    elif kind == ConceptKind.VALUE:
        result = get_row_get_value(context, row_index, var).is_duration()
    else:
        raise ValueError(f"ConceptKind {kind} is not covered by this step")

    assert_that(result, is_(is_duration))


@step("answer get row({row_index:Int}) get {kind:ConceptKind}({var:Var}) is struct: {is_struct:Bool}")
def step_impl(context: Context, row_index: int, kind: ConceptKind, var: str, is_struct: bool):
    if kind == ConceptKind.ATTRIBUTE_TYPE:
        result = get_row_get_attribute_type(context, row_index, var).is_struct()
    elif kind == ConceptKind.ATTRIBUTE:
        result = get_row_get_attribute(context, row_index, var).is_struct()
    elif kind == ConceptKind.VALUE:
        result = get_row_get_value(context, row_index, var).is_struct()
    else:
        raise ValueError(f"ConceptKind {kind} is not covered by this step")

    assert_that(result, is_(is_struct))


@step("answer get row({row_index:Int}) get {kind:ConceptKind}({var:Var}) get iid exists")
def step_impl(context: Context, row_index: int, kind: ConceptKind, var: str):
    if kind == ConceptKind.ENTITY:
        thing_iid = get_row_get_entity(context, row_index, var).iid
    elif kind == ConceptKind.RELATION:
        thing_iid = get_row_get_relation(context, row_index, var).iid
    else:
        raise ValueError(f"ConceptKind {kind} is not covered by this step")

    assert_that(thing_iid, is_not(None))


def parse_expected_value(value_type: str, value: str):
    expected_value_type = parse_predefined_value_type_opt(value_type)
    if expected_value_type is not None:
        if expected_value_type == PredefinedValueType.BOOLEAN:
            return bool(value)
        elif expected_value_type == PredefinedValueType.LONG:
            return int(value)
        elif expected_value_type == PredefinedValueType.DOUBLE:
            return float(value)
        elif expected_value_type == PredefinedValueType.DECIMAL:
            return Decimal(value)
        elif expected_value_type == PredefinedValueType.STRING:
            return value[1:-1]
        elif expected_value_type == PredefinedValueType.DATE:
            date_format = "%Y-%m-%d"
            return datetime.strptime(value, date_format).date()
        elif expected_value_type == PredefinedValueType.DATETIME:
            return Datetime.from_string(value)
        elif expected_value_type == PredefinedValueType.DATETIME_TZ:
            # TODO: Offset is not supported
            value_dt, value_tz = value.split(" ")
            return Datetime.from_string(value_dt, value_tz)
        elif expected_value_type == PredefinedValueType.DURATION:
            return Duration.from_string(value)
        else:
            raise ValueError(f"PredefinedValueType {expected_value_type} is not covered by this step")
    else: # consider it as a declared struct type, return it as a string representation
        return value


@step("answer get row({row_index:Int}) get attribute({var:Var}) get {value_type} value: {value}")
def step_impl(context: Context, row_index: int, var: str, value_type: str, value: str):
    attribute_value = get_row_get_attribute(context, row_index, var).get_value()
    expected_value = parse_expected_value(value_type, value)
    if isinstance(attribute_value, map): # compare struct's string representations
        assert_that(f"{attribute_value}", is_(expected_value))
    else:
        assert_that(attribute_value, is_(expected_value))


# @step("answer get row({row_index:Int}) get attribute({var:Var}) as {value_type}: {value}")
# def step_impl(context: Context, row_index: int, var: str, value_type: str, value: str):
#     attribute_value = get_row_get_attribute(context, row_index, var).get_value()
#     expected_value = parse_expected_value(value_type, value)
#     if isinstance(attribute_value, map): # compare struct's string representations
#         assert_that(f"{attribute_value}", is_(expected_value))
#     else:
#         assert_that(attribute_value, is_(expected_value))
#
#
# @step("answer get row({row_index:Int}) get value({var:Var}) get: {value}")
# def step_impl(context: Context, row_index: int, var: str, value_type: str, value: str):
#     value = get_row_get_value(context, row_index, var).get()
#     expected_value = parse_expected_value(value_type, value)
#     if isinstance(attribute_value, map): # compare struct's string representations
#         assert_that(f"{attribute_value}", is_(expected_value))
#     else:
#         assert_that(attribute_value, is_(expected_value))
#
#
# @step("answer get row({row_index:Int}) get value({var:Var}) as {value_type}: {value}")
# def step_impl(context: Context, row_index: int, var: str, value_type: str, value: str):
#     attribute_value = get_row_get_value(context, row_index, var).get_value()
#     expected_value = parse_expected_value(value_type, value)
#     if isinstance(attribute_value, map): # compare struct's string representations
#         assert_that(f"{attribute_value}", is_(expected_value))
#     else:
#         assert_that(attribute_value, is_(expected_value))


# @step("get answers of typeql insert")
# def step_impl(context: Context):
#     context.clear_answers()
#     context.answers = [answer for answer in context.tx().query.insert(query=context.text)]
#
#
# @step("get answers of typeql get")
# def step_impl(context: Context):
#     context.clear_answers()
#     context.answers = [answer for answer in context.tx().query.get(query=context.text)]
#
#
# @step("typeql get; throws exception")
# def step_impl(context: Context):
#     assert_that(calling(next).with_args(context.tx().query.get(query=context.text)), raises(TypeDBDriverException))
#
#
# @step("typeql get; throws exception containing \"{pattern}\"")
# def step_impl(context: Context, pattern: str):
#     assert_that(calling(next).with_args(context.tx().query.get(query=context.text)),
#                 raises(TypeDBDriverException, re.escape(pattern)))
#
#
# @step("get answer of typeql get aggregate")
# def step_impl(context: Context):
#     context.clear_answers()
#     context.value_answer = context.tx().query.get_aggregate(query=context.text).resolve()
#
#
# @step("typeql get aggregate; throws exception")
# def step_impl(context: Context):
#     assert_that(calling(lambda query: context.tx().query.get_aggregate(query).resolve()).with_args(query=context.text), raises(TypeDBDriverException))
#
#
# @step("get answers of typeql get group")
# def step_impl(context: Context):
#     context.clear_answers()
#     context.answer_groups = [group for group in context.tx().query.get_group(query=context.text)]
#
#
# @step("typeql get group; throws exception")
# def step_impl(context: Context):
#     assert_that(calling(next).with_args(context.tx().query.get_group(query=context.text)),
#                 raises(TypeDBDriverException))
#
#
# @step("get answers of typeql get group aggregate")
# def step_impl(context: Context):
#     context.clear_answers()
#     context.value_answer_groups = [group for group in context.tx().query.get_group_aggregate(query=context.text)]
#
#
# @step("answer size is: {expected_size:Int}")
# def step_impl(context: Context, expected_size: int):
#     assert_that(context.answers, has_length(expected_size),
#                 "Expected [%d] answers, but got [%d]" % (expected_size, len(context.answers)))
#
# @step("get answers of typeql fetch")
# def get_answers_typeql_fetch(context: Context):
#     context.fetch_answers = list(context.tx().query.fetch(query=context.text))
#
# @step("typeql fetch; throws exception")
# def typeql_fetch_throws(context: Context):
#     assert_that(calling(next).with_args(context.tx().query.fetch(query=context.text)),
#                 raises(TypeDBDriverException))
#
# @step("fetch answers are")
# def fetch_answers_are(context: Context):
#     expected = json.loads(context.text)
#     actual = context.fetch_answers
#     assert_that(json_matches(expected, actual), is_(True),
#                 "expected: {}\nactual: {}".format(expected, actual))
#
# @step("rules contain: {rule_label}")
# def step_impl(context: Context, rule_label: str):
#     return rule_label in [rule.label for rule in context.tx().logic.get_rules()]
#
#
# @step("rules do not contain: {rule_label}")
# def step_impl(context: Context, rule_label: str):
#     return not (rule_label in [rule.label for rule in context.tx().logic.get_rules()])
#
#
# class ConceptMatchResult:
#
#     def __init__(self, expected: str, actual: Optional[str], error: str = None):
#         self.matches = actual == expected
#         self.actual = actual
#         self.expected = expected
#         self.error = error
#
#     @staticmethod
#     def of(expected: Any, actual: Any):
#         return ConceptMatchResult(expected=str(expected), actual=str(actual))
#
#     @staticmethod
#     def of_error(expected: Any, error: str):
#         return ConceptMatchResult(expected=str(expected), actual=None, error=error)
#
#     def __str__(self):
#         if self.error:
#             return "[error: %s]" % self.error
#         else:
#             return "[expected: %s, actual: %s, matches: %s]" % (self.expected, self.actual, self.matches)
#
#
# class ConceptMatcher(ABC):
#
#     @abstractmethod
#     def match(self, context: Context, concept: Concept) -> ConceptMatchResult:
#         pass
#
#
# class TypeLabelMatcher(ConceptMatcher):
#
#     def __init__(self, label: str):
#         self.label = parse_label(label)
#
#     def match(self, context: Context, concept: Concept):
#         if concept.is_type():
#             return ConceptMatchResult.of(self.label, concept.as_type().get_label())
#         else:
#             return ConceptMatchResult.of_error(self.label, "%s was matched by Label, but it is not a Type." % concept)
#
#
# class AttributeMatcher(ConceptMatcher, ABC):
#
#     def __init__(self, type_and_value: str):
#         self.type_and_value = type_and_value
#         s = type_and_value.split(":", 1)
#         assert_that(s, has_length(2),
#                     "[%s] is not a valid attribute identifier. It should have format \"type_label:value\"." % type_and_value)
#         self.type_label, self.value_string = s
#
#     def check(self, attribute: Attribute):
#         if attribute.is_boolean():
#             return ConceptMatchResult.of(parse_bool(self.value_string), attribute.as_boolean())
#         elif attribute.is_long():
#             return ConceptMatchResult.of(parse_int(self.value_string), attribute.as_long())
#         elif attribute.is_double():
#             return ConceptMatchResult.of(parse_float(self.value_string), attribute.as_double())
#         elif attribute.is_string():
#             return ConceptMatchResult.of(self.value_string, attribute.as_string())
#         elif attribute.is_datetime():
#             return ConceptMatchResult.of(parse_datetime(self.value_string), attribute.as_datetime())
#         else:
#             raise ValueError("Unrecognised value type " + str(type(attribute)))
#
#
# class AttributeValueMatcher(AttributeMatcher):
#
#     def match(self, context: Context, concept: Concept):
#         if not concept.is_attribute():
#             return ConceptMatchResult.of_error(self.type_and_value,
#                                                "%s was matched by Attribute Value, but it is not an Attribute." % concept)
#
#         attribute = concept.as_attribute()
#
#         if self.type_label != attribute.get_type().get_label().name:
#             return ConceptMatchResult.of_error(self.type_and_value,
#                                                "%s was matched by Attribute Value expecting type label [%s], but its actual type is %s." % (
#                                                    attribute, self.type_label, attribute.get_type()))
#
#         return self.check(attribute)
#
#
# class ThingKeyMatcher(AttributeMatcher):
#
#     def match(self, context: Context, concept: Concept):
#         if not concept.is_thing():
#             return ConceptMatchResult.of_error(self.type_and_value,
#                                                "%s was matched by Key, but it is not a Thing." % concept)
#
#         keys = [key for key in concept.as_thing().get_has(context.tx(), annotations={Annotation.key()})]
#
#         for key in keys:
#             if key.get_type().get_label().name == self.type_label:
#                 return self.check(key)
#
#         return ConceptMatchResult.of_error(self.type_and_value,
#                                            "%s was matched by Key expecting key type [%s], but it doesn't own any key of that type." % (
#                                                concept, self.type_label))
#
#
# class ValueMatcher(ConceptMatcher):
#
#     def __init__(self, value_type_and_value: str):
#         self.value_type_and_value = value_type_and_value
#         s = value_type_and_value.split(":", 1)
#         assert_that(s, has_length(2),
#                     "[%s] is not a valid identifier. It should have format \"value_type:value\"." % value_type_and_value)
#         self.value_type_name, self.value_string = s
#
#     def match(self, context: Context, concept: Concept):
#         if not concept.is_value():
#             return ConceptMatchResult.of_error(self.value_type_and_value,
#                                                "%s was matched by Value, but it is not Value." % concept)
#
#         value = concept.as_value()
#
#         value_type = parse_value_type(self.value_type_name)
#         if value_type != value.get_value_type():
#             return ConceptMatchResult.of_error(self.value_type_and_value,
#                                                "%s was matched by Value expecting value type [%s], but its actual value type is %s." % (
#                                                    value, value_type, value.get_value_type()))
#
#         return self.check(value)
#
#     def check(self, value: Value):
#         if value.is_boolean():
#             return ConceptMatchResult.of(parse_bool(self.value_string), value.get())
#         elif value.is_long():
#             return ConceptMatchResult.of(parse_int(self.value_string), value.get())
#         elif value.is_double():
#             return ConceptMatchResult.of(parse_float(self.value_string), value.get())
#         elif value.is_string():
#             return ConceptMatchResult.of(self.value_string, value.get())
#         elif value.is_datetime():
#             return ConceptMatchResult.of(parse_datetime(self.value_string), value.get())
#         else:
#             raise ValueError("Unrecognised value type " + str(type(value)))
#
#
# def parse_concept_identifier(value: str):
#     identifier_type, identifier_body = value.split(":", 1)
#     if identifier_type == "label":
#         return TypeLabelMatcher(label=identifier_body)
#     elif identifier_type == "key":
#         return ThingKeyMatcher(type_and_value=identifier_body)
#     elif identifier_type == "attr":
#         return AttributeValueMatcher(type_and_value=identifier_body)
#     elif identifier_type == "value":
#         return ValueMatcher(value_type_and_value=identifier_body)
#     else:
#         raise ValueError("Failed to parse concept identifier: " + value)
#
#
# class AnswerMatchResult:
#
#     def __init__(self, concept_match_results: list[ConceptMatchResult]):
#         self.concept_match_results = concept_match_results
#
#     def matches(self):
#         for result in self.concept_match_results:
#             if not result.matches:
#                 return False
#         return True
#
#     def __str__(self):
#         return "[matches: %s, concept_match_results: %s]" % (
#             self.matches(), [str(x) for x in self.concept_match_results])
#
#
# @step("order of answer concepts is")
# def step_impl(context: Context):
#     answer_identifiers = parse_table(context.table)
#     assert_that(context.answers, has_length(len(answer_identifiers)),
#                 "The number of answers [%d] should match the number of answer identifiers [%d]." % (
#                     len(context.answers), len(answer_identifiers)))
#     for i in range(len(context.answers)):
#         answer = context.answers[i]
#         answer_identifier = answer_identifiers[i]
#         result = match_answer_concepts(context, answer_identifier, answer)
#         assert_that(result.matches(), is_(True),
#                     "The answer at index [%d] does not match the identifier [%s].\nThe match results were: %s" % (
#                         i, answer_identifier, result))
#
#
# def assert_value(value: Value, expected_answer: Union[int, float], reason: Optional[str] = None):
#     if value.is_long():
#         assert_that(value.as_long(), is_(expected_answer), reason)
#     elif value.is_double():
#         assert_that(value.as_double(), is_(close_to(expected_answer, delta=0.001)), reason)
#     else:
#         assert False
#
#
# @step("aggregate value is: {expected_answer:Float}")
# def step_impl(context: Context, expected_answer: float):
#     assert_value(context.value_answer, expected_answer)
#
#
# @step("aggregate answer is empty")
# def step_impl(context: Context):
#     assert_that(context.value_answer is None)
#
#
# class AnswerIdentifierGroup:
#     GROUP_COLUMN_NAME = "owner"
#
#     def __init__(self, raw_answer_identifiers: list[list[tuple[str, str]]]):
#         self.owner_identifier = next(
#             entry[1] for entry in raw_answer_identifiers[0] if entry[0] == self.GROUP_COLUMN_NAME)
#         self.answer_identifiers = [[(var, concept_identifier) for (var, concept_identifier) in raw_answer_identifier if
#                                     var != self.GROUP_COLUMN_NAME]
#                                    for raw_answer_identifier in raw_answer_identifiers]
#
#
# @step("answer groups are")
# def step_impl(context: Context):
#     raw_answer_identifiers = parse_table(context.table)
#     grouped_answer_identifiers = defaultdict(list)
#     for raw_answer_identifier in raw_answer_identifiers:
#         owner = next(entry[1] for entry in raw_answer_identifier if entry[0] == AnswerIdentifierGroup.GROUP_COLUMN_NAME)
#         grouped_answer_identifiers[owner].append(raw_answer_identifier)
#     answer_identifier_groups = [AnswerIdentifierGroup(raw_identifiers) for raw_identifiers in
#                                 grouped_answer_identifiers.values()]
#
#     assert_that(context.answer_groups, has_length(len(answer_identifier_groups)),
#                 "Expected [%d] answer groups, but found [%d]." % (
#                     len(answer_identifier_groups), len(context.answer_groups)))
#
#     for answer_identifier_group in answer_identifier_groups:
#         identifier = parse_concept_identifier(answer_identifier_group.owner_identifier)
#         answer_group = next(
#             (group for group in context.answer_groups if identifier.match(context, group.owner()).matches), None)
#         assert_that(answer_group is not None,
#                     reason="The group identifier [%s] does not match any of the answer group owners." % answer_identifier_group.owner_identifier)
#
#         result_set = [(ai, [], []) for ai in answer_identifier_group.answer_identifiers]
#         for answer_identifier, matched_answers, match_attempts in result_set:
#             for answer in answer_group.concept_rows():
#                 result = match_answer_concepts(context, answer_identifier, answer)
#                 match_attempts.append(result)
#                 if result.matches():
#                     matched_answers.append(answer)
#             assert_that(matched_answers, has_length(1),
#                         "Each answer identifier should match precisely 1 answer, but [%d] answers matched the identifier [%s].\nThe match results were: %s" % (
#                             len(matched_answers), answer_identifier, [str(x) for x in match_attempts]))
#
#         for answer in answer_group.concept_rows():
#             matches = 0
#             for answer_identifier, matched_answers, match_attempts in result_set:
#                 if answer in matched_answers:
#                     matches += 1
#                     break
#             if matches != 1:
#                 match_attempts = []
#                 for answer_identifier in answer_identifier_group.answer_identifiers:
#                     match_attempts.append(match_answer_concepts(context, answer_identifier, answer))
#                 assert_that(matches, is_(1),
#                             "Each answer should match precisely 1 answer identifier, but [%d] answer identifiers matched the answer [%s].\nThe match results were: %s" % (
#                                 matches, answer, [str(x) for x in match_attempts]))
#
#
# @step("group aggregate values are")
# def step_impl(context: Context):
#     raw_answer_identifiers = parse_table(context.table)
#     expectations = {}
#     for raw_answer_identifier in raw_answer_identifiers:
#         owner = next(entry[1] for entry in raw_answer_identifier if entry[0] == AnswerIdentifierGroup.GROUP_COLUMN_NAME)
#         expected_answer = parse_float(next(entry[1] for entry in raw_answer_identifier if entry[0] == "value"))
#         expectations[owner] = expected_answer
#
#     assert_that(context.value_answer_groups, has_length(len(expectations)),
#                 reason="Expected [%d] answer groups, but found [%d]." % (
#                     len(expectations), len(context.value_answer_groups)))
#
#     for (owner_identifier, expected_answer) in expectations.items():
#         identifier = parse_concept_identifier(owner_identifier)
#         value_group = next(
#             (group for group in context.value_answer_groups if identifier.match(context, group.owner()).matches),
#             None)
#         assert_that(value_group is not None,
#                     reason="The group identifier [%s] does not match any of the answer group owners." % owner_identifier)
#
#         actual_answer = value_group.value().get()
#         assert_value(value_group.value(), expected_answer,
#                              reason="Expected answer [%f] for group [%s], but got [%f]" % (
#                                  expected_answer, owner_identifier, actual_answer))
#
#
# @step("group aggregate answer value is empty")
# def step_impl(context: Context):
#     assert_that(len(context.value_answer_groups) == 1, reason="This step only work with 1 group aggregate answer")
#     assert_that(context.value_answer_groups[0].value() is None)
