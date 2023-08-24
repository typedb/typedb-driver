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

from behave import *
from hamcrest import *
from typedb.client import *

from tests.behaviour.config.parameters import parse_bool, RootLabel
from tests.behaviour.context import Context


@step("entity {var:Var} is null: {is_null}")
@step("attribute {var:Var} is null: {is_null}")
@step("relation {var:Var} is null: {is_null}")
def step_impl(context: Context, var: str, is_null):
    is_null = parse_bool(is_null)
    assert_that(context.get(var) is None, is_(is_null))


@step("entity {var:Var} is deleted: {is_deleted}")
@step("attribute {var:Var} is deleted: {is_deleted}")
@step("relation {var:Var} is deleted: {is_deleted}")
def step_impl(context: Context, var: str, is_deleted):
    is_deleted = parse_bool(is_deleted)
    assert_that(context.get(var).is_deleted(context.tx()), is_(is_deleted))


@step("{root_label:RootLabel} {var:Var} has type: {type_label}")
def step_impl(context: Context, root_label: RootLabel, var: str, type_label: str):
    thing_type = context.get_thing_type(root_label, type_label)
    assert_that(context.get(var).get_type(), is_(thing_type))


@step("delete entity: {var:Var}")
@step("delete attribute: {var:Var}")
@step("delete relation: {var:Var}")
def step_impl(context: Context, var: str):
    context.get(var).delete(context.tx())


@step("entity {var1:Var} set has: {var2:Var}; throws exception")
@step("attribute {var1:Var} set has: {var2:Var}; throws exception")
@step("relation {var1:Var} set has: {var2:Var}; throws exception")
def step_impl(context: Context, var1: str, var2: str):
    try:
        context.get(var1).set_has(context.tx(), context.get(var2).as_attribute())
        assert False
    except TypeDBClientException:
        pass


@step("entity {var1:Var} set has: {var2:Var}")
@step("attribute {var1:Var} set has: {var2:Var}")
@step("relation {var1:Var} set has: {var2:Var}")
def step_impl(context: Context, var1: str, var2: str):
    context.get(var1).set_has(context.tx(), context.get(var2).as_attribute())


@step("entity {var1:Var} unset has: {var2:Var}")
@step("attribute {var1:Var} unset has: {var2:Var}")
@step("relation {var1:Var} unset has: {var2:Var}")
def step_impl(context: Context, var1: str, var2: str):
    context.get(var1).unset_has(context.tx(), context.get(var2).as_attribute())


@step("entity {var1:Var} get keys contain: {var2:Var}")
@step("attribute {var1:Var} get keys contain: {var2:Var}")
@step("relation {var1:Var} get keys contain: {var2:Var}")
def step_impl(context: Context, var1: str, var2: str):
    assert_that(context.get(var1).get_has(context.tx(), annotations={Annotation.key()}), has_item(context.get(var2).as_attribute()))


@step("entity {var1:Var} get keys do not contain: {var2:Var}")
@step("attribute {var1:Var} get keys do not contain: {var2:Var}")
@step("relation {var1:Var} get keys do not contain: {var2:Var}")
def step_impl(context: Context, var1: str, var2: str):
    assert_that(context.get(var1).get_has(context.tx(), annotations={Annotation.key()}), not_(has_item(context.get(var2).as_attribute())))


@step("entity {var1:Var} get attributes contain: {var2:Var}")
@step("attribute {var1:Var} get attributes contain: {var2:Var}")
@step("relation {var1:Var} get attributes contain: {var2:Var}")
def step_impl(context: Context, var1: str, var2: str):
    assert_that(context.get(var1).get_has(context.tx()), has_item(context.get(var2).as_attribute()))


@step("entity {var1:Var} get attributes({type_label}) contain: {var2:Var}")
@step("attribute {var1:Var} get attributes({type_label}) contain: {var2:Var}")
@step("relation {var1:Var} get attributes({type_label}) contain: {var2:Var}")
@step("entity {var1:Var} get attributes({type_label}) as(boolean) contain: {var2:Var}")
@step("attribute {var1:Var} get attributes({type_label}) as(boolean) contain: {var2:Var}")
@step("relation {var1:Var} get attributes({type_label}) as(boolean) contain: {var2:Var}")
@step("entity {var1:Var} get attributes({type_label}) as(long) contain: {var2:Var}")
@step("attribute {var1:Var} get attributes({type_label}) as(long) contain: {var2:Var}")
@step("relation {var1:Var} get attributes({type_label}) as(long) contain: {var2:Var}")
@step("entity {var1:Var} get attributes({type_label}) as(double) contain: {var2:Var}")
@step("attribute {var1:Var} get attributes({type_label}) as(double) contain: {var2:Var}")
@step("relation {var1:Var} get attributes({type_label}) as(double) contain: {var2:Var}")
@step("entity {var1:Var} get attributes({type_label}) as(string) contain: {var2:Var}")
@step("attribute {var1:Var} get attributes({type_label}) as(string) contain: {var2:Var}")
@step("relation {var1:Var} get attributes({type_label}) as(string) contain: {var2:Var}")
@step("entity {var1:Var} get attributes({type_label}) as(datetime) contain: {var2:Var}")
@step("attribute {var1:Var} get attributes({type_label}) as(datetime) contain: {var2:Var}")
@step("relation {var1:Var} get attributes({type_label}) as(datetime) contain: {var2:Var}")
def step_impl(context: Context, var1: str, type_label: str, var2: str):
    assert_that(context.get(var1).get_has(context.tx(), attribute_type=context.tx().concepts.get_attribute_type(type_label)), has_item(context.get(var2).as_attribute()))


@step("entity {var1:Var} get attributes do not contain: {var2:Var}")
@step("attribute {var1:Var} get attributes do not contain: {var2:Var}")
@step("relation {var1:Var} get attributes do not contain: {var2:Var}")
def step_impl(context: Context, var1: str, var2: str):
    assert_that(context.get(var1).get_has(context.tx()), not_(has_item(context.get(var2).as_attribute())))


@step("entity {var1:Var} get attributes({type_label}) do not contain: {var2:Var}")
@step("attribute {var1:Var} get attributes({type_label}) do not contain: {var2:Var}")
@step("relation {var1:Var} get attributes({type_label}) do not contain: {var2:Var}")
@step("entity {var1:Var} get attributes({type_label}) as(boolean) do not contain: {var2:Var}")
@step("attribute {var1:Var} get attributes({type_label}) as(boolean) do not contain: {var2:Var}")
@step("relation {var1:Var} get attributes({type_label}) as(boolean) do not contain: {var2:Var}")
@step("entity {var1:Var} get attributes({type_label}) as(long) do not contain: {var2:Var}")
@step("attribute {var1:Var} get attributes({type_label}) as(long) do not contain: {var2:Var}")
@step("relation {var1:Var} get attributes({type_label}) as(long) do not contain: {var2:Var}")
@step("entity {var1:Var} get attributes({type_label}) as(double) do not contain: {var2:Var}")
@step("attribute {var1:Var} get attributes({type_label}) as(double) do not contain: {var2:Var}")
@step("relation {var1:Var} get attributes({type_label}) as(double) do not contain: {var2:Var}")
@step("entity {var1:Var} get attributes({type_label}) as(string) do not contain: {var2:Var}")
@step("attribute {var1:Var} get attributes({type_label}) as(string) do not contain: {var2:Var}")
@step("relation {var1:Var} get attributes({type_label}) as(string) do not contain: {var2:Var}")
@step("entity {var1:Var} get attributes({type_label}) as(datetime) do not contain: {var2:Var}")
@step("attribute {var1:Var} get attributes({type_label}) as(datetime) do not contain: {var2:Var}")
@step("relation {var1:Var} get attributes({type_label}) as(datetime) do not contain: {var2:Var}")
def step_impl(context: Context, var1: str, type_label: str, var2: str):
    assert_that(context.get(var1).get_has(context.tx(), attribute_type=context.tx().concepts.get_attribute_type(type_label)),
                not_(has_item(context.get(var2).as_attribute())))


@step("entity {var1:Var} get relations({label:ScopedLabel}) contain: {var2:Var}")
@step("attribute {var1:Var} get relations({label:ScopedLabel}) contain: {var2:Var}")
@step("relation {var1:Var} get relations({label:ScopedLabel}) contain: {var2:Var}")
def step_impl(context: Context, var1: str, label: Label, var2: str):
    assert_that(context.get(var1).get_relations(
        context.tx(),
        context.tx().concepts.get_relation_type(label.scope).get_relates(context.tx(), label.name)),
        has_item(context.get(var2)))


@step("entity {var1:Var} get relations contain: {var2:Var}")
@step("attribute {var1:Var} get relations contain: {var2:Var}")
@step("relation {var1:Var} get relations contain: {var2:Var}")
def step_impl(context: Context, var1: str, var2: str):
    assert_that(context.get(var1).get_relations(context.tx()), has_item(context.get(var2)))


@step("entity {var1:Var} get relations({label:ScopedLabel}) do not contain: {var2:Var}")
@step("attribute {var1:Var} get relations({label:ScopedLabel}) do not contain: {var2:Var}")
@step("relation {var1:Var} get relations({label:ScopedLabel}) do not contain: {var2:Var}")
def step_impl(context: Context, var1: str, label: Label, var2: str):
    assert_that(context.get(var1).get_relations(
        context.tx(),
        context.tx().concepts.get_relation_type(label.scope).get_relates(context.tx(), label.name)),
        not_(has_item(context.get(var2))))


@step("entity {var1:Var} get relations do not contain: {var2:Var}")
@step("attribute {var1:Var} get relations do not contain: {var2:Var}")
@step("relation {var1:Var} get relations do not contain: {var2:Var}")
def step_impl(context: Context, var1: str, var2: str):
    assert_that(context.get(var1).get_relations(context.tx()), not_(has_item(context.get(var2))))
