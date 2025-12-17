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

from abc import ABC, abstractmethod
from typing import Optional, List
from typedb.api.user.user import User

from typedb.api.connection.consistency_level import ConsistencyLevel


class UserManager(ABC):
    """
    Provides access to all user management methods.
    """

    @abstractmethod
    def all(self, consistency_level: Optional[ConsistencyLevel] = None) -> List[User]:
        """
        Retrieves all users which exist on the TypeDB server.

        :param consistency_level: The consistency level to use for the operation. Strongest possible by default

        Examples:
        ---------
        ::

           driver.users.all()
           driver.users.all(ConsistencyLevel.Strong())
        """
        pass

    @abstractmethod
    def contains(self, username: str, consistency_level: Optional[ConsistencyLevel] = None) -> bool:
        """
        Checks if a user with the given name exists.

        :param username: The username to be checked
        :param consistency_level: The consistency level to use for the operation. Strongest possible by default

        Examples:
        ---------
        ::

           driver.users.contains(username)
           driver.users.contains(username, ConsistencyLevel.Strong())
        """
        pass

    @abstractmethod
    def get(self, username: str, consistency_level: Optional[ConsistencyLevel] = None) -> Optional[User]:
        """
        Retrieves a user with the given name.

        :param username: The name of the user to retrieve
        :param consistency_level: The consistency level to use for the operation. Strongest possible by default

        Examples:
        ---------
        ::

           driver.users.get(username)
           driver.users.get(username, ConsistencyLevel.Strong())
        """
        pass

    @abstractmethod
    def get_current(self, consistency_level: Optional[ConsistencyLevel] = None) -> Optional[User]:
        """
        Retrieves the name of the user who opened the current connection.

        :param consistency_level: The consistency level to use for the operation. Strongest possible by default

        Examples:
        ---------
        ::

           driver.users.get_current()
           driver.users.get_current(ConsistencyLevel.Strong())
        """
        pass

    @abstractmethod
    def create(self, username: str, password: str, consistency_level: Optional[ConsistencyLevel] = None) -> None:
        """
        Creates a user with the given name and password.

        :param username: The name of the user to be created
        :param password: The password of the user to be created
        :param consistency_level: The consistency level to use for the operation. Strongest possible by default

        Examples:
        ---------
        ::

           driver.users.create(username, password)
           driver.users.create(username, password, ConsistencyLevel.Strong())
        """
        pass
