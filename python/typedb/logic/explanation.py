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

from typedb.native_client_wrapper import explanation_get_rule, explanation_get_conclusion, \
    explanation_get_mapped_variables, explanation_get_condition, string_iterator_next, \
    explanation_get_mapping, explanation_to_string, explanation_equals, Explanation as NativeExplanation

from typedb.api.logic.explanation import Explanation
from typedb.common.exception import TypeDBClientExceptionExt, ILLEGAL_STATE, MISSING_VARIABLE, NULL_NATIVE_OBJECT
from typedb.common.iterator_wrapper import IteratorWrapper
from typedb.common.native_wrapper import NativeWrapper
from typedb.concept.answer.concept_map import _ConceptMap
from typedb.logic.rule import _Rule

if TYPE_CHECKING:
    from typedb.api.answer.concept_map import ConceptMap
    from typedb.api.logic.rule import Rule


class _Explanation(Explanation, NativeWrapper[NativeExplanation]):

    def __init__(self, explanation: NativeExplanation):
        if not explanation:
            raise TypeDBClientExceptionExt(NULL_NATIVE_OBJECT)
        super().__init__(explanation)

    @property
    def _native_object_not_owned_exception(self) -> TypeDBClientExceptionExt:
        return TypeDBClientExceptionExt.of(ILLEGAL_STATE)

    def rule(self) -> Rule:
        return _Rule(explanation_get_rule(self.native_object))

    def conclusion(self) -> ConceptMap:
        return _ConceptMap(explanation_get_conclusion(self.native_object))

    def condition(self) -> ConceptMap:
        return _ConceptMap(explanation_get_condition(self.native_object))

    def query_variables(self) -> set[str]:
        return set(IteratorWrapper(explanation_get_mapped_variables(self.native_object), string_iterator_next))

    def query_variable_mapping(self, var: str) -> set[str]:
        if not var:
            raise TypeDBClientExceptionExt(MISSING_VARIABLE)
        return set(IteratorWrapper(explanation_get_mapping(self.native_object, var), string_iterator_next))

    def __repr__(self):
        return explanation_to_string(self.native_object)

    def __eq__(self, other):
        if other is self:
            return True
        if not other or type(self) != type(other):
            return False
        return explanation_equals(self.native_object, other.native_object)

    def __hash__(self):
        return hash((self.rule(), self.condition(), self.conclusion()))
