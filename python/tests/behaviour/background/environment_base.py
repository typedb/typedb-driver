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
    context.init_transaction_options_if_needed_fn = lambda: init_transaction_options_if_needed(context)
    context.init_query_options_if_needed_fn = lambda: init_query_options_if_needed(context)


def before_scenario(context: Context):
    # setup context state
    context.background_driver = None
    context.transactions = []
    context.transactions_parallel = []
    context.background_transactions = []
    context.answer = None  # QueryAnswer
    context.unwrapped_answer = None  # OkQueryAnswer / ConceptRowIterator / ConceptDocumentIterator
    context.collected_answer = None  # [ConceptRow] / [dict]
    context.concurrent_answers = None
    context.unwrapped_concurrent_answers = None
    # setup context functions
    context.tx = lambda: next(iter(context.transactions), None)
    context.clear_answers = lambda: _clear_answers_impl(context)
    context.clear_concurrent_answers = lambda: _clear_concurrent_answers_impl(context)
    context.transaction_options = None
    context.query_options = None


def _clear_answers_impl(context: Context):
    context.answers = None
    context.unwrapped_answer = None
    context.collected_answer = None


def _clear_concurrent_answers_impl(context: Context):
    context.concurrent_answers = None
    context.unwrapped_concurrent_answers = None


def after_scenario(context: Context, scenario):
    if scenario.status == Status.skipped:
        return
    if context.driver.is_open():
        context.driver.close()
    if context.background_driver and context.background_driver.is_open():
        context.background_driver.close()


def init_transaction_options_if_needed(context: Context):
    if not context.transaction_options:
        context.transaction_options = TransactionOptions()


def init_query_options_if_needed(context: Context):
    if not context.query_options:
        context.query_options = QueryOptions()


def after_all(_: Context):
    pass
