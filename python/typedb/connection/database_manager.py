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

from typing import TYPE_CHECKING

from typedb.native_client_wrapper import databases_contains, databases_create, database_manager_new, databases_get, \
    databases_all, database_iterator_next, DatabaseManager as NativeDatabaseManager

from typedb.api.connection.database import DatabaseManager
from typedb.common.exception import TypeDBClientExceptionExt, DATABASE_DELETED, ILLEGAL_STATE, MISSING_DB_NAME
from typedb.common.iterator_wrapper import IteratorWrapper
from typedb.common.native_wrapper import NativeWrapper
from typedb.connection.database import _Database

if TYPE_CHECKING:
    from typedb.native_client_wrapper import Connection as NativeConnection


def _not_blank(name: str) -> str:
    if not name or name.isspace():
        raise TypeDBClientExceptionExt.of(MISSING_DB_NAME)
    return name


class _DatabaseManager(DatabaseManager, NativeWrapper[NativeDatabaseManager]):

    def __init__(self, connection: NativeConnection):
        super().__init__(database_manager_new(connection))

    @property
    def _native_object_not_owned_exception(self) -> TypeDBClientExceptionExt:
        return TypeDBClientExceptionExt.of(ILLEGAL_STATE)

    def get(self, name: str) -> _Database:
        if not self.contains(name):
            raise TypeDBClientExceptionExt.of(DATABASE_DELETED, name)
        return _Database(databases_get(self.native_object, name))

    def contains(self, name: str) -> bool:
        return databases_contains(self.native_object, _not_blank(name))

    def create(self, name: str) -> None:
        databases_create(self.native_object, _not_blank(name))

    def all(self) -> list[_Database]:
        return list(map(_Database, IteratorWrapper(databases_all(self.native_object), database_iterator_next)))
