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
from typing import TYPE_CHECKING, Iterator, Mapping

from typedb.api.concept.concept import Concept

if TYPE_CHECKING:
    from typedb.api.concept.thing.attribute import Attribute
    from typedb.api.concept.thing.relation import Relation
    from typedb.api.concept.type.attribute_type import AttributeType
    from typedb.api.concept.type.role_type import RoleType
    from typedb.api.concept.type.thing_type import ThingType
    from typedb.api.concept.type.annotation import Annotation
    from typedb.api.connection.transaction import TypeDBTransaction
    from typedb.common.promise import Promise


class Thing(Concept, ABC):
    @abstractmethod
    def get_iid(self) -> str:
        """
        Retrieves the unique id of the ``Thing``.

        :return:

        Examples
        --------
        ::

            thing.get_iid()
        """
        pass

    @abstractmethod
    def get_type(self) -> ThingType:
        """
        Retrieves the type which this ``Thing`` belongs to.

        :return:

        Examples
        --------
        ::

            thing.get_type()
        """
        pass

    @abstractmethod
    def is_inferred(self) -> bool:
        """
        Checks if this ``Thing`` is inferred by a [Reasoning Rule].

        :return:

        Examples
        --------
        ::

            thing.is_inferred()
        """
        pass

    def is_thing(self) -> bool:
        """
        Checks if the concept is a ``Thing``.

        :return:

        Examples
        --------
        ::

            thing.is_thing()
        """
        return True

    def as_thing(self) -> Thing:
        """
        Casts the concept to ``Thing``.

        :return:

        Examples
        --------
        ::

            thing.as_thing()
        """
        return self

    @abstractmethod
    def set_has(self, transaction: TypeDBTransaction, attribute: Attribute) -> Promise[None]:
        """
        Assigns an ``Attribute`` to be owned by this ``Thing``.

        :param transaction: The current transaction
        :param attribute: The ``Attribute`` to be owned by this ``Thing``.
        :return:

        Examples
        --------
        ::

            thing.set_has(transaction, attribute).resolve()
        """
        pass

    @abstractmethod
    def unset_has(self, transaction: TypeDBTransaction, attribute: Attribute) -> Promise[None]:
        """
        Unassigns an ``Attribute`` from this ``Thing``.

        :param transaction: The current transaction
        :param attribute: The ``Attribute`` to be disowned from this ``Thing``.
        :return:

        Examples
        --------
        ::

            thing.unset_has(transaction, attribute).resolve()
        """
        pass

    @abstractmethod
    def get_has(
        self,
        transaction: TypeDBTransaction,
        attribute_type: AttributeType = None,
        attribute_types: list[AttributeType] = None,
        annotations: set[Annotation] = frozenset()
    ) -> Iterator[Attribute]:
        """
        Retrieves the ``Attribute``\ s that this ``Thing`` owns.
        Optionally, filtered by an ``AttributeType`` or a list of
        ``AttributeType``\ s.
        Optionally, filtered by ``Annotation``\ s.

        :param transaction: The current transaction
        :param attribute_type: The ``AttributeType`` to filter
            the attributes by
        :param attribute_types: The ``AttributeType``\ s to filter
            the attributes by
        :param annotations: Only retrieve attributes with all given
            ``Annotation``\ s

        :return:

        Examples
        --------
        ::

            thing.get_has(transaction)
           thing.get_has(transaction, attribute_type=attribute_type,
                         annotations=set(Annotation.key()))
        """
        pass

    @abstractmethod
    def get_relations(self, transaction: TypeDBTransaction, role_types: list[RoleType] = None) -> Iterator[Relation]:
        """
        Retrieves all the ``Relation``\ s which this ``Thing`` plays a role in,
        optionally filtered by one or more given roles.

        :param transaction: The current transaction
        :param role_types: The list of roles to filter the relations by.

        :return:

        Examples
        --------
        ::

            thing.get_relations(transaction, role_types)
        """
        pass

    @abstractmethod
    def get_playing(self, transaction: TypeDBTransaction) -> Iterator[RoleType]:
        """
        Retrieves the roles that this ``Thing`` is currently playing.

        :param transaction: The current transaction
        :return:

        Examples
        --------
        ::

            thing.get_playing(transaction)
        """
        pass

    @abstractmethod
    def delete(self, transaction: TypeDBTransaction) -> Promise[None]:
        """
        Deletes this ``Thing``.

        :param transaction: The current transaction
        :return:

        Examples
        --------
        ::

            thing.delete(transaction).resolve()
        """
        pass

    @abstractmethod
    def is_deleted(self, transaction: TypeDBTransaction) -> Promise[bool]:
        """
        Checks if this ``Thing`` is deleted.

        :param transaction: The current transaction
        :return:

        Examples
        --------
        ::

            thing.is_deleted(transaction).resolve()
        """
        pass
