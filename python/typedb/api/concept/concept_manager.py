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


class ConceptManager(ABC):

    @abstractmethod
    def get_root_entity_type(self) -> EntityType:
        pass

    @abstractmethod
    def get_root_relation_type(self) -> RelationType:
        pass

    @abstractmethod
    def get_root_attribute_type(self) -> AttributeType:
        pass

    @abstractmethod
    def get_entity_type(self, label: str) -> EntityType:
        pass

    @abstractmethod
    def put_entity_type(self, label: str) -> EntityType:
        pass

    @abstractmethod
    def get_relation_type(self, label: str) -> RelationType:
        pass

    @abstractmethod
    def put_relation_type(self, label: str) -> RelationType:
        pass

    @abstractmethod
    def get_attribute_type(self, label: str) -> AttributeType:
        pass

    @abstractmethod
    def put_attribute_type(self, label: str, value_type: ValueType) -> AttributeType:
        pass

    @abstractmethod
    def get_entity(self, iid: str) -> Entity:
        pass

    @abstractmethod
    def get_relation(self, iid: str) -> Relation:
        pass

    @abstractmethod
    def get_attribute(self, iid: str) -> Attribute:
        pass

    @abstractmethod
    def get_schema_exception(self) -> list[TypeDBException]:
        pass
