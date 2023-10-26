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

from abc import abstractmethod, ABC
from itertools import chain
from typing import Optional, Iterator, TYPE_CHECKING, Any

from typedb.native_driver_wrapper import (
    TypeDBDriverExceptionNative, bool_promise_resolve, concept_iterator_next, concept_promise_resolve, thing_type_delete,
    thing_type_get_label, thing_type_get_owns, thing_type_get_owns_overridden, thing_type_get_plays,
    thing_type_get_plays_overridden, thing_type_get_syntax, thing_type_is_abstract, thing_type_is_deleted, thing_type_is_root,
    thing_type_set_abstract, thing_type_set_label, thing_type_set_owns, thing_type_set_plays, thing_type_unset_abstract,
    thing_type_unset_owns, thing_type_unset_plays, void_promise_resolve,
)

from typedb.api.concept.type.thing_type import ThingType
from typedb.common.exception import TypeDBDriverException
from typedb.common.iterator_wrapper import IteratorWrapper
from typedb.common.label import Label
from typedb.common.promise import Promise
from typedb.common.transitivity import Transitivity
from typedb.concept.concept_factory import wrap_attribute_type, wrap_role_type
from typedb.concept.type.type import _Type

if TYPE_CHECKING:
    from typedb.api.concept.type.attribute_type import AttributeType
    from typedb.api.concept.value.value import ValueType
    from typedb.api.concept.type.annotation import Annotation
    from typedb.concept.type.attribute_type import _AttributeType
    from typedb.concept.type.role_type import _RoleType
    from typedb.connection.transaction import _Transaction


class _ThingType(ThingType, _Type, ABC):
    def as_thing_type(self) -> ThingType:
        return self

    def is_root(self) -> bool:
        try:
            return thing_type_is_root(self.native_object)
        except TypeDBDriverExceptionNative as e:
            raise TypeDBDriverException.of(e)

    def is_abstract(self) -> bool:
        try:
            return thing_type_is_abstract(self.native_object)
        except TypeDBDriverExceptionNative as e:
            raise TypeDBDriverException.of(e)

    def get_label(self) -> Label:
        try:
            return Label.of(thing_type_get_label(self.native_object))
        except TypeDBDriverExceptionNative as e:
            raise TypeDBDriverException.of(e)

    def delete(self, transaction: _Transaction) -> Promise[None]:
        promise = thing_type_delete(transaction.native_object, self.native_object)
        return Promise(lambda: void_promise_resolve(promise))

    def is_deleted(self, transaction: _Transaction) -> Promise[bool]:
        promise = thing_type_is_deleted(transaction.native_object, self.native_object)
        return Promise(lambda: bool_promise_resolve(promise))

    def set_label(self, transaction: _Transaction, new_label: Label) -> Promise[None]:
        promise = thing_type_set_label(transaction.native_object, self.native_object, new_label)
        return Promise(lambda: void_promise_resolve(promise))

    @abstractmethod
    def get_instances(
        self,
        transaction: _Transaction,
        transitivity: Transitivity = Transitivity.TRANSITIVE,
    ):
        pass

    def set_abstract(self, transaction: _Transaction) -> Promise[None]:
        promise = thing_type_set_abstract(transaction.native_object, self.native_object)
        return Promise(lambda: void_promise_resolve(promise))

    def unset_abstract(self, transaction: _Transaction) -> Promise[None]:
        promise = thing_type_unset_abstract(transaction.native_object, self.native_object)
        return Promise(lambda: void_promise_resolve(promise))

    def set_plays(
        self,
        transaction: _Transaction,
        role_type: _RoleType,
        overridden_role_type: Optional[_RoleType] = None,
    ) -> Promise[None]:
        promise = thing_type_set_plays(
            transaction.native_object,
            self.native_object,
            role_type.native_object,
            overridden_role_type.native_object if overridden_role_type else None,
        )
        return Promise(lambda: void_promise_resolve(promise))

    def unset_plays(self, transaction: _Transaction, role_type: _RoleType) -> Promise[None]:
        promise = thing_type_unset_plays(transaction.native_object, self.native_object, role_type.native_object)
        return Promise(lambda: void_promise_resolve(promise))

    def set_owns(
        self,
        transaction: _Transaction,
        attribute_type: _AttributeType,
        overridden_type: Optional[_AttributeType] = None,
        annotations: Optional[set[Annotation]] = None,
    ) -> Promise[None]:
        overridden_type_native = (overridden_type.native_object if overridden_type else None)
        annotations_array = ([anno.native_object for anno in annotations] if annotations else [])
        promise = thing_type_set_owns(
            transaction.native_object,
            self.native_object,
            attribute_type.native_object,
            overridden_type_native,
            annotations_array,
        )
        return Promise(lambda: void_promise_resolve(promise))

    def unset_owns(self, transaction: _Transaction, attribute_type: _AttributeType) -> Promise[None]:
        promise = thing_type_unset_owns(transaction.native_object, self.native_object, attribute_type.native_object)
        return Promise(lambda: void_promise_resolve(promise))

    def get_plays(self, transaction: _Transaction, transitivity: Transitivity = Transitivity.TRANSITIVE) -> Iterator[_RoleType]:
        try:
            return map(
                wrap_role_type,
                IteratorWrapper(
                    thing_type_get_plays(transaction.native_object, self.native_object, transitivity.value), concept_iterator_next
                )
            )
        except TypeDBDriverExceptionNative as e:
            raise TypeDBDriverException.of(e)

    def get_plays_overridden(self, transaction: _Transaction, role_type: _RoleType) -> Promise[Optional[_RoleType]]:
        if res := thing_type_get_plays_overridden(transaction.native_object, self.native_object, role_type.native_object):
            return wrap_role_type(res)
        return None

    def get_owns(
        self,
        transaction: _Transaction,
        value_type: Optional[ValueType] = None,
        transitivity: Transitivity = Transitivity.TRANSITIVE,
        annotations: Optional[set[Annotation]] = None,
    ) -> Iterator[AttributeType]:
        try:
            return map(
                wrap_attribute_type,
                IteratorWrapper(
                    thing_type_get_owns(
                        transaction.native_object, self.native_object, value_type.native_object if value_type else None,
                        transitivity.value, [anno.native_object for anno in annotations] if annotations else []
                    ), concept_iterator_next
                )
            )
        except TypeDBDriverExceptionNative as e:
            raise TypeDBDriverException.of(e)

    def get_owns_overridden(self, transaction: _Transaction, attribute_type: _AttributeType) -> Promise[Optional[AttributeType]]:
        promise = thing_type_get_owns_overridden(transaction.native_object, self.native_object, attribute_type.native_object)
        return Promise.map(wrap_attribute_type, lambda: concept_promise_resolve(promise))

    def get_syntax(self, transaction: _Transaction) -> Promise[str]:
        return thing_type_get_syntax(transaction.native_object, self.native_object)


class _Root(_ThingType):
    ROOT_LABEL = Label.of("thing")

    def get_label(self) -> Label:
        return self.ROOT_LABEL

    def get_supertype(self, transaction: _Transaction) -> Promise[Optional[_ThingType]]:
        return Promise(lambda: None)

    def get_supertypes(self, transaction: _Transaction) -> Iterator[_ThingType]:
        return (self,)

    def get_subtypes(
        self,
        transaction: _Transaction,
        transitivity: Transitivity = Transitivity.TRANSITIVE,
    ) -> Iterator[Any]:
        return chain(
            (self,),
            transaction.concepts.get_root_entity_type().get_subtypes(transaction, transitivity),
            transaction.concepts.get_root_relation_type().get_subtypes(transaction, transitivity),
            transaction.concepts.get_root_attribute_type().get_subtypes(transaction, transitivity),
        )

    def get_instances(
        self,
        transaction: _Transaction,
        transitivity: Transitivity = Transitivity.TRANSITIVE,
    ) -> Iterator[Any]:
        return chain(
            (self,),
            transaction.concepts.get_root_entity_type().get_instances(transaction, transitivity),
            transaction.concepts.get_root_relation_type().get_instances(transaction, transitivity),
            transaction.concepts.get_root_attribute_type().get_instances(transaction, transitivity),
        )
