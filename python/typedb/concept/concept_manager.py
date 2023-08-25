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

from typing import Optional, TYPE_CHECKING

from typedb.native_client_wrapper import concepts_get_entity_type, concepts_get_relation_type, \
    concepts_get_attribute_type, concepts_put_entity_type, concepts_put_relation_type, concepts_put_attribute_type, \
    concepts_get_entity, concepts_get_relation, concepts_get_attribute, concepts_get_schema_exceptions, \
    schema_exception_message, schema_exception_code, Transaction as NativeTransaction

from typedb.api.concept.concept_manager import ConceptManager
from typedb.common.exception import TypeDBClientExceptionExt, TypeDBException, MISSING_LABEL, MISSING_IID, \
    TRANSACTION_CLOSED
from typedb.common.native_wrapper import NativeWrapper
from typedb.concept.thing.attribute import _Attribute
from typedb.concept.thing.entity import _Entity
from typedb.concept.thing.relation import _Relation
from typedb.concept.type.attribute_type import _AttributeType
from typedb.concept.type.entity_type import _EntityType
from typedb.concept.type.relation_type import _RelationType

if TYPE_CHECKING:
    from typedb.api.concept.value.value import ValueType


def _not_blank_label(label: str) -> str:
    if not label or label.isspace():
        raise TypeDBClientExceptionExt.of(MISSING_LABEL)
    return label


def _not_blank_iid(iid: str) -> str:
    if not iid or iid.isspace():
        raise TypeDBClientExceptionExt.of(MISSING_IID)
    return iid


class _ConceptManager(ConceptManager, NativeWrapper[NativeTransaction]):

    def __init__(self, transaction: NativeTransaction):
        super().__init__(transaction)

    @property
    def _native_object_not_owned_exception(self) -> TypeDBClientExceptionExt:
        return TypeDBClientExceptionExt.of(TRANSACTION_CLOSED)

    @property
    def native_transaction(self) -> NativeTransaction:
        return self.native_object

    def get_root_entity_type(self) -> _EntityType:
        return self.get_entity_type("entity")

    def get_root_relation_type(self) -> _RelationType:
        return self.get_relation_type("relation")

    def get_root_attribute_type(self) -> _AttributeType:
        return self.get_attribute_type("attribute")

    def get_entity_type(self, label: str) -> Optional[_EntityType]:
        if _type := concepts_get_entity_type(self.native_transaction, _not_blank_label(label)):
            return _EntityType(_type)
        return None

    def get_relation_type(self, label: str) -> Optional[_RelationType]:
        if _type := concepts_get_relation_type(self.native_transaction, _not_blank_label(label)):
            return _RelationType(_type)
        return None

    def get_attribute_type(self, label: str) -> Optional[_AttributeType]:
        if _type := concepts_get_attribute_type(self.native_transaction, _not_blank_label(label)):
            return _AttributeType(_type)
        return None

    def put_entity_type(self, label: str) -> _EntityType:
        return _EntityType(concepts_put_entity_type(self.native_transaction, _not_blank_label(label)))

    def put_relation_type(self, label: str) -> _RelationType:
        return _RelationType(concepts_put_relation_type(self.native_transaction, _not_blank_label(label)))

    def put_attribute_type(self, label: str, value_type: ValueType) -> _AttributeType:
        return _AttributeType(concepts_put_attribute_type(self.native_transaction, _not_blank_label(label),
                                                          value_type.native_object))

    def get_entity(self, iid: str) -> Optional[_Entity]:
        if concept := concepts_get_entity(self.native_transaction, _not_blank_iid(iid)):
            return _Entity(concept)
        return None

    def get_relation(self, iid: str) -> Optional[_Relation]:
        if concept := concepts_get_relation(self.native_transaction, _not_blank_iid(iid)):
            return _Relation(concept)
        return None

    def get_attribute(self, iid: str) -> Optional[_Attribute]:
        if concept := concepts_get_attribute(self.native_transaction, _not_blank_iid(iid)):
            return _Attribute(concept)
        return None

    def get_schema_exception(self) -> list[TypeDBException]:
        return [TypeDBException(schema_exception_code(e), schema_exception_message(e))
                for e in concepts_get_schema_exceptions(self.native_transaction)]
