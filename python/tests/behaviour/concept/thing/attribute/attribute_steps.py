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

from tests.behaviour.context import Context


@step("attribute({type_label}) get instances contain: {var:Var}")
def step_impl(context: Context, type_label: str, var: str):
    assert_that(context.get(var),
                is_in(context.tx().concepts.get_attribute_type(type_label).get_instances(context.tx())))


@step("attribute({type_label}) get instances is empty")
def step_impl(context: Context, type_label: str):
    assert_that(calling(next).with_args(context.tx().concepts.get_attribute_type(type_label)
                                        .get_instances(context.tx())), raises(StopIteration))


@step("attribute {var1:Var} get owners contain: {var2:Var}")
def step_impl(context: Context, var1: str, var2: str):
    assert_that(context.get(var2), is_in(context.get(var1).as_attribute().get_owners(context.tx())))


@step("attribute {var1:Var} get owners do not contain: {var2:Var}")
def step_impl(context: Context, var1: str, var2: str):
    assert_that(context.get(var2), not_(is_in(context.get(var1).as_attribute().get_owners(context.tx()))))


@step("attribute {var:Var} has value type: {value_type:ValueType}")
def step_impl(context: Context, var: str, value_type: ValueType):
    assert_that(context.get(var).as_attribute().get_type().get_value_type(), is_(value_type))


@step("attribute({type_label}) as(boolean) put: {value:Bool}; throws exception")
def step_impl(context: Context, type_label: str, value: bool):
    assert_that(calling(context.tx().concepts.get_attribute_type(type_label).put).with_args(context.tx(), value),
                raises(TypeDBClientException))


@step("{var:Var} = attribute({type_label}) as(boolean) put: {value:Bool}")
def step_impl(context: Context, var: str, type_label: str, value: bool):
    context.put(var, context.tx().concepts.get_attribute_type(type_label).put(context.tx(), value))


@step("attribute({type_label}) as(long) put: {value:Int}; throws exception")
def step_impl(context: Context, type_label: str, value: int):
    assert_that(calling(context.tx().concepts.get_attribute_type(type_label).put).with_args(context.tx(), value),
                raises(TypeDBClientException))


@step("{var:Var} = attribute({type_label}) as(long) put: {value:Int}")
def step_impl(context: Context, var: str, type_label: str, value: int):
    context.put(var, context.tx().concepts.get_attribute_type(type_label).put(context.tx(), value))


@step("attribute({type_label}) as(double) put: {value:Float}; throws exception")
def step_impl(context: Context, type_label: str, value: float):
    assert_that(calling(context.tx().concepts.get_attribute_type(type_label).put).with_args(context.tx(), value),
                raises(TypeDBClientException))


@step("{var:Var} = attribute({type_label}) as(double) put: {value:Float}")
def step_impl(context: Context, var: str, type_label: str, value: float):
    context.put(var, context.tx().concepts.get_attribute_type(type_label).put(context.tx(), value))


@step("attribute({type_label}) as(string) put: {value}; throws exception")
def step_impl(context: Context, type_label: str, value: str):
    assert_that(calling(context.tx().concepts.get_attribute_type(type_label).put).with_args(context.tx(), value),
                raises(TypeDBClientException))


@step("{var:Var} = attribute({type_label}) as(string) put: {value}")
def step_impl(context: Context, var: str, type_label: str, value: str):
    context.put(var, context.tx().concepts.get_attribute_type(type_label).put(context.tx(), value))


@step("attribute({type_label}) as(datetime) put: {value:DateTime}; throws exception")
def step_impl(context: Context, type_label: str, value: datetime):
    assert_that(calling(context.tx().concepts.get_attribute_type(type_label).put).with_args(context.tx(), value),
                raises(TypeDBClientException))


@step("{var:Var} = attribute({type_label}) as(datetime) put: {value:DateTime}")
def step_impl(context: Context, var: str, type_label: str, value: datetime):
    context.put(var, context.tx().concepts.get_attribute_type(type_label).put(context.tx(), value))


@step("{var:Var} = attribute({type_label}) as(boolean) get: {value:Bool}")
def step_impl(context: Context, var: str, type_label: str, value: bool):
    context.put(var, context.tx().concepts.get_attribute_type(type_label).get(context.tx(), value))


@step("{var:Var} = attribute({type_label}) as(long) get: {value:Int}")
def step_impl(context: Context, var: str, type_label: str, value: int):
    context.put(var, context.tx().concepts.get_attribute_type(type_label).get(context.tx(), value))


@step("{var:Var} = attribute({type_label}) as(double) get: {value:Float}")
def step_impl(context: Context, var: str, type_label: str, value: float):
    context.put(var, context.tx().concepts.get_attribute_type(type_label).get(context.tx(), value))


@step("{var:Var} = attribute({type_label}) as(string) get: {value}")
def step_impl(context: Context, var: str, type_label: str, value: str):
    context.put(var, context.tx().concepts.get_attribute_type(type_label).get(context.tx(), value))


@step("{var:Var} = attribute({type_label}) as(datetime) get: {value:DateTime}")
def step_impl(context: Context, var: str, type_label: str, value: datetime):
    print(context.tx().concepts.get_attribute_type(type_label), flush=True)
    context.put(var, context.tx().concepts.get_attribute_type(type_label).get(context.tx(), value))


@step("attribute {var:Var} has boolean value: {value:Bool}")
def step_impl(context: Context, var: str, value: bool):
    assert_that(context.get(var).as_attribute().as_boolean(), is_(value))


@step("attribute {var:Var} has long value: {value:Int}")
def step_impl(context: Context, var: str, value: int):
    assert_that(context.get(var).as_attribute().as_long(), is_(value))


@step("attribute {var:Var} has double value: {value:Float}")
def step_impl(context: Context, var: str, value: float):
    assert_that(context.get(var).as_attribute().as_double(), is_(value))


@step("attribute {var:Var} has string value: {value}")
def step_impl(context: Context, var: str, value: str):
    assert_that(context.get(var).as_attribute().as_string(), is_(value))


@step("attribute {var:Var} has datetime value: {value:DateTime}")
def step_impl(context: Context, var: str, value: datetime):
    assert_that(context.get(var).as_attribute().as_datetime(), is_(value))

