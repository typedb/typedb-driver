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

from typing import Iterator, Optional, TYPE_CHECKING

from typedb.api.answer.concept_row import ConceptRow
from typedb.api.answer.query_type import QueryType
from typedb.common.exception import TypeDBDriverException, ILLEGAL_STATE, NULL_NATIVE_OBJECT
from typedb.common.iterator_wrapper import IteratorWrapper
from typedb.common.native_wrapper import NativeWrapper
from typedb.common.validation import require_non_null, require_non_negative
from typedb.concept import concept_factory
from typedb.native_driver_wrapper import (string_iterator_next, concept_iterator_next, concept_row_get,
                                          concept_row_get_index, concept_row_to_string, concept_row_equals,
                                          concept_row_get_column_names, concept_row_get_query_type,
                                          concept_row_get_concepts, ConceptRow as NativeConceptRow,
                                          TypeDBDriverExceptionNative)

if TYPE_CHECKING:
    from typedb.api.concept.concept import Concept


class _ConceptRow(ConceptRow, NativeWrapper[NativeConceptRow]):

    def __init__(self, concept_row: NativeConceptRow):
        if not concept_row:
            raise TypeDBDriverException(NULL_NATIVE_OBJECT)
        super().__init__(concept_row)

    @property
    def _native_object_not_owned_exception(self) -> TypeDBDriverException:
        return TypeDBDriverException(ILLEGAL_STATE)

    def column_names(self) -> Iterator[str]:
        return IteratorWrapper(concept_row_get_column_names(self.native_object), string_iterator_next)

    @property
    def query_type(self) -> QueryType:
        return QueryType(concept_row_get_query_type(self.native_object))

    def concepts(self) -> Iterator[Concept]:
        return map(concept_factory.wrap_concept, IteratorWrapper(concept_row_get_concepts(self.native_object),
                                                                 concept_iterator_next))

    def get(self, column_name: str) -> Optional[Concept]:
        require_non_null(column_name, "column_name")
        try:
            concept = concept_row_get(self.native_object, column_name)
        except TypeDBDriverExceptionNative as e:
            raise TypeDBDriverException.of(e) from None
        return concept_factory.wrap_concept(concept) if concept else None

    def get_index(self, column_index: int) -> Optional[Concept]:
        require_non_negative(column_index, "column_index")
        try:
            concept = concept_row_get_index(self.native_object, column_index)
        except TypeDBDriverExceptionNative as e:
            raise TypeDBDriverException.of(e) from None
        return concept_factory.wrap_concept(concept) if concept else None

    def __repr__(self):
        return concept_row_to_string(self.native_object)

    def __eq__(self, other):
        if other is self:
            return True
        if other is None or not isinstance(other, self.__class__):
            return False
        return concept_row_equals(self.native_object, other.native_object)

    def __hash__(self):
        return hash((tuple(self.variables()), tuple(self.concepts())))
