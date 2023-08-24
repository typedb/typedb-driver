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

from behave import *
from typedb.common.exception import TypeDBClientException

from tests.behaviour.context import Context


@step(u'typedb has configuration')
def step_impl(context):
    # TODO: implement configuring the TypeDB runner when a python typedb-runner is available
    pass


@step(u'typedb starts')
def step_impl(context):
    # TODO: start TypeDB via a python typedb-runner once one is available
    pass


@step(u'connection opens with default authentication')
def step_impl(context):
    context.setup_context_client_fn()
    for database in context.client.databases.all():
        database.delete()


@step(u'connection opens with authentication: {username:Words}, {password:Words}')
def step_impl(context, username: str, password: str):
    context.setup_context_client_fn(username, password)


@step(u'connection opens with authentication: {username:Words}, {password:Words}; throws exception')
def step_impl(context, username: str, password: str):
    try:
        context.setup_context_client_fn(username, password)
        assert False
    except TypeDBClientException:
        pass


@step(u'connection closes')
def step_impl(context):
    context.client.close()


@step(u'typedb stops')
def step_impl(context):
    # TODO: stop TypeDB via a python typedb-runner once one is available
    pass


@step("connection has been opened")
def step_impl(context: Context):
    assert context.client and context.client.is_open()


@step("connection does not have any database")
def step_impl(context: Context):
    assert len(context.client.databases.all()) == 0
