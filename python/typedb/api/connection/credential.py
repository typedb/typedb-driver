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

from typing import Optional

from typedb.native_client_wrapper import credential_new, Credential as NativeCredential

from typedb.common.exception import TypeDBClientExceptionExt, CLUSTER_CREDENTIAL_INCONSISTENT, ILLEGAL_STATE
from typedb.common.native_wrapper import NativeWrapper


class TypeDBCredential(NativeWrapper[NativeCredential]):

    def __init__(self, username: str, password: str, *, tls_root_ca_path: Optional[str] = None,
                 tls_enabled: bool = True):
        if tls_root_ca_path is not None and not tls_enabled:
            raise TypeDBClientExceptionExt.of(CLUSTER_CREDENTIAL_INCONSISTENT)
        super().__init__(credential_new(username, password, tls_root_ca_path, tls_enabled))

    @property
    def _native_object_not_owned_exception(self) -> TypeDBClientExceptionExt:
        return TypeDBClientExceptionExt.of(ILLEGAL_STATE)
