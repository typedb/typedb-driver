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
from typing import TYPE_CHECKING, Iterator, Union, Optional

from typedb.api.concept.type.thing_type import ThingType
from typedb.common.transitivity import Transitivity

if TYPE_CHECKING:
    from typedb.api.concept.thing.relation import Relation
    from typedb.api.concept.type.role_type import RoleType
    from typedb.api.connection.transaction import TypeDBTransaction
    from typedb.common.promise import Promise


class RelationType(ThingType, ABC):
    """
    Relation types (or subtypes of the relation root type) represent relationships
    between types. Relation types have roles.

    Other types can play roles in relations if it’s mentioned in their definition.

    A relation type must specify at least one role.
    """

    def is_relation_type(self) -> bool:
        """
        Checks if the concept is a ``RelationType``.

        :return:

        Examples
        --------
        ::

            relation_type.is_relation_type()
        """
        return True

    def as_relation_type(self) -> RelationType:
        """
        Casts the concept to ``RelationType``.

        :return:

        Examples
        --------
        ::

            relation_type.as_relation_type()
        """
        return self

    @abstractmethod
    def create(self, transaction: TypeDBTransaction) -> Promise[Relation]:
        """
        Creates and returns an instance of this ``RelationType``.

        :param transaction: The current transaction
        :return:

        Examples
        --------
        ::

            relation_type.create(transaction).resolve()
        """
        pass

    @abstractmethod
    def get_instances(
        self,
        transaction: TypeDBTransaction,
        transitivity: Transitivity = Transitivity.TRANSITIVE,
    ) -> Iterator[Relation]:
        """
        Retrieves all direct and indirect (or direct only) ``Relation``\ s
        that are instances of this ``RelationType``.

        :param transaction: The current transaction
        :param transitivity: ``Transitivity.TRANSITIVE`` for direct
            and indirect instances, ``Transitivity.EXPLICIT`` for direct
            relates only
        :return:

        Examples
        --------
        ::

            relation_type.get_instances(transaction, transitivity)
        """
        pass

    @abstractmethod
    def get_relates(
        self,
        transaction: TypeDBTransaction,
        role_label: Optional[str] = None,
        transitivity: Transitivity = Transitivity.TRANSITIVE,
    ) -> Union[Promise[Optional[RoleType]], Iterator[RoleType]]:
        """
        Retrieves roles that this ``RelationType`` relates to directly
        or via inheritance. If ``role_label`` is given, returns
        a corresponding ``RoleType`` or ``None``.

        :param transaction: The current transaction
        :param role_label: Label of the role we wish to retrieve (optional)
        :param transitivity: ``Transitivity.TRANSITIVE`` for direct
            and inherited relates, ``Transitivity.EXPLICIT`` for direct
            relates only
        :return:

        Examples
        --------
        ::

            relation_type.get_relates(transaction, role_label, transitivity).resolve()
            relation_type.get_relates(transaction, transitivity)
        """
        pass

    @abstractmethod
    def get_relates_overridden(self, transaction: TypeDBTransaction, role_label: str) -> Promise[Optional[RoleType]]:
        """
        Retrieves a ``RoleType`` that is overridden by the role with
        the ``role_label``.

        :param transaction: The current transaction
        :param role_label: Label of the role that overrides an inherited role
        :return:

        Examples
        --------
        ::

            relation_type.get_relates_overridden(transaction, role_label).resolve()
        """
        pass

    @abstractmethod
    def set_relates(
        self,
        transaction: TypeDBTransaction,
        role_label: str,
        overridden_label: Optional[str] = None,
    ) -> Promise[None]:
        """
        Sets the new role that this ``RelationType`` relates to.
        If we are setting an overriding type this way, we have to also pass
        the overridden type as a second argument.

        :param transaction: The current transaction
        :param role_label: The new role for the ``RelationType`` to relate to
        :param overridden_label: The label being overridden, if applicable
        :return:

        Examples
        --------
        ::

            relation_type.set_relates(transaction, role_label).resolve()
            relation_type.set_relates(transaction, role_label, overridden_label).resolve()
        """
        pass

    @abstractmethod
    def unset_relates(self, transaction: TypeDBTransaction, role_label: str) -> Promise[None]:
        """
        Disallows this ``RelationType`` from relating to the given role.

        :param transaction: The current transaction
        :param role_label: The role to not relate to the relation type.
        :return:

        Examples
        --------
        ::

            relation_type.unset_relates(transaction, role_label).resolve()
        """
        pass

    @abstractmethod
    def get_subtypes(
        self,
        transaction: TypeDBTransaction,
        transitivity: Transitivity = Transitivity.TRANSITIVE,
    ) -> Iterator[RelationType]:
        """
        Retrieves all direct and indirect (or direct only) subtypes
        of the ``RelationType``.

        :param transaction: The current transaction
        :param transitivity: ``Transitivity.TRANSITIVE`` for direct
            and indirect subtypes, ``Transitivity.EXPLICIT`` for direct
            subtypes only
        :return:

        Examples
        --------
        ::

            relation_type.get_subtypes(transaction, transitivity)
        """
        pass

    @abstractmethod
    def set_supertype(self, transaction: TypeDBTransaction, super_relation_type: RelationType) -> Promise[None]:
        """
        Sets the supplied ``RelationType`` as the supertype of the current ``RelationType``.

        :param transaction: The current transaction
        :param super_relation_type: The ``RelationType`` to set as the supertype
            of this ``RelationType``
        :return:

        Examples
        --------
        ::

            relation_type.set_supertype(transaction, super_relation_type).resolve()
        """
        pass
