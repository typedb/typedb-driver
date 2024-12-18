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

from typedb.api.concept.concept import Concept
from typedb.common.datetime import Datetime
from typedb.common.duration import Duration


class Value(Concept, ABC):
    @abstractmethod
    def get_type(self) -> str:
        """
        Retrieves the ``str`` describing the value type of this ``Value`` concept.

        :return:

        Examples
        --------
        ::

            value.get_type()
        """
        pass

    @abstractmethod
    def get(self) -> Concept.VALUE:
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
    def get_boolean(self) -> bool:
        """
        Returns a ``boolean`` value of this value concept. If the value has
        another type, raises an exception.

        :return:

        Examples
        --------
        ::

            value.get_boolean()
        """
        pass

    @abstractmethod
    def get_integer(self) -> int:
        """
        Returns a ``integer`` value of this value concept. If the value has
        another type, raises an exception.

        :return:

        Examples
        --------
        ::

            value.get_integer()
        """
        pass

    @abstractmethod
    def get_double(self) -> float:
        """
        Returns a ``double`` value of this value concept. If the value has
        another type, raises an exception.

        :return:

        Examples
        --------
        ::

            value.get_double()
        """
        pass

    @abstractmethod
    def get_decimal(self) -> Decimal:
        """
        Returns a ``decimal`` value of this value concept. If the value has
        another type, raises an exception.

        :return:

        Examples
        --------
        ::

            value.get_decimal()
        """
        pass

    @abstractmethod
    def get_string(self) -> str:
        """
        Returns a ``string`` value of this value concept. If the value has
        another type, raises an exception.

        :return:

        Examples
        --------
        ::

            value.get_string()
        """
        pass

    @abstractmethod
    def get_date(self) -> date:
        """
        Returns a timezone naive ``date`` value of this value concept. If the value has
        another type, raises an exception.

        :return:

        Examples
        --------
        ::

            value.get_date()
        """
        pass

    @abstractmethod
    def get_datetime(self) -> Datetime:
        """
        Returns a timezone naive ``datetime`` value of this value concept. If the value has
        another type, raises an exception.

        :return:

        Examples
        --------
        ::

            value.get_datetime()
        """
        pass

    @abstractmethod
    def get_datetime_tz(self) -> Datetime:
        """
        Returns a timezone naive ``datetime_tz`` value of this value concept. If the value has
        another type, raises an exception.

        :return:

        Examples
        --------
        ::

            value.get_datetime_tz()
        """
        pass

    @abstractmethod
    def get_duration(self) -> Duration:
        """
        Returns a timezone naive ``duration`` value of this value concept. If the value has
        another type, raises an exception.

        :return:

        Examples
        --------
        ::

            value.get_duration()
        """
        pass

    @abstractmethod
    def get_struct(self) -> Concept.STRUCT:
        """
        Returns a ``struct`` value of this value concept represented as a map from field names to values.
        If the value has another type, raises an exception.

        :return:

        Examples
        --------
        ::

            value.get_struct()
        """
        pass
