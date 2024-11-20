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

from typing import TYPE_CHECKING

from typedb.api.concept.instance.entity import Entity
from typedb.common.exception import TypeDBDriverException, NULL_CONCEPT_PROPERTY
from typedb.concept.concept_factory import wrap_entity_type
from typedb.concept.instance.instance import _Instance
from typedb.native_driver_wrapper import entity_get_type

if TYPE_CHECKING:
    from typedb.concept.type.entity_type import _EntityType


class _Entity(Entity, _Instance):

    def get_type(self) -> _EntityType:
        return wrap_entity_type(entity_get_type(self.native_object))

    def get_iid(self) -> str:
        if (iid := self.try_get_iid()) is None:
            raise TypeDBDriverException(NULL_CONCEPT_PROPERTY, self.__class__.__name__)
        return iid

    def __eq__(self, other):
        if other is self:
            return True
        if other is None or not isinstance(other, self.__class__):
            return False
        return self.get_iid() == other.get_iid()

    def __hash__(self):
        return hash(self.get_iid())
