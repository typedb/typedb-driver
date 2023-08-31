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

from tests.behaviour.config.parameters import parse_dict, parse_var
from tests.behaviour.context import Context


@step("relation({type_label}) create new instance; throws exception")
def step_impl(context: Context, type_label: str):
    assert_that(calling(context.tx().concepts.get_relation_type(type_label).create).with_args(context.tx()),
                raises(TypeDBClientException))


@step("{var:Var} = relation({type_label}) create new instance")
def step_impl(context: Context, var: str, type_label: str):
    context.put(var, context.tx().concepts.get_relation_type(type_label).create(context.tx()))


@step("{var:Var} = relation({type_label}) create new instance with key({key_type}): {key_value}")
def step_impl(context: Context, var: str, type_label: str, key_type: str, key_value: str):
    key = context.tx().concepts.get_attribute_type(key_type).put(context.tx(), key_value)
    relation = context.tx().concepts.get_relation_type(type_label).create(context.tx())
    relation.set_has(context.tx(), key)
    context.put(var, relation)


@step("{var:Var} = relation({type_label}) get instance with key({key_type}): {key_value}")
def step_impl(context: Context, var: str, type_label: str, key_type: str, key_value: str):
    context.put(var, next((owner for owner in context.tx().concepts.get_attribute_type(key_type).get(context.tx(), key_value).get_owners(context.tx())
                           if owner.get_type().get_label() == Label.of(type_label)), None))


@step("relation({type_label}) get instances contain: {var:Var}")
def step_impl(context: Context, type_label: str, var: str):
    assert_that(context.tx().concepts.get_relation_type(type_label).get_instances(context.tx()), has_item(context.get(var)))


@step("relation({type_label}) get instances do not contain: {var:Var}")
def step_impl(context: Context, type_label: str, var: str):
    assert_that(context.tx().concepts.get_relation_type(type_label).get_instances(context.tx()), not_(has_item(context.get(var))))


@step("relation({type_label}) get instances is empty")
def step_impl(context: Context, type_label: str):
    assert_that(calling(next).with_args(context.tx().concepts.get_relation_type(type_label).get_instances(context.tx())), raises(StopIteration))


@step("relation {var1:Var} add player for role({role_label}): {var2:Var}")
def step_impl(context: Context, var1: str, role_label: str, var2: str):
    relation = context.get(var1).as_relation()
    relation.add_player(context.tx(), relation.get_type().get_relates(context.tx(), role_label), context.get(var2))


@step("relation {var1:Var} remove player for role({role_label}): {var2:Var}")
def step_impl(context: Context, var1: str, role_label: str, var2: str):
    relation = context.get(var1).as_relation()
    relation.remove_player(context.tx(), relation.get_type().get_relates(context.tx(), role_label), context.get(var2))


@step("relation {var1:Var} add player for role({role_label}): {var2:Var}; throws exception")
def step_impl(context: Context, var1: str, role_label: str, var2: str):
    adding_player_throws_exception(context, var1, role_label, var2)


def adding_player_throws_exception(context: Context, var1: str, role_label: str, var2: str):
    relation = context.get(var1).as_relation()
    try:
        relation.add_player(
            context.tx(),
            relation.get_type().get_relates(context.tx(), role_label),
            context.get(var2))
        assert False;
    except TypeDBClientException:
        pass


@step("relation {var:Var} get players contain")
def step_impl(context: Context, var: str):
    players = parse_dict(context.table)
    relation = context.get(var).as_relation()
    players_by_role_type = relation.get_players(context.tx())
    for (role_label, var2) in players.items():
        assert_that(players_by_role_type.get(relation.get_type().get_relates(context.tx(), role_label)), has_item(context.get(parse_var(var2))))


@step("relation {var:Var} get players do not contain")
def step_impl(context: Context, var: str):
    players = parse_dict(context.table)
    relation = context.get(var).as_relation()
    players_by_role_type = relation.get_players(context.tx())
    for (role_label, var2) in players.items():
        assert_that(players_by_role_type.get(relation.get_type().get_relates(context.tx(), role_label)), not_(has_item(context.get(parse_var(var2)))))


@step("relation {var1:Var} get players contain: {var2:Var}")
def step_impl(context: Context, var1: str, var2: str):
    assert_that(context.get(var1).as_relation().get_players_by_role_type(context.tx()), has_item(context.get(var2)))


@step("relation {var1:Var} get players do not contain: {var2:Var}")
def step_impl(context: Context, var1: str, var2: str):
    assert_that(context.get(var1).as_relation().get_players_by_role_type(context.tx()), not_(has_item(context.get(var2))))


@step("relation {var1:Var} get players for role({role_label}) contain: {var2:Var}")
def step_impl(context: Context, var1: str, role_label: str, var2: str):
    relation = context.get(var1).as_relation()
    assert_that(relation.get_players_by_role_type(context.tx(), relation.get_type().get_relates(context.tx(), role_label)),
        has_item(context.get(var2)))


@step("relation {var1:Var} get players for role({role_label}) do not contain: {var2:Var}")
def step_impl(context: Context, var1: str, role_label: str, var2: str):
    relation = context.get(var1).as_relation()
    assert_that(relation.get_players_by_role_type(context.tx(), relation.get_type().get_relates(context.tx(), role_label)),
        not_(has_item(context.get(var2))))
