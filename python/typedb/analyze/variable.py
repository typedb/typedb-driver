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

from typing import TYPE_CHECKING

from typedb.common.exception import TypeDBDriverException, ILLEGAL_STATE
from typedb.common.native_wrapper import NativeWrapper

from typedb.api.analyze.variable import Variable

from typedb.native_driver_wrapper import (
    Variable as NativeVariable,
    variable_id_as_u32,
    variable_string_repr,
)

if TYPE_CHECKING:
    pass


class _Variable(Variable, NativeWrapper[NativeVariable]):
    @property
    def _native_object_not_owned_exception(self) -> TypeDBDriverException:
        return TypeDBDriverException(ILLEGAL_STATE)

    def _id(self) -> int:
        return variable_id_as_u32(self.native_object)

    def __hash__(self) -> int:
        return hash(self._id())

    def __eq__(self, other: object) -> bool:
        if other is self:
            return True
        if other is None or not isinstance(other, self.__class__):
            return False
        return self._id() == other._id()

    def __repr__(self) -> str:
        return variable_string_repr(self.native_object)
