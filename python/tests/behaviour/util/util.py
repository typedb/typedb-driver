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
from collections import Counter

from hamcrest import *


def assert_collections_equal(collection1: list, collection2: list):
    assert_that(Counter(collection1), is_(equal_to(Counter(collection2))))


def json_matches(lhs, rhs) -> bool:
    if type(lhs) is dict:
        if type(rhs) is not dict or len(lhs) != len(rhs):
            return False
        for key, lhs_value in lhs.items():
            if key not in rhs:
                return False
            if not json_matches(lhs_value, rhs[key]):
                return False
        return True
    elif type(lhs) is list:
        if type(rhs) is not list or len(lhs) != len(rhs):
            return False
        rhs_matches = set()
        for lhs_item in lhs:
            for i, rhs_item in enumerate(rhs):
                if i in rhs_matches:
                    continue
                if json_matches(lhs_item, rhs_item):
                    rhs_matches.add(i)
                    break
        return len(rhs_matches) == len(rhs)
    else:
        return lhs == rhs
