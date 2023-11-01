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
    from typedb.common.promise import Promise


class Relation(Thing, ABC):
    """
    Relation is an instance of a relation type and can be uniquely addressed
    by a combination of its type, owned attributes and role players.
    """

    def is_relation(self) -> bool:
        """
        Checks if the concept is a ``Relation``.

        :return:

        Examples
        --------
        ::

            relation.is_relation()
        """
        return True

    def as_relation(self) -> Relation:
        """
        Casts the concept to ``Relation``.

        :return:

        Examples
        --------
        ::

            relation.as_relation()
        """
        return self

    @abstractmethod
    def get_type(self) -> RelationType:
        """
        Retrieves the type which this ``Relation`` belongs to.

        :return:

        Examples
        --------
        ::

            relation.get_type()
        """
        pass

    @abstractmethod
    def add_player(self, transaction: TypeDBTransaction, role_type: RoleType, player: Thing) -> Promise[None]:
        """
        Adds a new role player to play the given role in this ``Relation``.

        :param transaction: The current transaction
        :param role_type: The role to be played by the ``player``
        :param player: The thing to play the role
        :return:

        Examples
        --------
        ::

            relation.add_player(transaction, role_type, player).resolve()
        """
        pass

    @abstractmethod
    def remove_player(self, transaction: TypeDBTransaction, role_type: RoleType, player: Thing) -> Promise[None]:
        """
        Removes the association of the given instance that plays the given role in this ``Relation``.

        :param transaction: The current transaction
        :param role_type: The role to no longer be played by the thing in this ``Relation``
        :param player: The instance to no longer play the role in this ``Relation``
        :return:

        Examples
        --------
        ::

            relation.remove_player(transaction, role_type, player).resolve()
        """
        pass

    @abstractmethod
    def get_players_by_role_type(self, transaction: TypeDBTransaction, *role_types: RoleType) -> Iterator[Thing]:
        """
        Retrieves all role players of this ``Relation``, optionally
        filtered by given role types.

        :param transaction: The current transaction
        :param role_types: 0 or more role types
        :return:

        Examples
        --------
        ::

            relation.get_players_by_role_type(transaction)
            relation.get_players_by_role_type(transaction, role_type1, role_type2)
        """
        pass

    @abstractmethod
    def get_players(self, transaction: TypeDBTransaction) -> dict[RoleType, list[Thing]]:
        """
        Retrieves a mapping of all instances involved in the ``Relation``
        and the role each play.

        :param transaction: The current transaction
        :return:

        Examples
        --------
        ::

            relation.get_players(transaction)
        """
        pass

    @abstractmethod
    def get_relating(self, transaction: TypeDBTransaction) -> Iterator[RoleType]:
        """
        Retrieves all role types currently played in this ``Relation``.

        :param transaction: The current transaction
        :return:

        Examples
        --------
        ::

            relation.get_relating(transaction)
        """
        pass
