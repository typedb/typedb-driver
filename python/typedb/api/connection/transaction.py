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
from typing import TYPE_CHECKING, Dict, List, Optional, Tuple, Union, Callable

if TYPE_CHECKING:
    from typedb.api.answer.query_answer import QueryAnswer
    from typedb.api.concept.concept import Concept
    from typedb.api.concept.given_rows import GivenRows
    from typedb.api.connection.transaction_options import TransactionOptions
    from typedb.api.connection.query_options import QueryOptions
    from typedb.common import Promise


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

    @property
    @abstractmethod
    def options(self) -> TransactionOptions:
        """
        The options for the transaction
        """
        pass

    @abstractmethod
    def query(self, query: str, options: Optional[QueryOptions] = None,
              given_rows: Optional[Union[GivenRows, List[Dict[str, object]], Tuple[List[str], List[List[object]]]]] = None) -> Promise[QueryAnswer]:
        """
        Execute a TypeQL query in this transaction.

        :param query: The query to execute.
        :param options: The ``QueryOptions`` to execute the query with.
        :param given_rows: Rows given to the query as input. May be a ``GivenRows`` object,
            a list of dicts mapping variable names to values, or a ``(variables, rows)`` tuple.
            Items in the dicts/rows may be ``Concept`` instances or primitives supported by ``TypeDB.Concept.try_convert_to_value``.

        Examples:
        ---------
        ::

            transaction.query("define entity person;", options).resolve()

        ::

            query = "given $n: string, $a: integer; insert $p isa person, has name == $n, has age == $a;"
            rows = TypeDB.Concept.given_rows(
                ["n", "a"],
                [
                    [TypeDB.Concept.new_string("Alice"), TypeDB.Concept.new_integer(28)],  # First row
                    [TypeDB.Concept.new_string("Bob"),   TypeDB.Concept.new_integer(26)],  # Second row
                ]
            )
            transaction.query(query, given_rows=rows).resolve()

        ::

            transaction.query(query, given_rows=[{"n": "Alice", "a": 28}, {"n": "Bob", "a": 26}]).resolve()
        """
        pass

    @abstractmethod
    def commit(self) -> None:
        """
        Commits the changes made via this transaction to the TypeDB database.
        **Whether or not the transaction is commited successfully, it gets closed after the commit call.**


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

        Examples:
        ---------
        ::

            transaction.rollback()
        """
        pass

    @abstractmethod
    def on_close(self, function: Callable) -> None:
        """
        Registers a callback function which will be executed when this transaction is closed.

        :param function: The callback function.

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
