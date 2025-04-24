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

# TODO: Reuse code from the community environment when needed, update for multiple clusters
IGNORE_TAGS = ["ignore", "ignore-typedb-driver", "ignore-typedb-driver-python"]


def before_all(context: Context):
    environment_base.before_all(context)
    # context.credentials_root_ca_path = os.environ["ROOT_CA"] # TODO: test root ca with cluster
    context.create_driver_fn = lambda host="localhost", port=None, user=None, password=None: \
        create_driver(context, host, port, user, password)
    context.setup_context_driver_fn = lambda host="localhost", port=None, username=None, password=None: \
        setup_context_driver(context, host, port, username, password)


def before_scenario(context: Context, scenario):
    for tag in IGNORE_TAGS:
        if tag in scenario.effective_tags:
            scenario.skip("tagged with @" + tag)
            return
    environment_base.before_scenario(context)


def setup_context_driver(context, host="localhost", port=None, username=None, password=None):
    # TODO: Use root_ca_path in connection
    context.driver = create_driver(context, host, port, username, password)


def create_driver(context, host="localhost", port=None, username=None, password=None) -> Driver:
    if port is None:
        port = int(context.config.userdata["port"])
    if username is None:
        username = "admin"
    if password is None:
        password = "password"
    credentials = Credentials(username, password)
    return TypeDB.driver(address=f"{host}:{port}", credentials=credentials, driver_options=DriverOptions())


def after_scenario(context: Context, scenario):
    environment_base.after_scenario(context, scenario)

    # TODO: reset the database through the TypeDB runner once it exists
    context.setup_context_driver_fn()
    for database in context.driver.databases.all():
        database.delete()
    for user in context.driver.users.all():
        if user.name == "admin":
            continue
        context.driver.users.get(user.name).delete()
    context.driver.close()


def after_all(context: Context):
    environment_base.after_all(context)
