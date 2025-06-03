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

import tempfile
import uuid
from pathlib import Path

from behave.model_core import Status
from tests.behaviour.context import Context
from tests.behaviour.util.util import delete_dir
from typedb.driver import *


def before_all(context: Context):
    context.THREAD_POOL_SIZE = 32
    context.init_transaction_options_if_needed_fn = lambda: _init_transaction_options_if_needed(context)
    context.init_query_options_if_needed_fn = lambda: _init_query_options_if_needed(context)


def before_scenario(context: Context):
    # setup context state
    context.temp_dir = None
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
    context.full_path = lambda file_name: _full_path(context, file_name)
    context.tx = lambda: next(iter(context.transactions), None)
    context.clear_answers = lambda: _clear_answers(context)
    context.clear_concurrent_answers = lambda: _clear_concurrent_answers(context)
    context.transaction_options = None
    context.query_options = None


def after_scenario(context: Context, scenario):
    if scenario.status == Status.skipped:
        return
    if context.temp_dir:
        delete_dir(context.temp_dir)
    if context.driver.is_open():
        context.driver.close()
    if context.background_driver and context.background_driver.is_open():
        context.background_driver.close()


def after_all(_: Context):
    pass


def _clear_answers(context: Context):
    context.answers = None
    context.unwrapped_answer = None
    context.collected_answer = None


def _clear_concurrent_answers(context: Context):
    context.concurrent_answers = None
    context.unwrapped_concurrent_answers = None


def _full_path(context: Context, file_name: str) -> Path:
    return _get_temp_dir(context) / file_name


def _init_transaction_options_if_needed(context: Context):
    if not context.transaction_options:
        context.transaction_options = TransactionOptions()


def _init_query_options_if_needed(context: Context):
    if not context.query_options:
        context.query_options = QueryOptions()


def _get_temp_dir(context: Context) -> Path:
    if context.temp_dir is None:
        context.temp_dir = Path(tempfile.gettempdir()) / f"temp-{uuid.uuid4()}"
        context.temp_dir.mkdir(parents=True, exist_ok=True)
    return context.temp_dir
