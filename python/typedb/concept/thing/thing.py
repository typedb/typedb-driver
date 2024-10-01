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

from __future__ import annotations

from abc import ABC
from typing import TYPE_CHECKING, Iterator, Optional

from typedb.api.concept.thing.thing import Thing
from typedb.common.exception import TypeDBDriverException, GET_HAS_WITH_MULTIPLE_FILTERS
from typedb.common.iterator_wrapper import IteratorWrapper
from typedb.common.promise import Promise
from typedb.concept.concept import _Concept
from typedb.concept.concept_factory import wrap_attribute, wrap_relation, wrap_role_type


class _Thing(Thing, _Concept, ABC):
    pass
