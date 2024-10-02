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

from abc import ABC

from typedb.api.answer.query_answer import QueryAnswer


class ConceptRowIterator(QueryAnswer, ABC):
    """
    Represents an iterator over ``ConceptRow``s returned as a server answer.
    """

    def is_concept_rows(self) -> bool:
        """
        Checks if the query answer is a ``ConceptRowIterator``.

        :return:

        Examples
        --------
        ::

          query_answer.is_concept_rows()
        """
        return True

    def as_concept_rows(self) -> ConceptRowIterator:
        """
        Casts the query answer to ``ConceptRowIterator``.

        :return:

        Examples
        --------
        ::

          query_answer.as_concept_rows()
        """
        return self
