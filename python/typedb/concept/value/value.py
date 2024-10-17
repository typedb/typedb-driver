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

from typedb.api.concept.value.value import Value
from typedb.common.datetime import Datetime
from typedb.common.duration import Duration
from typedb.common.exception import TypeDBDriverException, UNEXPECTED_NATIVE_VALUE, ILLEGAL_STATE, INVALID_VALUE_CASTING
from typedb.common.iterator_wrapper import IteratorWrapper
from typedb.concept.concept import _Concept
from typedb.concept.concept_factory import wrap_concept
from typedb.native_driver_wrapper import (value_get_value_type, value_is_boolean, value_is_long, value_is_double,
                                          value_is_decimal, value_is_string, value_is_date, value_is_datetime,
                                          value_is_datetime_tz, value_is_duration,
                                          value_is_struct, value_get_boolean, value_get_long, value_get_double,
                                          value_get_decimal, value_get_string,
                                          value_get_date_as_seconds, value_get_datetime, value_get_datetime_tz,
                                          value_get_duration, value_get_struct,
                                          string_and_opt_value_iterator_next)


class _Value(Value, _Concept):
    DECIMAL_SCALE = 19

    def get_type(self) -> str:
        return value_get_value_type(self.native_object)

    def get(self) -> Value.VALUE:
        if self.is_boolean():
            return self.as_boolean()
        elif self.is_long():
            return self.as_long()
        elif self.is_double():
            return self.as_double()
        elif self.is_decimal():
            return self.as_decimal()
        elif self.is_string():
            return self.as_string()
        elif self.is_date():
            return self.as_date()
        elif self.is_datetime():
            return self.as_datetime()
        elif self.is_datetime_tz():
            return self.as_datetime_tz()
        elif self.is_duration():
            return self.as_duration()
        elif self.is_struct():
            return self.as_struct()
        else:
            raise TypeDBDriverException(ILLEGAL_STATE)

    def is_boolean(self) -> bool:
        return value_is_boolean(self.native_object)

    def is_long(self) -> bool:
        return value_is_long(self.native_object)

    def is_double(self) -> bool:
        return value_is_double(self.native_object)

    def is_decimal(self) -> bool:
        return value_is_decimal(self.native_object)

    def is_string(self) -> bool:
        return value_is_string(self.native_object)

    def is_date(self) -> bool:
        return value_is_date(self.native_object)

    def is_datetime(self) -> bool:
        return value_is_datetime(self.native_object)

    def is_datetime_tz(self) -> bool:
        return value_is_datetime_tz(self.native_object)

    def is_duration(self) -> bool:
        return value_is_duration(self.native_object)

    def is_struct(self) -> bool:
        return value_is_struct(self.native_object)

    def as_boolean(self) -> bool:
        if not self.is_boolean():
            raise TypeDBDriverException(INVALID_VALUE_CASTING, "boolean")
        return value_get_boolean(self.native_object)

    def as_long(self) -> int:
        if not self.is_long():
            raise TypeDBDriverException(INVALID_VALUE_CASTING, "long")
        return value_get_long(self.native_object)

    def as_double(self) -> float:
        if not self.is_double():
            raise TypeDBDriverException(INVALID_VALUE_CASTING, "double")
        return value_get_double(self.native_object)

    def as_decimal(self) -> Decimal:
        if not self.is_decimal():
            raise TypeDBDriverException(INVALID_VALUE_CASTING, "decimal")
        native_decimal = value_get_decimal(self.native_object)
        return Decimal(native_decimal.integer) + Decimal(native_decimal.fractional) / Decimal(10 ** self.DECIMAL_SCALE)

    def as_string(self) -> str:
        if not self.is_string():
            raise TypeDBDriverException(INVALID_VALUE_CASTING, "string")
        return value_get_string(self.native_object)

    def as_date(self) -> date:
        if not self.is_date():
            raise TypeDBDriverException(INVALID_VALUE_CASTING, "date")
        return date.fromtimestamp(value_get_date_as_seconds(self.native_object))

    def as_datetime(self) -> Datetime:
        if not self.is_datetime():
            raise TypeDBDriverException(INVALID_VALUE_CASTING, "datetime")
        native_datetime = value_get_datetime(self.native_object)
        return Datetime.utcfromtimestamp(native_datetime.seconds, native_datetime.subsec_nanos)

    def as_datetime_tz(self) -> Datetime:
        if not self.is_datetime_tz():
            raise TypeDBDriverException(INVALID_VALUE_CASTING, "datetime-tz")
        native_datetime_tz = value_get_datetime_tz(self.native_object)
        native_datetime = native_datetime_tz.datetime_in_nanos
        if native_datetime_tz.is_fixed_offset:
            return Datetime.fromtimestamp(native_datetime.seconds, native_datetime.subsec_nanos,
                                          offset_seconds=native_datetime_tz.local_minus_utc_offset)
        else:
            return Datetime.fromtimestamp(native_datetime.seconds, native_datetime.subsec_nanos,
                                          tz_name=native_datetime_tz.zone_name)

    def as_duration(self) -> Duration:
        if not self.is_duration():
            raise TypeDBDriverException(INVALID_VALUE_CASTING, "duration")
        native_duration = value_get_duration(self.native_object)
        return Duration(months=native_duration.months, days=native_duration.days, nanos=native_duration.nanos)

    def as_struct(self) -> Value.STRUCT:
        if not self.is_struct():
            raise TypeDBDriverException(INVALID_VALUE_CASTING, "struct")
        result = {}
        for field_and_value in IteratorWrapper(value_get_struct(self.native_object),
                                               string_and_opt_value_iterator_next):
            field_name = field_and_value.get_string()
            native_value = field_and_value.get_value()
            result_value = None
            if native_value:
                value = wrap_concept(native_value)
                if not value.is_value():
                    raise TypeDBDriverException(UNEXPECTED_NATIVE_VALUE)
                result_value = value.as_value()
            result[field_name] = result_value

    def __str__(self):
        return str(self.get())

    def __eq__(self, other):
        if other is self:
            return True
        if not other or type(self) is not type(other) or self.get_value_type() != other.get_value_type():
            return False
        return self.get() == other.get()

    def __hash__(self):
        return hash(self.get())
