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

from abc import ABC, abstractmethod
from typing import Iterator, TYPE_CHECKING

if TYPE_CHECKING:
    from typedb.api.concept.concept import Concept


class ConceptRow(ABC):
    """
    Contains a row of concepts with a header.
    """

    @abstractmethod
    def column_names(self) -> Iterator[str]:
        """
        Produces an iterator over all column names (variables) in the header of this ``ConceptRow``.
        Shared between all the rows in a QueryAnswer.

        :return:

        Examples
        --------
        ::

          concept_row.column_names()
        """
        pass

    @property
    @abstractmethod
    def query_type(self) -> QueryType:
        """
        Retrieves the executed query's type of this ``ConceptRow``.
        Shared between all the rows in a QueryAnswer.

        :return:

        Examples
        --------
        ::

          concept_row.query_type()
        """
        pass

    @abstractmethod
    def get(self, column_name: str) -> Concept:
        """
        Retrieves a concept for a given column name (variable).

        :param column_name: The string representation of a variable (column name from ``column_names``)
        :return:

        Examples
        --------
        ::

          concept_row.get(column_name)
        """
        pass

    @abstractmethod
    def get_index(self, column_index: int) -> Concept:
        """
        Retrieves a concept for a given index of the header (''column_names'').

        :param column_index: The column index
        :return:

        Examples
        --------
        ::

          concept_row.get_index(column_index)
        """
        pass

    @abstractmethod
    def concepts(self) -> Iterator[Concept]:
        """
        Produces an iterator over all concepts in this `ConceptRow`, skipping empty results.

        :return:

        Examples
        --------
        ::

          concept_row.concepts()
        """
        pass
