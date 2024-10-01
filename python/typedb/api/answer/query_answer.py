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


class QueryAnswer(ABC):
    """
    General answer on a query returned by a server. Can be a simple Ok response or a collection of concepts.
    """

    def is_ok(self) -> bool:
        """
        Checks if the query answer is an ``Ok``.

        :return:

        Examples
        --------
        ::

          query_answer.is_ok()
        """
        return False

    def is_concept_rows(self) -> bool:
        """
        Checks if the query answer is a ``ConceptRowIterator``.

        :return:

        Examples
        --------
        ::

          query_answer.is_concept_rows()
        """
        return False

    def is_concept_trees(self) -> bool:
        """
        Checks if the query answer is a ``ConceptTreeIterator``.

        :return:

        Examples
        --------
        ::

          query_answer.is_concept_trees()
        """
        return False

    def as_ok(self) -> OkQueryAnswer:
        """
        Casts the query answer to ``OkQueryAnswer``.

        :return:

        Examples
        --------
        ::

          query_answer.as_ok()
        """
        raise TypeDBDriverException(INVALID_QUERY_ANSWER_CASTING, (self.__class__.__name__, "OkQueryAnswer"))

    def as_concept_rows(self) -> ConceptRowIterator:
        """
        Casts the query answer to ``ConceptRowIterator``.

        :return:

        Examples
        --------
        ::

          query_answer.as_concept_rows()
        """
        raise TypeDBDriverException(INVALID_QUERY_ANSWER_CASTING, (self.__class__.__name__, "ConceptRowIterator"))

    def as_concept_trees(self) -> ConceptTreeIterator:
        """
        Casts the query answer to ``ConceptTreeIterator``.

        :return:

        Examples
        --------
        ::

          query_answer.as_concept_trees()
        """
        raise TypeDBDriverException(INVALID_QUERY_ANSWER_CASTING, (self.__class__.__name__, "ConceptTreeIterator"))