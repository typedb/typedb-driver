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

from typedb.api.concept.type.thing_type import ThingType
from typedb.common.transitivity import Transitivity

if TYPE_CHECKING:
    from typedb.api.concept.thing.entity import Entity
    from typedb.api.connection.transaction import TypeDBTransaction
    from typedb.common.promise import Promise


class EntityType(ThingType, ABC):
    """
    Entity types represent the classification of independent objects
    in the data model of the business domain.
    """

    def is_entity_type(self) -> bool:
        """
        Checks if the concept is an ``EntityType``.

        :return:

        Examples
        --------
        ::

            entity_type.is_entity_type()
        """
        return True

    def as_entity_type(self) -> EntityType:
        """
        Casts the concept to ``EntityType``.

        :return:

        Examples
        --------
        ::

            entity_type.as_entity_type()
        """
        return self

    @abstractmethod
    def create(self, transaction: TypeDBTransaction) -> Promise[Entity]:
        """
        Creates and returns a new instance of this ``EntityType``.

        :param transaction: The current transaction
        :return:

        Examples
        --------
        ::

            entity_type.create(transaction).resolve()
        """
        pass

    @abstractmethod
    def get_subtypes(
        self,
        transaction: TypeDBTransaction,
        transitivity: Transitivity = Transitivity.TRANSITIVE,
    ) -> Iterator[EntityType]:
        """
        Retrieves all direct and indirect (or direct only) subtypes of the
        ``EntityType``.

        :param transaction: The current transaction
        :param transitivity: ``Transitivity.TRANSITIVE`` for direct
            and indirect subtypes, ``Transitivity.EXPLICIT`` for direct
            subtypes only
        :return:

        Examples
        --------
        ::

            entity_type.get_subtypes(transaction, transitivity)
        """
        pass

    @abstractmethod
    def get_instances(
        self,
        transaction: TypeDBTransaction,
        transitivity: Transitivity = Transitivity.TRANSITIVE,
    ) -> Iterator[Entity]:
        """
        Retrieves all direct and indirect (or direct only) ``Entity`` objects
        that are instances of this ``EntityType``.

        :param transaction: The current transaction
        :param transitivity: ``Transitivity.TRANSITIVE`` for direct
            and indirect instances, ``Transitivity.EXPLICIT`` for direct
            instances only
        :return:

        Examples
        --------
        ::

            entity_type.get_instances(transaction, transitivity)
        """
        pass

    @abstractmethod
    def set_supertype(self, transaction: TypeDBTransaction, super_entity_type: EntityType) -> Promise[None]:
        """
        Sets the supplied ``EntityType`` as the supertype of the current ``EntityType``.

        :param transaction: The current transaction
        :param super_entity_type: The ``EntityType`` to set as the supertype of
            this ``EntityType``
        :return:

        Examples
        --------
        ::

            entity_type.set_supertype(transaction, super_entity_type).resolve()
        """
        pass
