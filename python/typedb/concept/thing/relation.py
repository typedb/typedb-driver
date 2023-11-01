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

from typing import Iterator, Any, TYPE_CHECKING

from typedb.native_driver_wrapper import (
    TypeDBDriverExceptionNative, concept_iterator_next, relation_add_role_player, relation_get_players_by_role_type,
    relation_get_relating, relation_get_role_players, relation_get_type, relation_remove_role_player, role_player_get_player,
    role_player_get_role_type, role_player_iterator_next,
)

from typedb.api.concept.thing.relation import Relation
from typedb.common.exception import TypeDBDriverException
from typedb.common.iterator_wrapper import IteratorWrapper
from typedb.common.promise import Promise
from typedb.concept.concept_factory import wrap_relation_type, wrap_role_type, wrap_thing
from typedb.concept.thing.thing import _Thing
from typedb.concept.type.role_type import _RoleType
from typedb.native_driver_wrapper import void_promise_resolve

if TYPE_CHECKING:
    from typedb.concept.type.relation_type import _RelationType
    from typedb.connection.transaction import _Transaction


class _Relation(Relation, _Thing):
    def get_type(self) -> _RelationType:
        return wrap_relation_type(relation_get_type(self.native_object))

    def add_player(self, transaction: _Transaction, role_type: _RoleType, player: _Thing) -> Promise[None]:
        promise = relation_add_role_player(
            transaction.native_object, self.native_object, role_type.native_object, player.native_object
        )
        return Promise(lambda: void_promise_resolve(promise))

    def remove_player(self, transaction: _Transaction, role_type: _RoleType, player: _Thing) -> Promise[None]:
        promise = relation_remove_role_player(
            transaction.native_object, self.native_object, role_type.native_object, player.native_object
        )
        return Promise(lambda: void_promise_resolve(promise))

    def get_players_by_role_type(self, transaction: _Transaction, *role_types: _RoleType) -> Iterator[Any]:
        try:
            native_role_types = [rt.native_object for rt in role_types]
            return map(
                wrap_thing,
                IteratorWrapper(
                    relation_get_players_by_role_type(transaction.native_object, self.native_object, native_role_types),
                    concept_iterator_next
                )
            )
        except TypeDBDriverExceptionNative as e:
            raise TypeDBDriverException.of(e)

    def get_players(self, transaction: _Transaction) -> dict[_RoleType, list[_Thing]]:
        try:
            role_players = {}
            for role_player in IteratorWrapper(relation_get_role_players(transaction.native_object, self.native_object),
                                               role_player_iterator_next):
                role = wrap_role_type(role_player_get_role_type(role_player))
                player = wrap_thing(role_player_get_player(role_player))
                role_players.setdefault(role, [])
                role_players[role].append(player)
            return role_players
        except TypeDBDriverExceptionNative as e:
            raise TypeDBDriverException.of(e)

    def get_relating(self, transaction: _Transaction) -> Iterator[_RoleType]:
        try:
            return map(
                wrap_role_type,
                IteratorWrapper(relation_get_relating(transaction.native_object, self.native_object), concept_iterator_next)
            )
        except TypeDBDriverExceptionNative as e:
            raise TypeDBDriverException.of(e)
