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

from typedb.api.server.replication_role import ReplicationRole
from typedb.api.server.server import Server
from typedb.common.exception import TypeDBDriverException, NULL_NATIVE_OBJECT, ILLEGAL_STATE
from typedb.common.native_wrapper import NativeWrapper
from typedb.native_driver_wrapper import (server_has_role, server_has_term, server_get_address, \
                                          server_get_id, server_get_role, server_is_primary,
                                          server_get_term, \
                                          Server as NativeServer, TypeDBDriverExceptionNative)


class _Server(Server, NativeWrapper[NativeServer]):

    def __init__(self, server: NativeServer):
        if not server:
            raise TypeDBDriverException(NULL_NATIVE_OBJECT)
        super().__init__(server)

    @property
    def _native_object_not_owned_exception(self) -> TypeDBDriverException:
        return TypeDBDriverException(ILLEGAL_STATE)

    @property
    def id(self) -> int:
        return server_get_id(self.native_object)

    @property
    def address(self) -> str:
        return server_get_address(self.native_object)

    @property
    def role(self) -> Optional[ReplicationRole]:
        return ReplicationRole(server_get_role(self.native_object)) \
            if server_has_role(self.native_object) else None

    def is_primary(self) -> bool:
        return server_is_primary(self.native_object)

    @property
    def term(self) -> int:
        return server_get_term(self.native_object) \
            if server_has_term(self.native_object) else None

    def __str__(self):
        return self.address

    def __repr__(self):
        return f"Server('{str(self)}')"
