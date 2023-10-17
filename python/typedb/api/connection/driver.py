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
from typing import Optional, TYPE_CHECKING

if TYPE_CHECKING:
    from typedb.api.connection.database import DatabaseManager
    from typedb.api.connection.options import TypeDBOptions
    from typedb.api.connection.session import TypeDBSession, SessionType
    from typedb.api.user.user import UserManager, User


class TypeDBDriver(ABC):

    @abstractmethod
    def is_open(self) -> bool:
        """
        Checks whether this connection is presently open.

        :return:

        Examples:
        ---------
        ::

            driver.is_open()
        """
        pass

    @property
    @abstractmethod
    def databases(self) -> DatabaseManager:
        """
        The ``DatabaseManager`` for this connection, providing access to database management methods.
        """
        pass

    @abstractmethod
    def session(self, database_name: str, session_type: SessionType, options: Optional[TypeDBOptions] = None
                ) -> TypeDBSession:
        """
        Opens a communication tunnel (session) to the given database on the running TypeDB server.
        For more information on the methods, available with sessions, see the ``TypeDBSession`` section.

        :param database_name: The name of the database with which the session connects
        :param session_type: The type of session to be created (DATA or SCHEMA)
        :param options: ``TypeDBOptions`` for the session
        :return:

        Examples:
        ---------
        ::

            driver.session(database, session_type, options)
        """
        pass

    @abstractmethod
    def close(self) -> None:
        """
        Closes the driver. Before instantiating a new driver, the driver that’s currently open should first be closed.

        :return:

        Examples:
        ---------
        ::

            driver.close()
        """
        pass

    @property
    @abstractmethod
    def users(self) -> UserManager:
        """
        The ``UserManager`` instance for this connection, providing access to user management methods.
        Only for TypeDB Enterprise.
        """
        pass

    @abstractmethod
    def user(self) -> User:
        """
        Returns the logged-in user for the connection. Only for TypeDB Enterprise.

        :return:

        Examples:
        ---------
        ::

            driver.user()
        """
        pass

    @abstractmethod
    def __enter__(self):
        pass

    @abstractmethod
    def __exit__(self, exc_type, exc_val, exc_tb):
        pass
