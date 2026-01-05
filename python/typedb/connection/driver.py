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

from typing import TYPE_CHECKING, Optional, Tuple

from typedb.api.connection.consistency_level import ConsistencyLevel
from typedb.api.connection.driver import Driver
from typedb.api.connection.transaction import Transaction
from typedb.api.connection.transaction_options import TransactionOptions
from typedb.api.server.server_version import ServerVersion
from typedb.common.exception import TypeDBDriverException, DRIVER_CLOSED, INVALID_ADDRESS_FORMAT
from typedb.common.iterator_wrapper import IteratorWrapper
from typedb.common.native_wrapper import NativeWrapper
from typedb.common.validation import require_non_null, require_non_negative
from typedb.database.database_manager import _DatabaseManager
from typedb.connection.server_replica import _ServerReplica
from typedb.connection.transaction import _Transaction
from typedb.native_driver_wrapper import driver_new_with_description, driver_new_with_addresses_with_description, \
    driver_new_with_address_translation_with_description, driver_is_open, driver_force_close, driver_register_replica, \
    driver_deregister_replica, driver_replicas, driver_primary_replica, driver_server_version, \
    driver_update_address_translation, server_replica_iterator_next, TypeDBDriver as NativeDriver, \
    TypeDBDriverExceptionNative
from typedb.user.user_manager import _UserManager

if TYPE_CHECKING:
    from typedb.api.connection.driver_options import DriverOptions
    from typedb.api.connection.credentials import Credentials
    from typedb.api.connection.transaction import Transaction, TransactionType
    from typedb.api.user.user_manager import UserManager
    from typedb.api.server.server_replica import ServerReplica


class _Driver(Driver, NativeWrapper[NativeDriver]):

    def __init__(self, addresses: str | list[str] | dict[str, str], credentials: Credentials,
                 driver_options: DriverOptions):
        require_non_null(addresses, "addresses")
        require_non_null(credentials, "credentials")
        require_non_null(driver_options, "driver_options")

        try:
            if isinstance(addresses, str):
                native_driver = driver_new_with_description(addresses, credentials.native_object,
                                                            driver_options.native_object,
                                                            Driver.LANGUAGE)
            elif isinstance(addresses, list):
                native_driver = driver_new_with_addresses_with_description(addresses, credentials.native_object,
                                                                           driver_options.native_object,
                                                                           Driver.LANGUAGE)
            elif isinstance(addresses, dict):
                public_addresses, private_addresses = _Driver._get_translated_addresses(addresses)
                native_driver = driver_new_with_address_translation_with_description(public_addresses,
                                                                                     private_addresses,
                                                                                     credentials.native_object,
                                                                                     driver_options.native_object,
                                                                                     Driver.LANGUAGE)
            else:
                raise TypeDBDriverException(INVALID_ADDRESS_FORMAT)
        except TypeDBDriverExceptionNative as e:
            raise TypeDBDriverException.of(e) from None
        super().__init__(native_driver)

    @property
    def _native_object_not_owned_exception(self) -> TypeDBDriverException:
        return TypeDBDriverException(DRIVER_CLOSED)

    @property
    def _native_driver(self) -> NativeDriver:
        return self.native_object

    def is_open(self) -> bool:
        return driver_is_open(self._native_driver)

    @property
    def databases(self) -> _DatabaseManager:
        return _DatabaseManager(self._native_driver)

    @property
    def users(self) -> UserManager:
        return _UserManager(self._native_driver)

    def server_version(self, consistency_level: Optional[ConsistencyLevel] = None) -> ServerVersion:
        try:
            consistency_level = ConsistencyLevel.native_value(consistency_level)
            return ServerVersion(driver_server_version(self._native_driver, consistency_level))
        except TypeDBDriverExceptionNative as e:
            raise TypeDBDriverException.of(e) from None

    def transaction(self, database_name: str, transaction_type: TransactionType,
                    options: Optional[TransactionOptions] = None) -> Transaction:
        require_non_null(database_name, "database_name")
        require_non_null(transaction_type, "transaction_type")
        return _Transaction(self, database_name, transaction_type, options if options else TransactionOptions())

    def replicas(self, consistency_level: Optional[ConsistencyLevel] = None) -> set[ServerReplica]:
        try:
            consistency_level = ConsistencyLevel.native_value(consistency_level)
            replica_iter = IteratorWrapper(driver_replicas(self._native_driver, consistency_level),
                                           server_replica_iterator_next)
            return set(_ServerReplica(server_replica) for server_replica in replica_iter)
        except TypeDBDriverExceptionNative as e:
            raise TypeDBDriverException.of(e) from None

    def primary_replica(self, consistency_level: Optional[ConsistencyLevel] = None) -> Optional[ServerReplica]:
        consistency_level = ConsistencyLevel.native_value(consistency_level)
        if res := driver_primary_replica(self._native_driver, consistency_level):
            return _ServerReplica(res)
        return None

    def register_replica(self, replica_id: int, address: str) -> None:
        require_non_negative(replica_id, "replica_id")
        require_non_null(address, "address")
        try:
            driver_register_replica(self._native_driver, replica_id, address)
        except TypeDBDriverExceptionNative as e:
            raise TypeDBDriverException.of(e) from None

    def deregister_replica(self, replica_id: int) -> None:
        require_non_negative(replica_id, "replica_id")
        try:
            driver_deregister_replica(self._native_driver, replica_id)
        except TypeDBDriverExceptionNative as e:
            raise TypeDBDriverException.of(e) from None

    def update_address_translation(self, address_translation: dict[str, str]) -> None:
        require_non_null(address_translation, "address_translation")
        public_addresses, private_addresses = _Driver._get_translated_addresses(address_translation)
        try:
            driver_update_address_translation(self._native_driver, public_addresses, private_addresses)
        except TypeDBDriverExceptionNative as e:
            raise TypeDBDriverException.of(e) from None

    def close(self) -> None:
        driver_force_close(self._native_driver)

    @classmethod
    def _get_translated_addresses(cls, address_translation: dict[str, str]) -> Tuple[list[str], list[str]]:
        public_addresses = list(address_translation.keys())
        private_addresses = [address_translation[public] for public in public_addresses]
        return public_addresses, private_addresses

    def __enter__(self):
        return self

    def __exit__(self, exc_type, exc_val, exc_tb):
        self.close()
        if exc_tb is not None:
            return False
