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
from typedb.api.user.user_manager import UserManager
from typedb.common.exception import TypeDBDriverException
from typedb.common.iterator_wrapper import IteratorWrapper
from typedb.common.validation import require_non_null
from typedb.native_driver_wrapper import users_contains, users_create, users_all, users_get, \
    users_get_current, user_iterator_next, TypeDBDriverExceptionNative
from typedb.user.user import _User

if TYPE_CHECKING:
    from typedb.api.user.user import User
    from typedb.native_driver_wrapper import TypeDBDriver as NativeDriver


class _UserManager(UserManager):

    def __init__(self, native_driver: NativeDriver):
        self.native_driver = native_driver

    def all(self, consistency_level: Optional[ConsistencyLevel] = None) -> list[User]:
        try:
            native_users = users_all(self.native_driver, ConsistencyLevel.native_value(consistency_level))
            return [_User(user, self) for user in IteratorWrapper(native_users, user_iterator_next)]
        except TypeDBDriverExceptionNative as e:
            raise TypeDBDriverException.of(e) from None

    def contains(self, username: str, consistency_level: Optional[ConsistencyLevel] = None) -> bool:
        require_non_null(username, "username")
        try:
            return users_contains(self.native_driver, username, ConsistencyLevel.native_value(consistency_level))
        except TypeDBDriverExceptionNative as e:
            raise TypeDBDriverException.of(e) from None

    def get(self, username: str, consistency_level: Optional[ConsistencyLevel] = None) -> Optional[User]:
        require_non_null(username, "username")
        try:
            if user := users_get(self.native_driver, username, ConsistencyLevel.native_value(consistency_level)):
                return _User(user, self)
            return None
        except TypeDBDriverExceptionNative as e:
            raise TypeDBDriverException.of(e) from None

    def get_current(self, consistency_level: Optional[ConsistencyLevel] = None) -> Optional[User]:
        try:
            if user := users_get_current(self.native_driver, ConsistencyLevel.native_value(consistency_level)):
                return _User(user, self)
            return None
        except TypeDBDriverExceptionNative as e:
            raise TypeDBDriverException.of(e) from None

    def create(self, username: str, password: str, consistency_level: Optional[ConsistencyLevel] = None) -> None:
        require_non_null(username, "username")
        require_non_null(password, "password")
        try:
            users_create(self.native_driver, username, password, ConsistencyLevel.native_value(consistency_level))
        except TypeDBDriverExceptionNative as e:
            raise TypeDBDriverException.of(e) from None
