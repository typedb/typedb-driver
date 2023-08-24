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

from typing import Optional

from typedb.native_client_wrapper import database_get_name, database_schema, database_delete, database_rule_schema, \
    database_type_schema, ReplicaInfo, replica_info_get_address, replica_info_is_primary, replica_info_is_preferred, \
    replica_info_get_term, database_get_replicas_info, database_get_primary_replica_info, \
    database_get_preferred_replica_info, replica_info_iterator_next, Database as NativeDatabase

from typedb.api.connection.database import Database, Replica
from typedb.common.exception import TypeDBClientExceptionExt, DATABASE_DELETED, NULL_NATIVE_OBJECT
from typedb.common.iterator_wrapper import IteratorWrapper
from typedb.common.native_wrapper import NativeWrapper


class _Database(Database, NativeWrapper[NativeDatabase]):

    def __init__(self, database: NativeDatabase):
        if not database:
            raise TypeDBClientExceptionExt(NULL_NATIVE_OBJECT)
        super().__init__(database)
        self._name = database_get_name(database)

    @property
    def _native_object_not_owned_exception(self) -> TypeDBClientExceptionExt:
        return TypeDBClientExceptionExt.of(DATABASE_DELETED)

    @property
    def name(self) -> str:
        if not self._native_object.thisown:
            raise self._native_object_not_owned_exception
        return self._name

    def schema(self) -> str:
        return database_schema(self.native_object)

    def rule_schema(self) -> str:
        return database_rule_schema(self.native_object)

    def type_schema(self) -> str:
        return database_type_schema(self.native_object)

    def delete(self) -> None:
        self.native_object.thisown = 0
        database_delete(self._native_object)

    def replicas(self) -> set[Replica]:
        repl_iter = IteratorWrapper(database_get_replicas_info(self.native_object), replica_info_iterator_next)
        return set(_Database.Replica(replica_info) for replica_info in repl_iter)

    def primary_replica(self) -> Optional[Replica]:
        if res := database_get_primary_replica_info(self.native_object):
            return _Database.Replica(res)
        return None

    def preferred_replica(self) -> Optional[Replica]:
        if res := database_get_preferred_replica_info(self.native_object):
            return _Database.Replica(res)
        return None

    def __str__(self):
        return self.name

    def __repr__(self):
        return f"Database('{str(self)}')"

    class Replica(Replica):

        def __init__(self, replica_info: ReplicaInfo):
            self._info = replica_info

        def database(self) -> Database:
            pass

        def address(self) -> str:
            return replica_info_get_address(self._info)

        def is_primary(self) -> bool:
            return replica_info_is_primary(self._info)

        def is_preferred(self) -> bool:
            return replica_info_is_preferred(self._info)

        def term(self) -> int:
            return replica_info_get_term(self._info)
