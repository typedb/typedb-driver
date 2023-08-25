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

from time import sleep

from behave import *

from tests.behaviour.context import Context

import os
import time


@step("set time-zone is: {time_zone_label}")
def step_impl(context: Context, time_zone_label: str):
    os.environ["TZ"] = time_zone_label
    time.tzset()


@step("wait {seconds} seconds")
def step_impl(context: Context, seconds: str):
    sleep(float(seconds))
