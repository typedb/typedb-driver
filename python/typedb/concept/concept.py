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

from abc import ABC, abstractmethod
from datetime import date
from decimal import Decimal
from typing import Optional

from typedb.api.concept.concept import Concept
from typedb.common.datetime import Datetime
from typedb.common.duration import Duration
from typedb.common.exception import TypeDBDriverException, ILLEGAL_STATE, NULL_NATIVE_OBJECT, UNEXPECTED_NATIVE_VALUE
from typedb.common.iterator_wrapper import IteratorWrapper
from typedb.common.native_wrapper import NativeWrapper
from typedb.concept.concept_factory import wrap_concept
from typedb.concept.concept_factory import wrap_value
from typedb.native_driver_wrapper import (concept_try_get_iid, concept_to_string, concept_equals, concept_get_label,
                                          concept_try_get_label, concept_try_get_value_type, concept_try_get_value,
                                          concept_is_boolean, concept_is_integer, concept_is_double, concept_is_decimal,
                                          concept_is_string, concept_is_date,
                                          concept_is_datetime, concept_is_datetime_tz, concept_is_duration,
                                          concept_is_struct,
                                          concept_get_boolean, concept_get_integer, concept_get_double,
                                          concept_get_decimal, concept_get_string,
                                          concept_get_date_as_seconds, concept_get_datetime, concept_get_datetime_tz,
                                          concept_get_duration, concept_get_struct,
                                          string_and_opt_value_iterator_next,
                                          Concept as NativeConcept)


class _Concept(Concept, NativeWrapper[NativeConcept], ABC):
    DECIMAL_SCALE = 19

    def __init__(self, concept: NativeConcept):
        if not concept:
            raise TypeDBDriverException(NULL_NATIVE_OBJECT)
        super().__init__(concept)

    def get_label(self) -> str:
        return concept_get_label(self.native_object)

    def try_get_label(self) -> Optional[str]:
        return concept_try_get_label(self.native_object)

    def try_get_iid(self) -> Optional[str]:
        return concept_try_get_iid(self.native_object)

    def try_get_value_type(self) -> Optional[str]:
        return concept_try_get_value_type(self.native_object)

    def try_get_value(self) -> Optional[Concept.VALUE]:
        if (value := self._try_get_value_concept()) is None:
            return None
        return value.get()

    def is_boolean(self) -> bool:
        return concept_is_boolean(self.native_object)

    def is_integer(self) -> bool:
        return concept_is_integer(self.native_object)

    def is_double(self) -> bool:
        return concept_is_double(self.native_object)

    def is_decimal(self) -> bool:
        return concept_is_decimal(self.native_object)

    def is_string(self) -> bool:
        return concept_is_string(self.native_object)

    def is_date(self) -> bool:
        return concept_is_date(self.native_object)

    def is_datetime(self) -> bool:
        return concept_is_datetime(self.native_object)

    def is_datetime_tz(self) -> bool:
        return concept_is_datetime_tz(self.native_object)

    def is_duration(self) -> bool:
        return concept_is_duration(self.native_object)

    def is_struct(self) -> bool:
        return concept_is_struct(self.native_object)

    def try_get_boolean(self) -> Optional[bool]:
        if self.is_type() or not self.is_boolean():
            return None
        return concept_get_boolean(self.native_object)

    def try_get_integer(self) -> Optional[int]:
        if self.is_type() or not self.is_integer():
            return None
        return concept_get_integer(self.native_object)

    def try_get_double(self) -> Optional[float]:
        if self.is_type() or not self.is_double():
            return None
        return concept_get_double(self.native_object)

    def try_get_decimal(self) -> Optional[Decimal]:
        if self.is_type() or not self.is_decimal():
            return None
        native_decimal = concept_get_decimal(self.native_object)
        return Decimal(native_decimal.integer) + Decimal(native_decimal.fractional) / Decimal(10 ** self.DECIMAL_SCALE)

    def try_get_string(self) -> Optional[str]:
        if self.is_type() or not self.is_string():
            return None
        return concept_get_string(self.native_object)

    def try_get_date(self) -> Optional[date]:
        if self.is_type() or not self.is_date():
            return None
        return date.fromtimestamp(concept_get_date_as_seconds(self.native_object))

    def try_get_datetime(self) -> Optional[Datetime]:
        if self.is_type() or not self.is_datetime():
            return None
        native_datetime = concept_get_datetime(self.native_object)
        return Datetime.utcfromtimestamp(native_datetime.seconds, native_datetime.subsec_nanos)

    def try_get_datetime_tz(self) -> Optional[Datetime]:
        if self.is_type() or not self.is_datetime_tz():
            return None
        native_datetime_tz = concept_get_datetime_tz(self.native_object)
        native_datetime = native_datetime_tz.datetime_in_nanos
        if native_datetime_tz.is_fixed_offset:
            return Datetime.fromtimestamp(native_datetime.seconds, native_datetime.subsec_nanos,
                                          offset_seconds=native_datetime_tz.local_minus_utc_offset)
        else:
            return Datetime.fromtimestamp(native_datetime.seconds, native_datetime.subsec_nanos,
                                          tz_name=native_datetime_tz.zone_name)

    def try_get_duration(self) -> Optional[Duration]:
        if self.is_type() or not self.is_duration():
            return None
        native_duration = concept_get_duration(self.native_object)
        return Duration(months=native_duration.months, days=native_duration.days, nanos=native_duration.nanos)

    def try_get_struct(self) -> Optional[Concept.STRUCT]:
        if self.is_type() or not self.is_struct():
            return None
        result = {}
        for field_and_value in IteratorWrapper(concept_get_struct(self.native_object),
                                               string_and_opt_value_iterator_next):
            field_name = field_and_value.get_string()
            native_value = field_and_value.get_value()
            result_value = None
            if native_value:
                value = wrap_concept(native_value)
                if not value.is_value():
                    raise TypeDBDriverException(UNEXPECTED_NATIVE_VALUE)
                result_value = value.try_get_value()
            result[field_name] = result_value

    @property
    def _native_object_not_owned_exception(self) -> TypeDBDriverException:
        return TypeDBDriverException(ILLEGAL_STATE)

    def _try_get_value_concept(self) -> Optional["_Value"]:
        if (value := concept_try_get_value(self.native_object)) is None:
            return None
        return wrap_value(value)

    def __repr__(self):
        return concept_to_string(self.native_object)

    def __eq__(self, other):
        return other and isinstance(other, _Concept) and concept_equals(self.native_object, other.native_object)

    @abstractmethod
    def __hash__(self):
        pass
