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

from behave.model_core import Status
from tests.behaviour.config.parameters import Kind
from tests.behaviour.context import Context
from typedb.driver import *


def before_all(context: Context):
    context.THREAD_POOL_SIZE = 32


def before_scenario(context: Context):
    # setup context state
    context.transactions = {}
    context.transactions_parallel = []
    context.things = {}
    # setup context functions
    context.tx = lambda: context.transactions[0]
    context.get = lambda var: context.things[var]
    context.put = lambda var, thing: _put_impl(context, var, thing)
    context.get_thing_type = lambda root_label, type_label: _get_thing_type_impl(context, root_label, type_label)
    context.clear_answers = lambda: _clear_answers_impl(context)
    context.option_setters = {
        # "transaction-timeout-millis": lambda options, value: setattr(options, "transaction_timeout_millis", value),
    }


def _put_impl(context: Context, variable: str, thing: Thing):
    context.things[variable] = thing


def _get_thing_type_impl(context: Context, root_label: Kind, type_label: str):
    if root_label == Kind.ENTITY:
        return context.tx().getQueryType.get_entity_type(type_label).resolve()
    elif root_label == Kind.ATTRIBUTE:
        return context.tx().getQueryType.get_attribute_type(type_label).resolve()
    elif root_label == Kind.RELATION:
        return context.tx().getQueryType.get_relation_type(type_label).resolve()
    else:
        raise ValueError("Unrecognised value")


def _clear_answers_impl(context: Context):
    context.answers = None
    context.fetch_answers = None
    context.value_answer = None
    context.value_answer_groups = None


def after_scenario(context: Context, scenario):
    if scenario.status == Status.skipped:
        return
    if context.driver.is_open():
        context.driver.close()


def after_all(context: Context):
    pass
