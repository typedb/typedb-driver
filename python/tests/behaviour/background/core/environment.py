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

from typedb.client import *

from tests.behaviour.background import environment_base
from tests.behaviour.context import Context

IGNORE_TAGS = ["ignore", "ignore-client-python", "ignore-typedb-client-python"]


def before_all(context: Context):
    environment_base.before_all(context)
    context.setup_context_client_fn = lambda user=None, password=None: setup_context_client(context, user, password)


def before_scenario(context: Context, scenario):
    for tag in IGNORE_TAGS:
        if tag in scenario.effective_tags:
            scenario.skip("tagged with @" + tag)
            return
    environment_base.before_scenario(context)


def setup_context_client(context, username=None, password=None):
    if username is not None or password is not None:
        raise Exception("Core client does not support authentication")
    context.client = TypeDB.core_client(address="localhost:%d" % int(context.config.userdata["port"]))
    context.session_options = TypeDBOptions(infer=True)
    context.transaction_options = TypeDBOptions(infer=True)


def after_scenario(context: Context, scenario):
    environment_base.after_scenario(context, scenario)

    # TODO: reset the database through the TypeDB runner once it exists
    context.setup_context_client_fn()
    for database in context.client.databases.all():
        database.delete()
    context.client.close()


def after_all(context: Context):
    environment_base.after_all(context)
