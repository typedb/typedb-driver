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

from typing import TYPE_CHECKING, Optional

from typedb.native_client_wrapper import session_new, session_on_close, session_force_close, session_is_open, \
    session_get_database_name, SessionCallbackDirector, Session as NativeSession

from typedb.api.connection.options import TypeDBOptions
from typedb.api.connection.session import TypeDBSession
from typedb.common.exception import TypeDBClientExceptionExt, SESSION_CLOSED
from typedb.common.native_wrapper import NativeWrapper
from typedb.connection.transaction import _Transaction

if TYPE_CHECKING:
    from typedb.api.connection.session import SessionType
    from typedb.api.connection.transaction import TypeDBTransaction, TransactionType
    from typedb.connection.database import _Database


class _Session(TypeDBSession, NativeWrapper[NativeSession]):

    def __init__(self, database: _Database, session_type: SessionType, options: Optional[TypeDBOptions] = None):
        if not options:
            options = TypeDBOptions()
        self._type = session_type
        self._options = options
        native_database = database.native_object
        native_database.thisown = 0
        super().__init__(session_new(native_database, session_type.value, options.native_object))

    @property
    def _native_object_not_owned_exception(self) -> TypeDBClientExceptionExt:
        return TypeDBClientExceptionExt.of(SESSION_CLOSED)

    def is_open(self) -> bool:
        return session_is_open(self.native_object)

    @property
    def type(self) -> SessionType:
        return self._type

    def database_name(self) -> str:
        return session_get_database_name(self.native_object)

    @property
    def options(self) -> TypeDBOptions:
        return self._options

    def transaction(self, transaction_type: TransactionType, options: TypeDBOptions = None) -> TypeDBTransaction:
        return _Transaction(self, transaction_type, options)

    def close(self) -> None:
        session_force_close(self.native_object)

    def on_close(self, function: callable):
        session_on_close(self.native_object, _Session.Callback(function).__disown__())

    class Callback(SessionCallbackDirector):

        def __init__(self, function: callable):
            super().__init__()
            self._function = function

        def callback(self) -> None:
            self._function()

    def __enter__(self):
        return self

    def __exit__(self, exc_type, exc_val, exc_tb):
        self.close()
        if exc_tb is not None:
            return False
