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

from tests.behaviour.background import environment_base
from tests.behaviour.context import Context
from typedb.driver import *


def before_all(context: Context):
    environment_base.before_all(context)
    context.create_driver_fn = lambda address=None, user=None, password=None: \
        create_driver(context, address, user, password)
    context.setup_context_driver_fn = lambda address=None, username=None, password=None: \
        setup_context_driver(context, address, username, password)
    context.default_address = TypeDB.DEFAULT_ADDRESS


def before_scenario(context: Context, scenario):
    environment_base.before_scenario(context, scenario)


def setup_context_driver(context, address=None, username=None, password=None):
    context.driver = create_driver(context, address, username, password)


def create_driver(context, address=None, username=None, password=None) -> Driver:
    if address is None:
        address = context.default_address
    if username is None:
        username = context.DEFAULT_USERNAME
    if password is None:
        password = context.DEFAULT_PASSWORD
    credentials = Credentials(username, password)
    return TypeDB.driver(addresses=address, credentials=credentials, driver_options=context.driver_options)


def after_scenario(context: Context, scenario):
    environment_base.after_scenario(context, scenario)


def after_all(context: Context):
    environment_base.after_all(context)
