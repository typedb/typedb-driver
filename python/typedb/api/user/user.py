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


class User(ABC):
    """TypeDB user information"""

    @property
    @abstractmethod
    def name(self) -> str:
        """
        Returns the name of this user.

        :return:
        """
        pass

    # TODO: Not implemented
    # @abstractmethod
    # def password_expiry_seconds(self) -> Optional[int]:
    #     """
    #     Returns the number of seconds remaining till this user's current password expires.
    #
    #     :return:
    #     """
    #     pass

    @abstractmethod
    def update_password(self, password: str) -> None:
        """
        Updates the password for this user.

        :param password_old: The current password of this user
        :param password_new: The new password
        :return:
        """
        pass

    @abstractmethod
    def delete(self) -> None:
        """
        Deletes a user with the given name.

        :param username: The name of the user to be deleted
        :return:

        Examples:
        ---------
        ::

           driver.users.delete(username)
        """
        pass


class UserManager(ABC):
    """
    Provides access to all user management methods.
    """

    @abstractmethod
    def contains(self, username: str) -> bool:
        """
        Checks if a user with the given name exists.

        :param username: The user name to be checked
        :return:

        Examples:
        ---------
        ::

           driver.users.contains(username)
        """
        pass

    @abstractmethod
    def create(self, username: str, password: str) -> None:
        """
        Create a user with the given name and password.

        :param username: The name of the user to be created
        :param password: The password of the user to be created
        :return:

        Examples:
        ---------
        ::

           driver.users.create(username, password)
        """
        pass

    @abstractmethod
    def get(self, username: str) -> Optional[User]:
        """
        Retrieve a user with the given name.

        :param username: The name of the user to retrieve
        :return:

        Examples:
        ---------
        ::

           driver.users.get(username)
        """
        pass

    @abstractmethod
    def get_current_user(self) -> Optional[User]:
        """
        Retrieve the name of the user who opened the current connection.

        :return:

        Examples:
        ---------
        ::

           driver.users.get_current_user()
        """
        pass

    @abstractmethod
    def all(self) -> List[User]:
        """
        Retrieves all users which exist on the TypeDB server.

        :return:

        Examples:
        ---------
        ::

           driver.users.all()

        """
        pass
