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
import os

from typedb.driver import *

from tests.behaviour.background import environment_base
from tests.behaviour.context import Context

IGNORE_TAGS = ["ignore", "ignore-driver-python", "ignore-typedb-driver-python", "ignore-typedb-enterprise-driver-python"]


def before_all(context: Context):
    environment_base.before_all(context)
    context.credential_root_ca_path = os.environ["ROOT_CA"]
    context.setup_context_driver_fn = lambda user="admin", password="password": \
        setup_context_driver(context, user, password)


def before_scenario(context: Context, scenario):
    for tag in IGNORE_TAGS:
        if tag in scenario.effective_tags:
            scenario.skip("tagged with @" + tag)
            return
    environment_base.before_scenario(context)


def setup_context_driver(context, username, password):
    credential = TypeDBCredential(username, password, tls_root_ca_path=context.credential_root_ca_path)
    context.driver = TypeDB.enterprise_driver(addresses=["localhost:" + context.config.userdata["port"]],
                                           credential=credential)
    context.session_options = TypeDBOptions(infer=True)
    context.transaction_options = TypeDBOptions(infer=True)


def after_scenario(context: Context, scenario):
    environment_base.after_scenario(context, scenario)

    # TODO: reset the database through the TypeDB runner once it exists
    context.setup_context_driver_fn()
    for database in context.driver.databases.all():
        database.delete()

    for user in context.driver.users.all():
        if user.username() != "admin":
            context.driver.users.delete(user.username())
    context.driver.close()


def after_all(context: Context):
    environment_base.after_all(context)
