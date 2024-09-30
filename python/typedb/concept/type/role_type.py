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


class _RoleType(_Type, RoleType):
    def get_label(self) -> Label:
        try:
            return Label.of(role_type_get_label(self.native_object))
        except TypeDBDriverExceptionNative as e:
            raise TypeDBDriverException.of(e)
