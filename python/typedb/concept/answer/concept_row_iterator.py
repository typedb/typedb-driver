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

from typing import Iterator, TYPE_CHECKING

from typedb.native_driver_wrapper import concept_row_get_variables, string_iterator_next, concept_row_get_values, \
    concept_iterator_next, concept_row_get, concept_row_get_explainables, concept_row_to_string, concept_row_equals, \
    explainables_get_relation, explainables_get_attribute, explainables_get_ownership, \
    explainables_get_relations_keys, explainables_get_attributes_keys, explainables_get_ownerships_keys, \
    string_pair_iterator_next, explainables_to_string, explainables_equals, explainable_get_conjunction, \
    explainable_get_id, ConceptRow as NativeConceptRow

from typedb.api.answer.concept_row import ConceptRow
from typedb.common.exception import TypeDBDriverException, ILLEGAL_STATE, MISSING_VARIABLE, \
    NONEXISTENT_EXPLAINABLE_CONCEPT, NONEXISTENT_EXPLAINABLE_OWNERSHIP, NULL_NATIVE_OBJECT, VARIABLE_DOES_NOT_EXIST
from typedb.common.iterator_wrapper import IteratorWrapper
from typedb.common.native_wrapper import NativeWrapper
from typedb.concept import concept_factory

if TYPE_CHECKING:
    from typedb.native_driver_wrapper import QueryAnswer as NativeQueryAnswer


class _ConceptRowIterator(_QueryAnswer, ConceptRowIterator):
    def __init__(self, query_answer: NativeQueryAnswer):
        super().__init__(query_answer)
        self.iterator = map(_ConceptRow, IteratorWrapper(self.native_object.into_rows(), concept_row_iterator_next))

    def __iter__(self):
        return iter(self.iterator)

    def __next__(self):
        return next(self.iterator)
