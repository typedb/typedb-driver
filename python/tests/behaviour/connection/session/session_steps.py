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

from behave import *
from hamcrest import *
from typedb.client import *

from tests.behaviour.config.parameters import parse_bool, parse_list
from tests.behaviour.context import Context

SCHEMA = SessionType.SCHEMA
DATA = SessionType.DATA


def open_sessions_for_databases(context: Context, names: list, session_type):
    for name in names:
        sess = context.client.session(name, session_type, context.session_options)
        context.sessions.append(sess)


@step("connection open schema session for database: {database_name}")
def step_impl(context: Context, database_name):
    open_sessions_for_databases(context, [database_name], SCHEMA)


@step("connection open data session for database: {database_name}")
@step("connection open session for database: {database_name}")
def step_impl(context: Context, database_name: str):
    open_sessions_for_databases(context, [database_name], DATA)


@step("connection open schema session for database")
@step("connection open schema session for databases")
@step("connection open schema sessions for database")
@step("connection open schema sessions for databases")
def step_impl(context: Context):
    names = parse_list(context.table)
    open_sessions_for_databases(context, names, SCHEMA)


@step("connection open data session for database")
@step("connection open data session for databases")
@step("connection open data sessions for database")
@step("connection open data sessions for databases")
@step("connection open session for database")
@step("connection open session for databases")
@step("connection open sessions for database")
@step("connection open sessions for databases")
def step_impl(context: Context):
    names = parse_list(context.table)
    open_sessions_for_databases(context, names, DATA)


@step("connection open data sessions in parallel for databases")
@step("connection open sessions in parallel for databases")
def step_impl(context: Context):
    names = parse_list(context.table)
    assert_that(len(names), is_(less_than_or_equal_to(context.THREAD_POOL_SIZE)))
    with ThreadPoolExecutor(max_workers=context.THREAD_POOL_SIZE) as executor:
        for name in names:
            context.sessions_parallel.append(executor.submit(partial(context.client.session, name, DATA)))


@step("connection close all sessions")
def step_impl(context: Context):
    for session in context.sessions:
        session.close()
    context.sessions = []


@step("session is null: {is_null}")
@step("sessions are null: {is_null}")
def step_impl(context: Context, is_null):
    is_null = parse_bool(is_null)
    for session in context.sessions:
        assert_that(session is None, is_(is_null))


@step("session is open: {is_open}")
@step("sessions are open: {is_open}")
def step_impl(context: Context, is_open):
    is_open = parse_bool(is_open)
    for session in context.sessions:
        assert_that(session.is_open(), is_(is_open))


@step("sessions in parallel are null: {is_null}")
def step_impl(context: Context, is_null):
    is_null = parse_bool(is_null)
    for future_session in context.sessions_parallel:
        assert_that(future_session.result() is None, is_(is_null))


@step("sessions in parallel are open: {is_open}")
def step_impl(context: Context, is_open):
    is_open = parse_bool(is_open)
    for future_session in context.sessions_parallel:
        assert_that(future_session.result().is_open(), is_(is_open))


def sessions_have_databases(context: Context, names: list[str]):
    assert_that(context.sessions, has_length(equal_to(len(names))))
    session_iter = iter(context.sessions)
    for name in names:
        assert_that(next(session_iter).database_name(), is_(name))


@step("session has database: {database_name}")
@step("sessions have database: {database_name}")
def step_impl(context: Context, database_name: str):
    sessions_have_databases(context, [database_name])


# TODO: session(s) has/have databases in other implementations, simplify
@step("sessions have databases")
def step_impl(context: Context):
    database_names = parse_list(context.table)
    sessions_have_databases(context, database_names)


@step("sessions in parallel have databases")
def step_impl(context: Context):
    database_names = parse_list(context.table)
    assert_that(context.sessions_parallel, has_length(equal_to(len(database_names))))
    future_session_iter = iter(context.sessions_parallel)
    for name in database_names:
        assert_that(next(future_session_iter).result().database_name(), is_(name))


######################################
# session configuration              #
######################################

@step("set session option {option} to: {value:Int}")
def step_impl(context: Context, option: str, value: int):
    if option not in context.option_setters:
        raise Exception("Unrecognised option: " + option)
    context.option_setters[option](context.session_options, value)
