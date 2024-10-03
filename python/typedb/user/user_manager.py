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
# from typing import TYPE_CHECKING, Optional
#
# from typedb.native_driver_wrapper import user_manager_new, users_contains, users_create, users_delete, users_all, \
#     users_get, users_set_password, users_current_user, user_iterator_next, UserManager as NativeUserManager, \
#     TypeDBDriverExceptionNative
#
# from typedb.api.user.user import UserManager
# from typedb.common.exception import TypeDBDriverException, ILLEGAL_STATE
# from typedb.common.iterator_wrapper import IteratorWrapper
# from typedb.common.native_wrapper import NativeWrapper
# from typedb.user.user import _User
#
# if TYPE_CHECKING:
#     from typedb.api.user.user import User
#     from typedb.native_driver_wrapper import TypeDBDriver as NativeDriver
#
#
# class _UserManager(UserManager, NativeWrapper[NativeUserManager]):
#
#     def __init__(self, connection: NativeDriver):
#         super().__init__(user_manager_new(connection))
#
#     @property
#     def _native_object_not_owned_exception(self) -> TypeDBDriverException:
#         return TypeDBDriverException(ILLEGAL_STATE)
#
#     def contains(self, username: str) -> bool:
#         try:
#             return users_contains(self.native_object, username)
#         except TypeDBDriverExceptionNative as e:
#             raise TypeDBDriverException.of(e)
#
#     def create(self, username: str, password: str) -> None:
#         try:
#             users_create(self.native_object, username, password)
#         except TypeDBDriverExceptionNative as e:
#             raise TypeDBDriverException.of(e)
#
#     def delete(self, username: str) -> None:
#         try:
#             users_delete(self.native_object, username)
#         except TypeDBDriverExceptionNative as e:
#             raise TypeDBDriverException.of(e)
#
#     def all(self) -> list[User]:
#         try:
#             return [_User(user, self) for user in IteratorWrapper(users_all(self.native_object), user_iterator_next)]
#         except TypeDBDriverExceptionNative as e:
#             raise TypeDBDriverException.of(e)
#
#     def get(self, username: str) -> Optional[User]:
#         try:
#             if user := users_get(self.native_object, username):
#                 return _User(user, self)
#             return None
#         except TypeDBDriverExceptionNative as e:
#             raise TypeDBDriverException.of(e)
#
#     def password_set(self, username: str, password: str) -> None:
#         try:
#             users_set_password(self.native_object, username, password)
#         except TypeDBDriverExceptionNative as e:
#             raise TypeDBDriverException.of(e)
#
#     def get_current_user(self) -> User:
#         try:
#             return _User(users_current_user(self.native_object), self)
#         except TypeDBDriverExceptionNative as e:
#             raise TypeDBDriverException.of(e)
