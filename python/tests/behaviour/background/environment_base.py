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
from tests.behaviour.context import Context
from typedb.driver import *


def before_all(context: Context):
    context.THREAD_POOL_SIZE = 32


def before_scenario(context: Context):
    # setup context state
    context.transactions = {}
    context.transactions_parallel = []
    context.answer = None  # QueryAnswer
    context.unwrapped_answer = None  # OkQueryAnswer / ConceptRowIterator / ConceptTreeIterator
    context.collected_answer = None  # [ConceptRow] / ... ?
    context.things = {}
    # setup context functions
    context.tx = lambda: next(iter(context.transactions), None)
    context.get = lambda var: context.things[var]
    context.put = lambda var, thing: _put_impl(context, var, thing)
    context.clear_answers = lambda: _clear_answers_impl(context)
    context.option_setters = {
        # "transaction-timeout-millis": lambda options, value: setattr(options, "transaction_timeout_millis", value),
    }


def _clear_answers_impl(context: Context):
    context.answers = None
    context.unwrapped_answer = None
    context.collected_answer = None


def after_scenario(context: Context, scenario):
    if scenario.status == Status.skipped:
        return
    if context.driver.is_open():
        context.driver.close()


def after_all(_: Context):
    pass
