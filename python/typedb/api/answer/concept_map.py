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
from typing import Mapping, Iterator, TYPE_CHECKING

if TYPE_CHECKING:
    from typedb.api.concept.concept import Concept


class ConceptMap(ABC):
    """
    Contains a mapping of variables to concepts.
    """

    @abstractmethod
    def variables(self) -> Iterator[str]:
        """
        Produces an iterator over all variables in this ``ConceptMap``.

        :return:

        Examples
        --------
        ::

          concept_map.variables()
        """
        pass

    @abstractmethod
    def concepts(self) -> Iterator[Concept]:
        """
        Produces an iterator over all concepts in this ``ConceptMap``.

        :return:

        Examples
        --------
        ::

          concept_map.concepts()
        """
        pass

    @abstractmethod
    def map(self) -> Mapping[str, Concept]:
        """
        Returns the inner ``Mapping`` where keys are query variables, and values are concepts.

        :return:

        Examples
        --------
        ::

          concept_map.concepts()
        """
        pass

    @abstractmethod
    def get(self, variable: str) -> Concept:
        """
        Retrieves a concept for a given variable name.

        :param variable: The string representation of a variable
        :return:

        Examples
        --------
        ::

          concept_map.get(variable)
        """
        pass

    @abstractmethod
    def explainables(self) -> Explainables:
        """
        Gets the ``Explainables`` object for this ``ConceptMap``, exposing
        which of the concepts in this ``ConceptMap`` are explainable.

        :return:

        Examples
        --------
        ::

          concept_map.explainables()
        """
        pass

    class Explainables(ABC):
        """
        Contains explainable objects.
        """

        @abstractmethod
        def relation(self, variable: str) -> ConceptMap.Explainable:
            """
            Retrieves the explainable relation with the given variable name.

            :param variable: The string representation of a variable
            :return:

            Examples
            --------
            ::

               concept_map.explainables().relation(variable)
            """
            pass

        @abstractmethod
        def attribute(self, variable: str) -> ConceptMap.Explainable:
            """
            Retrieves the explainable attribute with the given variable name.

            :param variable: The string representation of a variable
            :return:

            Examples
            --------
            ::

               concept_map.explainables().attribute(variable)
            """
            pass

        @abstractmethod
        def ownership(self, owner: str, attribute: str) -> ConceptMap.Explainable:
            """
            Retrieves the explainable attribute ownership with the pair of (owner, attribute) variable names.

            :param owner: The string representation of the owner variable
            :param attribute: The string representation of the attribute variable
            :return:

            Examples
            --------
            ::

               concept_map.explainables().ownership(owner, attribute)
            """
            pass

        @abstractmethod
        def relations(self) -> Mapping[str, ConceptMap.Explainable]:
            """
            Retrieves all of this ``ConceptMap``’s explainable relations.

            :return:

            Examples
            --------
            ::

               concept_map.explainables().relations()
            """
            pass

        @abstractmethod
        def attributes(self) -> Mapping[str, ConceptMap.Explainable]:
            """
            Retrieves all of this ``ConceptMap``’s explainable attributes.

            :return:

            Examples
            --------
            ::

               concept_map.explainables().attributes()
            """
            pass

        @abstractmethod
        def ownerships(self) -> Mapping[tuple[str, str], ConceptMap.Explainable]:
            """
            Retrieves all of this ``ConceptMap``’s explainable ownerships.

            :return:

            Examples
            --------
            ::

               concept_map.explainables().ownerships()
            """
            pass

    class Explainable(ABC):
        """
        Contains an explainable object.
        """

        @abstractmethod
        def conjunction(self) -> str:
            """
            Retrieves the subquery of the original query that is actually being explained.

            :return:

            Examples
            --------
            ::

               explainable.conjunction()
            """
            pass

        @abstractmethod
        def id(self) -> int:
            """
            Retrieves the unique ID that identifies this ``Explainable``.

            :return:

            Examples
            --------
            ::

               explainable.id()
            """
            pass
