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

from typedb.native_driver_wrapper import Object, Boolean, Long, Double, String, DateTime

from typedb.api.concept.concept import Concept
from typedb.common.exception import TypeDBDriverException, UNEXPECTED_NATIVE_VALUE


class Value(Concept, ABC):

    @abstractmethod
    def get_value_type(self) -> ValueType:
        """
        Retrieves the ``ValueType`` of this value concept.

        :return:

        Examples
        --------
        ::

            value.get_value_type()
        """
        pass

    @abstractmethod
    def get(self) -> Union[bool, int, float, str, datetime]:
        """
        Retrieves the value which this value concept holds.

        :return:

        Examples
        --------
        ::

            value.get()
        """
        pass

    def is_value(self) -> bool:
        """
        Checks if the concept is a ``Value``.

        :return:

        Examples
        --------
        ::

            value.is_value()
        """
        return True

    def as_value(self) -> Value:
        """
        Casts the concept to ``Value``.

        :return:

        Examples
        --------
        ::

            value.as_value()
        """
        return self

    @abstractmethod
    def is_boolean(self) -> bool:
        """
        Returns ``True`` if the value which this value concept holds is of type ``boolean``.
        Otherwise, returns ``False``.

        :return:

        Examples
        --------
        ::

            value.is_boolean()
        """
        pass

    @abstractmethod
    def is_long(self) -> bool:
        """
        Returns ``True`` if the value which this value concept holds is of type ``long``.
        Otherwise, returns ``False``.

        :return:

        Examples
        --------
        ::

            value.is_long()
        """
        pass

    @abstractmethod
    def is_double(self) -> bool:
        """
        Returns ``True`` if the value which this value concept holds is of type ``double``.
        Otherwise, returns ``False``.

        :return:

        Examples
        --------
        ::

            value.is_double()
        """
        pass

    @abstractmethod
    def is_string(self) -> bool:
        """
        Returns ``True`` if the value which this value concept holds is of type ``string``.
        Otherwise, returns ``False``.

        :return:

        Examples
        --------
        ::

            value.is_string()
        """
        pass

    @abstractmethod
    def is_datetime(self) -> bool:
        """
        Returns ``True`` if the value which this value concept holds is of type ``datetime``.
        Otherwise, returns ``False``.

        :return:

        Examples
        --------
        ::

            value.is_datetime()
        """
        pass

    @abstractmethod
    def as_boolean(self) -> bool:
        """
        Returns a ``boolean`` value of this value concept. If the value has
        another type, raises an exception.

        :return:

        Examples
        --------
        ::

            value.as_boolean()
        """
        pass

    @abstractmethod
    def as_long(self) -> int:
        """
        Returns a ``long`` value of this value concept. If the value has
        another type, raises an exception.

        :return:

        Examples
        --------
        ::

            value.as_long()
        """
        pass

    @abstractmethod
    def as_double(self) -> float:
        """
        Returns a ``double`` value of this value concept. If the value has
        another type, raises an exception.

        :return:

        Examples
        --------
        ::

            value.as_double()
        """
        pass

    @abstractmethod
    def as_string(self) -> str:
        """
        Returns a ``string`` value of this value concept. If the value has
        another type, raises an exception.

        :return:

        Examples
        --------
        ::

            value.as_string()
        """
        pass

    @abstractmethod
    def as_datetime(self) -> datetime:
        """
        Returns a ``datetime`` value of this value concept. If the value has
        another type, raises an exception.

        :return:

        Examples
        --------
        ::

            value.as_datetime()
        """
        pass


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

    def __repr__(self):
        return f"_ValueType(is_writable={self._is_writable}, is_keyable={self._is_keyable}, {self._native_object})"


class ValueType(Enum):
    """ TypeQL value types for attributes and value concepts. """
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
        raise TypeDBDriverException(UNEXPECTED_NATIVE_VALUE)
