#
#   Copyright (C) 2022 Vaticle
#
#   Licensed to the Apache Software Foundation (ASF) under one
#   or more contributor license agreements.  See the NOTICE file
#   distributed with this work for additional information
#   regarding copyright ownership.  The ASF licenses this file
#   to you under the Apache License, Version 2.0 (the
#   "License"); you may not use this file except in compliance
#   with the License.  You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
#   Unless required by applicable law or agreed to in writing,
#   software distributed under the License is distributed on an
#   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
#   KIND, either express or implied.  See the License for the
#   specific language governing permissions and limitations
#   under the License.
#

from __future__ import annotations

from typing import Optional, TYPE_CHECKING

from typedb.native_client_wrapper import connection_open_plaintext, connection_open_encrypted, connection_is_open, \
    connection_force_close, Connection as NativeConnection

from typedb.api.connection.client import TypeDBClient
from typedb.api.connection.options import TypeDBOptions
from typedb.common.exception import TypeDBClientExceptionExt, CLIENT_CLOSED
from typedb.common.native_wrapper import NativeWrapper
from typedb.connection.database_manager import _DatabaseManager
from typedb.connection.session import _Session
from typedb.user.user_manager import _UserManager

if TYPE_CHECKING:
    from typedb.api.connection.credential import TypeDBCredential
    from typedb.api.connection.session import SessionType
    from typedb.api.user.user import UserManager, User


class _Client(TypeDBClient, NativeWrapper[NativeConnection]):

    def __init__(self, addresses: list[str], credential: Optional[TypeDBCredential] = None):
        if credential:
            native_connection = connection_open_encrypted(addresses, credential.native_object)
        else:
            native_connection = connection_open_plaintext(addresses[0])
        super().__init__(native_connection)
        self._database_manager = _DatabaseManager(native_connection)
        self._user_manager = _UserManager(native_connection)

    @property
    def _native_object_not_owned_exception(self) -> TypeDBClientExceptionExt:
        return TypeDBClientExceptionExt.of(CLIENT_CLOSED)

    @property
    def _native_connection(self) -> NativeConnection:
        return self.native_object

    def session(self, database: str, session_type: SessionType, options: TypeDBOptions = None) -> _Session:
        return _Session(self.databases.get(database), session_type, options if options else TypeDBOptions())

    def is_open(self) -> bool:
        return connection_is_open(self._native_connection)

    @property
    def databases(self) -> _DatabaseManager:
        return self._database_manager

    @property
    def users(self) -> UserManager:
        return self._user_manager

    def user(self) -> User:
        return self._user_manager.get_current_user()

    def __enter__(self):
        return self

    def __exit__(self, exc_type, exc_val, exc_tb):
        self.close()
        if exc_tb is not None:
            return False

    def close(self) -> None:
        connection_force_close(self._native_connection)
