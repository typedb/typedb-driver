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

from abc import ABC, abstractmethod
from datetime import datetime
from enum import Enum
from typing import Mapping, Union

from typedb.native_client_wrapper import Object, Boolean, Long, Double, String, DateTime

from typedb.api.concept.concept import Concept
from typedb.common.exception import TypeDBClientExceptionExt, UNEXPECTED_NATIVE_VALUE


class Value(Concept, ABC):

    @abstractmethod
    def get_value_type(self) -> ValueType:
        pass

    @abstractmethod
    def get(self) -> Union[bool, int, float, str, datetime]:
        pass

    def is_value(self) -> bool:
        return True

    def as_value(self) -> Value:
        return self

    @abstractmethod
    def is_boolean(self) -> bool:
        pass

    @abstractmethod
    def is_long(self) -> bool:
        pass

    @abstractmethod
    def is_double(self) -> bool:
        pass

    @abstractmethod
    def is_string(self) -> bool:
        pass

    @abstractmethod
    def is_datetime(self) -> bool:
        pass

    @abstractmethod
    def as_boolean(self) -> bool:
        pass

    @abstractmethod
    def as_long(self) -> int:
        pass

    @abstractmethod
    def as_double(self) -> float:
        pass

    @abstractmethod
    def as_string(self) -> str:
        pass

    @abstractmethod
    def as_datetime(self) -> datetime:
        pass

    def to_json(self) -> Mapping[str, Union[str, int, float, bool]]:
        return {
            "value_type": str(self.get_value_type()),
            "value": self.get() if not self.is_datetime() else self.get().isoformat(timespec='milliseconds')
        }


class _ValueType:

    def __init__(self, is_writable: bool, is_keyable: bool, native_object):
        self._is_writable = is_writable
        self._is_keyable = is_keyable
        self._native_object = native_object

    @property
    def native_object(self):
        return self._native_object

    def is_writable(self) -> bool:
        return self._is_writable

    def is_keyable(self) -> bool:
        return self._is_keyable


class ValueType(Enum):
    OBJECT = _ValueType(False, False, Object)
    BOOLEAN = _ValueType(True, False, Boolean)
    LONG = _ValueType(True, True, Long)
    DOUBLE = _ValueType(True, False, Double)
    STRING = _ValueType(True, True, String)
    DATETIME = _ValueType(True, True, DateTime)

    @property
    def native_object(self):
        return self.value.native_object

    def __str__(self):
        return self.name.lower()

    def __repr__(self):
        return str(self)

    @staticmethod
    def of(value_type: Union[Object, Boolean, Long, Double, String, DateTime]) -> ValueType:
        for type_ in ValueType:
            if type_.native_object == value_type:
                return type_
        raise TypeDBClientExceptionExt(UNEXPECTED_NATIVE_VALUE)
