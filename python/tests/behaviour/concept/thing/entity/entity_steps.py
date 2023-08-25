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


@step("entity({type_label}) create new instance; throws exception")
def step_impl(context: Context, type_label: str):
    assert_that(calling(context.tx().concepts.get_entity_type(type_label).create).with_args(context.tx()),
                raises(TypeDBClientException))


@step("{var:Var} = entity({type_label}) create new instance")
def step_impl(context: Context, var: str, type_label: str):
    context.put(var, context.tx().concepts.get_entity_type(type_label).create(context.tx()))


@step("{var:Var} = entity({type_label}) create new instance with key({key_type}): {key_value}")
def step_impl(context: Context, var: str, type_label: str, key_type: str, key_value: str):
    key = context.tx().concepts.get_attribute_type(key_type).put(context.tx(), key_value)
    entity = context.tx().concepts.get_entity_type(type_label).create(context.tx())
    entity.set_has(context.tx(), key)
    context.put(var, entity)


@step("{var:Var} = entity({type_label}) get instance with key({key_type}): {key_value}")
def step_impl(context: Context, var: str, type_label: str, key_type: str, key_value: str):
    context.put(var, next((owner for owner in context.tx().concepts.get_attribute_type(key_type)
                          .get(context.tx(), key_value).get_owners(context.tx())
                           if owner.get_type().get_label() == Label.of(type_label)), None))


@step("entity({type_label}) get instances contain: {var:Var}")
def step_impl(context: Context, type_label: str, var: str):
    assert_that(context.tx().concepts.get_entity_type(type_label).get_instances(context.tx()),
                has_item(context.get(var)))


@step("entity({type_label}) get instances is empty")
def step_impl(context: Context, type_label: str):
    assert_that(calling(next).with_args(context.tx().concepts.get_entity_type(type_label).get_instances(context.tx())),
                raises(StopIteration))
