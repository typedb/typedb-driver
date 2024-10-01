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

from typing import TYPE_CHECKING, Optional

from typedb.api.answer.value_group import ValueGroup
from typedb.common.exception import TypeDBDriverException, NULL_NATIVE_OBJECT, ILLEGAL_STATE
from typedb.common.native_wrapper import NativeWrapper
from typedb.concept import concept_factory
from typedb.concept.value.value import _Value
from typedb.native_driver_wrapper import value_group_get_owner, value_group_get_value, \
    value_group_to_string, value_group_equals, ValueGroup as NativeValueGroup

if TYPE_CHECKING:
    from typedb.api.concept.concept import Concept
    from typedb.api.concept.value.value import Value


class _ValueGroup(ValueGroup, NativeWrapper[NativeValueGroup]):

    def __init__(self, value_group: NativeValueGroup):
        if not value_group:
            raise TypeDBDriverException(NULL_NATIVE_OBJECT)
        super().__init__(value_group)

    @property
    def _native_object_not_owned_exception(self) -> TypeDBDriverException:
        return TypeDBDriverException(ILLEGAL_STATE)

    def owner(self) -> Concept:
        return concept_factory.wrap_concept(value_group_get_owner(self.native_object))

    def value(self) -> Optional[Value]:
        if native_value := value_group_get_value(self.native_object):
            return _Value(native_value)
        else:
            return None

    def __repr__(self):
        return value_group_to_string(self.native_object)

    def __eq__(self, other):
        return other and isinstance(other, ValueGroup) and \
            value_group_equals(self.native_object, self.native_object)

    def __hash__(self):
        return hash((self.owner(), self.value()))
