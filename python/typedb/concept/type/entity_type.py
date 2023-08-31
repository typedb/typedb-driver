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

from typedb.native_client_wrapper import entity_type_create, entity_type_get_subtypes, entity_type_get_instances, \
    entity_type_get_supertypes, entity_type_get_supertype, entity_type_set_supertype, concept_iterator_next

from typedb.api.concept.type.entity_type import EntityType
from typedb.common.iterator_wrapper import IteratorWrapper
from typedb.common.transitivity import Transitivity
from typedb.concept.concept_factory import wrap_entity
from typedb.concept.type.thing_type import _ThingType

if TYPE_CHECKING:
    from typedb.concept.thing.entity import _Entity
    from typedb.connection.transaction import _Transaction


class _EntityType(EntityType, _ThingType):

    def create(self, transaction: _Transaction) -> _Entity:
        return wrap_entity(entity_type_create(transaction.native_object, self.native_object))

    def set_supertype(self, transaction: _Transaction, super_entity_type: _EntityType) -> None:
        entity_type_set_supertype(transaction.native_object, self.native_object,
                                  super_entity_type.native_object)

    def get_supertype(self, transaction: _Transaction) -> Optional[_EntityType]:
        if res := entity_type_get_supertype(transaction.native_object, self.native_object):
            return _EntityType(res)
        return None

    def get_supertypes(self, transaction: _Transaction) -> Iterator[_EntityType]:
        return map(_EntityType, IteratorWrapper(entity_type_get_supertypes(transaction.native_object,
                                                                           self.native_object),
                                                concept_iterator_next))

    def get_subtypes(self, transaction: _Transaction, transitivity: Transitivity = Transitivity.TRANSITIVE
                     ) -> Iterator[_EntityType]:
        return map(_EntityType, IteratorWrapper(entity_type_get_subtypes(transaction.native_object, self.native_object,
                                                                         transitivity.value),
                                                concept_iterator_next))

    def get_instances(self, transaction: _Transaction, transitivity: Transitivity = Transitivity.TRANSITIVE
                      ) -> Iterator[_Entity]:
        return map(wrap_entity, IteratorWrapper(entity_type_get_instances(transaction.native_object, self.native_object,
                                                                          transitivity.value),
                                                concept_iterator_next))
