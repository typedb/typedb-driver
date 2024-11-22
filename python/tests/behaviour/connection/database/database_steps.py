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

from behave import *
from hamcrest import *
from tests.behaviour.config.parameters import MayError, parse_list
from tests.behaviour.context import Context
from tests.behaviour.util.util import assert_collections_equal
from typedb.driver import *


def create_databases(driver: Driver, names: list[str]):
    for name in names:
        driver.databases.create(name)


def delete_databases(driver: Driver, names: list[str]):
    for name in names:
        driver.databases.get(name).delete()


@step("connection create database: {name:NonSemicolon}{may_error:MayError}")
def step_impl(context: Context, name: str, may_error: MayError):
    may_error.check(lambda: create_databases(context.driver, [name]))


@step("connection create database with empty name{may_error:MayError}")
def step_impl(context: Context, may_error: MayError):
    may_error.check(lambda: create_databases(context.driver, [""]))


@step("connection create databases")
def step_impl(context: Context):
    names = parse_list(context.table)
    create_databases(context.driver, names)


@step("connection create databases in parallel")
def step_impl(context: Context):
    names = parse_list(context.table)
    assert_that(len(names), is_(less_than_or_equal_to(context.THREAD_POOL_SIZE)))
    with ThreadPoolExecutor(max_workers=context.THREAD_POOL_SIZE) as executor:
        for name in names:
            executor.submit(partial(context.driver.databases.create, name))


@step("in background, connection create database: {name:NonSemicolon}{may_error:MayError}")
def step_impl(context: Context, name: str, may_error: MayError):
    background = context.create_driver_fn()
    may_error.check(lambda: create_databases(background, [name]))
    background.close()


@step("connection delete database: {name:NonSemicolon}{may_error:MayError}")
def step_impl(context: Context, name: str, may_error: MayError):
    may_error.check(lambda: delete_databases(context.driver, [name]))


@step("connection delete databases")
def step_impl(context: Context):
    delete_databases(context.driver, names=parse_list(context.table))


@step("connection delete databases in parallel")
def step_impl(context: Context):
    names = parse_list(context.table)
    assert_that(len(names), is_(less_than_or_equal_to(context.THREAD_POOL_SIZE)))
    with ThreadPoolExecutor(max_workers=context.THREAD_POOL_SIZE) as executor:
        for name in names:
            executor.submit(partial(context.driver.databases.get(name).delete))


@step("in background, connection delete database: {name:NonSemicolon}{may_error:MayError}")
def step_impl(context: Context, name: str, may_error: MayError):
    background = context.create_driver_fn()
    may_error.check(lambda: delete_databases(background, [name]))
    background.close()


# TODO: Use "contains" instead. Cover "all" interface explicitly
def has_databases(context: Context, names: list[str]):
    assert_collections_equal([db.name for db in context.driver.databases.all()], names)


@step("connection has database: {name}")
def step_impl(context: Context, name: str):
    has_databases(context, [name])


@step("connection has databases")
def step_impl(context: Context):
    has_databases(context, names=parse_list(context.table))


def does_not_have_databases(context: Context, names: list[str]):
    databases = [db.name for db in context.driver.databases.all()]
    for name in names:
        assert_that(name, not_(is_in(databases)))


@step("connection does not have database: {name}")
def step_impl(context: Context, name: str):
    does_not_have_databases(context, [name])


@step("connection does not have databases")
def step_impl(context: Context):
    does_not_have_databases(context, names=parse_list(context.table))


@step("connection get database({name}) has schema")
def step_impl(context: Context, name: str):
    expected_schema = context.text
    real_schema = context.driver.databases.get(name).schema()
    assert_that(real_schema, is_(expected_schema))


@step("connection get database({name}) has type schema")
def step_impl(context: Context, name: str):
    expected_schema = context.text
    real_schema = context.driver.databases.get(name).type_schema()
    assert_that(real_schema, is_(expected_schema))
