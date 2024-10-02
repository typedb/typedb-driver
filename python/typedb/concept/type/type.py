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

from abc import ABC

from typedb.api.concept.type.type import Type
from typedb.concept.concept import _Concept


class _Type(Type, _Concept, ABC):

    def as_type(self) -> Type:
        return self

    def __str__(self):
        return type(self).__name__ + "[label: %s]" % self.get_label()

    def __eq__(self, other):
        if other is self:
            return True
        if other is None or not isinstance(other, self.__class__):
            return False
        return self.get_label() == other.get_label()

    def __hash__(self):
        return self.get_label().__hash__()
