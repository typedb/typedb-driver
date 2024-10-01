# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
#   Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.

from __future__ import annotations

from typing import TYPE_CHECKING

from typedb.api.connection.database import DatabaseManager
from typedb.common.exception import TypeDBDriverException, DATABASE_DELETED, ILLEGAL_STATE, MISSING_DB_NAME
from typedb.common.iterator_wrapper import IteratorWrapper
from typedb.common.native_wrapper import NativeWrapper
from typedb.connection.database import _Database
from typedb.native_driver_wrapper import databases_contains, databases_create, databases_get, \
    databases_all, database_iterator_next, TypeDBDriver as NativeDriver, TypeDBDriverExceptionNative

if TYPE_CHECKING:
    from typedb.native_driver_wrapper import TypeDBDriver as NativeDriver


def _not_blank(name: str) -> str:
    if not name or name.isspace():
        raise TypeDBDriverException(MISSING_DB_NAME)
    return name


class _DatabaseManager(DatabaseManager, NativeWrapper[NativeDriver]):

    def __init__(self, native_driver: NativeDriver):
        super().__init__(native_driver)

    @property
    def _native_object_not_owned_exception(self) -> TypeDBDriverException:
        return TypeDBDriverException(ILLEGAL_STATE)

    def get(self, name: str) -> _Database:
        try:
            if not self.contains(name):
                raise TypeDBDriverException(DATABASE_DELETED, name)
            return _Database(databases_get(self.native_object, name))
        except TypeDBDriverExceptionNative as e:
            raise TypeDBDriverException.of(e)

    def contains(self, name: str) -> bool:
        try:
            return databases_contains(self.native_object, _not_blank(name))
        except TypeDBDriverExceptionNative as e:
            raise TypeDBDriverException.of(e)

    def create(self, name: str) -> None:
        try:
            databases_create(self.native_object, _not_blank(name))
        except TypeDBDriverExceptionNative as e:
            raise TypeDBDriverException.of(e)

    def all(self) -> list[_Database]:
        try:
            return list(map(_Database, IteratorWrapper(databases_all(self.native_object), database_iterator_next)))
        except TypeDBDriverExceptionNative as e:
            raise TypeDBDriverException.of(e)
