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
from typing import Iterator, TYPE_CHECKING

if TYPE_CHECKING:
    from typedb.api.answer.concept_map import ConceptMap
    from typedb.api.concept.concept import Concept


class ConceptMapGroup(ABC):
    """
    Contains an element of the group query result.
    """

    @abstractmethod
    def owner(self) -> Concept:
        """
        Retrieves the concept that is the group owner.

        :return:

        Examples:
        ---------
        ::

            concept_map_group.owner()
        """
        pass

    @abstractmethod
    def concept_maps(self) -> Iterator[ConceptMap]:
        """
        Retrieves the ConceptMaps of the group.

        :return:

        Examples:
        ---------
        ::

            concept_map_group.concept_maps()
        """
        pass
