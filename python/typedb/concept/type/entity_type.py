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

from typedb.api.concept.type.entity_type import EntityType
from typedb.common.exception import TypeDBDriverException
from typedb.common.label import Label
from typedb.concept.type.thing_type import _ThingType
from typedb.native_driver_wrapper import entity_type_get_label, TypeDBDriverExceptionNative


class _EntityType(EntityType, _ThingType):

    def get_label(self) -> Label:
        try:
            return Label.of(entity_type_get_label(self.native_object))
        except TypeDBDriverExceptionNative as e:
            raise TypeDBDriverException.of(e)
