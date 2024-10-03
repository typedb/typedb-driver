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

from __future__ import annotations

from abc import ABC
from typing import TYPE_CHECKING

from typedb.common.exception import TypeDBDriverException, INVALID_CONCEPT_CASTING

if TYPE_CHECKING:
    from typedb.api.concept.thing.attribute import Attribute
    from typedb.api.concept.thing.entity import Entity
    from typedb.api.concept.thing.relation import Relation
    from typedb.api.concept.thing.thing import Thing
    from typedb.api.concept.type.attribute_type import AttributeType
    from typedb.api.concept.type.entity_type import EntityType
    from typedb.api.concept.type.relation_type import RelationType
    from typedb.api.concept.type.role_type import RoleType
    from typedb.api.concept.type.thing_type import ThingType
    from typedb.api.concept.type.type import Type
    from typedb.api.concept.value.value import Value


class Concept(ABC):

    def is_type(self) -> bool:
        """
        Checks if the concept is a ``Type``.

        :return:

        Examples
        --------
        ::

            concept.is_type()
        """
        return False

    def is_thing_type(self) -> bool:
        """
        Checks if the concept is a ``ThingType``.

        :return:

        Examples
        --------
        ::

            concept.is_thing_type()
        """
        return False

    def is_entity_type(self) -> bool:
        """
        Checks if the concept is an ``EntityType``.

        :return:

        Examples
        --------
        ::

            concept.is_entity_type()
        """
        return False

    def is_attribute_type(self) -> bool:
        """
        Checks if the concept is an ``AttributeType``.

        :return:

        Examples
        --------
        ::

            concept.is_attribute_type()
        """
        return False

    def is_relation_type(self) -> bool:
        """
        Checks if the concept is a ``RelationType``.

        :return:

        Examples
        --------
        ::

            concept.is_relation_type()
        """
        return False

    def is_role_type(self) -> bool:
        """
        Checks if the concept is a ``RoleType``.

        :return:

        Examples
        --------
        ::

            concept.is_role_type()
        """
        return False

    def is_thing(self) -> bool:
        """
        Checks if the concept is a ``Thing``.

        :return:

        Examples
        --------
        ::

            concept.is_thing()
        """
        return False

    def is_entity(self) -> bool:
        """
        Checks if the concept is an ``Entity``.

        :return:

        Examples
        --------
        ::

            concept.is_entity()
        """
        return False

    def is_attribute(self) -> bool:
        """
        Checks if the concept is an ``Attribute``.

        :return:

        Examples
        --------
        ::

            concept.is_attribute()
        """
        return False

    def is_relation(self) -> bool:
        """
        Checks if the concept is a ``Relation``.

        :return:

        Examples
        --------
        ::

            concept.is_relation()
        """
        return False

    def is_value(self) -> bool:
        """
        Checks if the concept is a ``Value``.

        :return:

        Examples
        --------
        ::

            concept.is_value()
        """
        return False

    def as_type(self) -> Type:
        """
        Casts the concept to ``Type``.

        :return:

        Examples
        --------
        ::

            concept.as_type()
        """
        raise TypeDBDriverException(INVALID_CONCEPT_CASTING, (self.__class__.__name__, "Type"))

    def as_thing_type(self) -> ThingType:
        """
        Casts the concept to ``ThingType``.

        :return:

        Examples
        --------
        ::

            concept.as_thing_type()
        """
        raise TypeDBDriverException(INVALID_CONCEPT_CASTING, (self.__class__.__name__, "ThingType"))

    def as_entity_type(self) -> EntityType:
        """
        Casts the concept to ``EntityType``.

        :return:

        Examples
        --------
        ::

            concept.as_entity_type()
        """
        raise TypeDBDriverException(INVALID_CONCEPT_CASTING, (self.__class__.__name__, "EntityType"))

    def as_attribute_type(self) -> AttributeType:
        """
        Casts the concept to ``AttributeType``.

        :return:

        Examples
        --------
        ::

            concept.as_attribute_type()
        """
        raise TypeDBDriverException(INVALID_CONCEPT_CASTING, (self.__class__.__name__, "AttributeType"))

    def as_relation_type(self) -> RelationType:
        """
        Casts the concept to ``RelationType``.

        :return:

        Examples
        --------
        ::

            concept.as_relation_type()
        """
        raise TypeDBDriverException(INVALID_CONCEPT_CASTING, (self.__class__.__name__, "RelationType"))

    def as_role_type(self) -> RoleType:
        """
        Casts the concept to ``RoleType``.

        :return:

        Examples
        --------
        ::

            concept.as_role_type()
        """
        raise TypeDBDriverException(INVALID_CONCEPT_CASTING, (self.__class__.__name__, "RoleType"))

    def as_thing(self) -> Thing:
        """
        Casts the concept to ``Thing``.

        :return:

        Examples
        --------
        ::

            concept.as_thing()
        """
        raise TypeDBDriverException(INVALID_CONCEPT_CASTING, (self.__class__.__name__, "Thing"))

    def as_entity(self) -> Entity:
        """
        Casts the concept to ``Entity``.

        :return:

        Examples
        --------
        ::

            concept.as_entity()
        """
        raise TypeDBDriverException(INVALID_CONCEPT_CASTING, (self.__class__.__name__, "Entity"))

    def as_attribute(self) -> Attribute:
        """
        Casts the concept to ``Attribute``.

        :return:

        Examples
        --------
        ::

            concept.as_attribute()
        """
        raise TypeDBDriverException(INVALID_CONCEPT_CASTING, (self.__class__.__name__, "Attribute"))

    def as_relation(self) -> Relation:
        """
        Casts the concept to ``Relation``.

        :return:

        Examples
        --------
        ::

            concept.as_relation()
        """
        raise TypeDBDriverException(INVALID_CONCEPT_CASTING, (self.__class__.__name__, "Relation"))

    def as_value(self) -> Value:
        """
        Casts the concept to ``Value``.

        :return:

        Examples
        --------
        ::

            concept.as_value()
        """
        raise TypeDBDriverException(INVALID_CONCEPT_CASTING, (self.__class__.__name__, "Value"))
