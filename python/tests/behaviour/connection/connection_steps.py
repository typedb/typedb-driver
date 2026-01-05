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

from behave import *
from hamcrest import *
from tests.behaviour.config.parameters import MayError, check_is_none
from tests.behaviour.context import Context


def replace_host(address: str, new_host: str) -> str:
    return address.replace("127.0.0.1", new_host)


def replace_port(address: str, new_port: str) -> str:
    address_parts = address.rsplit(":", 1)
    address_parts[-1] = new_port
    return "".join(address_parts)


@step(u'typedb has configuration')
def step_impl(context: Context):
    # TODO: implement configuring the TypeDB runner when a python typedb-runner is available
    pass


@step(u'typedb starts')
def step_impl(context: Context):
    # TODO: start TypeDB via a python typedb-runner once one is available
    pass


@step(u'connection opens with default authentication')
def step_impl(context: Context):
    context.setup_context_driver_fn()


@step(u'connection opens with a wrong host{may_error:MayError}')
def step_impl(context: Context, may_error: MayError):
    address = context.default_address
    if isinstance(address, str):
        address = replace_host(address, "surely-not-localhost")
    elif isinstance(address, list):
        address = [replace_host(addr, "surely-not-localhost") for addr in address]
    else:
        raise Exception("Unexpected default address: cannot finish the test")
    may_error.check(lambda: context.setup_context_driver_fn(address=address))


@step(u'connection opens with a wrong port{may_error:MayError}')
def step_impl(context: Context, may_error: MayError):
    address = context.default_address
    if isinstance(address, str):
        address = replace_port(address, "0")
    elif isinstance(address, list):
        address = [replace_port(addr, "0") for addr in address]
    else:
        raise Exception("Unexpected default address: cannot finish the test")
    may_error.check(lambda: context.setup_context_driver_fn(address=address))


@step(
    u"connection opens with username '{username:NonSemicolon}', password '{password:NonSemicolon}'{may_error:MayError}")
def step_impl(context: Context, username: str, password: str, may_error: MayError):
    may_error.check(lambda: context.setup_context_driver_fn(username=username, password=password))


@step(u'connection closes')
def step_impl(context: Context):
    context.driver.close()


@step(u'typedb stops')
def step_impl(context: Context):
    # TODO: stop TypeDB via a python typedb-runner once one is available
    pass


@step("connection is open: {is_open:Bool}")
def step_impl(context: Context, is_open: bool):
    real_is_open = hasattr(context, 'driver') and context.driver and context.driver.is_open()
    assert_that(real_is_open, equal_to(is_open))


@step(u'connection contains distribution{may_error:MayError}')
def step_impl(context: Context, may_error: MayError):
    may_error.check(lambda: assert_that(len(context.driver.server_version().distribution), greater_than(0)))


@step(u'connection contains version{may_error:MayError}')
def step_impl(context: Context, may_error: MayError):
    may_error.check(lambda: assert_that(len(context.driver.server_version().version), greater_than(0)))


@step("connection has {count:Int} replica")
@step("connection has {count:Int} replicas")
def step_impl(context: Context, count: int):
    assert_that(len(context.driver.replicas()), equal_to(count))


@step(u'connection contains primary replica')
def step_impl(context: Context):
    check_is_none(context.driver.primary_replica(), False)


@step("connection has {count:Int} database")
@step("connection has {count:Int} databases")
def step_impl(context: Context, count: int):
    assert_that(len(context.driver.databases.all()), equal_to(count))


@step("connection has {count:Int} user")
@step("connection has {count:Int} users")
def step_impl(context: Context, count: int):
    assert_that(len(context.driver.users.all()), equal_to(count))


@step("set driver option use_replication to: {value:Bool}")
def step_impl(context: Context, value: bool):
    context.driver_options.use_replication = value


@step("set driver option primary_failover_retries to: {value:Int}")
def step_impl(context: Context, value: int):
    context.driver_options.primary_failover_retries = value


@step("set driver option replica_discovery_attempts to: {value:Int}")
def step_impl(context: Context, value: int):
    context.driver_options.replica_discovery_attempts = value
