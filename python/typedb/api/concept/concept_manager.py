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
from typing import TYPE_CHECKING

if TYPE_CHECKING:
    from typedb.api.concept.thing.attribute import Attribute
    from typedb.api.concept.thing.entity import Entity
    from typedb.api.concept.thing.relation import Relation
    from typedb.api.concept.type.attribute_type import AttributeType
    from typedb.api.concept.type.entity_type import EntityType
    from typedb.api.concept.type.relation_type import RelationType
    from typedb.api.concept.value.value import ValueType
    from typedb.common.exception import TypeDBException
    from typedb.common.promise import Promise


class ConceptManager(ABC):
    """
    Provides access for all Concept API methods.
    """

    @abstractmethod
    def get_root_entity_type(self) -> EntityType:
        """
        Retrieves the root ``EntityType``, "entity".

        :return:

        Examples
        --------
        ::

            transaction.concepts.get_root_entity_type()
        """
        pass

    @abstractmethod
    def get_root_relation_type(self) -> RelationType:
        """
        Retrieve the root ``RelationType``, "relation".

        :return:

        Examples
        --------
        ::

            transaction.concepts.get_root_relation_type()
        """
        pass

    @abstractmethod
    def get_root_attribute_type(self) -> AttributeType:
        """
        Retrieve the root ``AttributeType``, "attribute".

        :return:

        Examples
        --------
        ::

            transaction.concepts.get_root_attribute_type()
        """
        pass

    @abstractmethod
    def get_entity_type(self, label: str) -> Promise[EntityType]:
        """
        Retrieves an ``EntityType`` by its label.

        :param label: The label of the ``EntityType`` to retrieve
        :return:

        Examples
        --------
        ::

            transaction.concepts.get_entity_type(label).resolve()
        """
        pass

    @abstractmethod
    def get_relation_type(self, label: str) -> Promise[RelationType]:
        """
        Retrieves a ``RelationType`` by its label.

        :param label: The label of the ``RelationType`` to retrieve
        :return:

        Examples
        --------
        ::

            transaction.concepts.get_relation_type(label).resolve()
        """
        pass

    @abstractmethod
    def get_attribute_type(self, label: str) -> Promise[AttributeType]:
        """
        Retrieves an ``AttributeType`` by its label.

        :param label: The label of the ``AttributeType`` to retrieve
        :return:

        Examples
        --------
        ::

            transaction.concepts.get_attribute_type(label).resolve()
        """
        pass

    @abstractmethod
    def put_entity_type(self, label: str) -> Promise[EntityType]:
        """
        Creates a new ``EntityType`` if none exists with the given label,
        otherwise retrieves the existing one.

        :param label: The label of the ``EntityType`` to create or retrieve
        :return:

        Examples
        --------
        ::

            transaction.concepts.put_entity_type(label).resolve()
        """
        pass

    @abstractmethod
    def put_relation_type(self, label: str) -> Promise[RelationType]:
        """
        Creates a new ``RelationType`` if none exists with the given label,
        otherwise retrieves the existing one.

        :param label: The label of the ``RelationType`` to create or retrieve
        :return:

        Examples
        --------
        ::

            transaction.concepts.put_relation_type(label).resolve()
        """
        pass

    @abstractmethod
    def put_attribute_type(self, label: str, value_type: ValueType) -> Promise[AttributeType]:
        """
        Creates a new ``AttributeType`` if none exists with the given label,
        or retrieves the existing one.

        :param label: The label of the ``AttributeType`` to create or retrieve
        :param value_type: The value type of the ``AttributeType`` to create
            or retrieve.
        :return:

        Examples
        --------
        ::

            transaction.concepts.put_attribute_type(label, value_type).resolve()
        """
        pass

    @abstractmethod
    def get_entity(self, iid: str) -> Promise[Entity]:
        """
        Retrieves an ``Entity`` by its iid.

        :param iid: The iid of the ``Entity`` to retrieve
        :return:

        Examples
        --------
        ::

            transaction.concepts.get_entity(iid).resolve()
        """
        pass

    @abstractmethod
    def get_relation(self, iid: str) -> Promise[Relation]:
        """
        Retrieves a ``Relation`` by its iid.

        :param iid: The iid of the ``Relation`` to retrieve
        :return:

        Examples
        --------
        ::

            transaction.concepts.get_relation(iid).resolve()
        """
        pass

    @abstractmethod
    def get_attribute(self, iid: str) -> Promise[Attribute]:
        """
        Retrieves an ``Attribute`` by its iid.

        :param iid: The iid of the ``Attribute`` to retrieve
        :return:

        Examples
        --------
        ::

            transaction.concepts.get_attribute(iid).resolve()
        """
        pass

    @abstractmethod
    def get_schema_exception(self) -> list[TypeDBException]:
        """
        Retrieves a list of all schema exceptions for the current transaction.

        :return:

        Examples
        --------
        ::

            transaction.concepts.get_schema_exception()
        """
        pass
