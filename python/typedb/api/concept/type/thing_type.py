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
    from typedb.api.concept.type.annotation import Annotation
    from typedb.api.concept.thing.thing import Thing
    from typedb.api.concept.type.role_type import RoleType
    from typedb.api.concept.value.value import ValueType
    from typedb.api.concept.type.attribute_type import AttributeType
    from typedb.api.connection.transaction import TypeDBTransaction
    from typedb.common.promise import Promise


class ThingType(Type, ABC):
    def is_thing_type(self) -> bool:
        """
        Checks if the concept is a ``ThingType``.

        :return:

        Examples
        --------
        ::

             thing_type.is_thing_type()
        """
        return True

    @abstractmethod
    def get_supertype(self, transaction: TypeDBTransaction) -> Promise[Optional[ThingType]]:
        """
        Retrieves the most immediate supertype of the ``ThingType``.

        :param transaction: The current transaction
        :return:

        Examples
        --------
        ::

            thing_type.get_supertype(transaction).resolve()
        """
        pass

    @abstractmethod
    def get_supertypes(self, transaction: TypeDBTransaction) -> Iterator[ThingType]:
        """
        Retrieves all supertypes of the ``ThingType``.

        :param transaction: The current transaction
        :return:

        Examples
        --------
        ::

            thing_type.get_supertypes(transaction)
        """
        pass

    @abstractmethod
    def get_subtypes(
        self,
        transaction: TypeDBTransaction,
        transitivity: Transitivity = Transitivity.TRANSITIVE,
    ) -> Iterator[ThingType]:
        """
        Retrieves all direct and indirect (or direct only) subtypes of the ``ThingType``.

        :param transaction: The current transaction
        :param transitivity: ``Transitivity.TRANSITIVE`` for direct
            and indirect subtypes, ``Transitivity.EXPLICIT`` for direct
            subtypes only
        :return:

        Examples
        --------
        ::

            thing_type.get_subtypes(transaction)
            thing_type.get_subtypes(transaction, Transitivity.EXPLICIT)
        """
        pass

    @abstractmethod
    def get_instances(
        self,
        transaction: TypeDBTransaction,
        transitivity: Transitivity = Transitivity.TRANSITIVE,
    ) -> Iterator[Thing]:
        """
        Retrieves all direct and indirect (or direct only) ``Thing`` objects
        that are instances of this ``ThingType``.

        :param transaction: The current transaction
        :param transitivity: ``Transitivity.TRANSITIVE`` for direct
            and indirect instances, ``Transitivity.EXPLICIT`` for direct
            instances only
        :return:

        Examples
        --------
        ::

            thing_type.get_instances(transaction)
            thing_type.get_instances(transaction, Transitivity.EXPLICIT)
        """
        pass

    @abstractmethod
    def set_abstract(self, transaction: TypeDBTransaction) -> Promise[None]:
        """
        Set a ``ThingType`` to be abstract, meaning it cannot have instances.

        :param transaction: The current transaction
        :return:

        Examples
        --------
        ::

            thing_type.set_abstract(transaction).resolve()
        """
        pass

    @abstractmethod
    def unset_abstract(self, transaction: TypeDBTransaction) -> Promise[None]:
        """
        Set a ``ThingType`` to be non-abstract, meaning it can have instances.

        :param transaction: The current transaction
        :return:

        Examples
        --------
        ::

            thing_type.unset_abstract(transaction).resolve()
        """
        pass

    @abstractmethod
    def set_plays(
        self,
        transaction: TypeDBTransaction,
        role_type: RoleType,
        overriden_type: Optional[RoleType] = None,
    ) -> Promise[None]:
        """
        Allows the instances of this ``ThingType`` to play the given role.

        :param transaction: The current transaction
        :param role_type: The role to be played by the instances of this type
        :param overriden_type: The role type that this role overrides,
            if applicable
        :return:

        Examples
        --------
        ::

            thing_type.set_plays(transaction, role_type).resolve()
            thing_type.set_plays(transaction, role_type, overridden_type).resolve()
        """
        pass

    @abstractmethod
    def unset_plays(self, transaction: TypeDBTransaction, role_type: RoleType) -> Promise[None]:
        """
        Disallows the instances of this ``ThingType`` from playing the given role.

        :param transaction: The current transaction
        :param role_type: The role to not be played by the instances of this type.
        :return:

        Examples
        --------
        ::

            thing_type.unset_plays(transaction, role_type).resolve()
        """
        pass

    @abstractmethod
    def set_owns(
        self,
        transaction: TypeDBTransaction,
        attribute_type: AttributeType,
        overridden_type: Optional[AttributeType] = None,
        annotations: Optional[set[Annotation]] = None,
    ) -> Promise[None]:
        """
        Allows the instances of this ``ThingType`` to own
        the given ``AttributeType``.

        :param transaction: The current transaction
        :param attribute_type: The ``AttributeType`` to be owned
            by the instances of this type.
        :param overridden_type: The ``AttributeType`` that this attribute
            ownership overrides, if applicable.
        :param annotations: Adds annotations to the ownership.
        :return:

        Examples
        --------
        ::

            thing_type.set_owns(transaction, attribute_type).resolve()
            thing_type.set_owns(transaction, attribute_type,
                                overridden_type=overridden_type,
                                annotations={Annotation.key()}).resolve()
        """
        pass

    @abstractmethod
    def unset_owns(self, transaction: TypeDBTransaction, attribute_type: AttributeType) -> Promise[None]:
        """
        Disallows the instances of this ``ThingType`` from owning
        the given ``AttributeType``.

        :param transaction: The current transaction
        :param attribute_type: The ``AttributeType`` to not be owned by the type.
        :return:

        Examples
        --------
        ::

            thing_type.unset_owns(transaction, attribute_type).resolve()
        """
        pass

    @abstractmethod
    def get_plays(
        self,
        transaction: TypeDBTransaction,
        transitivity: Transitivity = Transitivity.TRANSITIVE,
    ) -> Iterator[RoleType]:
        """
        Retrieves all direct and inherited (or direct only) roles that
        are allowed to be played by the instances of this ``ThingType``.

        :param transaction: The current transaction
        :param transitivity: ``Transitivity.TRANSITIVE`` for direct
            and indirect playing, ``Transitivity.EXPLICIT`` for direct
            playing only
        :return:

        Examples
        --------
        ::

            thing_type.get_plays(transaction)
            thing_type.get_plays(transaction, Transitivity.EXPLICIT)
        """
        pass

    @abstractmethod
    def get_plays_overridden(self, transaction: TypeDBTransaction, role_type: RoleType) -> Promise[Optional[RoleType]]:
        """
        Retrieves a ``RoleType`` that is overridden by the given ``role_type``
        for this ``ThingType``.

        :param transaction: The current transaction
        :param role_type: The ``RoleType`` that overrides an inherited role
        :return:

        Examples
        --------
        ::

            thing_type.get_plays_overridden(transaction, role_type).resolve()
        """
        pass

    @abstractmethod
    def get_owns(
        self,
        transaction: TypeDBTransaction,
        value_type: Optional[ValueType] = None,
        transitivity: Transitivity = Transitivity.TRANSITIVE,
        annotations: Optional[set[Annotation]] = None,
    ) -> Iterator[AttributeType]:
        """
        Retrieves ``AttributeType`` that the instances of this ``ThingType``
        are allowed to own directly or via inheritance.

        :param transaction: The current transaction
        :param value_type: If specified, only attribute types of this
            ``ValueType`` will be retrieved.
        :param transitivity: ``Transitivity.TRANSITIVE`` for direct
            and inherited ownership, ``Transitivity.EXPLICIT`` for direct
            ownership only
        :param annotations: Only retrieve attribute types owned with annotations.
        :return:

        Examples
        --------
        ::

            thing_type.get_owns(transaction)
            thing_type.get_owns(transaction, value_type,
                                transitivity=Transitivity.EXPLICIT,
                                annotations={Annotation.key()})
        """
        pass

    @abstractmethod
    def get_owns_overridden(
        self,
        transaction: TypeDBTransaction,
        attribute_type: AttributeType,
    ) -> Promise[Optional[AttributeType]]:
        """
        Retrieves an ``AttributeType``, ownership of which is overridden
        for this ``ThingType`` by a given ``attribute_type``.

        :param transaction: The current transaction
        :param attribute_type: The ``AttributeType`` that overrides requested
            ``AttributeType``
        :return:

        Examples
        --------
        ::

            thing_type.get_owns_overridden(transaction, attribute_type).resolve()
        """
        pass

    @abstractmethod
    def get_syntax(self, transaction: TypeDBTransaction) -> Promise[str]:
        """
        Produces a pattern for creating this ``ThingType`` in a ``define`` query.

        :param transaction: The current transaction
        :return:

        Examples
        --------
        ::

            thing_type.get_syntax(transaction).resolve()
        """
        pass
