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
from typing import TYPE_CHECKING

from typedb.api.concept.concept import Concept
from typedb.api.concept.instance.instance import Instance
from typedb.common.datetime import Datetime
from typedb.common.duration import Duration

if TYPE_CHECKING:
    from typedb.api.concept.type.attribute_type import AttributeType


class Attribute(Instance, ABC):
    """
    Attribute is an instance of the attribute type and has a value.
    This value is fixed and unique for every given instance of the
    attribute type.

    Attributes can be uniquely addressed by their type and value.
    """

    @abstractmethod
    def get_type(self) -> AttributeType:
        """
        Retrieves the type which this ``Attribute`` belongs to.

        :return:

        Examples
        --------
        ::

            attribute.get_type()
        """
        pass

    @abstractmethod
    def get_value(self) -> Concept.VALUE:
        """
        Retrieves the value which the ``Attribute`` instance holds.

        :return:

        Examples
        --------
        ::

            attribute.get_value()
        """
        pass

    @abstractmethod
    def get_value_type(self) -> str:
        """
        Retrieves the description of the value type of the value which the ``Attribute`` instance holds.

        :return:

        Examples
        --------
        ::

            attribute.get_value_type()
        """
        pass

    @abstractmethod
    def get_boolean(self) -> bool:
        """
        Returns a ``boolean`` value of the value concept that this attribute holds. If the value has
        another type, raises an exception.

        :return:

        Examples
        --------
        ::

            attribute.get_boolean()
        """
        pass

    @abstractmethod
    def get_integer(self) -> int:
        """
        Returns a ``integer`` value of the value concept that this attribute holds. If the value has
        another type, raises an exception.

        :return:

        Examples
        --------
        ::

            attribute.get_integer()
        """
        pass

    @abstractmethod
    def get_double(self) -> float:
        """
        Returns a ``double`` value of the value concept that this attribute holds. If the value has
        another type, raises an exception.

        :return:

        Examples
        --------
        ::

            attribute.get_double()
        """
        pass

    @abstractmethod
    def get_decimal(self) -> Decimal:
        """
        Returns a ``decimal`` value of the value concept that this attribute holds. If the value has
        another type, raises an exception.

        :return:

        Examples
        --------
        ::

            attribute.get_decimal()
        """
        pass

    @abstractmethod
    def get_string(self) -> str:
        """
        Returns a ``string`` value of the value concept that this attribute holds. If the value has
        another type, raises an exception.

        :return:

        Examples
        --------
        ::

            attribute.get_string()
        """
        pass

    @abstractmethod
    def get_date(self) -> date:
        """
        Returns a timezone naive ``date`` value of the value concept that this attribute holds. If the value has
        another type, raises an exception.

        :return:

        Examples
        --------
        ::

            attribute.get_date()
        """
        pass

    @abstractmethod
    def get_datetime(self) -> Datetime:
        """
        Returns a timezone naive ``datetime`` value of the value concept that this attribute holds. If the value has
        another type, raises an exception.

        :return:

        Examples
        --------
        ::

            attribute.get_datetime()
        """
        pass

    @abstractmethod
    def get_datetime_tz(self) -> Datetime:
        """
        Returns a timezone naive ``datetime_tz`` value of the value concept that this attribute holds. If the value has
        another type, raises an exception.

        :return:

        Examples
        --------
        ::

            attribute.get_datetime_tz()
        """
        pass

    @abstractmethod
    def get_duration(self) -> Duration:
        """
        Returns a timezone naive ``duration`` value of the value concept that this attribute holds. If the value has
        another type, raises an exception.

        :return:

        Examples
        --------
        ::

            attribute.get_duration()
        """
        pass

    @abstractmethod
    def get_struct(self) -> Concept.STRUCT:
        """
        Returns a ``struct`` value of the value concept that this attribute holds represented as a map from field names
        to values. If the value has another type, raises an exception.

        :return:

        Examples
        --------
        ::

            attribute.get_struct()
        """
        pass

    def is_attribute(self) -> bool:
        """
        Checks if the concept is an ``Attribute``.

        :return:

        Examples
        --------
        ::

            attribute.is_attribute()
        """
        return True

    def as_attribute(self) -> Attribute:
        """
        Casts the concept to ``Attribute``.

        :return:

        Examples
        --------
        ::

            attribute.as_attribute()
        """
        return self
