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

from tests.behaviour.config.parameters import ConsistencyLevel, parse_list
from typedb.api.server.replica_role import ReplicaRole


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


@step(u'connection opens to single server with default authentication')
def step_impl(context: Context):
    context.setup_context_driver_fn(address=[context.default_address[0]])


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


@step(u'connection primary replica exists')
def step_impl(context: Context):
    check_is_none(context.driver.primary_replica(), False)


@step(u'connection get replica({address}) {exists_or_doesnt:ExistsOrDoesnt}')
def step_impl(context: Context, address: str, exists_or_doesnt: bool):
    replicas = context.driver.replicas()
    exists = any(r.address == address for r in replicas)
    assert_that(exists, equal_to(exists_or_doesnt), f"replica {address}")


@step(u'connection get replica({address}) has term')
def step_impl(context: Context, address: str):
    replicas = context.driver.replicas()
    replica = next((r for r in replicas if r.address == address), None)
    check_is_none(replica, False)
    check_is_none(replica.term, False)


@step(u'connection replicas have roles')
def step_impl(context: Context):
    replicas = context.driver.replicas()
    expected_roles = parse_list(context)
    expected_primary_count = 0
    expected_secondary_count = 0
    expected_candidate_count = 0
    for role in expected_roles:
        if role == 'primary':
            expected_primary_count += 1
        elif role == 'secondary':
            expected_secondary_count += 1
        elif role == 'candidate':
            expected_candidate_count += 1
        else:
            raise ValueError(f"Unknown replica role: {role}")

    actual_primary_count = sum(1 for r in replicas if r.role.is_primary())
    actual_secondary_count = sum(1 for r in replicas if r.role.is_secondary())
    actual_candidate_count = sum(1 for r in replicas if r.role.is_candidate())

    assert_that(actual_primary_count, equal_to(expected_primary_count),
                f"Expected {expected_primary_count} primary replicas, found {actual_primary_count}")
    assert_that(actual_secondary_count, equal_to(expected_secondary_count),
                f"Expected {expected_secondary_count} secondary replicas, found {actual_secondary_count}")
    assert_that(actual_candidate_count, equal_to(expected_candidate_count),
                f"Expected {expected_candidate_count} candidate replicas, found {actual_candidate_count}")

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


@step("set database operation consistency to: {consistency_level:ConsistencyLevel}")
def step_impl(context: Context, consistency_level: ConsistencyLevel):
    context.database_operation_consistency = consistency_level
