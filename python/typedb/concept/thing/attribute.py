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

from typing import Any, Iterator, Mapping, Optional, TYPE_CHECKING, Union

from typedb.native_client_wrapper import attribute_get_type, attribute_get_value, attribute_get_owners, \
    concept_iterator_next

from typedb.api.concept.thing.attribute import Attribute
from typedb.api.concept.value.value import ValueType
from typedb.common.iterator_wrapper import IteratorWrapper
from typedb.concept.concept_factory import wrap_attribute_type, wrap_thing, wrap_value
from typedb.concept.thing.thing import _Thing
from typedb.concept.type.attribute_type import _AttributeType
from typedb.concept.value.value import _Value

if TYPE_CHECKING:
    from datetime import datetime
    from typedb.concept.type.thing_type import _ThingType
    from typedb.connection.transaction import _Transaction


class _Attribute(Attribute, _Thing):

    def _value(self) -> _Value:
        return wrap_value(attribute_get_value(self.native_object))

    def get_type(self) -> _AttributeType:
        return wrap_attribute_type(attribute_get_type(self.native_object))

    def get_value(self) -> Union[bool, int, float, str, datetime]:
        return wrap_value(attribute_get_value(self.native_object)).get()

    def get_value_type(self) -> ValueType:
        return self._value().get_value_type()

    def is_boolean(self) -> bool:
        return self._value().is_boolean()

    def is_long(self) -> bool:
        return self._value().is_long()

    def is_double(self) -> bool:
        return self._value().is_double()

    def is_string(self) -> bool:
        return self._value().is_string()

    def is_datetime(self) -> bool:
        return self._value().is_datetime()

    def as_boolean(self) -> bool:
        return self._value().as_boolean()

    def as_long(self) -> int:
        return self._value().as_long()

    def as_double(self) -> float:
        return self._value().as_double()

    def as_string(self) -> str:
        return self._value().as_string()

    def as_datetime(self) -> datetime:
        return self._value().as_datetime()

    def to_json(self) -> Mapping[str, Union[str, int, float, bool]]:
        return {"type": self.get_type().get_label().scoped_name()} | self._value().to_json()

    def get_owners(self, transaction: _Transaction, owner_type: Optional[_ThingType] = None) -> Iterator[Any]:
        return map(wrap_thing, IteratorWrapper(attribute_get_owners(transaction.native_object, self.native_object,
                                                                    owner_type.native_object if owner_type else None),
                                               concept_iterator_next))
