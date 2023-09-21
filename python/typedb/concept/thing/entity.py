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

from typing import TYPE_CHECKING

from typedb.native_driver_wrapper import entity_get_type

from typedb.api.concept.thing.entity import Entity
from typedb.concept.concept_factory import wrap_entity_type
from typedb.concept.thing.thing import _Thing

if TYPE_CHECKING:
    from typedb.concept.type.entity_type import _EntityType


class _Entity(Entity, _Thing):

    def get_type(self) -> _EntityType:
        return wrap_entity_type(entity_get_type(self.native_object))
