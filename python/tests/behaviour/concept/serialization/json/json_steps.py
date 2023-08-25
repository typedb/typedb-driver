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

import json
from collections import Counter
from hamcrest.core.base_matcher import BaseMatcher
from typing import TypeVar, Generic

from behave import *
from hamcrest import *

from tests.behaviour.context import Context

T = TypeVar('T')


class UnorderedEqualTo(BaseMatcher, Generic[T]):
    def __init__(self, expected: list[T]):
        self.expected = Counter([json.dumps(item, sort_keys=True) for item in expected])

    def _matches(self, actual: list[T]) -> bool:
        actual = Counter([json.dumps(item, sort_keys=True) for item in actual])
        return actual == self.expected

    def describe_to(self, description):
        description.append_text('is equal, in any order, to ').append_text(repr(self.expected))


def unordered_equal_to(expected: list[T]) -> UnorderedEqualTo[T]:
    return UnorderedEqualTo(expected)


@step("JSON serialization of answers matches")
def step_impl(context: Context):
    expected = json.loads(context.text)
    actual = [answer.to_json() for answer in context.answers]
    assert_that(actual, unordered_equal_to(expected))
