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


class UserManager(ABC):
    """
    Provides access to all user management methods.
    """

    @abstractmethod
    def all(self) -> List[User]:
        """
        Retrieves all users which exist on the TypeDB server.

        Examples:
        ---------
        ::

           driver.users.all()
        """
        pass

    @abstractmethod
    def contains(self, username: str) -> bool:
        """
        Checks if a user with the given name exists.

        :param username: The username to be checked

        Examples:
        ---------
        ::

           driver.users.contains(username)
        """
        pass

    @abstractmethod
    def get(self, username: str) -> Optional[User]:
        """
        Retrieves a user with the given name.

        :param username: The name of the user to retrieve

        Examples:
        ---------
        ::

           driver.users.get(username)
        """
        pass

    @abstractmethod
    def get_current(self) -> Optional[User]:
        """
        Retrieves the name of the user who opened the current connection.

        Examples:
        ---------
        ::

           driver.users.get_current()
        """
        pass

    @abstractmethod
    def create(self, username: str, password: str) -> None:
        """
        Creates a user with the given name and password.

        :param username: The name of the user to be created
        :param password: The password of the user to be created

        Examples:
        ---------
        ::

           driver.users.create(username, password)
        """
        pass
