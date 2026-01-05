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

from typing import Optional

from typedb.api.connection.consistency_level import ConsistencyLevel
from typedb.api.database.database import Database
from typedb.common.exception import TypeDBDriverException, DATABASE_DELETED, NULL_NATIVE_OBJECT
from typedb.common.native_wrapper import NativeWrapper
from typedb.common.validation import require_non_null
from typedb.native_driver_wrapper import database_get_name, database_schema, database_delete, database_type_schema, \
    database_export_to_file, Database as NativeDatabase, \
    TypeDBDriverExceptionNative


class _Database(Database, NativeWrapper[NativeDatabase]):

    def __init__(self, database: NativeDatabase):
        if not database:
            raise TypeDBDriverException(NULL_NATIVE_OBJECT)
        super().__init__(database)
        self._name = database_get_name(database)

    @property
    def _native_object_not_owned_exception(self) -> TypeDBDriverException:
        return TypeDBDriverException(DATABASE_DELETED)

    @property
    def name(self) -> str:
        if not self._native_object.thisown:
            raise self._native_object_not_owned_exception
        return self._name

    def schema(self, consistency_level: Optional[ConsistencyLevel] = None) -> str:
        try:
            return database_schema(self.native_object, ConsistencyLevel.native_value(consistency_level))
        except TypeDBDriverExceptionNative as e:
            raise TypeDBDriverException.of(e) from None

    def type_schema(self, consistency_level: Optional[ConsistencyLevel] = None) -> str:
        try:
            return database_type_schema(self.native_object, ConsistencyLevel.native_value(consistency_level))
        except TypeDBDriverExceptionNative as e:
            raise TypeDBDriverException.of(e) from None

    def export_to_file(self, schema_file_path: str, data_file_path: str,
                       consistency_level: Optional[ConsistencyLevel] = None) -> None:
        require_non_null(schema_file_path, "schema_file_path")
        require_non_null(data_file_path, "data_file_path")
        try:
            consistency_level = ConsistencyLevel.native_value(consistency_level)
            return database_export_to_file(self.native_object, schema_file_path, data_file_path, consistency_level)
        except TypeDBDriverExceptionNative as e:
            raise TypeDBDriverException.of(e) from None

    def delete(self, consistency_level: Optional[ConsistencyLevel] = None) -> None:
        try:
            self._native_object.thisown = 0
            database_delete(self._native_object, ConsistencyLevel.native_value(consistency_level))
        except TypeDBDriverExceptionNative as e:
            raise TypeDBDriverException.of(e) from None

    def __str__(self):
        return self.name

    def __repr__(self):
        return f"Database('{str(self)}')"
