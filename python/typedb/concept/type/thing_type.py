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


class _ThingType(ThingType, _Type, ABC):
    def as_thing_type(self) -> ThingType:
        return self
