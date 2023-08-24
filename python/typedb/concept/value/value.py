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

from datetime import datetime, timezone
from functools import singledispatchmethod
from typing import Union

from typedb.native_client_wrapper import value_new_boolean, value_new_long, value_new_double, value_new_string, \
    value_new_date_time_from_millis, value_is_boolean, value_is_long, value_is_double, value_is_string, \
    value_is_date_time, value_get_boolean, value_get_long, value_get_double, value_get_string, \
    value_get_date_time_as_millis

from typedb.api.concept.value.value import Value, ValueType
from typedb.common.exception import TypeDBClientExceptionExt, UNEXPECTED_NATIVE_VALUE, ILLEGAL_STATE, MISSING_VALUE
from typedb.concept.concept import _Concept


class _Value(Value, _Concept):

    @singledispatchmethod
    def of(value):
        raise TypeDBClientExceptionExt.of(UNEXPECTED_NATIVE_VALUE)

    @of.register
    def _(value: bool):
        return _Value(value_new_boolean(value))

    @of.register
    def _(value: int):
        return _Value(value_new_long(value))

    @of.register
    def _(value: float):
        return _Value(value_new_double(value))

    @of.register
    def _(value: str):
        if not value:
            raise TypeDBClientExceptionExt(MISSING_VALUE)
        return _Value(value_new_string(value))

    @of.register
    def _(value: datetime):
        return _Value(value_new_date_time_from_millis(int(value.replace(tzinfo=timezone.utc).timestamp() * 1000)))

    @of.register
    def _(value: Value):
        return value

    def get_value_type(self) -> ValueType:
        if self.is_boolean():
            return ValueType.BOOLEAN
        elif self.is_long():
            return ValueType.LONG
        elif self.is_double():
            return ValueType.DOUBLE
        elif self.is_string():
            return ValueType.STRING
        elif self.is_datetime():
            return ValueType.DATETIME
        else:
            raise TypeDBClientExceptionExt(ILLEGAL_STATE)

    def get(self) -> Union[bool, int, float, str, datetime]:
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
            raise TypeDBClientExceptionExt(ILLEGAL_STATE)

    def is_boolean(self) -> bool:
        return value_is_boolean(self.native_object)

    def is_long(self) -> bool:
        return value_is_long(self.native_object)

    def is_double(self) -> bool:
        return value_is_double(self.native_object)

    def is_string(self) -> bool:
        return value_is_string(self.native_object)

    def is_datetime(self) -> bool:
        return value_is_date_time(self.native_object)

    def as_boolean(self) -> bool:
        return value_get_boolean(self.native_object)

    def as_long(self) -> int:
        return value_get_long(self.native_object)

    def as_double(self) -> float:
        return value_get_double(self.native_object)

    def as_string(self) -> str:
        return value_get_string(self.native_object)

    def as_datetime(self) -> datetime:
        return datetime.utcfromtimestamp(value_get_date_time_as_millis(self.native_object) / 1000)

    def __str__(self):
        return str(self.get())

    def __repr__(self):
        return f"{self.get_value_type()}({self.get()})"

    def __hash__(self):
        return hash(self.get())
