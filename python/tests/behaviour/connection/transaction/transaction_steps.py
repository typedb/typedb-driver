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

from concurrent.futures.thread import ThreadPoolExecutor
from functools import partial
from typing import Callable, Optional

from behave import *
from hamcrest import *
from tests.behaviour.config.parameters import MayError, parse_list, parse_transaction_type
from tests.behaviour.context import Context
from typedb.api.connection.transaction import TransactionType
from typedb.driver import *


def open_transactions_of_type(context: Context, database_name: str, transaction_types: list[TransactionType]):
    context.transactions = []
    for transaction_type in transaction_types:
        transaction = context.driver.transaction(database_name, transaction_type)  # , context.transaction_options
        context.transactions.append(transaction)


@step(
    "connection open {transaction_type:TransactionType} transaction for database: {database_name:Words}{may_error:MayError}")
def step_impl(context: Context, transaction_type: TransactionType, database_name: str, may_error: MayError):
    may_error.check(lambda: open_transactions_of_type(context, database_name, [transaction_type]))


@step("connection open transactions for database: {database_name}, of type")
def step_impl(context: Context, database_name: str):
    transaction_types = list(map(parse_transaction_type, parse_list(context.table)))
    open_transactions_of_type(context, database_name, transaction_types)


def for_each_transaction_is(context: Context, assertion: Callable[[Transaction], None]):
    for transaction in context.transactions:
        assertion(transaction)


def assert_transaction_open(transaction: Optional[Transaction], is_open: bool):
    assert_that(transaction is not None and transaction.is_open(), is_(is_open))


@step("transaction is open: {is_open:Bool}")
def step_impl(context: Context, is_open: bool):
    assert_transaction_open(context.tx(), is_open)


@step("transactions are open: {are_open:Bool}")
def step_impl(context: Context, are_open: bool):
    for_each_transaction_is(context, lambda tx: assert_transaction_open(tx, are_open))


@step("transaction commits{may_error:MayError}")
def step_impl(context: Context, may_error: MayError):
    may_error.check(lambda: context.tx().commit())


@step("transaction closes")
def step_impl(context: Context):
    context.tx().close()


@step("transaction rollbacks{may_error:MayError}")
def step_impl(context: Context, may_error: MayError):
    may_error.check(lambda: context.tx().rollback())


def for_each_transaction_has_type(context: Context, transaction_types: list):
    assert_that(context.transactions, has_length(len(transaction_types)))
    transactions_iterator = iter(context.transactions)
    for transaction_type in transaction_types:
        assert_that(next(transactions_iterator).type, is_(transaction_type))


@step("transactions have type")
def step_impl(context: Context):
    transaction_types = list(map(parse_transaction_type, parse_list(context.table)))
    for_each_transaction_has_type(context, transaction_types)


@step("transaction has type: {transaction_type:TransactionType}")
def step_impl(context: Context, transaction_type: TransactionType):
    for_each_transaction_has_type(context, [transaction_type])


@step("connection open transactions in parallel for database: {name:Words}, of type")
def step_impl(context: Context, name: str):
    types = list(map(parse_transaction_type, parse_list(context.table)))
    assert_that(len(types), is_(less_than_or_equal_to(context.THREAD_POOL_SIZE)))
    with ThreadPoolExecutor(max_workers=context.THREAD_POOL_SIZE) as executor:
        context.transactions_parallel = []
        for type_ in types:
            context.transactions_parallel.append(
                executor.submit(partial(context.driver.transaction, name, type_)))


def transactions_in_parallel_are(context: Context, assertion: Callable[[Transaction], None]):
    for future_transaction in context.transactions_parallel:
        assertion(future_transaction.result())


@step("transactions in parallel are open: {are_open:Bool}")
def step_impl(context: Context, are_open: bool):
    transactions_in_parallel_are(context, lambda tx: assert_transaction_open(tx, are_open))


@step("transactions in parallel have type")
def step_impl(context: Context):
    types = list(map(parse_transaction_type, parse_list(context.table)))
    future_transactions = context.transactions_parallel
    assert_that(future_transactions, has_length(len(types)))
    future_transactions_iter = iter(future_transactions)
    for type_ in types:
        assert_that(next(future_transactions_iter).result().type, is_(type_))


@step("set transaction option {option} to: {value:Int}")
def step_impl(context: Context, option: str, value: int):
    if option not in context.option_setters:
        raise Exception("Unrecognised option: " + option)
    context.option_setters[option](context.transaction_options, value)


@step("schedule database creation on transaction close: {database_name:Words}")
def step_impl(context: Context, database_name: str):
    context.tx().on_close(lambda: context.driver.databases.create(database_name))
