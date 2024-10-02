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
from typing import List


class Database(ABC):

    @property
    @abstractmethod
    def name(self) -> str:
        """
        The database name as a string.
        """
        pass

    @abstractmethod
    def schema(self) -> str:
        """
        Returns a full schema text as a valid TypeQL define query string.

        :return:

        Examples:
        ---------
        ::

            database.schema()
        """
        pass

    def type_schema(self) -> str:
        """
        Returns the types in the schema as a valid TypeQL define query string.

        :return:

        Examples:
        ---------
        ::

            database.type_schema()
        """
        pass

    @abstractmethod
    def delete(self) -> None:
        """
        Deletes this database.

        :return:

        Examples:
        ---------
        ::

            database.delete()
        """
        pass

    # @abstractmethod
    # def replicas(self) -> Set[Replica]:
    #     """
    #     Set of ``Replica`` instances for this database.
    #     *Only works in TypeDB Cloud*
    #
    #     :return:
    #
    #     Examples:
    #     ---------
    #     ::
    #
    #         database.replicas()
    #     """
    #     pass
    #
    # @abstractmethod
    # def primary_replica(self) -> Optional[Replica]:
    #     """
    #     Returns the primary replica for this database.
    #     *Only works in TypeDB Cloud*
    #
    #     :return:
    #
    #     Examples:
    #     ---------
    #     ::
    #
    #         database.primary_replica()
    #     """
    #     pass
    #
    # @abstractmethod
    # def preferred_replica(self) -> Optional[Replica]:
    #     """
    #     Returns the preferred replica for this database.
    #     Operations which can be run on any replica will prefer to use this replica.
    #     *Only works in TypeDB Cloud*
    #
    #     :return:
    #
    #     Examples:
    #     ---------
    #     ::
    #
    #         database.preferred_replica()
    #     """
    #     pass


#
#
# class Replica(ABC):
#     """
#     The metadata and state of an individual raft replica of a database.
#     """
#
#     @abstractmethod
#     def database(self) -> Database:
#         """
#         Retrieves the database for which this is a replica
#
#         :return:
#         """
#         pass
#
#     @abstractmethod
#     def server(self) -> str:
#         """
#         The server hosting this replica
#
#         :return:
#         """
#         pass
#
#     @abstractmethod
#     def is_primary(self) -> bool:
#         """
#         Checks whether this is the primary replica of the raft cluster.
#
#         :return:
#         """
#
#         pass
#
#     @abstractmethod
#     def is_preferred(self) -> bool:
#         """
#         Checks whether this is the preferred replica of the raft cluster.
#         If true, Operations which can be run on any replica will prefer to use this replica.
#
#         :return:
#         """
#         pass
#
#     @abstractmethod
#     def term(self) -> int:
#         """
#         The raft protocol 'term' of this replica.
#
#         :return:
#         """
#         pass


class DatabaseManager(ABC):
    """
    Provides access to all database management methods.
    """

    @abstractmethod
    def get(self, name: str) -> Database:
        """
        Retrieve the database with the given name.

        :param name: The name of the database to retrieve
        :return:

        Examples:
        ---------
        ::

            driver.databases.get(name)
        """
        pass

    @abstractmethod
    def contains(self, name: str) -> bool:
        """
        Checks if a database with the given name exists

        :param name: The database name to be checked
        :return:

        Examples:
        ---------
        ::

            driver.databases.contains(name)
        """
        pass

    @abstractmethod
    def create(self, name: str) -> None:
        """
        Create a database with the given name

        :param name: The name of the database to be created
        :return:

        Examples:
        ---------
        ::

            driver.databases.create(name)
        """
        pass

    @abstractmethod
    def all(self) -> List[Database]:
        """
        Retrieves all databases present on the TypeDB server

        :return:

        Examples:
        ---------
        ::

            driver.databases.all()
        """
        pass
