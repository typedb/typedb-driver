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
from typing import TYPE_CHECKING, Iterator

from typedb.api.concept.thing.thing import Thing

if TYPE_CHECKING:
    from typedb.api.concept.type.role_type import RoleType
    from typedb.api.concept.type.relation_type import RelationType
    from typedb.api.connection.transaction import TypeDBTransaction


class Relation(Thing, ABC):

    def is_relation(self) -> bool:
        return True

    def as_relation(self) -> Relation:
        return self

    @abstractmethod
    def get_type(self) -> RelationType:
        pass

    @abstractmethod
    def add_player(self, transaction: TypeDBTransaction, role_type: RoleType, player: Thing) -> None:
        pass

    @abstractmethod
    def remove_player(self, transaction: TypeDBTransaction, role_type: RoleType, player: Thing) -> None:
        pass

    @abstractmethod
    def get_players_by_role_type(self, transaction: TypeDBTransaction, *role_types: RoleType) -> Iterator[Thing]:
        pass

    @abstractmethod
    def get_players(self, transaction: TypeDBTransaction) -> dict[RoleType, list[Thing]]:
        pass

    @abstractmethod
    def get_relating(self, transaction: TypeDBTransaction) -> Iterator[RoleType]:
        pass
