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

from behave.model_core import Status
from typedb.client import *

from tests.behaviour.config.parameters import RootLabel
from tests.behaviour.context import Context


def before_all(context: Context):
    context.THREAD_POOL_SIZE = 32


def before_scenario(context: Context):
    # setup context state
    context.sessions = []
    context.sessions_to_transactions = {}
    context.sessions_parallel = []
    context.sessions_to_transactions_parallel = {}
    context.sessions_parallel_to_transactions_parallel = {}
    context.things = {}
    # setup context functions
    context.tx = lambda: context.sessions_to_transactions[context.sessions[0]][0]
    context.get = lambda var: context.things[var]
    context.put = lambda var, thing: _put_impl(context, var, thing)
    context.get_thing_type = lambda root_label, type_label: _get_thing_type_impl(context, root_label, type_label)
    context.clear_answers = lambda: _clear_answers_impl(context)
    context.option_setters = {
        "session-idle-timeout-millis": lambda options, value:  setattr(options, "session_idle_timeout_millis", value),
        "transaction-timeout-millis": lambda options, value:  setattr(options, "transaction_timeout_millis", value),
    }


def _put_impl(context: Context, variable: str, thing: Thing):
    context.things[variable] = thing


def _get_thing_type_impl(context: Context, root_label: RootLabel, type_label: str):
    if root_label == RootLabel.ENTITY:
        return context.tx().concepts.get_entity_type(type_label)
    elif root_label == RootLabel.ATTRIBUTE:
        return context.tx().concepts.get_attribute_type(type_label)
    elif root_label == RootLabel.RELATION:
        return context.tx().concepts.get_relation_type(type_label)
    else:
        raise ValueError("Unrecognised value")


def _clear_answers_impl(context: Context):
    context.answers = None
    context.numeric_answer = None
    context.answer_groups = None
    context.numeric_answer_groups = None


def after_scenario(context: Context, scenario):
    if scenario.status == Status.skipped:
        return
    if context.client.is_open():
        context.client.close()


def after_all(context: Context):
    pass
