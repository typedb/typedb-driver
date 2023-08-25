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

from concurrent.futures.thread import ThreadPoolExecutor
from functools import partial
from typing import Callable

from behave import *
from hamcrest import *
from typedb.client import *

from tests.behaviour.config.parameters import parse_transaction_type, parse_list, parse_bool
from tests.behaviour.context import Context


def for_each_session_open_transaction_of_type(context: Context, transaction_types: list[TransactionType]):
    for session in context.sessions:
        transactions = []
        for transaction_type in transaction_types:
            transaction = session.transaction(transaction_type, context.transaction_options)
            transactions.append(transaction)
        context.sessions_to_transactions[session] = transactions


# TODO: this is implemented as open(s) in some clients - get rid of that, simplify them
@step("session opens transaction of type: {transaction_type}")
@step("for each session, open transaction of type: {transaction_type}")
def step_impl(context: Context, transaction_type: str):
    transaction_type = parse_transaction_type(transaction_type)
    for_each_session_open_transaction_of_type(context, [transaction_type])


@step("for each session, open transaction of type")
@step("for each session, open transactions of type")
def step_impl(context: Context):
    transaction_types = list(map(parse_transaction_type, parse_list(context.table)))
    for_each_session_open_transaction_of_type(context, transaction_types)


def open_transactions_of_type_throws_exception(context: Context, transaction_types: list[TransactionType]):
    for session in context.sessions:
        for transaction_type in transaction_types:
            try:
                session.transaction(transaction_type)
                assert False
            except TypeDBClientException:
                pass


@step("session open transaction of type; throws exception: {transaction_type}")
def step_impl(context: Context, transaction_type):
    transaction_type = parse_transaction_type(transaction_type)
    open_transactions_of_type_throws_exception(context, [transaction_type])


# TODO: transaction(s) in other implementations, simplify
@step("for each session, open transactions of type; throws exception")
def step_impl(context: Context):
    open_transactions_of_type_throws_exception(context, list(map(lambda raw_type: parse_transaction_type(raw_type), parse_list(context.table))))


def for_each_session_transactions_are(context: Context, assertion: Callable[[TypeDBTransaction], None]):
    for session in context.sessions:
        for transaction in context.sessions_to_transactions[session]:
            assertion(transaction)


def assert_transaction_null(transaction: TypeDBTransaction, is_null: bool):
    assert_that(transaction is None, is_(is_null))


@step("session transaction is null: {is_null}")
@step("for each session, transaction is null: {is_null}")
@step("for each session, transactions are null: {is_null}")
def step_impl(context: Context, is_null):
    is_null = parse_bool(is_null)
    for_each_session_transactions_are(context, lambda tx: assert_transaction_null(tx, is_null))


def assert_transaction_open(transaction: TypeDBTransaction, is_open: bool):
    assert_that(transaction.is_open(), is_(is_open))


@step("session transaction is open: {is_open}")
@step("for each session, transaction is open: {is_open}")
@step("for each session, transactions are open: {is_open}")
def step_impl(context: Context, is_open):
    is_open = parse_bool(is_open)
    for_each_session_transactions_are(context, lambda tx: assert_transaction_open(tx, is_open))


@step("session transaction commits")
@step("transaction commits")
def step_impl(context: Context):
    context.tx().commit()

@step("session transaction closes")
def step_impl(context: Context):
    context.tx().close()

@step("session transaction commits; throws exception")
@step("transaction commits; throws exception")
def step_impl(context: Context):
    try:
        context.tx().commit()
        assert False
    except TypeDBClientException:
        pass


@step("transaction commits; throws exception containing \"{exception}\"")
def step_impl(context: Context, exception: str):
    assert_that(calling(context.tx().commit), raises(TypeDBClientException, exception))


@step("for each session, transaction commits")
@step("for each session, transactions commit")
def step_impl(context: Context):
    for session in context.sessions:
        for transaction in context.sessions_to_transactions[session]:
            transaction.commit()


@step("for each session, transaction commits; throws exception")
@step("for each session, transactions commit; throws exception")
def step_impl(context: Context):
    for session in context.sessions:
        for transaction in context.sessions_to_transactions[session]:
            try:
                transaction.commit()
                assert False
            except TypeDBClientException:
                pass


# TODO: close(s) in other implementations - simplify
@step("for each session, transaction closes")
def step_impl(context: Context):
    for session in context.sessions:
        for transaction in context.sessions_to_transactions[session]:
            transaction.close()


def for_each_session_transaction_has_type(context: Context, transaction_types: list):
    for session in context.sessions:
        transactions = context.sessions_to_transactions[session]
        assert_that(transactions, has_length(len(transaction_types)))
        transactions_iterator = iter(transactions)
        for transaction_type in transaction_types:
            assert_that(next(transactions_iterator).transaction_type, is_(transaction_type))


# NOTE: behave ignores trailing colons in feature files
@step("for each session, transaction has type")
@step("for each session, transactions have type")
def step_impl(context: Context):
    transaction_types = list(map(parse_transaction_type, parse_list(context.table)))
    for_each_session_transaction_has_type(context, transaction_types)


# TODO: this is overcomplicated in some clients (has/have, transaction(s))
@step("for each session, transaction has type: {transaction_type}")
@step("session transaction has type: {transaction_type}")
def step_impl(context: Context, transaction_type):
    transaction_type = parse_transaction_type(transaction_type)
    for_each_session_transaction_has_type(context, [transaction_type])


##############################################
# sequential sessions, parallel transactions #
##############################################

# TODO: transaction(s) in other implementations - simplify
@step("for each session, open transactions in parallel of type")
def step_impl(context: Context):
    types = list(map(parse_transaction_type, parse_list(context.table)))
    assert_that(len(types), is_(less_than_or_equal_to(context.THREAD_POOL_SIZE)))
    with ThreadPoolExecutor(max_workers=context.THREAD_POOL_SIZE) as executor:
        for session in context.sessions:
            context.sessions_to_transactions_parallel[session] = []
            for type_ in types:
                context.sessions_to_transactions_parallel[session].append(executor.submit(partial(session.transaction, type_)))


def for_each_session_transactions_in_parallel_are(context: Context, assertion: Callable[[TypeDBTransaction], None]):
    for session in context.sessions:
        for future_transaction in context.sessions_to_transactions_parallel[session]:
            assertion(future_transaction.result())


@step("for each session, transactions in parallel are null: {is_null}")
def step_impl(context: Context, is_null):
    is_null = parse_bool(is_null)
    for_each_session_transactions_in_parallel_are(context, lambda tx: assert_transaction_null(tx, is_null))


@step("for each session, transactions in parallel are open: {is_open}")
def step_impl(context: Context, is_open):
    is_open = parse_bool(is_open)
    for_each_session_transactions_in_parallel_are(context, lambda tx: assert_transaction_open(tx, is_open))


@step("for each session, transactions in parallel have type")
def step_impl(context: Context):
    types = list(map(parse_transaction_type, parse_list(context.table)))
    for session in context.sessions:
        future_transactions = context.sessions_to_transactions_parallel[session]
        assert_that(future_transactions, has_length(len(types)))
        future_transactions_iter = iter(future_transactions)
        for type_ in types:
            assert_that(next(future_transactions_iter).result().transaction_type, is_(type_))


############################################
# parallel sessions, parallel transactions #
############################################

def for_each_session_in_parallel_transactions_in_parallel_are(context: Context, assertion):
    for future_session in context.sessions_parallel:
        for future_transaction in context.sessions_parallel_to_transactions_parallel[future_session]:
            assertion(future_transaction)


@step("for each session in parallel, transactions in parallel are null: {is_null}")
def step_impl(context: Context, is_null):
    is_null = parse_bool(is_null)
    for_each_session_in_parallel_transactions_in_parallel_are(context, lambda tx: assert_transaction_null(tx, is_null))


@step("for each session in parallel, transactions in parallel are open: {is_open}")
def step_impl(context: Context, is_open):
    is_open = parse_bool(is_open)
    for_each_session_in_parallel_transactions_in_parallel_are(context, lambda tx: assert_transaction_open(tx, is_open))


######################################
# transaction configuration          #
######################################

@step("set transaction option {option} to: {value:Int}")
def step_impl(context: Context, option: str, value: int):
    if option not in context.option_setters:
        raise Exception("Unrecognised option: " + option)
    context.option_setters[option](context.transaction_options, value)


######################################
# transaction behaviour with queries #
######################################

@step("for each transaction, define query; throws exception containing \"{exception}\"")
def step_impl(context: Context, exception: str):
    for session in context.sessions:
        for transaction in context.sessions_to_transactions[session]:
            try:
                transaction.query.define(context.text)
                assert False
            except TypeDBClientException as e:
                assert_that(exception, is_in(str(e)))
