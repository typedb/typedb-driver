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

from tests.behaviour.config.parameters import parse_list, parse_bool, parse_label
from tests.behaviour.context import Context


@step("relation({relation_label}) set relates role: {role_label} as {super_role}; throws exception")
def step_impl(context: Context, relation_label: str, role_label: str, super_role: str):
    try:
        context.tx().concepts.get_relation_type(relation_label).set_relates(context.tx(), role_label, overridden_label=super_role)
        assert False
    except TypeDBClientException:
        pass


@step("relation({relation_label}) set relates role: {role_label} as {super_role}")
def step_impl(context: Context, relation_label: str, role_label: str, super_role: str):
    context.tx().concepts.get_relation_type(relation_label).set_relates(context.tx(), role_label, overridden_label=super_role)


@step("relation({relation_label}) set relates role: {role_label}; throws exception")
def step_impl(context: Context, relation_label: str, role_label: str):
    try:
        context.tx().concepts.get_relation_type(relation_label).set_relates(context.tx(), role_label)
        assert False
    except TypeDBClientException:
        pass


@step("relation({relation_label}) set relates role: {role_label}")
def step_impl(context: Context, relation_label: str, role_label: str):
    context.tx().concepts.get_relation_type(relation_label).set_relates(context.tx(), role_label)


@step("relation({relation_label}) unset related role: {role_label}; throws exception")
def step_impl(context: Context, relation_label: str, role_label: str):
    try:
        context.tx().concepts.get_relation_type(relation_label).unset_relates(context.tx(), role_label)
        assert False
    except TypeDBClientException:
        pass


@step("relation({relation_label}) unset related role: {role_label}")
def step_impl(context: Context, relation_label: str, role_label: str):
    context.tx().concepts.get_relation_type(relation_label).unset_relates(context.tx(), role_label)


@step("relation({relation_label}) remove related role: {role_label}")
def step_impl(context: Context, relation_label: str, role_label: str):
    context.tx().concepts.get_relation_type(relation_label).get_relates(context.tx(), role_label).delete(context.tx())


@step("relation({relation_label}) get role({role_label}) is null: {is_null}")
def step_impl(context: Context, relation_label: str, role_label: str, is_null):
    is_null = parse_bool(is_null)
    assert_that(context.tx().concepts.get_relation_type(relation_label).get_relates(context.tx(), role_label) is None, is_(is_null))


@step("relation({relation_label}) get overridden role({role_label}) is null: {is_null}")
def step_impl(context: Context, relation_label: str, role_label: str, is_null):
    is_null = parse_bool(is_null)
    assert_that(context.tx().concepts.get_relation_type(relation_label).get_relates_overridden(context.tx(), role_label) is None, is_(is_null))


@step("relation({relation_label}) get role({role_label}) set label: {new_label}")
def step_impl(context: Context, relation_label: str, role_label: str, new_label: str):
    context.tx().concepts.get_relation_type(relation_label).get_relates(context.tx(), role_label).set_label(context.tx(), new_label)


@step("relation({relation_label}) get role({role_label}) get label: {get_label}")
def step_impl(context: Context, relation_label: str, role_label: str, get_label: str):
    assert_that(context.tx().concepts.get_relation_type(relation_label).get_relates(context.tx(), role_label).get_label().name, is_(get_label))


@step("relation({relation_label}) get overridden role({role_label}) get label: {get_label}")
def step_impl(context: Context, relation_label: str, role_label: str, get_label: str):
    assert_that(context.tx().concepts.get_relation_type(relation_label).get_relates_overridden(context.tx(), role_label).get_label().name, is_(get_label))


@step("relation({relation_label}) get role({role_label}) is abstract: {is_abstract}")
def step_impl(context: Context, relation_label: str, role_label: str, is_abstract: str):
    is_abstract = parse_bool(is_abstract)
    assert_that(context.tx().concepts.get_relation_type(relation_label).get_relates(context.tx(), role_label).is_abstract(), is_(is_abstract))


def get_actual_related_role_scoped_labels(context: Context, relation_label: str):
    return [r.get_label() for r in context.tx().concepts.get_relation_type(relation_label).get_relates(context.tx())]


@step("relation({relation_label}) get related roles contain")
def step_impl(context: Context, relation_label: str):
    role_labels = [parse_label(s) for s in parse_list(context.table)]
    actuals = get_actual_related_role_scoped_labels(context, relation_label)
    for role_label in role_labels:
        assert_that(actuals, has_item(role_label))


@step("relation({relation_label}) get related roles do not contain")
def step_impl(context: Context, relation_label: str):
    role_labels = [parse_label(s) for s in parse_list(context.table)]
    actuals = get_actual_related_role_scoped_labels(context, relation_label)
    for role_label in role_labels:
        assert_that(actuals, not_(has_item(role_label)))


def get_actual_related_role_explicit_labels(context: Context, relation_label: str):
    return [r.get_label()
            for r in context.tx().concepts.get_relation_type(relation_label)
            .get_relates(context.tx(), transitivity=Transitivity.EXPLICIT)]


@step("relation({relation_label}) get related explicit roles contain")
def step_impl(context: Context, relation_label: str):
    role_labels = [parse_label(s) for s in parse_list(context.table)]
    actuals = get_actual_related_role_explicit_labels(context, relation_label)
    for role_label in role_labels:
        assert_that(actuals, has_item(role_label))


@step("relation({relation_label}) get related explicit roles do not contain")
def step_impl(context: Context, relation_label: str):
    role_labels = [parse_label(s) for s in parse_list(context.table)]
    actuals = get_actual_related_role_explicit_labels(context, relation_label)
    for role_label in role_labels:
        assert_that(actuals, not_(has_item(role_label)))


@step("relation({relation_label}) get role({role_label}) get supertype: {super_label:ScopedLabel}")
def step_impl(context: Context, relation_label: str, role_label: str, super_label: Label):
    supertype = context.tx().concepts.get_relation_type(super_label.scope).get_relates(context.tx(), super_label.name)
    assert_that(supertype, is_(context.tx().concepts.get_relation_type(relation_label).get_relates(context.tx(), role_label).get_supertype(context.tx())))


def get_actual_related_role_supertypes_scoped_labels(context: Context, relation_label: str, role_label: str):
    return [r.get_label() for r in context.tx().concepts.get_relation_type(relation_label).get_relates(context.tx(), role_label).get_supertypes(context.tx())]


@step("relation({relation_label}) get role({role_label}) get supertypes contain")
def step_impl(context: Context, relation_label: str, role_label: str):
    super_labels = [parse_label(s) for s in parse_list(context.table)]
    actuals = get_actual_related_role_supertypes_scoped_labels(context, relation_label, role_label)
    for super_label in super_labels:
        assert_that(actuals, has_item(super_label))


@step("relation({relation_label}) get role({role_label}) get supertypes do not contain")
def step_impl(context: Context, relation_label: str, role_label: str):
    super_labels = [parse_label(s) for s in parse_list(context.table)]
    actuals = get_actual_related_role_supertypes_scoped_labels(context, relation_label, role_label)
    for super_label in super_labels:
        assert_that(actuals, not_(has_item(super_label)))


def get_actual_related_role_players_scoped_labels(context: Context, relation_label: str, role_label: str):
    return [r.get_label() for r in context.tx().concepts.get_relation_type(relation_label).get_relates(context.tx(), role_label).get_player_types(context.tx())]


@step("relation({relation_label}) get role({role_label}) get players contain")
def step_impl(context: Context, relation_label: str, role_label: str):
    player_labels = [parse_label(s) for s in parse_list(context.table)]
    actuals = get_actual_related_role_players_scoped_labels(context, relation_label, role_label)
    for player_label in player_labels:
        assert_that(actuals, has_item(player_label))


@step("relation({relation_label}) get role({role_label}) get players do not contain")
def step_impl(context: Context, relation_label: str, role_label: str):
    player_labels = [parse_label(s) for s in parse_list(context.table)]
    actuals = get_actual_related_role_players_scoped_labels(context, relation_label, role_label)
    for player_label in player_labels:
        assert_that(actuals, not_(has_item(player_label)))


def get_actual_related_role_subtypes_scoped_labels(context: Context, relation_label: str, role_label: str):
    return [r.get_label() for r in context.tx().concepts.get_relation_type(relation_label).get_relates(context.tx(), role_label).get_subtypes(context.tx())]


@step("relation({relation_label}) get role({role_label}) get subtypes contain")
def step_impl(context: Context, relation_label: str, role_label: str):
    sub_labels = [parse_label(s) for s in parse_list(context.table)]
    actuals = get_actual_related_role_subtypes_scoped_labels(context, relation_label, role_label)
    for sub_label in sub_labels:
        assert_that(actuals, has_item(sub_label))


@step("relation({relation_label}) get role({role_label}) get subtypes do not contain")
def step_impl(context: Context, relation_label: str, role_label: str):
    sub_labels = [parse_label(s) for s in parse_list(context.table)]
    actuals = get_actual_related_role_subtypes_scoped_labels(context, relation_label, role_label)
    for sub_label in sub_labels:
        assert_that(actuals, not_(has_item(sub_label)))
