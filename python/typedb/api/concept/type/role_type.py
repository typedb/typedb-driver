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
from typing import TYPE_CHECKING, Iterator, Optional

from typedb.api.concept.type.type import Type
from typedb.common.transitivity import Transitivity

if TYPE_CHECKING:
    from typedb.api.concept.thing.thing import Thing
    from typedb.api.concept.thing.relation import Relation
    from typedb.api.concept.type.relation_type import RelationType
    from typedb.api.concept.type.thing_type import ThingType
    from typedb.api.connection.transaction import TypeDBTransaction


class RoleType(Type, ABC):

    def is_role_type(self) -> bool:
        return True

    def as_role_type(self) -> RoleType:
        return self

    @abstractmethod
    def get_supertype(self, transaction: TypeDBTransaction) -> Optional[RoleType]:
        pass

    @abstractmethod
    def get_supertypes(self, transaction: TypeDBTransaction) -> Iterator[RoleType]:
        pass

    @abstractmethod
    def get_subtypes(self, transaction: TypeDBTransaction, transitivity: Transitivity = Transitivity.TRANSITIVE
                     ) -> Iterator[RoleType]:
        pass

    @abstractmethod
    def get_relation_type(self, transaction: TypeDBTransaction) -> RelationType:
        pass

    @abstractmethod
    def get_relation_types(self, transaction: TypeDBTransaction) -> Iterator[RelationType]:
        pass

    @abstractmethod
    def get_player_types(self, transaction: TypeDBTransaction, transitivity: Transitivity = Transitivity.TRANSITIVE
                         ) -> Iterator[ThingType]:
        pass

    @abstractmethod
    def get_relation_instances(self, transaction: TypeDBTransaction,
                               transitivity: Transitivity = Transitivity.TRANSITIVE) -> Iterator[Relation]:
        pass

    @abstractmethod
    def get_player_instances(self, transaction: TypeDBTransaction,
                             transitivity: Transitivity = Transitivity.TRANSITIVE) -> Iterator[Thing]:
        pass
