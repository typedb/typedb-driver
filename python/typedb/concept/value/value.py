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

import math
from datetime import date, datetime, timezone
from decimal import Decimal

from typedb.api.concept.concept import Concept
from typedb.api.concept.value.value import Value
from typedb.common.datetime import Datetime
from typedb.common.duration import Duration
from typedb.common.exception import TypeDBDriverException, ILLEGAL_STATE, \
    INVALID_VALUE_RETRIEVAL, NULL_CONCEPT_PROPERTY
from typedb.concept.concept import _Concept
from typedb.concept.concept_factory import wrap_value
from typedb.native_driver_wrapper import (
    concept_new_boolean, concept_new_integer, concept_new_double, concept_new_decimal,
    concept_new_string, concept_new_date_from_seconds, concept_new_datetime,
    concept_new_datetime_tz_iana, concept_new_datetime_tz_offset,
    concept_new_duration,
)


class _Value(Value, _Concept):
    def get_type(self) -> str:
        if (value_type := self.try_get_value_type()) is None:
            raise TypeDBDriverException(NULL_CONCEPT_PROPERTY, self.__class__.__name__)
        return value_type

    def get(self) -> Concept.VALUE:
        if self.is_boolean():
            return self.get_boolean()
        elif self.is_integer():
            return self.get_integer()
        elif self.is_double():
            return self.get_double()
        elif self.is_decimal():
            return self.get_decimal()
        elif self.is_string():
            return self.get_string()
        elif self.is_date():
            return self.get_date()
        elif self.is_datetime():
            return self.get_datetime()
        elif self.is_datetime_tz():
            return self.get_datetime_tz()
        elif self.is_duration():
            return self.get_duration()
        elif self.is_struct():
            return self.get_struct()
        else:
            raise TypeDBDriverException(ILLEGAL_STATE)

    def get_boolean(self) -> bool:
        if (value := self.try_get_boolean()) is None:
            raise TypeDBDriverException(INVALID_VALUE_RETRIEVAL, "boolean")
        return value

    def get_integer(self) -> int:
        if (value := self.try_get_integer()) is None:
            raise TypeDBDriverException(INVALID_VALUE_RETRIEVAL, "integer")
        return value

    def get_double(self) -> float:
        if (value := self.try_get_double()) is None:
            raise TypeDBDriverException(INVALID_VALUE_RETRIEVAL, "double")
        return value

    def get_decimal(self) -> Decimal:
        if (value := self.try_get_decimal()) is None:
            raise TypeDBDriverException(INVALID_VALUE_RETRIEVAL, "decimal")
        return value

    def get_string(self) -> str:
        if (value := self.try_get_string()) is None:
            raise TypeDBDriverException(INVALID_VALUE_RETRIEVAL, "string")
        return value

    def get_date(self) -> date:
        if (value := self.try_get_date()) is None:
            raise TypeDBDriverException(INVALID_VALUE_RETRIEVAL, "date")
        return value

    def get_datetime(self) -> Datetime:
        if (value := self.try_get_datetime()) is None:
            raise TypeDBDriverException(INVALID_VALUE_RETRIEVAL, "datetime")
        return value

    def get_datetime_tz(self) -> Datetime:
        if (value := self.try_get_datetime_tz()) is None:
            raise TypeDBDriverException(INVALID_VALUE_RETRIEVAL, "datetime-tz")
        return value

    def get_duration(self) -> Duration:
        if (value := self.try_get_duration()) is None:
            raise TypeDBDriverException(INVALID_VALUE_RETRIEVAL, "duration")
        return value

    def get_struct(self) -> Concept.STRUCT:
        if (value := self.try_get_struct()) is None:
            raise TypeDBDriverException(INVALID_VALUE_RETRIEVAL, "struct")
        return value

    @classmethod
    def new_boolean(cls, value: bool) -> "_Value":
        return wrap_value(concept_new_boolean(value))

    @classmethod
    def new_integer(cls, value: int) -> "_Value":
        return wrap_value(concept_new_integer(value))

    @classmethod
    def new_double(cls, value: float) -> "_Value":
        return wrap_value(concept_new_double(value))

    @classmethod
    def new_decimal(cls, value: Decimal) -> "_Value":
        integer_part = math.floor(value)
        fractional_part = int((value - Decimal(integer_part)) * Decimal(10 ** _Concept.DECIMAL_SCALE))
        return wrap_value(concept_new_decimal(integer_part, fractional_part))

    @classmethod
    def new_string(cls, value: str) -> "_Value":
        return wrap_value(concept_new_string(value))

    @classmethod
    def new_date(cls, value: date) -> "_Value":
        epoch_seconds = int(datetime(value.year, value.month, value.day, tzinfo=timezone.utc).timestamp())
        return wrap_value(concept_new_date_from_seconds(epoch_seconds))

    @classmethod
    def new_datetime(cls, value: Datetime) -> "_Value":
        seconds, nanos = value.to_seconds_and_nanos()
        return wrap_value(concept_new_datetime(seconds, nanos))

    @classmethod
    def new_datetime_tz(cls, value: Datetime) -> "_Value":
        seconds, nanos = value.to_seconds_and_nanos()
        if value.tz_name is not None:
            return wrap_value(concept_new_datetime_tz_iana(seconds, nanos, value.tz_name))
        else:
            return wrap_value(concept_new_datetime_tz_offset(seconds, nanos, value.offset_seconds))

    @classmethod
    def new_duration(cls, value: Duration) -> "_Value":
        return wrap_value(concept_new_duration(value.months, value.days, value.nanos))

    @classmethod
    def new_struct(cls, value: Concept.STRUCT) -> "_Value":
        raise NotImplementedError("new_struct is not yet implemented")

    def __str__(self):
        return str(self.get())

    def __eq__(self, other):
        if other is self:
            return True
        if not other or type(self) is not type(other) or self.try_get_value_type() != other.try_get_value_type():
            return False
        return self.get() == other.get()

    def __hash__(self):
        return hash(self.get())
