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

from abc import ABC, abstractmethod
from typing import TYPE_CHECKING, Iterator, Mapping, Union, Optional

from typedb.api.concept.thing.thing import Thing

if TYPE_CHECKING:
    from datetime import datetime
    from typedb.api.concept.value.value import ValueType
    from typedb.api.concept.type.attribute_type import AttributeType
    from typedb.api.concept.type.thing_type import ThingType
    from typedb.api.connection.transaction import TypeDBTransaction


class Attribute(Thing, ABC):

    @abstractmethod
    def get_type(self) -> AttributeType:
        pass

    @abstractmethod
    def get_value(self) -> Union[bool, int, float, str, datetime]:
        pass

    @abstractmethod
    def get_value_type(self) -> ValueType:
        pass

    def is_attribute(self) -> bool:
        return True

    def as_attribute(self) -> Attribute:
        return self

    @abstractmethod
    def is_boolean(self) -> bool:
        pass

    @abstractmethod
    def is_long(self) -> bool:
        pass

    @abstractmethod
    def is_double(self) -> bool:
        pass

    @abstractmethod
    def is_string(self) -> bool:
        pass

    @abstractmethod
    def is_datetime(self) -> bool:
        pass

    @abstractmethod
    def as_boolean(self) -> bool:
        pass

    @abstractmethod
    def as_long(self) -> int:
        pass

    @abstractmethod
    def as_double(self) -> float:
        pass

    @abstractmethod
    def as_string(self) -> str:
        pass

    @abstractmethod
    def as_datetime(self) -> datetime:
        pass

    @abstractmethod
    def to_json(self) -> Mapping[str, Union[str, int, float, bool]]:
        pass

    @abstractmethod
    def get_owners(self, transaction: TypeDBTransaction, owner_type: Optional[ThingType] = None) -> Iterator[Thing]:
        pass
