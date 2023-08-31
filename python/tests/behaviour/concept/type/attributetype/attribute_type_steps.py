#
# Copyright (C) 2022 Vaticle
#
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
#

from __future__ import annotations
from behave import *
from hamcrest import *

from tests.behaviour.config.parameters import parse_list, parse_label
from tests.behaviour.context import Context
from typedb.client import *


@step("put attribute type: {type_label}, with value type: {value_type:ValueType}")
def step_impl(context: Context, type_label: str, value_type: ValueType):
    context.tx().concepts.put_attribute_type(type_label, value_type)


@step("attribute({type_label}) get value type: {value_type:ValueType}")
def step_impl(context: Context, type_label: str, value_type: ValueType):
    assert_that(context.tx().concepts.get_attribute_type(type_label).get_value_type(),
                is_(value_type))


@step("attribute({type_label}) get supertype value type: {value_type:ValueType}")
def step_impl(context: Context, type_label: str, value_type: ValueType):
    supertype = context.tx().concepts.get_attribute_type(type_label).get_supertype(context.tx()).as_attribute_type()
    assert_that(supertype.get_value_type(), is_(value_type))


@step("attribute({type_label}) as({value_type:ValueType}) get subtypes contain")
def step_impl(context: Context, type_label: str, value_type: ValueType):
    sub_labels = [parse_label(s) for s in parse_list(context.table)]
    attribute_type = context.tx().concepts.get_attribute_type(type_label)
    actuals = list(map(lambda tt: tt.get_label(), attribute_type.get_subtypes_with_value_type(context.tx(),
                                                                                              value_type)))
    for sub_label in sub_labels:
        assert_that(sub_label, is_in(actuals))


@step("attribute({type_label}) as({value_type:ValueType}) get subtypes do not contain")
def step_impl(context: Context, type_label: str, value_type: ValueType):
    sub_labels = [parse_label(s) for s in parse_list(context.table)]
    attribute_type = context.tx().concepts.get_attribute_type(type_label)
    actuals = list(map(lambda tt: tt.get_label(), attribute_type.get_subtypes_with_value_type(context.tx(),
                                                                                              value_type)))
    for sub_label in sub_labels:
        assert_that(sub_label, not_(is_in(actuals)))


@step("attribute({type_label}) as({value_type:ValueType}) set regex: {regex}")
def step_impl(context: Context, type_label: str, value_type: ValueType, regex: str):
    assert_that(value_type, is_(ValueType.STRING))
    attribute_type = context.tx().concepts.put_attribute_type(type_label, value_type)
    attribute_type.set_regex(context.tx(), regex)


@step("attribute({type_label}) as({value_type:ValueType}) unset regex")
def step_impl(context: Context, type_label: str, value_type: ValueType):
    assert_that(value_type, is_(ValueType.STRING))
    attribute_type = context.tx().concepts.get_attribute_type(type_label)
    attribute_type.unset_regex(context.tx())


@step("attribute({type_label}) as({value_type:ValueType}) get regex: {regex}")
def step_impl(context: Context, type_label: str, value_type: ValueType, regex: str):
    assert_that(value_type, is_(ValueType.STRING))
    attribute_type = context.tx().concepts.get_attribute_type(type_label)
    assert_that(attribute_type.get_regex(context.tx()), is_(regex))


@step("attribute({type_label}) as({value_type:ValueType}) does not have any regex")
def step_impl(context: Context, type_label: str, value_type: ValueType):
    assert_that(value_type, is_(ValueType.STRING))
    attribute_type = context.tx().concepts.get_attribute_type(type_label)
    assert_that(attribute_type.get_regex(context.tx()), is_(None))


def attribute_get_owners_with_annotations_contain(context: Context, type_label: str, annotations: set[Annotation]):
    owner_labels = [parse_label(s) for s in parse_list(context.table)]
    attribute_type = context.tx().concepts.get_attribute_type(type_label)
    actuals = list(
        map(lambda tt: tt.get_label(), attribute_type.get_owners(context.tx(), annotations=annotations)))
    for owner_label in owner_labels:
        assert_that(actuals, has_item(owner_label))


@step("attribute({type_label}) get owners, with annotations: {annotations:Annotations}; contain")
def step_impl(context: Context, type_label: str, annotations: set[Annotation]):
    attribute_get_owners_with_annotations_contain(context, type_label, annotations)


@step("attribute({type_label}) get owners contain")
def step_impl(context: Context, type_label: str):
    attribute_get_owners_with_annotations_contain(context, type_label, set())


def attribute_get_owners_with_annotations_do_not_contain(context: Context, type_label: str,
                                                         annotations: set[Annotation]):
    owner_labels = [parse_label(s) for s in parse_list(context.table)]
    attribute_type = context.tx().concepts.get_attribute_type(type_label)
    actuals = list(
        map(lambda tt: tt.get_label(), attribute_type.get_owners(context.tx(), annotations=annotations)))
    for owner_label in owner_labels:
        assert_that(actuals, not_(has_item(owner_label)))


@step("attribute({type_label}) get owners, with annotations: {annotations:Annotations}; do not contain")
def step_impl(context: Context, type_label: str, annotations: set[Annotation]):
    attribute_get_owners_with_annotations_do_not_contain(context, type_label, annotations)


@step("attribute({type_label}) get owners do not contain")
def step_impl(context: Context, type_label: str):
    attribute_get_owners_with_annotations_do_not_contain(context, type_label, set())


def attribute_get_owners_explicit_with_annotations_contain(context: Context, type_label: str,
                                                           annotations: set[Annotation]):
    owner_labels = [parse_label(s) for s in parse_list(context.table)]
    attribute_type = context.tx().concepts.get_attribute_type(type_label)
    actuals = list(
        map(lambda tt: tt.get_label(),
            attribute_type.get_owners(context.tx(), annotations=annotations, transitivity=Transitivity.EXPLICIT)))
    for owner_label in owner_labels:
        assert_that(actuals, has_item(owner_label))


@step("attribute({type_label}) get owners explicit, with annotations: {annotations:Annotations}; contain")
def step_impl(context: Context, type_label: str, annotations: set[Annotation]):
    attribute_get_owners_explicit_with_annotations_contain(context, type_label, annotations)


@step("attribute({type_label}) get owners explicit contain")
def step_impl(context: Context, type_label: str):
    attribute_get_owners_explicit_with_annotations_contain(context, type_label, set())


def attribute_get_owners_explicit_with_annotations_do_not_contain(context: Context, type_label: str,
                                                                  annotations: set[Annotation]):
    owner_labels = [parse_label(s) for s in parse_list(context.table)]
    attribute_type = context.tx().concepts.get_attribute_type(type_label)
    actuals = list(
        map(lambda tt: tt.get_label(),
            attribute_type.get_owners(context.tx(), annotations=annotations, transitivity=Transitivity.EXPLICIT)))
    for owner_label in owner_labels:
        assert_that(actuals, not_(has_item(owner_label)))


@step("attribute({type_label}) get owners explicit, with annotations: {annotations:Annotations}; do not contain")
def step_impl(context: Context, type_label: str, annotations: set[Annotation]):
    attribute_get_owners_explicit_with_annotations_do_not_contain(context, type_label, annotations)


@step("attribute({type_label}) get owners explicit do not contain")
def step_impl(context: Context, type_label: str):
    attribute_get_owners_explicit_with_annotations_do_not_contain(context, type_label, set())
