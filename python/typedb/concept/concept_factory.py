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

from typing import TYPE_CHECKING, Optional

from typedb.native_client_wrapper import \
    concept_is_entity_type, concept_is_relation_type, concept_is_attribute_type, concept_is_root_thing_type, \
    concept_is_entity, concept_is_relation, concept_is_attribute, concept_is_value, concept_is_role_type

import typedb.concept
from typedb.common.exception import TypeDBClientExceptionExt, UNEXPECTED_NATIVE_VALUE
from typedb.concept.value.value import _Value

if TYPE_CHECKING:
    from typedb.concept.concept import _Concept
    from typedb.concept.thing.attribute import _Attribute
    from typedb.concept.thing.entity import _Entity
    from typedb.concept.thing.relation import _Relation
    from typedb.concept.thing.thing import _Thing
    from typedb.concept.type.attribute_type import _AttributeType
    from typedb.concept.type.entity_type import _EntityType
    from typedb.concept.type.relation_type import _RelationType
    from typedb.concept.type.role_type import _RoleType
    from typedb.concept.type.thing_type import _ThingType, _Root
    from typedb.native_client_wrapper import Concept as NativeConcept


def wrap_concept(native_concept: NativeConcept) -> _Concept:
    if concept_thing_type := _try_thing_type(native_concept):
        return concept_thing_type
    elif concept_thing := _try_thing(native_concept):
        return concept_thing
    elif concept_is_value(native_concept):
        return typedb.concept.value.value._Value(native_concept)
    elif concept_is_role_type(native_concept):
        return typedb.concept.type.role_type._RoleType(native_concept)
    else:
        raise TypeDBClientExceptionExt(UNEXPECTED_NATIVE_VALUE)


def wrap_thing_type(native_concept: NativeConcept) -> _ThingType:
    if concept_thing_type := _try_thing_type(native_concept):
        return concept_thing_type
    else:
        raise TypeDBClientExceptionExt(UNEXPECTED_NATIVE_VALUE)


def wrap_thing(native_concept: NativeConcept) -> _Thing:
    if concept_thing := _try_thing(native_concept):
        return concept_thing
    else:
        raise TypeDBClientExceptionExt(UNEXPECTED_NATIVE_VALUE)


def wrap_entity_type(native_concept: NativeConcept) -> _EntityType:
    if concept_is_entity_type(native_concept):
        return typedb.concept.type.entity_type._EntityType(native_concept)
    else:
        raise TypeDBClientExceptionExt(UNEXPECTED_NATIVE_VALUE)


def wrap_attribute_type(native_concept: NativeConcept) -> _AttributeType:
    if concept_is_attribute_type(native_concept):
        return typedb.concept.type.attribute_type._AttributeType(native_concept)
    else:
        raise TypeDBClientExceptionExt(UNEXPECTED_NATIVE_VALUE)


def wrap_relation_type(native_concept: NativeConcept) -> _RelationType:
    if concept_is_relation_type(native_concept):
        return typedb.concept.type.relation_type._RelationType(native_concept)
    else:
        raise TypeDBClientExceptionExt(UNEXPECTED_NATIVE_VALUE)


def wrap_role_type(native_concept: NativeConcept) -> _RoleType:
    if concept_is_role_type(native_concept):
        return typedb.concept.type.role_type._RoleType(native_concept)
    else:
        raise TypeDBClientExceptionExt(UNEXPECTED_NATIVE_VALUE)


def wrap_root(native_concept: NativeConcept) -> _Root:
    if concept_is_root_thing_type(native_concept):
        return typedb.concept.type.thing_type._Root(native_concept)
    else:
        raise TypeDBClientExceptionExt(UNEXPECTED_NATIVE_VALUE)


def wrap_entity(native_concept: NativeConcept) -> _Entity:
    if concept_is_entity(native_concept):
        return typedb.concept.thing.entity._Entity(native_concept)
    else:
        raise TypeDBClientExceptionExt(UNEXPECTED_NATIVE_VALUE)


def wrap_attribute(native_concept: NativeConcept) -> _Attribute:
    if concept_is_attribute(native_concept):
        return typedb.concept.thing.attribute._Attribute(native_concept)
    else:
        raise TypeDBClientExceptionExt(UNEXPECTED_NATIVE_VALUE)


def wrap_relation(native_concept: NativeConcept) -> _Relation:
    if concept_is_relation(native_concept):
        return typedb.concept.thing.relation._Relation(native_concept)
    else:
        raise TypeDBClientExceptionExt(UNEXPECTED_NATIVE_VALUE)


def wrap_value(native_concept: NativeConcept) -> _Value:
    if concept_is_value(native_concept):
        return typedb.concept.value.value._Value(native_concept)
    else:
        raise TypeDBClientExceptionExt(UNEXPECTED_NATIVE_VALUE)


def _try_thing_type(native_concept: NativeConcept) -> Optional[_ThingType]:
    if concept_is_entity_type(native_concept):
        return typedb.concept.type.entity_type._EntityType(native_concept)
    elif concept_is_attribute_type(native_concept):
        return typedb.concept.type.attribute_type._AttributeType(native_concept)
    elif concept_is_relation_type(native_concept):
        return typedb.concept.type.relation_type._RelationType(native_concept)
    elif concept_is_root_thing_type(native_concept):
        return typedb.concept.type.thing_type._Root(native_concept)
    else:
        return None


def _try_thing(native_concept: NativeConcept) -> Optional[_Thing]:
    if concept_is_entity(native_concept):
        return typedb.concept.thing.entity._Entity(native_concept)
    elif concept_is_attribute(native_concept):
        return typedb.concept.thing.attribute._Attribute(native_concept)
    elif concept_is_relation(native_concept):
        return typedb.concept.thing.relation._Relation(native_concept)
    else:
        return None
