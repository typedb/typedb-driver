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

from typedb.api.server.server_replica import ServerReplica
from typedb.common.exception import TypeDBDriverException, NULL_NATIVE_OBJECT, ILLEGAL_STATE
from typedb.common.native_wrapper import NativeWrapper
from typedb.native_driver_wrapper import server_replica_get_address, server_replica_get_id, server_replica_get_type, \
    server_replica_is_primary, server_replica_get_term, ServerReplica as NativeServerReplica, \
    TypeDBDriverExceptionNative


class _ServerReplica(ServerReplica, NativeWrapper[NativeServerReplica]):

    def __init__(self, server_replica: NativeServerReplica):
        if not server_replica:
            raise TypeDBDriverException(NULL_NATIVE_OBJECT)
        super().__init__(server_replica)

    @property
    def _native_object_not_owned_exception(self) -> TypeDBDriverException:
        return TypeDBDriverException(ILLEGAL_STATE)

    @property
    def id(self) -> int:
        return server_replica_get_id(self.native_object)

    @property
    def address(self) -> str:
        return server_replica_get_address(self.native_object)

    @property
    def replica_type(self) -> str:
        return server_replica_get_type(self.native_object)

    def is_primary(self) -> bool:
        return server_replica_is_primary(self.native_object)

    @property
    def term(self) -> str:
        return server_replica_get_term(self.native_object)

    def __str__(self):
        return self.address

    def __repr__(self):
        return f"ServerReplica('{str(self)}')"
