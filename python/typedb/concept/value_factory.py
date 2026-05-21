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

from math import floor
from datetime import date, datetime, timezone
from decimal import Decimal

from typedb.api.concept.concept import Concept
from typedb.common.datetime import Datetime
from typedb.common.duration import Duration
from typedb.concept.concept import _Concept
from typedb.concept.value.value import _Value
from typedb.concept.concept_factory import wrap_value
from typedb.native_driver_wrapper import (
    concept_new_boolean, concept_new_integer, concept_new_double, concept_new_decimal,
    concept_new_string, concept_new_date_from_seconds, concept_new_datetime,
    concept_new_datetime_tz_iana, concept_new_datetime_tz_offset,
    concept_new_duration,
)

class ValueFactory:
    @staticmethod
    def new_boolean(value: bool) -> "_Value":
        return wrap_value(concept_new_boolean(value))

    @staticmethod
    def new_integer(value: int) -> "_Value":
        return wrap_value(concept_new_integer(value))

    @staticmethod
    def new_double(value: float) -> "_Value":
        return wrap_value(concept_new_double(value))

    @staticmethod
    def new_decimal(value: Decimal) -> "_Value":
        integer_part = floor(value)
        fractional_part = int((value - Decimal(integer_part)) * Decimal(10 ** _Concept.DECIMAL_SCALE))
        return wrap_value(concept_new_decimal(integer_part, fractional_part))

    @staticmethod
    def new_string(value: str) -> "_Value":
        return wrap_value(concept_new_string(value))

    @staticmethod
    def new_date(value: date) -> "_Value":
        epoch_seconds = int(datetime(value.year, value.month, value.day, tzinfo=timezone.utc).timestamp())
        return wrap_value(concept_new_date_from_seconds(epoch_seconds))

    @staticmethod
    def new_datetime(value: Datetime) -> "_Value":
        seconds, nanos = value.to_seconds_and_nanos()
        return wrap_value(concept_new_datetime(seconds, nanos))

    @staticmethod
    def new_datetime_tz(value: Datetime) -> "_Value":
        seconds, nanos = value.to_seconds_and_nanos()
        if value.tz_name is not None:
            return wrap_value(concept_new_datetime_tz_iana(seconds, nanos, value.tz_name))
        else:
            return wrap_value(concept_new_datetime_tz_offset(seconds, nanos, value.offset_seconds))

    @staticmethod
    def new_duration(value: Duration) -> "_Value":
        return wrap_value(concept_new_duration(value.months, value.days, value.nanos))

    @staticmethod
    def new_struct(value: Concept.STRUCT) -> "_Value":
        raise NotImplementedError("new_struct is not yet implemented")
