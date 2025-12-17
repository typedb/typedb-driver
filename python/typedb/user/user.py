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

from typing import TYPE_CHECKING, Optional

from typedb.api.connection.consistency_level import ConsistencyLevel

from typedb.api.user.user import User
from typedb.common.exception import TypeDBDriverException, ILLEGAL_STATE
from typedb.common.native_wrapper import NativeWrapper
from typedb.common.validation import require_non_null
from typedb.native_driver_wrapper import user_get_name, user_update_password, user_delete, User as NativeUser, \
    TypeDBDriverExceptionNative

if TYPE_CHECKING:
    from typedb.user.user_manager import _UserManager


class _User(User, NativeWrapper[NativeUser]):

    def __init__(self, user: NativeUser, user_manager: _UserManager):
        super().__init__(user)
        self._user_manager = user_manager

    @property
    def _native_object_not_owned_exception(self) -> TypeDBDriverException:
        return TypeDBDriverException(ILLEGAL_STATE)

    @property
    def name(self) -> str:
        return user_get_name(self.native_object)

    def update_password(self, password: str, consistency_level: Optional[ConsistencyLevel] = None) -> None:
        require_non_null(password, "password")
        try:
            user_update_password(self.native_object, password, ConsistencyLevel.native_value(consistency_level))
        except TypeDBDriverExceptionNative as e:
            raise TypeDBDriverException.of(e) from None

    def delete(self, consistency_level: Optional[ConsistencyLevel] = None) -> None:
        try:
            user_delete(self.native_object, ConsistencyLevel.native_value(consistency_level))
        except TypeDBDriverExceptionNative as e:
            raise TypeDBDriverException.of(e) from None
