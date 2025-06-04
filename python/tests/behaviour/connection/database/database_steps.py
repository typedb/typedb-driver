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

import uuid
from concurrent.futures.thread import ThreadPoolExecutor
from functools import partial

from behave import *
from hamcrest import *
from tests.behaviour.config.parameters import MayError, parse_list
from tests.behaviour.context import Context
from tests.behaviour.util.util import read_file_to_string, remove_two_spaces_in_tabulation
from typedb.api.connection.transaction import TransactionType
from typedb.driver import *


def create_databases(driver: Driver, names: list[str]):
    for name in names:
        driver.databases.create(name)


def delete_databases(driver: Driver, names: list[str]):
    for name in names:
        driver.databases.get(name).delete()


def has_databases(context: Context, names: list[str]):
    all_database_names = [db.name for db in context.driver.databases.all()]
    for name in names:
        assert_that(all_database_names, has_item(name))


def execute_and_retrieve_schema_for_comparison(context: Context, schema_query: str) -> str:
    temp_database_name = create_temporary_database_with_schema(context, schema_query)
    return context.driver.databases.get(temp_database_name).schema()


def create_temporary_database_with_schema(context: Context, schema_query: str) -> str:
    name = f"temp-{uuid.uuid4()}"
    create_databases(context.driver, [name])
    transaction = context.driver.transaction(name, TransactionType.SCHEMA)
    transaction.query(schema_query).resolve()
    transaction.commit()
    return name


def import_database(context: Context, name: str, schema: str, data_file: str, may_error: MayError):
    may_error.check(lambda: context.driver.databases.import_file(name, schema, str(context.full_path(data_file))))


@step("connection create database: {name:NonSemicolon}{may_error:MayError}")
def step_impl(context: Context, name: str, may_error: MayError):
    may_error.check(lambda: create_databases(context.driver, [name]))


@step("connection create database with empty name{may_error:MayError}")
def step_impl(context: Context, may_error: MayError):
    may_error.check(lambda: create_databases(context.driver, [""]))


@step("connection create databases")
def step_impl(context: Context):
    names = parse_list(context)
    create_databases(context.driver, names)


@step("connection create databases in parallel")
def step_impl(context: Context):
    names = parse_list(context)
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
    delete_databases(context.driver, names=parse_list(context))


@step("connection delete databases in parallel")
def step_impl(context: Context):
    names = parse_list(context)
    assert_that(len(names), is_(less_than_or_equal_to(context.THREAD_POOL_SIZE)))
    with ThreadPoolExecutor(max_workers=context.THREAD_POOL_SIZE) as executor:
        for name in names:
            executor.submit(partial(context.driver.databases.get(name).delete))


@step("in background, connection delete database: {name:NonSemicolon}{may_error:MayError}")
def step_impl(context: Context, name: str, may_error: MayError):
    background = context.create_driver_fn()
    may_error.check(lambda: delete_databases(background, [name]))
    background.close()


@step("connection has database: {name:NonSemicolon}")
def step_impl(context: Context, name: str):
    has_databases(context, [name])


@step("connection has databases")
def step_impl(context: Context):
    has_databases(context, names=parse_list(context))


def does_not_have_databases(context: Context, names: list[str]):
    databases = [db.name for db in context.driver.databases.all()]
    for name in names:
        assert_that(name, not_(is_in(databases)))


@step("connection does not have database: {name:NonSemicolon}")
def step_impl(context: Context, name: str):
    does_not_have_databases(context, [name])


@step("connection does not have databases")
def step_impl(context: Context):
    does_not_have_databases(context, names=parse_list(context))


@step("connection get database({name:NonSemicolon}) has schema")
def step_impl(context: Context, name: str):
    expected_schema = context.text.strip()
    if expected_schema:
        expected_schema_retrieved = execute_and_retrieve_schema_for_comparison(context, expected_schema)
    else:
        expected_schema_retrieved = ""
    real_schema = context.driver.databases.get(name).schema()
    assert_that(real_schema, is_(expected_schema_retrieved))


@step("connection get database({name:NonSemicolon}) has type schema")
def step_impl(context: Context, name: str):
    expected_type_schema = context.text.strip()
    if expected_type_schema:
        temp_database_name = create_temporary_database_with_schema(context, expected_type_schema)
        expected_type_schema_retrieved = context.driver.databases.get(temp_database_name).type_schema()
    else:
        expected_type_schema_retrieved = ""
    real_type_schema = context.driver.databases.get(name).type_schema()
    assert_that(real_type_schema, is_(expected_type_schema_retrieved))


@step("file({file_name:NonSemicolon}) has schema")
def file_has_schema(context: Context, file_name: str):
    file_schema = read_file_to_string(context.full_path(file_name))
    expected_schema = context.text.strip()
    if expected_schema:
        expected_schema_retrieved = execute_and_retrieve_schema_for_comparison(context, remove_two_spaces_in_tabulation(
            expected_schema))
    else:
        expected_schema_retrieved = ""
    file_schema_retrieved = execute_and_retrieve_schema_for_comparison(context, file_schema)
    assert_that(file_schema_retrieved, is_(expected_schema_retrieved))


@step(
    "connection import database({name:NonSemicolon}) from schema file({schema_file:NonSemicolon}), data file({data_file:NonSemicolon}){may_error:MayError}")
def import_from_schema_file_data_file(context: Context, name: str, schema_file: str, data_file: str,
                                      may_error: MayError):
    schema = read_file_to_string(context.full_path(schema_file))
    import_database(context, name, schema, data_file, may_error)


@step(
    "connection import database({name:NonSemicolon}) from data file({data_file:NonSemicolon}) and schema{may_error:MayError}")
def import_from_data_file_and_schema(context: Context, name: str, data_file: str, may_error: MayError):
    schema = remove_two_spaces_in_tabulation(context.text.strip())
    import_database(context, name, schema, data_file, may_error)


@step(
    "connection get database({name:NonSemicolon}) export to schema file({schema_file:NonSemicolon}), data file({data_file:NonSemicolon}){may_error:MayError}")
def export_to_schema_file_data_file(context: Context, name: str, schema_file: str, data_file: str, may_error: MayError):
    may_error.check(lambda: context.driver.databases.get(name).export_file(
        str(context.full_path(schema_file)), str(context.full_path(data_file))
    ))
