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
from typing import TYPE_CHECKING

from typedb.api.concept.thing.thing import Thing
from typedb.api.concept.value.value import Value
from typedb.common.datetime import Datetime
from typedb.common.duration import Duration

if TYPE_CHECKING:
    from typedb.api.concept.type.attribute_type import AttributeType


class Attribute(Thing, ABC):
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
    def get_value(self) -> VALUE:
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
    def is_boolean(self) -> bool:
        """
        Returns ``True`` if this attribute holds a value of type ``boolean``.
        Otherwise, returns ``False``.

        :return:

        Examples
        --------
        ::

            attribute.is_boolean()
        """
        pass

    @abstractmethod
    def is_long(self) -> bool:
        """
        Returns ``True`` if this attribute holds a value of type ``long``.
        Otherwise, returns ``False``.

        :return:

        Examples
        --------
        ::

            attribute.is_long()
        """
        pass

    @abstractmethod
    def is_double(self) -> bool:
        """
        Returns ``True`` if this attribute holds a value of type ``double``.
        Otherwise, returns ``False``.

        :return:

        Examples
        --------
        ::

            attribute.is_double()
        """
        pass

    @abstractmethod
    def is_decimal(self) -> bool:
        """
        Returns ``True`` if this attribute holds a value of type ``decimal``.
        Otherwise, returns ``False``.

        :return:

        Examples
        --------
        ::

            attribute.is_decimal()
        """
        pass

    @abstractmethod
    def is_string(self) -> bool:
        """
        Returns ``True`` if this attribute holds a value of type ``string``.
        Otherwise, returns ``False``.

        :return:

        Examples
        --------
        ::

            attribute.is_string()
        """
        pass

    @abstractmethod
    def is_date(self) -> bool:
        """
        Returns ``True`` if this attribute holds a value of type ``date``.
        Otherwise, returns ``False``.

        :return:

        Examples
        --------
        ::

            attribute.is_date()
        """
        pass

    @abstractmethod
    def is_datetime(self) -> bool:
        """
        Returns ``True`` if this attribute holds a value of type ``datetime``.
        Otherwise, returns ``False``.

        :return:

        Examples
        --------
        ::

            attribute.is_datetime()
        """
        pass

    @abstractmethod
    def is_datetime_tz(self) -> bool:
        """
        Returns ``True`` if this attribute holds a value of type ``datetime_tz``.
        Otherwise, returns ``False``.

        :return:

        Examples
        --------
        ::

            attribute.is_datetime_tz()
        """
        pass

    @abstractmethod
    def is_duration(self) -> bool:
        """
        Returns ``True`` if this attribute holds a value of type ``duration``.
        Otherwise, returns ``False``.

        :return:

        Examples
        --------
        ::

            attribute.is_duration()
        """
        pass

    @abstractmethod
    def is_struct(self) -> bool:
        """
        Returns ``True`` if this attribute holds a value of type ``struct``.
        Otherwise, returns ``False``.

        :return:

        Examples
        --------
        ::

            attribute.is_struct()
        """
        pass

    @abstractmethod
    def as_boolean(self) -> bool:
        """
        Returns a ``boolean`` value of the value concept that this attribute holds. If the value has
        another type, raises an exception.

        :return:

        Examples
        --------
        ::

            attribute.as_boolean()
        """
        pass

    @abstractmethod
    def as_long(self) -> int:
        """
        Returns a ``long`` value of the value concept that this attribute holds. If the value has
        another type, raises an exception.

        :return:

        Examples
        --------
        ::

            attribute.as_long()
        """
        pass

    @abstractmethod
    def as_double(self) -> float:
        """
        Returns a ``double`` value of the value concept that this attribute holds. If the value has
        another type, raises an exception.

        :return:

        Examples
        --------
        ::

            attribute.as_double()
        """
        pass

    @abstractmethod
    def as_decimal(self) -> Decimal:
        """
        Returns a ``decimal`` value of the value concept that this attribute holds. If the value has
        another type, raises an exception.

        :return:

        Examples
        --------
        ::

            attribute.as_decimal()
        """
        pass

    @abstractmethod
    def as_string(self) -> str:
        """
        Returns a ``string`` value of the value concept that this attribute holds. If the value has
        another type, raises an exception.

        :return:

        Examples
        --------
        ::

            attribute.as_string()
        """
        pass

    @abstractmethod
    def as_date(self) -> date:
        """
        Returns a timezone naive ``date`` value of the value concept that this attribute holds. If the value has
        another type, raises an exception.

        :return:

        Examples
        --------
        ::

            attribute.as_date()
        """
        pass

    @abstractmethod
    def as_datetime(self) -> Datetime:
        """
        Returns a timezone naive ``datetime`` value of the value concept that this attribute holds. If the value has
        another type, raises an exception.

        :return:

        Examples
        --------
        ::

            attribute.as_datetime()
        """
        pass

    @abstractmethod
    def as_datetime_tz(self) -> Datetime:
        """
        Returns a timezone naive ``datetime_tz`` value of the value concept that this attribute holds. If the value has
        another type, raises an exception.

        :return:

        Examples
        --------
        ::

            attribute.as_datetime_tz()
        """
        pass

    @abstractmethod
    def as_duration(self) -> Duration:
        """
        Returns a timezone naive ``duration`` value of the value concept that this attribute holds. If the value has
        another type, raises an exception.

        :return:

        Examples
        --------
        ::

            attribute.as_duration()
        """
        pass

    @abstractmethod
    def as_struct(self) -> Value.STRUCT:
        """
        Returns a ``struct`` value of the value concept that this attribute holds represented as a map from field names
        to values. If the value has another type, raises an exception.

        :return:

        Examples
        --------
        ::

            attribute.as_struct()
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
