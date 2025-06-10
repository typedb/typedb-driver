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
from typedb.common.exception import TypeDBDriverException
from typedb.common.iterator_wrapper import IteratorWrapper
from typedb.common.validation import require_non_null
from typedb.connection.database import _Database
from typedb.native_driver_wrapper import databases_all, databases_contains, databases_create, databases_get, \
    databases_import_from_file, database_iterator_next, TypeDBDriverExceptionNative

if TYPE_CHECKING:
    from typedb.native_driver_wrapper import TypeDBDriver as NativeDriver


class _DatabaseManager(DatabaseManager):

    def __init__(self, native_driver: NativeDriver):
        self.native_driver = native_driver

    def get(self, name: str) -> _Database:
        require_non_null(name, "name")
        try:
            return _Database(databases_get(self.native_driver, name))
        except TypeDBDriverExceptionNative as e:
            raise TypeDBDriverException.of(e) from None

    def contains(self, name: str) -> bool:
        require_non_null(name, "name")
        try:
            return databases_contains(self.native_driver, name)
        except TypeDBDriverExceptionNative as e:
            raise TypeDBDriverException.of(e) from None

    def create(self, name: str) -> None:
        require_non_null(name, "name")
        try:
            databases_create(self.native_driver, name)
        except TypeDBDriverExceptionNative as e:
            raise TypeDBDriverException.of(e) from None

    def import_from_file(self, name: str, schema: str, data_file_path: str) -> None:
        require_non_null(name, "name")
        require_non_null(schema, "schema")
        require_non_null(data_file_path, "data_file_path")
        try:
            databases_import_from_file(self.native_driver, name, schema, data_file_path)
        except TypeDBDriverExceptionNative as e:
            raise TypeDBDriverException.of(e) from None

    def all(self) -> list[_Database]:
        try:
            return list(map(_Database, IteratorWrapper(databases_all(self.native_driver), database_iterator_next)))
        except TypeDBDriverExceptionNative as e:
            raise TypeDBDriverException.of(e) from None
