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
from typing import Dict, Optional, Union

from typedb.api.concept.concept import Concept
from typedb.common.datetime import Datetime
from typedb.common.duration import Duration


class Value(Concept, ABC):
    STRUCT = Dict[str, Optional["Value"]]
    VALUE = Union[bool, int, float, Decimal, str, date, Datetime, Duration, STRUCT]

    @abstractmethod
    def get_value_type(self) -> str:
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
    def get(self) -> VALUE:
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
    def is_decimal(self) -> bool:
        """
        Returns ``True`` if the value which this value concept holds is of type ``decimal``.
        Otherwise, returns ``False``.

        :return:

        Examples
        --------
        ::

            value.is_decimal()
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
    def is_date(self) -> bool:
        """
        Returns ``True`` if the value which this value concept holds is of type ``date``.
        Otherwise, returns ``False``.

        :return:

        Examples
        --------
        ::

            value.is_date()
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
    def is_datetime_tz(self) -> bool:
        """
        Returns ``True`` if the value which this value concept holds is of type ``datetime-tz``.
        Otherwise, returns ``False``.

        :return:

        Examples
        --------
        ::

            value.is_datetime_tz()
        """
        pass

    @abstractmethod
    def is_duration(self) -> bool:
        """
        Returns ``True`` if the value which this value concept holds is of type ``duration``.
        Otherwise, returns ``False``.

        :return:

        Examples
        --------
        ::

            value.is_duration()
        """
        pass

    @abstractmethod
    def is_struct(self) -> bool:
        """
        Returns ``True`` if the value which this value concept holds is of type ``struct``.
        Otherwise, returns ``False``.

        :return:

        Examples
        --------
        ::

            value.is_struct()
        """
        pass

    # TODO: Could be useful to have isStruct(struct_name)

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
    def as_decimal(self) -> Decimal:
        """
        Returns a ``decimal`` value of this value concept. If the value has
        another type, raises an exception.

        :return:

        Examples
        --------
        ::

            value.as_decimal()
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
    def as_date(self) -> date:
        """
        Returns a timezone naive ``date`` value of this value concept. If the value has
        another type, raises an exception.

        :return:

        Examples
        --------
        ::

            value.as_date()
        """
        pass

    @abstractmethod
    def as_datetime(self) -> Datetime:
        """
        Returns a timezone naive ``datetime`` value of this value concept. If the value has
        another type, raises an exception.

        :return:

        Examples
        --------
        ::

            value.as_datetime()
        """
        pass

    @abstractmethod
    def as_datetime_tz(self) -> Datetime:
        """
        Returns a timezone naive ``datetime_tz`` value of this value concept. If the value has
        another type, raises an exception.

        :return:

        Examples
        --------
        ::

            value.as_datetime_tz()
        """
        pass

    @abstractmethod
    def as_duration(self) -> Duration:
        """
        Returns a timezone naive ``duration`` value of this value concept. If the value has
        another type, raises an exception.

        :return:

        Examples
        --------
        ::

            value.as_duration()
        """
        pass

    @abstractmethod
    def as_struct(self) -> STRUCT:
        """
        Returns a ``struct`` value of this value concept represented as a map from field names to values.
        If the value has another type, raises an exception.

        :return:

        Examples
        --------
        ::

            value.as_struct()
        """
        pass
