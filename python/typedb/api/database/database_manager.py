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
from typing import List, Optional

from typedb.api.connection.consistency_level import ConsistencyLevel


class DatabaseManager(ABC):
    """
    Provides access to all database management methods.
    """

    @abstractmethod
    def all(self, consistency_level: Optional[ConsistencyLevel] = None) -> List[Database]:
        """
        Retrieves all databases present on the TypeDB server.

        :param consistency_level: The consistency level to use for the operation. Strongest possible by default

        Examples:
        ---------
        ::

            driver.databases.all()
            driver.databases.all(ConsistencyLevel.Strong())
        """
        pass

    @abstractmethod
    def contains(self, name: str, consistency_level: Optional[ConsistencyLevel] = None) -> bool:
        """
        Checks if a database with the given name exists.

        :param name: The database name to be checked
        :param consistency_level: The consistency level to use for the operation. Strongest possible by default

        Examples:
        ---------
        ::

            driver.databases.contains(name)
            driver.databases.contains(name, ConsistencyLevel.Strong())
        """
        pass

    @abstractmethod
    def get(self, name: str, consistency_level: Optional[ConsistencyLevel] = None) -> Database:
        """
        Retrieves the database with the given name.

        :param name: The name of the database to retrieve
        :param consistency_level: The consistency level to use for the operation. Strongest possible by default

        Examples:
        ---------
        ::

            driver.databases.get(name)
            driver.databases.get(name, ConsistencyLevel.Strong())
        """
        pass

    @abstractmethod
    def create(self, name: str, consistency_level: Optional[ConsistencyLevel] = None) -> None:
        """
        Creates a database with the given name.

        :param name: The name of the database to be created
        :param consistency_level: The consistency level to use for the operation. Strongest possible by default

        Examples:
        ---------
        ::

            driver.databases.create(name)
            driver.databases.create(name, ConsistencyLevel.Strong())
        """
        pass

    @abstractmethod
    def import_from_file(self, name: str, schema: str, data_file_path: str) -> None:
        """
        Creates a database with the given name based on previously exported another database's data loaded from a file.
        This is a blocking operation and may take a significant amount of time depending on the database size.

        :param name: The name of the database to be created
        :param schema: The schema definition query string for the database
        :param data_file_path: The exported database file path to import the data from

        Examples:
        ---------
        ::

            driver.databases.import_from_file(name, schema, "data.typedb")
        """
        pass
