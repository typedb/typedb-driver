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
from typing import Optional

from typedb.api.connection.consistency_level import ConsistencyLevel


class Database(ABC):

    @property
    @abstractmethod
    def name(self) -> str:
        """
        The database name as a string.
        """
        pass

    @abstractmethod
    def schema(self, consistency_level: Optional[ConsistencyLevel] = None) -> str:
        """
        Returns a full schema text as a valid TypeQL define query string.

        :param consistency_level: The consistency level to use for the operation. Strongest possible by default

        Examples:
        ---------
        ::

            database.schema()
            database.schema(ConsistencyLevel.Strong())
        """
        pass

    @abstractmethod
    def type_schema(self, consistency_level: Optional[ConsistencyLevel] = None) -> str:
        """
        Returns the types in the schema as a valid TypeQL define query string.

        :param consistency_level: The consistency level to use for the operation. Strongest possible by default

        Examples:
        ---------
        ::

            database.type_schema()
            database.type_schema(ConsistencyLevel.Strong())
        """
        pass

    def export_to_file(self, schema_file_path: str, data_file_path: str,
                       consistency_level: Optional[ConsistencyLevel] = None) -> None:
        """
        Export a database into a schema definition and a data files saved to the disk.
        This is a blocking operation and may take a significant amount of time depending on the database size.

        :param schema_file_path: The path to the schema definition file to be created
        :param data_file_path: The path to the data file to be created
        :param consistency_level: The consistency level to use for the operation. Strongest possible by default

        Examples:
        ---------
        ::

            database.export_to_file("schema.typeql", "data.typedb")
            database.export_to_file("schema.typeql", "data.typedb", ConsistencyLevel.Strong())
        """
        pass

    @abstractmethod
    def delete(self, consistency_level: Optional[ConsistencyLevel] = None) -> None:
        """
        Deletes this database.

        :param consistency_level: The consistency level to use for the operation. Strongest possible by default

        Examples:
        ---------
        ::

            database.delete()
            database.delete(ConsistencyLevel.Strong())
        """
        pass
