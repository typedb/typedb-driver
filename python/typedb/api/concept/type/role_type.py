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
    from typedb.common.promise import Promise


class RoleType(Type, ABC):
    """
    Roles are special internal types used by relations. We can not create
    an instance of a role in a database. But we can set an instance
    of another type (role player) to play a role in a particular instance
    of a relation type.

    Roles allow a schema to enforce logical constraints on types
    of role players.
    """

    def is_role_type(self) -> bool:
        """
        Checks if the concept is a ``RoleType``.

        :return:

        Examples
        --------
        ::

            role_type.is_role_type()
        """
        return True

    def as_role_type(self) -> RoleType:
        """
        Casts the concept to ``RoleType``.

        :return:

        Examples
        --------
        ::

            role_type.as_role_type()
        """
        return self

    @abstractmethod
    def get_supertype(self, transaction: TypeDBTransaction) -> Promise[Optional[RoleType]]:
        """
        Retrieves the most immediate supertype of the ``RoleType``.

        :param transaction: The current transaction
        :return:

        Examples
        --------
        ::

            role_type.get_supertype(transaction).resolve()
        """
        pass

    @abstractmethod
    def get_supertypes(self, transaction: TypeDBTransaction) -> Iterator[RoleType]:
        """
        Retrieves all supertypes of the ``RoleType``.

        :param transaction: The current transaction
        :return:

        Examples
        --------
        ::

            role_type.get_supertypes(transaction)
        """
        pass

    @abstractmethod
    def get_subtypes(
        self,
        transaction: TypeDBTransaction,
        transitivity: Transitivity = Transitivity.TRANSITIVE,
    ) -> Iterator[RoleType]:
        """
        Retrieves all direct and indirect (or direct only) subtypes of the ``RoleType``.

        :param transaction: The current transaction
        :param transitivity: ``Transitivity.TRANSITIVE`` for direct
            and indirect subtypes, ``Transitivity.EXPLICIT`` for direct
            subtypes only
        :return:

        Examples
        --------
        ::

            role_type.get_subtypes(transaction, transitivity)
        """
        pass

    @abstractmethod
    def get_relation_type(self, transaction: TypeDBTransaction) -> Promise[RelationType]:
        """
        Retrieves the ``RelationType`` that this role is directly related to.

        :param transaction: The current transaction
        :return:

        Examples
        --------
        ::

            role_type.get_relation_type(transaction).resolve()
        """
        pass

    @abstractmethod
    def get_relation_types(self, transaction: TypeDBTransaction) -> Iterator[RelationType]:
        """
        Retrieves ``RelationType``\ s that this role is related to
        (directly or indirectly).

        :param transaction: The current transaction
        :return:

        Examples
        --------
        ::

            role_type.get_relation_types(transaction)
        """
        pass

    @abstractmethod
    def get_player_types(
        self,
        transaction: TypeDBTransaction,
        transitivity: Transitivity = Transitivity.TRANSITIVE,
    ) -> Iterator[ThingType]:
        """
        Retrieves the ``ThingType``\ s whose instances play this role.

        :param transaction: The current transaction
        :param transitivity: ``Transitivity.TRANSITIVE`` for direct
            and indirect playing, ``Transitivity.EXPLICIT`` for direct
            playing only
        :return:

        Examples
        --------
        ::

            role_type.get_player_types(transaction, transitivity)
        """
        pass

    @abstractmethod
    def get_relation_instances(
        self,
        transaction: TypeDBTransaction,
        transitivity: Transitivity = Transitivity.TRANSITIVE,
    ) -> Iterator[Relation]:
        """
        Retrieves the ``Relation`` instances that this role is related to.

        :param transaction: The current transaction
        :param transitivity: ``Transitivity.TRANSITIVE`` for direct
            and indirect relation, ``Transitivity.EXPLICIT`` for direct
            relation only
        :return:

        Examples
        --------
        ::

            role_type.get_relation_instances(transaction, transitivity)
        """
        pass

    @abstractmethod
    def get_player_instances(
        self,
        transaction: TypeDBTransaction,
        transitivity: Transitivity = Transitivity.TRANSITIVE,
    ) -> Iterator[Thing]:
        """
        Retrieves the ``Thing`` instances that play this role.

        :param transaction: The current transaction
        :param transitivity: ``Transitivity.TRANSITIVE`` for direct
            and indirect playing, ``Transitivity.EXPLICIT`` for direct
            playing only
        :return:

        Examples
        --------
        ::

            role_type.get_player_instances(transaction, transitivity)
        """
        pass
