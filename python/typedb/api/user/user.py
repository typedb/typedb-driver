#
# Copyright (C) 2022 Vaticle
#
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
#

from abc import ABC, abstractmethod
from typing import Optional


class User(ABC):

    @abstractmethod
    def username(self) -> str:
        pass

    @abstractmethod
    def password_expiry_seconds(self) -> Optional[int]:
        pass

    @abstractmethod
    def password_update(self, password_old: str, password_new: str) -> None:
        pass


class UserManager(ABC):

    @abstractmethod
    def contains(self, username: str) -> bool:
        pass

    @abstractmethod
    def create(self, username: str, password: str) -> None:
        pass

    @abstractmethod
    def delete(self, username: str) -> None:
        pass

    @abstractmethod
    def get(self, username: str) -> Optional[User]:
        pass

    @abstractmethod
    def all(self) -> list[User]:
        pass

    @abstractmethod
    def password_set(self, username: str, password: str) -> None:
        pass
