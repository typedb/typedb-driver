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

from typedb.api.concept.type.attribute_type import AttributeType
from typedb.common.label import Label
from typedb.concept.type.thing_type import _ThingType
from typedb.native_driver_wrapper import (
    TypeDBDriverExceptionNative, attribute_type_get_value_type, attribute_type_get_label, attribute_type_is_untyped,
    attribute_type_is_boolean, attribute_type_is_long, attribute_type_is_double, attribute_type_is_decimal,
    attribute_type_is_string, attribute_type_is_date, attribute_type_is_datetime, attribute_type_is_datetime_tz,
    attribute_type_is_duration, attribute_type_is_struct,
)


class _AttributeType(AttributeType, _ThingType):

    def get_label(self) -> Label:
        try:
            return Label.of(attribute_type_get_label(self.native_object))
        except TypeDBDriverExceptionNative as e:
            raise TypeDBDriverException.of(e)

    def get_value_type(self) -> str:
        return attribute_type_get_value_type(self.native_object)

    def is_untyped(self) -> bool:
        return attribute_type_is_untyped(self.native_object)

    def is_boolean(self) -> bool:
        return attribute_type_is_boolean(self.native_object)

    def is_long(self) -> bool:
        return attribute_type_is_long(self.native_object)

    def is_double(self) -> bool:
        return attribute_type_is_double(self.native_object)

    def is_decimal(self) -> bool:
        return attribute_type_is_decimal(self.native_object)

    def is_string(self) -> bool:
        return attribute_type_is_string(self.native_object)

    def is_date(self) -> bool:
        return attribute_type_is_date(self.native_object)

    def is_datetime(self) -> bool:
        return attribute_type_is_datetime(self.native_object)

    def is_datetime_tz(self) -> bool:
        return attribute_type_is_datetime_tz(self.native_object)

    def is_duration(self) -> bool:
        return attribute_type_is_duration(self.native_object)

    def is_struct(self) -> bool:
        return attribute_type_is_struct(self.native_object)
