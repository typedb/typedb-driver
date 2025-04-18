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

from typing import TYPE_CHECKING, Optional

from typedb.common.exception import TypeDBDriverException, UNEXPECTED_NATIVE_VALUE
from typedb.native_driver_wrapper import \
    concept_is_entity_type, concept_is_relation_type, concept_is_attribute_type, \
    concept_is_entity, concept_is_relation, concept_is_attribute, concept_is_value, concept_is_role_type

if TYPE_CHECKING:
    from typedb.native_driver_wrapper import Concept as NativeConcept


def wrap_concept(native_concept: NativeConcept) -> _Concept:
    if concept_type := _try_type(native_concept):
        return concept_type
    elif concept_instance := _try_instance(native_concept):
        return concept_instance
    elif concept_is_value(native_concept):
        from typedb.concept.value.value import _Value
        return _Value(native_concept)
    elif concept_is_role_type(native_concept):
        from typedb.concept.type.role_type import _RoleType
        return _RoleType(native_concept)
    else:
        raise TypeDBDriverException(UNEXPECTED_NATIVE_VALUE)


def wrap_instance(native_concept: NativeConcept) -> _Instance:
    if concept_instance := _try_instance(native_concept):
        return concept_instance
    else:
        raise TypeDBDriverException(UNEXPECTED_NATIVE_VALUE)


def wrap_entity_type(native_concept: NativeConcept) -> _EntityType:
    if concept_is_entity_type(native_concept):
        from typedb.concept.type.entity_type import _EntityType
        return _EntityType(native_concept)
    else:
        raise TypeDBDriverException(UNEXPECTED_NATIVE_VALUE)


def wrap_attribute_type(native_concept: NativeConcept) -> _AttributeType:
    if concept_is_attribute_type(native_concept):
        from typedb.concept.type.attribute_type import _AttributeType
        return _AttributeType(native_concept)
    else:
        raise TypeDBDriverException(UNEXPECTED_NATIVE_VALUE)


def wrap_relation_type(native_concept: NativeConcept) -> _RelationType:
    if concept_is_relation_type(native_concept):
        from typedb.concept.type.relation_type import _RelationType
        return _RelationType(native_concept)
    else:
        raise TypeDBDriverException(UNEXPECTED_NATIVE_VALUE)


def wrap_entity(native_concept: NativeConcept) -> _Entity:
    if concept_is_entity(native_concept):
        from typedb.concept.instance.entity import _Entity
        return _Entity(native_concept)
    else:
        raise TypeDBDriverException(UNEXPECTED_NATIVE_VALUE)


def wrap_attribute(native_concept: NativeConcept) -> _Attribute:
    if concept_is_attribute(native_concept):
        from typedb.concept.instance.attribute import _Attribute
        return _Attribute(native_concept)
    else:
        raise TypeDBDriverException(UNEXPECTED_NATIVE_VALUE)


def wrap_relation(native_concept: NativeConcept) -> _Relation:
    if concept_is_relation(native_concept):
        from typedb.concept.instance.relation import _Relation
        return _Relation(native_concept)
    else:
        raise TypeDBDriverException(UNEXPECTED_NATIVE_VALUE)


def wrap_value(native_concept: NativeConcept) -> _Value:
    if concept_is_value(native_concept):
        from typedb.concept.value.value import _Value
        return _Value(native_concept)
    else:
        raise TypeDBDriverException(UNEXPECTED_NATIVE_VALUE)


def _try_type(native_concept: NativeConcept) -> Optional[_Type]:
    if concept_is_entity_type(native_concept):
        from typedb.concept.type.entity_type import _EntityType
        return _EntityType(native_concept)
    elif concept_is_attribute_type(native_concept):
        from typedb.concept.type.attribute_type import _AttributeType
        return _AttributeType(native_concept)
    elif concept_is_relation_type(native_concept):
        from typedb.concept.type.relation_type import _RelationType
        return _RelationType(native_concept)
    elif concept_is_role_type(native_concept):
        from typedb.concept.type.role_type import _RoleType
        return _RoleType(native_concept)
    else:
        return None


def _try_instance(native_concept: NativeConcept) -> Optional[_Instance]:
    if concept_is_entity(native_concept):
        from typedb.concept.instance.entity import _Entity
        return _Entity(native_concept)
    elif concept_is_attribute(native_concept):
        from typedb.concept.instance.attribute import _Attribute
        return _Attribute(native_concept)
    elif concept_is_relation(native_concept):
        from typedb.concept.instance.relation import _Relation
        return _Relation(native_concept)
    else:
        return None
