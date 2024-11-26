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

from typedb.api.connection.driver import Driver
# from typedb.api.connection.options import Options
from typedb.common.exception import TypeDBDriverException, DRIVER_CLOSED
from typedb.common.native_wrapper import NativeWrapper
from typedb.connection.database_manager import _DatabaseManager
from typedb.connection.transaction import _Transaction
from typedb.native_driver_wrapper import driver_open_core, driver_is_open, driver_force_close, \
    TypeDBDriver as NativeDriver, TypeDBDriverExceptionNative
from typedb.user.user_manager import _UserManager

if TYPE_CHECKING:
    from typedb.connection.connection_settings import ConnectionSettings
    from typedb.api.connection.credential import Credential
    from typedb.api.connection.transaction import TransactionType
    from typedb.api.user.user import UserManager


class _Driver(Driver, NativeWrapper[NativeDriver]):

    def __init__(self, is_cloud: bool, addresses: list[str] | dict[str], credential: Credential,
                 connection_settings: ConnectionSettings):
        try:
            # if is_cloud:
            # if isinstance(addresses, list):
            #     native_connection = driver_open_cloud(addresses, credential.native_object)
            # else:
            #     public_addresses = list(addresses.keys())
            #     private_addresses = [addresses[public] for public in public_addresses]
            #     native_connection = driver_open_cloud_translated(
            #         public_addresses, private_addresses, credential.native_object)
            # else:
            assert not is_cloud, "Cloud is not implemented"
            native_driver = driver_open_core(addresses[0], credential.native_object, connection_settings.native_object,
                                             Driver.LANGUAGE)
        except TypeDBDriverExceptionNative as e:
            raise TypeDBDriverException.of(e) from None
        super().__init__(native_driver)

    @classmethod
    def core(cls, address: str, credential: Credential, connection_settings: ConnectionSettings):
        return cls(is_cloud=False, addresses=[address], credential=credential, connection_settings=connection_settings)

    @classmethod
    def cloud(cls, addresses: list[str] | dict[str], credential: Credential, connection_settings: ConnectionSettings):
        return cls(is_cloud=True, addresses=addresses, credential=credential, connection_settings=connection_settings)

    @property
    def _native_object_not_owned_exception(self) -> TypeDBDriverException:
        return TypeDBDriverException(DRIVER_CLOSED)

    @property
    def _native_driver(self) -> NativeDriver:
        return self.native_object

    def transaction(self, database_name: str,
                    transaction_type: TransactionType) -> Transaction:  # , options: Options = None
        return _Transaction(self, database_name, transaction_type)  # , options if options else Options()

    def is_open(self) -> bool:
        return driver_is_open(self._native_driver)

    @property
    def databases(self) -> _DatabaseManager:
        return _DatabaseManager(self._native_driver)

    @property
    def users(self) -> UserManager:
        return _UserManager(self._native_driver)

    def __enter__(self):
        return self

    def __exit__(self, exc_type, exc_val, exc_tb):
        self.close()
        if exc_tb is not None:
            return False

    def close(self) -> None:
        driver_force_close(self._native_driver)
