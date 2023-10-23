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

from typedb.native_driver_wrapper import (
    Transaction as NativeTransaction, TypeDBDriverExceptionNative, concept_promise_resolve, concepts_get_attribute,
    concepts_get_attribute_type, concepts_get_entity, concepts_get_entity_type, concepts_get_relation, concepts_get_relation_type,
    concepts_get_root_attribute_type, concepts_get_root_entity_type, concepts_get_root_relation_type,
    concepts_get_schema_exceptions, concepts_put_attribute_type, concepts_put_entity_type, concepts_put_relation_type,
    schema_exception_code, schema_exception_message,
)

from typedb.api.concept.concept_manager import ConceptManager
from typedb.common.exception import TypeDBDriverException, TypeDBException, MISSING_LABEL, MISSING_IID, TRANSACTION_CLOSED
from typedb.common.native_wrapper import NativeWrapper
from typedb.common.promise import Promise
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
        raise TypeDBDriverException(MISSING_LABEL)
    return label


def _not_blank_iid(iid: str) -> str:
    if not iid or iid.isspace():
        raise TypeDBDriverException(MISSING_IID)
    return iid


class _ConceptManager(ConceptManager, NativeWrapper[NativeTransaction]):
    def __init__(self, transaction: NativeTransaction):
        super().__init__(transaction)

    @property
    def _native_object_not_owned_exception(self) -> TypeDBDriverException:
        return TypeDBDriverException(TRANSACTION_CLOSED)

    @property
    def native_transaction(self) -> NativeTransaction:
        return self.native_object

    def get_root_entity_type(self) -> _EntityType:
        return _EntityType(concepts_get_root_entity_type())

    def get_root_relation_type(self) -> _RelationType:
        return _RelationType(concepts_get_root_relation_type())

    def get_root_attribute_type(self) -> _AttributeType:
        return _AttributeType(concepts_get_root_attribute_type())

    def get_entity_type(self, label: str) -> Promise[Optional[_EntityType]]:
        promise = concepts_get_entity_type(self.native_transaction, _not_blank_label(label))
        return Promise.map(_EntityType, lambda: concept_promise_resolve(promise))

    def get_relation_type(self, label: str) -> Promise[Optional[_RelationType]]:
        promise = concepts_get_relation_type(self.native_transaction, _not_blank_label(label))
        return Promise.map(_RelationType, lambda: concept_promise_resolve(promise))

    def get_attribute_type(self, label: str) -> Promise[Optional[_AttributeType]]:
        promise = concepts_get_attribute_type(self.native_transaction, _not_blank_label(label))
        return Promise.map(_AttributeType, lambda: concept_promise_resolve(promise))

    def put_entity_type(self, label: str) -> Promise[_EntityType]:
        promise = concepts_put_entity_type(self.native_transaction, _not_blank_label(label))
        return Promise.map(_EntityType, lambda: concept_promise_resolve(promise))

    def put_relation_type(self, label: str) -> Promise[_RelationType]:
        promise = concepts_put_relation_type(self.native_transaction, _not_blank_label(label))
        return Promise.map(_RelationType, lambda: concept_promise_resolve(promise))

    def put_attribute_type(self, label: str, value_type: ValueType) -> Promise[_AttributeType]:
        promise = concepts_put_attribute_type(self.native_transaction, _not_blank_label(label), value_type.native_object)
        return Promise.map(_AttributeType, lambda: concept_promise_resolve(promise))

    def get_entity(self, iid: str) -> Promise[Optional[_Entity]]:
        promise = concepts_get_entity(self.native_transaction, _not_blank_iid(iid))
        return Promise.map(_Entity, lambda: concept_promise_resolve(promise))

    def get_relation(self, iid: str) -> Promise[Optional[_Relation]]:
        promise = concepts_get_relation(self.native_transaction, _not_blank_iid(iid))
        return Promise.map(_Relation, lambda: concept_promise_resolve(promise))

    def get_attribute(self, iid: str) -> Promise[Optional[_Attribute]]:
        promise = concepts_get_attribute(self.native_transaction, _not_blank_iid(iid))
        return Promise.map(_Attribute, lambda: concept_promise_resolve(promise))

    def get_schema_exception(self) -> list[TypeDBException]:
        try:
            return [
                TypeDBException(schema_exception_code(e), schema_exception_message(e))
                for e in concepts_get_schema_exceptions(self.native_transaction)
            ]
        except TypeDBDriverExceptionNative as e:
            raise TypeDBDriverException.of(e)
