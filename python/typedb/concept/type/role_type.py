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

from typing import TYPE_CHECKING, Any, Iterator, Optional

from typedb.api.concept.type.role_type import RoleType
from typedb.common.exception import TypeDBDriverException
from typedb.common.iterator_wrapper import IteratorWrapper
from typedb.common.label import Label
from typedb.common.promise import Promise
from typedb.common.transitivity import Transitivity
from typedb.concept.concept_factory import (wrap_relation, wrap_relation_type, wrap_thing, wrap_thing_type)
from typedb.concept.type.type import _Type
from typedb.native_driver_wrapper import (
    TypeDBDriverExceptionNative, bool_promise_resolve, concept_iterator_next, concept_promise_resolve, role_type_delete,
    role_type_get_name, role_type_get_player_instances, role_type_get_player_types, role_type_get_relation_instances,
    role_type_get_relation_type, role_type_get_relation_types, role_type_get_scope, role_type_get_subtypes,
    role_type_get_supertype, role_type_get_supertypes, role_type_is_abstract, role_type_is_deleted, role_type_is_root,
    role_type_set_label, void_promise_resolve
)

if TYPE_CHECKING:
    from typedb.concept.thing.relation import _Relation
    from typedb.concept.thing.thing import _Thing
    from typedb.concept.type.relation_type import _RelationType
    from typedb.connection.transaction import _Transaction


class _RoleType(_Type, RoleType):
    def is_root(self) -> bool:
        return role_type_is_root(self.native_object)

    def is_abstract(self) -> bool:
        return role_type_is_abstract(self.native_object)

    def get_label(self) -> Label:
        return Label.of(
            role_type_get_scope(self.native_object),
            role_type_get_name(self.native_object),
        )

    def delete(self, transaction: _Transaction) -> Promise[None]:
        promise = role_type_delete(transaction.native_object, self.native_object)
        return Promise(lambda: void_promise_resolve(promise))

    def is_deleted(self, transaction: _Transaction) -> Promise[bool]:
        promise = role_type_is_deleted(transaction.native_object, self.native_object)
        return Promise(lambda: bool_promise_resolve(promise))

    def set_label(self, transaction: _Transaction, new_label: Label) -> Promise[None]:
        promise = role_type_set_label(transaction.native_object, self.native_object, new_label)
        return Promise(lambda: void_promise_resolve(promise))

    def get_supertype(self, transaction: _Transaction) -> Promise[Optional[_RoleType]]:
        promise = role_type_get_supertype(transaction.native_object, self.native_object)
        return Promise.map(_RoleType, lambda: concept_promise_resolve(promise))

    def get_supertypes(self, transaction: _Transaction) -> Iterator[_RoleType]:
        try:
            return map(
                _RoleType,
                IteratorWrapper(role_type_get_supertypes(transaction.native_object, self.native_object), concept_iterator_next)
            )
        except TypeDBDriverExceptionNative as e:
            raise TypeDBDriverException.of(e)

    def get_subtypes(
        self,
        transaction: _Transaction,
        transitivity: Transitivity = Transitivity.TRANSITIVE,
    ) -> Iterator[_RoleType]:
        try:
            return map(
                _RoleType,
                IteratorWrapper(
                    role_type_get_subtypes(transaction.native_object, self.native_object, transitivity.value),
                    concept_iterator_next
                )
            )
        except TypeDBDriverExceptionNative as e:
            raise TypeDBDriverException.of(e)

    def get_relation_type(self, transaction: _Transaction) -> Promise[_RelationType]:
        promise = wrap_relation_type(role_type_get_relation_type(transaction.native_object, self.native_object))
        return Promise.map(wrap_relation_type, lambda: concept_promise_resolve(promise))

    def get_relation_types(self, transaction: _Transaction) -> Iterator[_RelationType]:
        try:
            return map(
                wrap_relation_type,
                IteratorWrapper(
                    role_type_get_relation_types(transaction.native_object, self.native_object), concept_iterator_next
                )
            )
        except TypeDBDriverExceptionNative as e:
            raise TypeDBDriverException.of(e)

    def get_player_types(self, transaction: _Transaction, transitivity: Transitivity = Transitivity.TRANSITIVE) -> Iterator[Any]:
        try:
            return map(
                wrap_thing_type,
                IteratorWrapper(
                    role_type_get_player_types(transaction.native_object, self.native_object, transitivity.value),
                    concept_iterator_next
                )
            )
        except TypeDBDriverExceptionNative as e:
            raise TypeDBDriverException.of(e)

    def get_relation_instances(
        self,
        transaction: _Transaction,
        transitivity: Transitivity = Transitivity.TRANSITIVE,
    ) -> Iterator[_Relation]:
        try:
            return map(
                wrap_relation,
                IteratorWrapper(
                    role_type_get_relation_instances(transaction.native_object, self.native_object, transitivity.value),
                    concept_iterator_next
                )
            )
        except TypeDBDriverExceptionNative as e:
            raise TypeDBDriverException.of(e)

    def get_player_instances(
        self,
        transaction: _Transaction,
        transitivity: Transitivity = Transitivity.TRANSITIVE,
    ) -> Iterator[_Thing]:
        try:
            return map(
                wrap_thing,
                IteratorWrapper(
                    role_type_get_player_instances(transaction.native_object, self.native_object, transitivity.value),
                    concept_iterator_next
                )
            )
        except TypeDBDriverExceptionNative as e:
            raise TypeDBDriverException.of(e)
