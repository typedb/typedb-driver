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

import enum
from abc import ABC, abstractmethod


# from typing import TYPE_CHECKING
#
# if TYPE_CHECKING:
# from typedb.api.connection.options import Options


class TransactionType(enum.Enum):
    """
    This class is used to specify the type of transaction.

    Examples
    --------
    ::

       driver.transaction(database, TransactionType.READ)
    """
    READ = 0
    WRITE = 1
    SCHEMA = 2

    def is_read(self) -> bool:
        return self is TransactionType.READ

    def is_write(self) -> bool:
        return self is TransactionType.WRITE

    def is_schema(self) -> bool:
        return self is TransactionType.SCHEMA


class Transaction(ABC):

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
    def type(self) -> TransactionType:
        """
        The transaction's type (READ, WRITE, or SCHEMA)
        """
        pass

    # @property
    # @abstractmethod
    # def options(self) -> Options:
    #     """
    #     The options for the transaction
    #     """
    #     pass

    @abstractmethod
    def query(self, query: str) -> Promise[QueryAnswer]:
        """
        Execute a TypeQL query in this transaction.

        :param query: The query to execute.
        :return:

        Examples:
        ---------
        ::

            transaction.query("define entity person;")
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
        Registers a callback function which will be executed when this transaction is closed.

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
