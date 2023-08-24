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

from typedb.native_client_wrapper import relation_type_create, relation_type_set_supertype, \
    relation_type_get_relates_for_role_label, relation_type_get_relates, relation_type_get_relates_overridden, \
    relation_type_set_relates, relation_type_unset_relates, relation_type_get_supertype, relation_type_get_supertypes, \
    relation_type_get_subtypes, relation_type_get_instances, concept_iterator_next

from typedb.api.concept.type.relation_type import RelationType
from typedb.common.iterator_wrapper import IteratorWrapper
from typedb.common.transitivity import Transitivity
from typedb.concept.concept_factory import wrap_relation, wrap_role_type
from typedb.concept.type.thing_type import _ThingType

if TYPE_CHECKING:
    from typedb.concept.thing.relation import _Relation
    from typedb.concept.type.role_type import _RoleType
    from typedb.connection.transaction import _Transaction


class _RelationType(RelationType, _ThingType):

    def create(self, transaction: _Transaction) -> _Relation:
        return wrap_relation(relation_type_create(transaction.native_object, self.native_object))

    def get_instances(self, transaction: _Transaction, transitivity: Transitivity = Transitivity.TRANSITIVE
                      ) -> Iterator[_Relation]:
        return map(wrap_relation,
                   IteratorWrapper(relation_type_get_instances(transaction.native_object,
                                                               self.native_object, transitivity.value),
                                   concept_iterator_next))

    def get_relates(self, transaction: _Transaction, role_label: Optional[str] = None,
                    transitivity: Transitivity = Transitivity.TRANSITIVE) \
            -> Union[Optional[_RoleType], Iterator[_RoleType]]:
        if role_label:
            if res := relation_type_get_relates_for_role_label(transaction.native_object,
                                                               self.native_object, role_label):
                return wrap_role_type(res)
            return None
        return map(wrap_role_type, IteratorWrapper(relation_type_get_relates(transaction.native_object,
                                                                             self.native_object,
                                                                             transitivity.value),
                                                   concept_iterator_next))

    def get_relates_overridden(self, transaction: _Transaction, role_label: str) -> Optional[_RoleType]:
        if res := relation_type_get_relates_overridden(transaction.native_object, self.native_object, role_label):
            return wrap_role_type(res)
        return None

    def set_relates(self, transaction: _Transaction, role_label: str, overridden_label: Optional[str] = None) -> None:
        relation_type_set_relates(transaction.native_object, self.native_object, role_label, overridden_label)

    def unset_relates(self, transaction: _Transaction, role_label: str) -> None:
        relation_type_unset_relates(transaction.native_object, self.native_object, role_label)

    def get_subtypes(self, transaction: _Transaction, transitivity: Transitivity = Transitivity.TRANSITIVE
                     ) -> Iterator[_RelationType]:
        return map(_RelationType, IteratorWrapper(relation_type_get_subtypes(transaction.native_object,
                                                                             self.native_object,
                                                                             transitivity.value),
                                                  concept_iterator_next))

    def get_supertype(self, transaction: _Transaction) -> Optional[_RelationType]:
        if res := relation_type_get_supertype(transaction.native_object, self.native_object):
            return _RelationType(res)
        return None

    def get_supertypes(self, transaction: _Transaction) -> Iterator[_RelationType]:
        return map(_RelationType, IteratorWrapper(relation_type_get_supertypes(transaction.native_object,
                                                                               self.native_object),
                                                  concept_iterator_next))

    def set_supertype(self, transaction: _Transaction, super_relation_type: _RelationType) -> None:
        relation_type_set_supertype(transaction.native_object, self.native_object, super_relation_type.native_object)
