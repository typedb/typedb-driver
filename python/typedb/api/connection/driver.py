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

from abc import ABC, abstractmethod
from typing import TYPE_CHECKING, Optional, Set, Mapping

if TYPE_CHECKING:
    from typedb.api.connection.consistency_level import ConsistencyLevel
    from typedb.api.database.database_manager import DatabaseManager
    from typedb.api.connection.transaction_options import TransactionOptions
    from typedb.api.connection.transaction import Transaction, TransactionType
    from typedb.api.user.user_manager import UserManager
    from typedb.api.server.server_replica import ServerReplica
    from typedb.api.server.server_version import ServerVersion


class Driver(ABC):
    LANGUAGE = "python"

    @abstractmethod
    def is_open(self) -> bool:
        """
        Checks whether this connection is presently open.

        Examples:
        ---------
        ::

            driver.is_open()
        """
        pass

    @property
    @abstractmethod
    def databases(self) -> DatabaseManager:
        """
        The ``DatabaseManager`` for this connection, providing access to database management methods.
        """
        pass

    @property
    @abstractmethod
    def users(self) -> UserManager:
        """
        The ``UserManager`` for this connection, providing access to user management methods.
        """
        pass

    @abstractmethod
    def server_version(self, consistency_level: Optional[ConsistencyLevel] = None) -> ServerVersion:
        """
        Retrieves the server's version.

        :param consistency_level: The consistency level to use for the operation. Strongest possible by default

        Examples:
        ---------
        ::

            driver.server_version()
            driver.server_version(ConsistencyLevel.Strong())
        """
        pass

    @abstractmethod
    def transaction(self, database_name: str, transaction_type: TransactionType,
                    options: Optional[TransactionOptions] = None) -> Transaction:
        """
        Opens a communication tunnel (transaction) to the given database on the running TypeDB server.

        :param database_name: The name of the database with which the transaction connects
        :param transaction_type: The type of transaction to be created (READ, WRITE, or SCHEMA)
        :param options: ``TransactionOptions`` to configure the opened transaction

        Examples:
        ---------
        ::

            driver.transaction(database, transaction_type, options)
        """
        pass

    @abstractmethod
    def replicas(self, consistency_level: Optional[ConsistencyLevel] = None) -> Set[ServerReplica]:
        """
        Set of ``Replica`` instances for this driver connection.

        :param consistency_level: The consistency level to use for the operation. Strongest possible by default

        Examples:
        ---------
        ::

            driver.replicas()
            driver.replicas(ConsistencyLevel.Strong())
        """
        pass

    @abstractmethod
    def primary_replica(self, consistency_level: Optional[ConsistencyLevel] = None) -> Optional[ServerReplica]:
        """
        Returns the primary replica for this driver connection.

        :param consistency_level: The consistency level to use for the operation. Strongest possible by default

        Examples:
        ---------
        ::

            driver.primary_replica()
            driver.primary_replica(ConsistencyLevel.Strong())
        """
        pass

    @abstractmethod
    def register_replica(self, replica_id: int, address: str) -> None:
        """
        Registers a new replica in the cluster the driver is currently connected to. The registered
        replica will become available eventually, depending on the behavior of the whole cluster.
        To register a replica, its clustering address should be passed, not the connection address.

        :param replica_id: The numeric identifier of the new replica
        :param address: The address(es) of the TypeDB replica as a string

        Examples:
        ---------
        ::

            driver.register_replica(2, "127.0.0.1:11729")
        """
        pass

    @abstractmethod
    def deregister_replica(self, replica_id: int) -> None:
        """
        Deregisters a replica from the cluster the driver is currently connected to. This replica
        will no longer play a raft role in this cluster.

        :param replica_id: The numeric identifier of the deregistered replica

        Examples:
        ---------
        ::

            driver.deregister_replica(2)
        """
        pass

    @abstractmethod
    def update_address_translation(self, address_translation: Mapping[str, str]) -> None:
        """
        Updates address translation of the driver. This lets you actualize new translation
        information without recreating the driver from scratch. Useful after registering new
        replicas requiring address translation.
        This operation will update existing connections using the provided addresses.

        :param address_translation: The translation of public TypeDB cluster replica addresses (keys) to server-side private addresses (values)

        Examples:
        ---------
        ::

            driver.update_address_translation({"typedb-cloud.ext:11729": "127.0.0.1:11729"})
        """
        pass

    @abstractmethod
    def close(self) -> None:
        """
        Closes the driver. Before instantiating a new driver, the driver that’s currently open should first be closed.

        Examples:
        ---------
        ::

            driver.close()
        """
        pass

    @abstractmethod
    def __enter__(self):
        pass

    @abstractmethod
    def __exit__(self, exc_type, exc_val, exc_tb):
        pass
