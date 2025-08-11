# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
#   Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.

from behave import *
from hamcrest import *
from tests.behaviour.config.parameters import MayError, parse_list
from tests.behaviour.context import Context
from tests.behaviour.util.util import assert_collections_equal
from typedb.driver import *


@step("get all users")
def step_impl(context: Context):
    assert_collections_equal([db.name for db in context.driver.users.all()], parse_list(context))


@step("get all users{may_error:MayError}")
def step_impl(context: Context, may_error: MayError):
    may_error.check(lambda: context.driver.users.all())


@step("get all users {contains_or_doesnt:ContainsOrDoesnt}: {username:NonSemicolon}")
def step_impl(context: Context, contains_or_doesnt: bool, username: str):
    names = [u.name for u in context.driver.users.all()]
    if contains_or_doesnt:
        assert_that(names, has_item(username))
    else:
        assert_that(names, not_(has_item(username)))


@step("get user: {username:NonSemicolon}{may_error:MayError}")
def step_impl(context: Context, username: str, may_error: MayError):
    may_error.check(lambda: context.driver.users.get(username))


@step("get user({user:NonSemicolon}) get name: {name:NonSemicolon}")
def step_impl(context: Context, user: str, name: str):
    assert_that(context.driver.users.get(user).name, is_(equal_to(name)))


@step("create user with username '{username:NonSemicolon}', password '{password:NonSemicolon}'{may_error:MayError}")
def step_impl(context: Context, username: str, password: str, may_error: MayError):
    may_error.check(lambda: context.driver.users.create(username, password))


@step(
    "get user({username:NonSemicolon}) update password to '{password:NonSemicolon}'{may_error:MayError}")
def step_impl(context: Context, username: str, password: str, may_error: MayError):
    may_error.check(lambda: context.driver.users.get(username).update_password(password))


@step("delete user: {username:NonSemicolon}{may_error:MayError}")
def step_impl(context: Context, username: str, may_error: MayError):
    may_error.check(lambda: context.driver.users.get(username).delete())


@step("get current username: {username:NonSemicolon}")
def step_impl(context: Context, username: str):
    assert_that(context.driver.users.get_current().name, is_(equal_to(username)))
