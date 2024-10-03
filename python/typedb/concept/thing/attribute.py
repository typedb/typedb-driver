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

from datetime import date
from decimal import Decimal
from typing import Mapping, Union

from typedb.api.concept.thing.attribute import Attribute
from typedb.api.concept.value.value import Value
from typedb.common.datetime import Datetime
from typedb.common.duration import Duration
from typedb.concept.concept_factory import wrap_attribute_type, wrap_value
from typedb.concept.thing.thing import _Thing
from typedb.concept.type.attribute_type import _AttributeType
from typedb.concept.value.value import _Value
from typedb.native_driver_wrapper import attribute_get_type, attribute_get_value


class _Attribute(Attribute, _Thing):

    def _value(self) -> _Value:
        return wrap_value(attribute_get_value(self.native_object))

    def get_type(self) -> _AttributeType:
        return wrap_attribute_type(attribute_get_type(self.native_object))

    def get_value(self) -> Value.VALUE:
        return self._value().get()

    def get_value_type(self) -> str:
        return self._value().get_value_type()

    def is_boolean(self) -> bool:
        return self._value().is_boolean()

    def is_long(self) -> bool:
        return self._value().is_long()

    def is_double(self) -> bool:
        return self._value().is_double()

    def is_decimal(self) -> bool:
        return self._value().is_decimal()

    def is_string(self) -> bool:
        return self._value().is_string()

    def is_date(self) -> bool:
        return self._value().is_date()

    def is_datetime(self) -> bool:
        return self._value().is_datetime()

    def is_datetime_tz(self) -> bool:
        return self._value().is_datetime_tz()

    def is_duration(self) -> bool:
        return self._value().is_duration()

    def is_struct(self) -> bool:
        return self._value().is_struct()

    def as_boolean(self) -> bool:
        return self._value().as_boolean()

    def as_long(self) -> int:
        return self._value().as_long()

    def as_double(self) -> float:
        return self._value().as_double()

    def as_decimal(self) -> Decimal:
        return self._value().as_decimal()

    def as_string(self) -> str:
        return self._value().as_string()

    def as_date(self) -> date:
        return self._value().as_date()

    def as_datetime(self) -> Datetime:
        return self._value().as_datetime()

    def as_datetime_tz(self) -> Datetime:
        return self._value().as_datetime_tz()

    def as_duration(self) -> Duration:
        return self._value().as_duration()

    def as_struct(self) -> Value.STRUCT:
        return self._value().as_struct()

    def to_json(self) -> Mapping[str, Union[str, int, float, bool]]:
        return {"type": self.get_type().get_label().scoped_name()} | self._value().to_json()

    def __eq__(self, other):
        if other is self:
            return True
        if other is None or not isinstance(other, self.__class__):
            return False
        return self.get_type().get_label() == other.get_type().get_label() and self.get_value() == other.get_value()

    def __hash__(self):
        return hash((self.get_type().get_label(), self.get_value()))
