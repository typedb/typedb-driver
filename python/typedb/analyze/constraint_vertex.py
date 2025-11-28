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

from typedb.api.analyze.constraint_vertex import ConstraintVertex

from typedb.analyze.variable import _Variable

from typedb.analyze.named_role import _NamedRole
from typedb.common.native_wrapper import NativeWrapper
from typedb.common.exception import TypeDBDriverException, ILLEGAL_STATE
from typedb.concept import concept_factory

from typedb.native_driver_wrapper import (
    ConstraintVertex as NativeConstraintVertex,
    VariableVertex, LabelVertex, ValueVertex, NamedRoleVertex,
    constraint_vertex_variant,
    constraint_vertex_as_variable,
    constraint_vertex_as_label,
    constraint_vertex_as_value,
    constraint_vertex_as_named_role,
)

if TYPE_CHECKING:
    import typedb
    from typedb.api.analyze import Variable


class _ConstraintVertex(ConstraintVertex, NativeWrapper[NativeConstraintVertex]):
    @property
    def _native_object_not_owned_exception(self) -> TypeDBDriverException:
        return TypeDBDriverException(ILLEGAL_STATE)

    def variant(self):
        return constraint_vertex_variant(self.native_object)

    def is_variable(self) -> bool:
        return self.variant() == VariableVertex

    def is_label(self) -> bool:
        return self.variant() == LabelVertex

    def is_value(self) -> bool:
        return self.variant() == ValueVertex

    def is_named_role(self) -> bool:
        return self.variant() == NamedRoleVertex

    def as_variable(self) -> Variable:
        return _Variable(constraint_vertex_as_variable(self.native_object))

    def as_label(self) -> "typedb.api.concept.type.type.Type":
        return concept_factory.wrap_concept(constraint_vertex_as_label(self.native_object))

    def as_value(self) -> "typedb.api.concept.value.value.Value":
        return concept_factory.wrap_concept(constraint_vertex_as_value(self.native_object)).as_value()

    def as_named_role(self) -> "typedb.api.analyze.named_role.NamedRole":
        return _NamedRole(constraint_vertex_as_named_role(self.native_object))

    def _unwrap(self):
        if self.is_variable():
            return self.as_variable()
        elif self.is_label():
            return self.as_label()
        elif self.is_value():
            return self.as_value()
        elif self.is_named_role():
            return self.as_named_role()
        else:
            raise TypeDBDriverException(ILLEGAL_STATE)

    def __repr__(self):
        return self._unwrap().__repr__()

    def __eq__(self, other):
        if other is self:
            return True
        if other is None or not isinstance(other, self.__class__):
            return False
        return self._unwrap() == other._unwrap()

    def __hash__(self):
        return hash(self._unwrap())
