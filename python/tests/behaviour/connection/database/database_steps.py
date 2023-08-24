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

from tests.behaviour.config.parameters import parse_list
from tests.behaviour.context import Context
from tests.behaviour.util.util import assert_collections_equal


def create_databases(context: Context, names: list[str]):
    for name in names:
        context.client.databases.create(name)


@step("connection create database: {database_name}")
def step_impl(context: Context, database_name: str):
    create_databases(context, [database_name])


# TODO: connection create database(s) in other implementations, simplify
@step("connection create databases")
def step_impl(context: Context):
    names = parse_list(context.table)
    create_databases(context, names)


@step("connection create databases in parallel")
def step_impl(context: Context):
    names = parse_list(context.table)
    assert_that(len(names), is_(less_than_or_equal_to(context.THREAD_POOL_SIZE)))
    with ThreadPoolExecutor(max_workers=context.THREAD_POOL_SIZE) as executor:
        for name in names:
            executor.submit(partial(context.client.databases.create, name))


def delete_databases(context: Context, names: list[str]):
    for name in names:
        context.client.databases.get(name).delete()


@step("connection delete database: {name}")
def step_impl(context: Context, name: str):
    delete_databases(context, [name])


@step("connection delete databases")
def step_impl(context: Context):
    delete_databases(context, names=parse_list(context.table))


def delete_databases_throws_exception(context: Context, names: list[str]):
    for name in names:
        try:
            context.client.databases.get(name).delete()
            assert False
        except TypeDBClientException:
            pass


@step("connection delete database; throws exception: {name}")
def step_impl(context: Context, name: str):
    delete_databases_throws_exception(context, [name])


@step("connection delete databases; throws exception")
def step_impl(context: Context):
    delete_databases_throws_exception(context, names=parse_list(context.table))


@step("connection delete databases in parallel")
def step_impl(context: Context):
    names = parse_list(context.table)
    assert_that(len(names), is_(less_than_or_equal_to(context.THREAD_POOL_SIZE)))
    with ThreadPoolExecutor(max_workers=context.THREAD_POOL_SIZE) as executor:
        for name in names:
            executor.submit(partial(context.client.databases.get(name).delete))


def has_databases(context: Context, names: list[str]):
    assert_collections_equal([db.name for db in context.client.databases.all()], names)


@step("connection has database: {name}")
def step_impl(context: Context, name: str):
    has_databases(context, [name])


@step("connection has databases")
def step_impl(context: Context):
    has_databases(context, names=parse_list(context.table))


def does_not_have_databases(context: Context, names: list[str]):
    databases = [db.name for db in context.client.databases.all()]
    for name in names:
        assert_that(name, not_(is_in(databases)))


@step("connection does not have database: {name}")
def step_impl(context: Context, name: str):
    does_not_have_databases(context, [name])


@step("connection does not have databases")
def step_impl(context: Context):
    does_not_have_databases(context, names=parse_list(context.table))
