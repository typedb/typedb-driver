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

import enum
from abc import ABC, abstractmethod
from typing import TYPE_CHECKING

if TYPE_CHECKING:
    from typedb.api.concept.concept_manager import ConceptManager
    from typedb.api.logic.logic_manager import LogicManager
    from typedb.api.connection.options import TypeDBOptions
    from typedb.api.query.query_manager import QueryManager


class TransactionType(enum.Enum):
    READ = 0
    WRITE = 1

    def is_read(self):
        return self is TransactionType.READ

    def is_write(self):
        return self is TransactionType.WRITE


class TypeDBTransaction(ABC):

    @abstractmethod
    def is_open(self) -> bool:
        pass

    @property
    @abstractmethod
    def transaction_type(self) -> TransactionType:
        pass

    @property
    @abstractmethod
    def options(self) -> TypeDBOptions:
        pass

    @property
    @abstractmethod
    def concepts(self) -> ConceptManager:
        pass

    @property
    @abstractmethod
    def logic(self) -> LogicManager:
        pass

    @property
    @abstractmethod
    def query(self) -> QueryManager:
        pass

    @abstractmethod
    def commit(self) -> None:
        pass

    @abstractmethod
    def rollback(self) -> None:
        pass

    @abstractmethod
    def on_close(self, function: callable):
        pass

    @abstractmethod
    def close(self) -> None:
        pass

    @abstractmethod
    def __enter__(self):
        pass

    @abstractmethod
    def __exit__(self, exc_type, exc_val, exc_tb):
        pass
