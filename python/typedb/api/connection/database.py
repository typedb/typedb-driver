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

from __future__ import annotations

from abc import ABC, abstractmethod
from typing import Optional


class Database(ABC):

    @property
    @abstractmethod
    def name(self) -> str:
        pass

    @abstractmethod
    def schema(self) -> str:
        pass

    def rule_schema(self) -> str:
        pass

    def type_schema(self) -> str:
        pass

    @abstractmethod
    def delete(self) -> None:
        pass

    @abstractmethod
    def replicas(self) -> set[Replica]:
        pass

    @abstractmethod
    def primary_replica(self) -> Optional[Replica]:
        pass

    @abstractmethod
    def preferred_replica(self) -> Optional[Replica]:
        pass


class Replica(ABC):

    @abstractmethod
    def database(self) -> Database:
        pass

    @abstractmethod
    def address(self) -> str:
        pass

    @abstractmethod
    def is_primary(self) -> bool:
        pass

    @abstractmethod
    def is_preferred(self) -> bool:
        pass

    @abstractmethod
    def term(self) -> int:
        pass


class DatabaseManager(ABC):

    @abstractmethod
    def get(self, name: str) -> Database:
        pass

    @abstractmethod
    def contains(self, name: str) -> bool:
        pass

    @abstractmethod
    def create(self, name: str) -> None:
        pass

    @abstractmethod
    def all(self) -> list[Database]:
        pass
