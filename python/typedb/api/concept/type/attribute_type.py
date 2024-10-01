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

from typedb.api.concept.type.thing_type import ThingType


class AttributeType(ThingType, ABC):
    """
    Attribute types represent properties that other types can own.

    Attribute types have a value type. This value type is fixed and unique
    for every given instance of the attribute type.

    Other types can own an attribute type. That means that instances of these
    other types can own an instance of this attribute type.
    This usually means that an object in our domain has a property
    with the matching value.

    Multiple types can own the same attribute type, and different instances
    of the same type or different types can share ownership of the same
    attribute instance.
    """

    @abstractmethod
    def get_value_type(self) -> str:
        """
        Retrieves the ``str`` describing the value type of this ``AttributeType``.

        :return:

        Examples
        --------
        ::

            attribute_type.get_value_type()
        """
        pass

    @abstractmethod
    def is_untyped(self) -> bool:
        """
        Returns ``True`` if this attribute type does not have a value type.
        Otherwise, returns ``False``.

        :return:

        Examples
        --------
        ::

            attribute_type.is_untyped()
        """
        pass

    @abstractmethod
    def is_boolean(self) -> bool:
        """
        Returns ``True`` if this attribute type is of type ``boolean``.
        Otherwise, returns ``False``.

        :return:

        Examples
        --------
        ::

            attribute_type.is_boolean()
        """
        pass

    @abstractmethod
    def is_long(self) -> bool:
        """
        Returns ``True`` if this attribute type is of type ``long``.
        Otherwise, returns ``False``.

        :return:

        Examples
        --------
        ::

            attribute_type.is_long()
        """
        pass

    @abstractmethod
    def is_double(self) -> bool:
        """
        Returns ``True`` if this attribute type is of type ``double``.
        Otherwise, returns ``False``.

        :return:

        Examples
        --------
        ::

            attribute_type.is_double()
        """
        pass

    @abstractmethod
    def is_decimal(self) -> bool:
        """
        Returns ``True`` if this attribute type is of type ``decimal``.
        Otherwise, returns ``False``.

        :return:

        Examples
        --------
        ::

            attribute_type.is_decimal()
        """
        pass

    @abstractmethod
    def is_string(self) -> bool:
        """
        Returns ``True`` if this attribute type is of type ``string``.
        Otherwise, returns ``False``.

        :return:

        Examples
        --------
        ::

            attribute_type.is_string()
        """
        pass

    @abstractmethod
    def is_date(self) -> bool:
        """
        Returns ``True`` if this attribute type is of type ``date``.
        Otherwise, returns ``False``.

        :return:

        Examples
        --------
        ::

            attribute_type.is_date()
        """
        pass

    @abstractmethod
    def is_datetime(self) -> bool:
        """
        Returns ``True`` if this attribute type is of type ``datetime``.
        Otherwise, returns ``False``.

        :return:

        Examples
        --------
        ::

            attribute_type.is_datetime()
        """
        pass

    @abstractmethod
    def is_datetime_tz(self) -> bool:
        """
        Returns ``True`` if this attribute type is of type ``datetime_tz``.
        Otherwise, returns ``False``.

        :return:

        Examples
        --------
        ::

            attribute_type.is_datetime_tz()
        """
        pass

    @abstractmethod
    def is_duration(self) -> bool:
        """
        Returns ``True`` if this attribute type is of type ``duration``.
        Otherwise, returns ``False``.

        :return:

        Examples
        --------
        ::

            attribute_type.is_duration()
        """
        pass

    @abstractmethod
    def is_struct(self) -> bool:
        """
        Returns ``True`` if this attribute type is of type ``struct``.
        Otherwise, returns ``False``.

        :return:

        Examples
        --------
        ::

            attribute_type.is_struct()
        """
        pass

    def as_attribute_type(self) -> AttributeType:
        """
        Casts the concept to ``AttributeType``.

        :return:

        Examples
        --------
        ::

            attribute.as_attribute_type()
        """
        return self

    def is_attribute_type(self) -> bool:
        """
        Checks if the concept is an ``AttributeType``.

        :return:

        Examples
        --------
        ::

            attribute.is_attribute_type()
        """
        return True
