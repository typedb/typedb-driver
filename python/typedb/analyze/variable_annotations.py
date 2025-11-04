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

from typing import TYPE_CHECKING, Iterator

from typedb.common.iterator_wrapper import IteratorWrapper
from typedb.common.native_wrapper import NativeWrapper
from typedb.common.exception import TypeDBDriverException, ILLEGAL_STATE, INVALID_VARIABLE_ANNOTATIONS_CASTING
from typedb.concept.concept_factory import wrap_concept

from typedb.analyze.variants import VariableAnnotationsVariant
from typedb.api.analyze.variable_annotations import VariableAnnotations

from typedb.native_driver_wrapper import (
    VariableAnnotations as NativeVariableAnnotations,
    InstanceAnnotations, TypeAnnotations, ValueAnnotations,
    variable_annotations_variant,
    variable_annotations_instance,
    variable_annotations_type,
    variable_annotations_value,
    string_iterator_next,
    concept_iterator_next,
)

if TYPE_CHECKING:
    from typedb.api.concept.type.type import Type


class _VariableAnnotations(VariableAnnotations, NativeWrapper[NativeVariableAnnotations]):
    @property
    def _native_object_not_owned_exception(self) -> TypeDBDriverException:
        return TypeDBDriverException(ILLEGAL_STATE)

    def variant(self):
        return variable_annotations_variant(self.native_object)

    def is_instance(self) -> bool:
        return self.variant() == InstanceAnnotations

    def is_type(self) -> bool:
        return self.variant() == TypeAnnotations

    def is_value(self) -> bool:
        return self.variant() == ValueAnnotations

    def as_instance(self) -> Iterator["Type"]:
        if not self.is_instance():
            raise TypeDBDriverException(
                INVALID_VARIABLE_ANNOTATIONS_CASTING, (self.variant(), VariableAnnotationsVariant.InstanceAnnotations)
            )
        native_iter = variable_annotations_instance(self.native_object)
        return map(lambda x: wrap_concept(x).as_type(), IteratorWrapper(native_iter, concept_iterator_next))

    def as_type(self) -> Iterator["Type"]:
        if not self.is_type():
            raise TypeDBDriverException(
                INVALID_VARIABLE_ANNOTATIONS_CASTING, (self.variant(), VariableAnnotationsVariant.TypeAnnotations)
            )
        native_iter = variable_annotations_type(self.native_object)
        return map(lambda x: wrap_concept(x).as_type(), IteratorWrapper(native_iter, concept_iterator_next))

    def as_value(self) -> Iterator[str]:
        if not self.is_value():
            raise TypeDBDriverException(
                INVALID_VARIABLE_ANNOTATIONS_CASTING, (self.variant(), VariableAnnotationsVariant.ValueAnnotations)
            )
        native_iter = variable_annotations_value(self.native_object)
        return IteratorWrapper(native_iter, string_iterator_next)
