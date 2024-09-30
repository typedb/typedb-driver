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

from datetime import date, datetime, timezone
from functools import singledispatchmethod
from typing import Union

from typedb.native_driver_wrapper import value_new_boolean, value_new_long, value_new_double, value_new_string, \
    value_new_datetime_from_millis, value_is_boolean, value_is_long, value_is_double, value_is_string, \
    value_is_datetime, value_get_boolean, value_get_long, value_get_double, value_get_string, \
    value_get_datetime_as_millis

from typedb.api.concept.value.value import Value, ValueType
from typedb.common.exception import TypeDBDriverException, UNEXPECTED_NATIVE_VALUE, ILLEGAL_STATE, MISSING_VALUE
from typedb.concept.concept import _Concept


class _Value(Value, _Concept):
    def get_value_type(self) -> str:
        return value_get_value_type(self.native_object)

    def get(self) -> VALUE:
        if self.is_boolean():
            return self.as_boolean()
        elif self.is_long():
            return self.as_long()
        elif self.is_double():
            return self.as_double()
        elif self.is_string():
            return self.as_string()
        elif self.is_datetime():
            return self.as_datetime()
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
        return value_get_boolean(self.native_object)

    def as_long(self) -> int:
        return value_get_long(self.native_object)

    def as_double(self) -> float:
        return value_get_double(self.native_object)

    def as_decimal(self) -> float:
        return value_get_decimal(self.native_object)

    def as_string(self) -> str:
        return value_get_string(self.native_object)

    def as_date(self) -> date:
        return datetime.utcfromtimestamp(value_get_date(self.native_object))

    def as_datetime(self) -> datetime:
        return datetime.utcfromtimestamp(value_get_datetime(self.native_object) / 1000)

    def as_datetime_tz(self) -> datetime:
        return datetime.fromtimestamp(value_get_datetime_tz(self.native_object) / 1000, timezone.utc)

    def as_duration(self) -> datetime:
        return datetime.utcfromtimestamp(value_get_duration(self.native_object))

    def as_struct(self) -> {str, Optional[Value]}:
        result = {}
        for field_and_value in IteratorWrapper(value_get_struct(self.native_object), string_and_opt_value_iterator_next):
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

    def __repr__(self):
        return f"{self.get_value_type()}({self.get()})"

    def __hash__(self):
        return hash(self.get())
