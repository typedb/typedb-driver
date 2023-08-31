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

from typing import Iterator, Optional, TYPE_CHECKING, Any

from typedb.native_client_wrapper import role_type_is_root, role_type_is_abstract, role_type_get_scope, \
    role_type_get_name, role_type_delete, role_type_is_deleted, role_type_set_label, role_type_get_supertype, \
    role_type_get_supertypes, role_type_get_subtypes, role_type_get_relation_instances, \
    role_type_get_player_instances, role_type_get_relation_type, role_type_get_relation_types, \
    role_type_get_player_types, concept_iterator_next

from typedb.api.concept.type.role_type import RoleType
from typedb.common.iterator_wrapper import IteratorWrapper
from typedb.common.label import Label
from typedb.common.transitivity import Transitivity
from typedb.concept.concept_factory import wrap_relation, wrap_thing, wrap_relation_type, wrap_thing_type
from typedb.concept.type.type import _Type

if TYPE_CHECKING:
    from typedb.connection.transaction import _Transaction
    from typedb.concept.thing.relation import _Relation
    from typedb.concept.thing.thing import _Thing
    from typedb.concept.type.relation_type import _RelationType


class _RoleType(_Type, RoleType):

    def is_root(self) -> bool:
        return role_type_is_root(self.native_object)

    def is_abstract(self) -> bool:
        return role_type_is_abstract(self.native_object)

    def get_label(self) -> Label:
        return Label.of(role_type_get_scope(self.native_object), role_type_get_name(self.native_object))

    def delete(self, transaction: _Transaction) -> None:
        role_type_delete(transaction.native_object, self.native_object)

    def is_deleted(self, transaction: _Transaction) -> bool:
        return role_type_is_deleted(transaction.native_object, self.native_object)

    def set_label(self, transaction: _Transaction, new_label: Label) -> None:
        role_type_set_label(transaction.native_object, self.native_object, new_label)

    def get_supertype(self, transaction: _Transaction) -> Optional[_RoleType]:
        if res := role_type_get_supertype(transaction.native_object, self.native_object):
            return _RoleType(res)
        return None

    def get_supertypes(self, transaction: _Transaction) -> Iterator[_RoleType]:
        return map(_RoleType, IteratorWrapper(role_type_get_supertypes(transaction.native_object, self.native_object),
                                              concept_iterator_next))

    def get_subtypes(self, transaction: _Transaction, transitivity: Transitivity = Transitivity.TRANSITIVE
                     ) -> Iterator[_RoleType]:
        return map(_RoleType, IteratorWrapper(role_type_get_subtypes(transaction.native_object, self.native_object,
                                                                     transitivity.value),
                                              concept_iterator_next))

    def get_relation_type(self, transaction: _Transaction) -> _RelationType:
        return wrap_relation_type(role_type_get_relation_type(transaction.native_object, self.native_object))

    def get_relation_types(self, transaction: _Transaction) -> Iterator[_RelationType]:
        return map(wrap_relation_type,
                   IteratorWrapper(role_type_get_relation_types(transaction.native_object, self.native_object),
                                   concept_iterator_next))

    def get_player_types(self, transaction: _Transaction, transitivity: Transitivity = Transitivity.TRANSITIVE
                         ) -> Iterator[Any]:
        return map(wrap_thing_type,
                   IteratorWrapper(role_type_get_player_types(transaction.native_object, self.native_object,
                                                              transitivity.value),
                                   concept_iterator_next))

    def get_relation_instances(self, transaction: _Transaction, transitivity: Transitivity = Transitivity.TRANSITIVE
                               ) -> Iterator[_Relation]:
        return map(wrap_relation,
                   IteratorWrapper(role_type_get_relation_instances(transaction.native_object,
                                                                    self.native_object, transitivity.value),
                                   concept_iterator_next))

    def get_player_instances(self, transaction: _Transaction, transitivity: Transitivity = Transitivity.TRANSITIVE
                             ) -> Iterator[_Thing]:
        return map(wrap_thing, IteratorWrapper(role_type_get_player_instances(transaction.native_object,
                                                                              self.native_object,
                                                                              transitivity.value),
                                               concept_iterator_next))
