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

import enum
from abc import ABC, abstractmethod
from typing import TYPE_CHECKING

if TYPE_CHECKING:
    from typedb.api.concept.concept_manager import ConceptManager
    from typedb.api.logic.logic_manager import LogicManager
    from typedb.api.connection.options import TypeDBOptions
    from typedb.api.query.query_manager import QueryManager


class TransactionType(enum.Enum):
    """
    This class is used to specify the type of transaction.

    Examples
    --------
    ::

       session.transaction(TransactionType.READ)
    """
    READ = 0
    WRITE = 1

    def is_read(self) -> bool:
        return self is TransactionType.READ

    def is_write(self) -> bool:
        return self is TransactionType.WRITE


class TypeDBTransaction(ABC):

    @abstractmethod
    def is_open(self) -> bool:
        """
        Checks whether this transaction is open.

        :return:

        Examples:
        ---------
        ::

            transaction.is_open()
        """
        pass

    @property
    @abstractmethod
    def transaction_type(self) -> TransactionType:
        """
        The transaction's type (READ or WRITE)
        """
        pass

    @property
    @abstractmethod
    def options(self) -> TypeDBOptions:
        """
        The options for the transaction
        """
        pass

    @property
    @abstractmethod
    def concepts(self) -> ConceptManager:
        """
        The ``ConceptManager`` for this transaction, providing access to all Concept API methods.
        """
        pass

    @property
    @abstractmethod
    def logic(self) -> LogicManager:
        """
        The ``LogicManager`` for this Transaction, providing access to all Concept API - Logic methods.
        """
        pass

    @property
    @abstractmethod
    def query(self) -> QueryManager:
        """
        The``QueryManager`` for this Transaction, from which any TypeQL query can be executed.
        """
        pass

    @abstractmethod
    def commit(self) -> None:
        """
        Commits the changes made via this transaction to the TypeDB database.
        **Whether or not the transaction is commited successfully, it gets closed after the commit call.**


        :return:

        Examples:
        ---------
        ::

            transaction.commit()
        """
        pass

    @abstractmethod
    def rollback(self) -> None:
        """
        Rolls back the uncommitted changes made via this transaction.

        :return:

        Examples:
        ---------
        ::

            transaction.rollback()
        """
        pass

    @abstractmethod
    def on_close(self, function: callable) -> None:
        """
        Registers a callback function which will be executed when this session is closed.

        :param function: The callback function.
        :return:

        Examples:
        ---------
        ::

            transaction.on_close(function)
        """
        pass

    @abstractmethod
    def close(self) -> None:
        """
        Closes the transaction.

        :return:

        Examples:
        ---------
        ::

            transaction.close()
        """
        pass

    @abstractmethod
    def __enter__(self):
        pass

    @abstractmethod
    def __exit__(self, exc_type, exc_val, exc_tb):
        pass
