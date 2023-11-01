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
from typing import Iterator, Optional, TYPE_CHECKING

if TYPE_CHECKING:
    from typedb.api.answer.concept_map import ConceptMap
    from typedb.api.answer.concept_map_group import ConceptMapGroup
    from typedb.api.answer.value_group import ValueGroup
    from typedb.api.concept.value.value import Value
    from typedb.api.connection.options import TypeDBOptions
    from typedb.api.logic.explanation import Explanation
    from typedb.common.promise import Promise


class QueryManager(ABC):
    """
    Provides methods for executing TypeQL queries in the transaction.
    """
    
    @abstractmethod
    def get(self, query: str, options: Optional[TypeDBOptions] = None) -> Iterator[ConceptMap]:
        """
        Performs a TypeQL Match (Get) query in the transaction.

        :param query:   The TypeQL Match (Get) query to be executed
        :param options: Specify query options
        :return:

        Examples
        --------
        ::

            transaction.query.get(query, options)
        """
        pass

    @abstractmethod
    def get_aggregate(self, query: str, options: Optional[TypeDBOptions] = None) -> Promise[Optional[Value]]:
        """
        Performs a TypeQL Match Aggregate query in the transaction.

        :param query: The TypeQL Match Aggregate query to be executed
        :param options: Specify query options
        :return:

        Examples
        --------
        ::

            transaction.query.get_aggregate(query, options).resolve()
        """
        pass

    @abstractmethod
    def get_group(self, query: str, options: Optional[TypeDBOptions] = None) -> Iterator[ConceptMapGroup]:
        """
        Performs a TypeQL Match Group query in the transaction.

        :param query: The TypeQL Match Group query to be executed
        :param options: Specify query options
        :return:

        Examples
        --------
        ::

             transaction.query.get_group(query, options)
        """
        pass

    @abstractmethod
    def get_group_aggregate(self, query: str, options: Optional[TypeDBOptions] = None) -> Iterator[ValueGroup]:
        """
        Performs a TypeQL Match Group Aggregate query in the transaction.

        :param query: The TypeQL Match Group Aggregate query to be executed
        :param options: Specify query options
        :return:

        Examples
        --------
        ::

            transaction.query.get_group_aggregate(query, options)
        """
        pass

    @abstractmethod
    def fetch(self, query: str, options: Optional[TypeDBOptions] = None) -> Iterator[dict]:
        """
        Performs a TypeQL Fetch query in the transaction.

        :param query:   The TypeQL Fetch query to be executed
        :param options: Specify query options
        :return:

        Examples
        --------
        ::

            transaction.query.fetch(query, options)
        """
        pass

    @abstractmethod
    def insert(self, query: str, options: Optional[TypeDBOptions] = None) -> Iterator[ConceptMap]:
        """
        Performs a TypeQL Insert query in the transaction.

        :param query: The TypeQL Insert query to be executed
        :param options: Specify query options
        :return:

        Examples
        --------
        ::

            transaction.query.insert(query, options)
        """
        pass

    @abstractmethod
    def delete(self, query: str, options: Optional[TypeDBOptions] = None) -> Promise[None]:
        """
        Performs a TypeQL Delete query in the transaction.

        :param query: The TypeQL Delete query to be executed
        :param options: Specify query options
        :return:

        Examples
        --------
        ::

            transaction.query.delete(query, options).resolve()
        """
        pass

    @abstractmethod
    def define(self, query: str, options: TypeDBOptions = None) -> Promise[None]:
        """
        Performs a TypeQL Define query in the transaction.

        :param query: The TypeQL Define query to be executed
        :param options: Specify query options
        :return:

        Examples
        --------
        ::

            transaction.query.define(query, options).resolve()
        """
        pass

    @abstractmethod
    def undefine(self, query: str, options: TypeDBOptions = None) -> Promise[None]:
        """
        Performs a TypeQL Undefine query in the transaction.

        :param query: The TypeQL Undefine query to be executed
        :param options: Specify query options
        :return:

        Examples
        --------
        ::

            transaction.query.undefine(query, options).resolve()
        """
        pass

    @abstractmethod
    def update(self, query: str, options: Optional[TypeDBOptions] = None) -> Iterator[ConceptMap]:
        """
        Performs a TypeQL Update query in the transaction.

        :param query: The TypeQL Update query to be executed
        :param options: Specify query options
        :return:

        Examples
        --------
        ::

            transaction.query.update(query, options)
        """
        pass

    @abstractmethod
    def explain(self, explainable: ConceptMap.Explainable, options: Optional[TypeDBOptions] = None) -> Iterator[Explanation]:
        """
        Performs a TypeQL Explain query in the transaction.

        :param explainable: The Explainable to be explained
        :param options: Specify query options

        :return:

        Examples
        --------
        ::

            transaction.query.explain(explainable, options)
        """
        pass
