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

from typedb.api.concept.concept import Concept
from typedb.api.concept.instance.attribute import Attribute
from typedb.common.datetime import Datetime
from typedb.common.duration import Duration
from typedb.common.exception import TypeDBDriverException, NULL_CONCEPT_PROPERTY
from typedb.concept.concept_factory import wrap_attribute_type
from typedb.concept.instance.instance import _Instance
from typedb.concept.type.attribute_type import _AttributeType
from typedb.concept.value.value import _Value
from typedb.native_driver_wrapper import attribute_get_type


class _Attribute(Attribute, _Instance):

    def get_type(self) -> _AttributeType:
        return wrap_attribute_type(attribute_get_type(self.native_object))

    def get_value(self) -> Concept.VALUE:
        return self._get_value_concept().get()

    def get_value_type(self) -> str:
        return self._get_value_concept().get_type()

    def get_boolean(self) -> bool:
        return self._get_value_concept().get_boolean()

    def get_integer(self) -> int:
        return self._get_value_concept().get_integer()

    def get_double(self) -> float:
        return self._get_value_concept().get_double()

    def get_decimal(self) -> Decimal:
        return self._get_value_concept().get_decimal()

    def get_string(self) -> str:
        return self._get_value_concept().get_string()

    def get_date(self) -> date:
        return self._get_value_concept().get_date()

    def get_datetime(self) -> Datetime:
        return self._get_value_concept().get_datetime()

    def get_datetime_tz(self) -> Datetime:
        return self._get_value_concept().get_datetime_tz()

    def get_duration(self) -> Duration:
        return self._get_value_concept().get_duration()

    def get_struct(self) -> Concept.STRUCT:
        return self._get_value_concept().get_struct()

    def to_json(self) -> Mapping[str, Union[str, int, float, bool]]:
        return {"type": self.get_type().get_label()} | self._get_value_concept().to_json()

    def _get_value_concept(self) -> _Value:
        if (value := self._try_get_value_concept()) is None:
            raise TypeDBDriverException(NULL_CONCEPT_PROPERTY, self.__class__.__name__)
        return value

    def __eq__(self, other):
        if other is self:
            return True
        if other is None or not isinstance(other, self.__class__):
            return False
        return self.get_type().get_label() == other.get_type().get_label() and self.get_value() == other.get_value()

    def __hash__(self):
        return hash((self.get_type().get_label(), self.get_value()))
