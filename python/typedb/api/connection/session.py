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
from enum import Enum
from typing import TYPE_CHECKING

from typedb.native_driver_wrapper import Data, Schema

if TYPE_CHECKING:
    from typedb.api.connection.options import TypeDBOptions
    from typedb.api.connection.transaction import TypeDBTransaction, TransactionType


class SessionType(Enum):
    """
    This class is used to specify the type of the session.

    Examples
    --------
    ::

       driver.session(database, SessionType.SCHEMA)
    """
    DATA = Data
    SCHEMA = Schema

    def is_data(self) -> bool:
        return self is SessionType.DATA

    def is_schema(self) -> bool:
        return self is SessionType.SCHEMA


class TypeDBSession(ABC):

    @abstractmethod
    def is_open(self) -> bool:
        """
        Checks whether this session is open.

        :return:

        Examples:
        ---------
        ::

            session.is_open()
        """
        pass

    @property
    @abstractmethod
    def type(self) -> SessionType:
        """
        The current session’s type (SCHEMA or DATA)
        """
        pass

    @abstractmethod
    def database_name(self) -> str:
        """
        Returns the name of the database of the session.

        :return:

        Examples:
        ---------
        ::

            session.database_name()
        """
        pass

    @property
    @abstractmethod
    def options(self) -> TypeDBOptions:
        """
        Gets the options for the session
        """
        pass

    @abstractmethod
    def transaction(self, transaction_type: TransactionType, options: TypeDBOptions = None) -> TypeDBTransaction:
        """
        Opens a transaction to perform read or write queries on the database connected to the session.

        :param transaction_type: The type of transaction to be created (READ or WRITE)
        :param options: Options for the session
        :return:

        Examples:
        ---------
        ::

            session.transaction(transaction_type, options)
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

            session.on_close(function)
        """
        pass

    @abstractmethod
    def close(self) -> None:
        """
        Closes the session.
        **Before opening a new session, the session currently open should first be closed.**

        :return:

        Examples:
        ---------
        ::

            session.close()
        """
        pass

    @abstractmethod
    def __enter__(self):
        pass

    @abstractmethod
    def __exit__(self, exc_type, exc_val, exc_tb):
        pass
