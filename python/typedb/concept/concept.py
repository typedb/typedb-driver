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

from abc import ABC, abstractmethod

from typedb.native_client_wrapper import concept_to_string, concept_equals, Concept as NativeConcept

from typedb.api.concept.concept import Concept
from typedb.common.exception import TypeDBClientExceptionExt, ILLEGAL_STATE, NULL_NATIVE_OBJECT
from typedb.common.native_wrapper import NativeWrapper


class _Concept(Concept, NativeWrapper[NativeConcept], ABC):

    def __init__(self, concept: NativeConcept):
        if not concept:
            raise TypeDBClientExceptionExt(NULL_NATIVE_OBJECT)
        super().__init__(concept)

    @property
    def _native_object_not_owned_exception(self) -> TypeDBClientExceptionExt:
        return TypeDBClientExceptionExt.of(ILLEGAL_STATE)

    def __repr__(self):
        return concept_to_string(self.native_object)

    def __eq__(self, other):
        return other and isinstance(other, _Concept) and concept_equals(self.native_object, other.native_object)

    @abstractmethod
    def __hash__(self):
        pass
