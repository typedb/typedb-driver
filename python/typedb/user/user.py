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

# from __future__ import annotations
#
# from typing import Optional, TYPE_CHECKING
#
# from typedb.native_driver_wrapper import user_get_username, user_get_password_expiry_seconds, user_password_update, \
#     User as NativeUser, TypeDBDriverExceptionNative
#
# from typedb.api.user.user import User
# from typedb.common.exception import TypeDBDriverException, ILLEGAL_STATE
# from typedb.common.native_wrapper import NativeWrapper
#
# if TYPE_CHECKING:
#     from typedb.user.user_manager import _UserManager
#
#
# class _User(User, NativeWrapper[NativeUser]):
#
#     def __init__(self, user: NativeUser, user_manager: _UserManager):
#         super().__init__(user)
#         self._user_manager = user_manager
#
#     @property
#     def _native_object_not_owned_exception(self) -> TypeDBDriverException:
#         return TypeDBDriverException(ILLEGAL_STATE)
#
#     def username(self) -> str:
#         return user_get_username(self.native_object)
#
#     def password_expiry_seconds(self) -> Optional[int]:
#         if res := user_get_password_expiry_seconds(self.native_object) >= 0:
#             return res
#         return None
#
#     def password_update(self, password_old: str, password_new: str) -> None:
#         try:
#             user_password_update(self.native_object, self._user_manager.native_object, password_old, password_new)
#         except TypeDBDriverExceptionNative as e:
#             raise TypeDBDriverException.of(e)
