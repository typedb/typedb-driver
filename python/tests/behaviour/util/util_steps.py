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

import os
import time
from time import sleep

from behave import *
from hamcrest import *
from tests.behaviour.context import Context
from tests.behaviour.util.util import write_file, is_file_empty


@step("set time-zone: {time_zone_name}")
def step_impl(context: Context, time_zone_name: str):
    os.environ["TZ"] = time_zone_name
    time.tzset()


@step("wait {seconds} seconds")
def step_impl(context: Context, seconds: str):
    sleep(float(seconds))


@step("file({name:NonSemicolon}) {exists_or_doesnt:ExistsOrDoesnt}")
def file_exists(context, name: str, exists_or_doesnt: bool):
    assert_that(context.full_path(name).exists(), is_(exists_or_doesnt))


@step("file({name:NonSemicolon}) {is_or_not:IsOrNot} empty")
def file_is_empty(context, name: str, is_or_not: bool):
    assert_that(is_file_empty(context.full_path(name)), is_(is_or_not))


@step("file({name:NonSemicolon}) write")
def file_write(context, name: str):
    write_file(context.full_path(name), context.text.strip())
