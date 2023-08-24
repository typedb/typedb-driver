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

from __future__ import annotations

from typing import TYPE_CHECKING

from typedb.native_client_wrapper import numeric_group_get_owner, numeric_group_get_numeric, \
    numeric_group_to_string, numeric_group_equals, NumericGroup as NativeNumericGroup

from typedb.api.answer.numeric_group import NumericGroup
from typedb.common.exception import TypeDBClientExceptionExt, NULL_NATIVE_OBJECT, ILLEGAL_STATE
from typedb.common.native_wrapper import NativeWrapper
from typedb.concept import concept_factory
from typedb.concept.answer.numeric import _Numeric

if TYPE_CHECKING:
    from typedb.api.answer.numeric import Numeric
    from typedb.api.concept.concept import Concept


class _NumericGroup(NumericGroup, NativeWrapper[NativeNumericGroup]):

    def __init__(self, numeric_group: NativeNumericGroup):
        if not numeric_group:
            raise TypeDBClientExceptionExt(NULL_NATIVE_OBJECT)
        super().__init__(numeric_group)

    @property
    def _native_object_not_owned_exception(self) -> TypeDBClientExceptionExt:
        return TypeDBClientExceptionExt.of(ILLEGAL_STATE)

    def owner(self) -> Concept:
        return concept_factory.wrap_concept(numeric_group_get_owner(self.native_object))

    def numeric(self) -> Numeric:
        return _Numeric(numeric_group_get_numeric(self.native_object))

    def __repr__(self):
        return numeric_group_to_string(self.native_object)

    def __eq__(self, other):
        return other and isinstance(other, NumericGroup) and \
            numeric_group_equals(self.native_object, self.native_object)

    def __hash__(self):
        return hash((self.owner(), self.numeric()))
