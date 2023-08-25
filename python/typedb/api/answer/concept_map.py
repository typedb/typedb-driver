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
from typing import Mapping, Union, Iterator, TYPE_CHECKING

if TYPE_CHECKING:
    from typedb.api.concept.concept import Concept


class ConceptMap(ABC):

    @abstractmethod
    def variables(self) -> Iterator[str]:
        pass

    @abstractmethod
    def concepts(self) -> Iterator[Concept]:
        pass

    @abstractmethod
    def get(self, variable: str) -> Concept:
        pass

    @abstractmethod
    def explainables(self) -> Explainables:
        pass

    def to_json(self) -> Mapping[str, Mapping[str, Union[str, int, float, bool]]]:
        return {var: self.get(var).to_json() for var in self.variables()}

    class Explainables(ABC):

        @abstractmethod
        def relation(self, variable: str) -> ConceptMap.Explainable:
            pass

        @abstractmethod
        def attribute(self, variable: str) -> ConceptMap.Explainable:
            pass

        @abstractmethod
        def ownership(self, owner: str, attribute: str) -> ConceptMap.Explainable:
            pass

        @abstractmethod
        def relations(self) -> Mapping[str, ConceptMap.Explainable]:
            pass

        @abstractmethod
        def attributes(self) -> Mapping[str, ConceptMap.Explainable]:
            pass

        @abstractmethod
        def ownerships(self) -> Mapping[tuple[str, str], ConceptMap.Explainable]:
            pass

    class Explainable(ABC):

        @abstractmethod
        def conjunction(self) -> str:
            pass

        @abstractmethod
        def id(self) -> int:
            pass
