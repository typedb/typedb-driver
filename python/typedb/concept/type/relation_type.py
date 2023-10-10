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

from typing import Iterator, Optional, Union, TYPE_CHECKING

from typedb.native_driver_wrapper import (
    TypeDBDriverExceptionNative, concept_iterator_next, concept_promise_resolve, relation_type_create,
    relation_type_get_instances, relation_type_get_relates, relation_type_get_relates_for_role_label,
    relation_type_get_relates_overridden, relation_type_get_subtypes, relation_type_get_supertype, relation_type_get_supertypes,
    relation_type_set_relates, relation_type_set_supertype, relation_type_unset_relates, void_promise_resolve
)

from typedb.api.concept.type.relation_type import RelationType
from typedb.common.exception import TypeDBDriverException
from typedb.common.iterator_wrapper import IteratorWrapper
from typedb.common.promise import Promise
from typedb.common.transitivity import Transitivity
from typedb.concept.concept_factory import wrap_relation, wrap_role_type
from typedb.concept.type.thing_type import _ThingType

if TYPE_CHECKING:
    from typedb.concept.thing.relation import _Relation
    from typedb.concept.type.role_type import _RoleType
    from typedb.connection.transaction import _Transaction


class _RelationType(RelationType, _ThingType):
    def create(self, transaction: _Transaction) -> Promise[_Relation]:
        promise = relation_type_create(transaction.native_object, self.native_object)
        return Promise.map(wrap_relation, lambda: concept_promise_resolve(promise))

    def get_instances(
        self,
        transaction: _Transaction,
        transitivity: Transitivity = Transitivity.TRANSITIVE,
    ) -> Iterator[_Relation]:
        try:
            return map(
                wrap_relation,
                IteratorWrapper(
                    relation_type_get_instances(transaction.native_object, self.native_object, transitivity.value),
                    concept_iterator_next
                )
            )
        except TypeDBDriverExceptionNative as e:
            raise TypeDBDriverException.of(e)

    def get_relates(
        self,
        transaction: _Transaction,
        role_label: Optional[str] = None,
        transitivity: Transitivity = Transitivity.TRANSITIVE,
    ) -> Union[Promise[Optional[_RoleType]], Iterator[_RoleType]]:
        if role_label:
            promise = relation_type_get_relates_for_role_label(transaction.native_object, self.native_object, role_label)
            return Promise.map(wrap_role_type, lambda: concept_promise_resolve(promise))
        else:
            try:
                return map(
                    wrap_role_type,
                    IteratorWrapper(
                        relation_type_get_relates(transaction.native_object, self.native_object, transitivity.value),
                        concept_iterator_next
                    )
                )
            except TypeDBDriverExceptionNative as e:
                raise TypeDBDriverException.of(e)

    def get_relates_overridden(self, transaction: _Transaction, role_label: str) -> Promise[Optional[_RoleType]]:
        promise = relation_type_get_relates_overridden(transaction.native_object, self.native_object, role_label)
        return Promise.map(wrap_role_type, lambda: concept_promise_resolve(promise))

    def set_relates(self, transaction: _Transaction, role_label: str, overridden_label: Optional[str] = None) -> Promise[None]:
        promise = relation_type_set_relates(transaction.native_object, self.native_object, role_label, overridden_label)
        return Promise(lambda: void_promise_resolve(promise))

    def unset_relates(self, transaction: _Transaction, role_label: str) -> Promise[None]:
        promise = relation_type_unset_relates(transaction.native_object, self.native_object, role_label)
        return Promise(lambda: void_promise_resolve(promise))

    def get_subtypes(
        self,
        transaction: _Transaction,
        transitivity: Transitivity = Transitivity.TRANSITIVE,
    ) -> Iterator[_RelationType]:
        try:
            return map(
                _RelationType,
                IteratorWrapper(
                    relation_type_get_subtypes(transaction.native_object, self.native_object, transitivity.value),
                    concept_iterator_next
                )
            )
        except TypeDBDriverExceptionNative as e:
            raise TypeDBDriverException.of(e)

    def get_supertype(self, transaction: _Transaction) -> Promise[Optional[_RelationType]]:
        promise = relation_type_get_supertype(transaction.native_object, self.native_object)
        return Promise.map(_RelationType, lambda: concept_promise_resolve(promise))

    def get_supertypes(self, transaction: _Transaction) -> Iterator[_RelationType]:
        try:
            return map(
                _RelationType,
                IteratorWrapper(
                    relation_type_get_supertypes(transaction.native_object, self.native_object), concept_iterator_next
                )
            )
        except TypeDBDriverExceptionNative as e:
            raise TypeDBDriverException.of(e)

    def set_supertype(self, transaction: _Transaction, super_relation_type: _RelationType) -> Promise[None]:
        promise = relation_type_set_supertype(transaction.native_object, self.native_object, super_relation_type.native_object)
        return Promise(lambda: void_promise_resolve(promise))
