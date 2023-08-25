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
from datetime import datetime
from typing import Mapping, Union, TYPE_CHECKING

from typedb.common.exception import TypeDBClientExceptionExt, INVALID_CONCEPT_CASTING

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
        return False

    def is_thing_type(self) -> bool:
        return False

    def is_entity_type(self) -> bool:
        return False

    def is_attribute_type(self) -> bool:
        return False

    def is_relation_type(self) -> bool:
        return False

    def is_role_type(self) -> bool:
        return False

    def is_thing(self) -> bool:
        return False

    def is_entity(self) -> bool:
        return False

    def is_attribute(self) -> bool:
        return False

    def is_relation(self) -> bool:
        return False

    def is_value(self) -> bool:
        return False

    def as_type(self) -> Type:
        raise TypeDBClientExceptionExt.of(INVALID_CONCEPT_CASTING, (self.__class__.__name__, "Type"))

    def as_thing_type(self) -> ThingType:
        raise TypeDBClientExceptionExt.of(INVALID_CONCEPT_CASTING, (self.__class__.__name__, "ThingType"))

    def as_entity_type(self) -> EntityType:
        raise TypeDBClientExceptionExt.of(INVALID_CONCEPT_CASTING, (self.__class__.__name__, "EntityType"))

    def as_attribute_type(self) -> AttributeType:
        raise TypeDBClientExceptionExt.of(INVALID_CONCEPT_CASTING, (self.__class__.__name__, "AttributeType"))

    def as_relation_type(self) -> RelationType:
        raise TypeDBClientExceptionExt.of(INVALID_CONCEPT_CASTING, (self.__class__.__name__, "RelationType"))

    def as_role_type(self) -> RoleType:
        raise TypeDBClientExceptionExt.of(INVALID_CONCEPT_CASTING, (self.__class__.__name__, "RoleType"))

    def as_thing(self) -> Thing:
        raise TypeDBClientExceptionExt.of(INVALID_CONCEPT_CASTING, (self.__class__.__name__, "Thing"))

    def as_entity(self) -> Entity:
        raise TypeDBClientExceptionExt.of(INVALID_CONCEPT_CASTING, (self.__class__.__name__, "Entity"))

    def as_attribute(self) -> Attribute:
        raise TypeDBClientExceptionExt.of(INVALID_CONCEPT_CASTING, (self.__class__.__name__, "Attribute"))

    def as_relation(self) -> Relation:
        raise TypeDBClientExceptionExt.of(INVALID_CONCEPT_CASTING, (self.__class__.__name__, "Relation"))

    def as_value(self) -> Value:
        raise TypeDBClientExceptionExt.of(INVALID_CONCEPT_CASTING, (self.__class__.__name__, "Value"))

    @abstractmethod
    def to_json(self) -> Mapping[str, Union[str, int, float, bool, datetime]]:
        pass
