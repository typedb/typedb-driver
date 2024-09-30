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


class _RelationType(RelationType, _ThingType):
    def get_label(self) -> Label:
        try:
            return Label.of(relation_type_get_label(self.native_object))
        except TypeDBDriverExceptionNative as e:
            raise TypeDBDriverException.of(e)
