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

from typedb.api.concept.thing.relation import Relation
from typedb.concept.concept_factory import wrap_relation_type
from typedb.concept.thing.thing import _Thing
from typedb.native_driver_wrapper import relation_get_type, relation_get_iid

if TYPE_CHECKING:
    from typedb.concept.type.relation_type import _RelationType


class _Relation(Relation, _Thing):

    def get_type(self) -> _RelationType:
        return wrap_relation_type(relation_get_type(self.native_object))

    def get_iid(self) -> str:
        return relation_get_iid(self.native_object)

    def __eq__(self, other):
        if other is self:
            return True
        if other is None or not isinstance(other, self.__class__):
            return False
        return self.get_iid() == other.get_iid()

    def __hash__(self):
        return hash(self.get_iid())
