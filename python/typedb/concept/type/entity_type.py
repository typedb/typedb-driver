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

from typing import Iterator, Optional, TYPE_CHECKING

from typedb.native_driver_wrapper import (
    TypeDBDriverExceptionNative, concept_iterator_next, concept_promise_resolve, entity_type_create, entity_type_get_instances,
    entity_type_get_subtypes, entity_type_get_supertype, entity_type_get_supertypes, entity_type_set_supertype,
    void_promise_resolve,
)

from typedb.api.concept.type.entity_type import EntityType
from typedb.common.exception import TypeDBDriverException
from typedb.common.iterator_wrapper import IteratorWrapper
from typedb.common.promise import Promise
from typedb.common.transitivity import Transitivity
from typedb.concept.concept_factory import wrap_entity
from typedb.concept.type.thing_type import _ThingType

if TYPE_CHECKING:
    from typedb.concept.thing.entity import _Entity
    from typedb.connection.transaction import _Transaction


class _EntityType(EntityType, _ThingType):
    def create(self, transaction: _Transaction) -> Promise[_Entity]:
        promise = entity_type_create(transaction.native_object, self.native_object)
        return Promise.map(wrap_entity, lambda: concept_promise_resolve(promise))

    def set_supertype(self, transaction: _Transaction, super_entity_type: _EntityType) -> Promise[None]:
        promise = entity_type_set_supertype(transaction.native_object, self.native_object, super_entity_type.native_object)
        return Promise(lambda: void_promise_resolve(promise))

    def get_supertype(self, transaction: _Transaction) -> Promise[Optional[_EntityType]]:
        promise = entity_type_get_supertype(transaction.native_object, self.native_object)
        return Promise.map(_EntityType, lambda: concept_promise_resolve(promise))

    def get_supertypes(self, transaction: _Transaction) -> Iterator[_EntityType]:
        try:
            return map(
                _EntityType,
                IteratorWrapper(entity_type_get_supertypes(transaction.native_object, self.native_object), concept_iterator_next)
            )
        except TypeDBDriverExceptionNative as e:
            raise TypeDBDriverException.of(e)

    def get_subtypes(self,
                     transaction: _Transaction,
                     transitivity: Transitivity = Transitivity.TRANSITIVE) -> Iterator[_EntityType]:
        try:
            return map(
                _EntityType,
                IteratorWrapper(
                    entity_type_get_subtypes(transaction.native_object, self.native_object, transitivity.value),
                    concept_iterator_next
                )
            )
        except TypeDBDriverExceptionNative as e:
            raise TypeDBDriverException.of(e)

    def get_instances(self, transaction: _Transaction, transitivity: Transitivity = Transitivity.TRANSITIVE) -> Iterator[_Entity]:
        try:
            return map(
                wrap_entity,
                IteratorWrapper(
                    entity_type_get_instances(transaction.native_object, self.native_object, transitivity.value),
                    concept_iterator_next
                )
            )
        except TypeDBDriverExceptionNative as e:
            raise TypeDBDriverException.of(e)
