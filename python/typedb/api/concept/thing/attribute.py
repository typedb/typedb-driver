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
from typing import TYPE_CHECKING, Iterator, Mapping, Union, Optional

from typedb.api.concept.thing.thing import Thing

if TYPE_CHECKING:
    from datetime import datetime
    from typedb.api.concept.value.value import ValueType
    from typedb.api.concept.type.attribute_type import AttributeType
    from typedb.api.concept.type.thing_type import ThingType
    from typedb.api.connection.transaction import TypeDBTransaction


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
    def get_value(self) -> Union[bool, int, float, str, datetime]:
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
    def get_value_type(self) -> ValueType:
        """
        Retrieves the type of the value which the ``Attribute`` instance holds.

        :return:
        Examples
        --------
        ::

            attribute.get_value_type()
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

    @abstractmethod
    def is_boolean(self) -> bool:
        """
        Returns ``True`` if the attribute value is of type ``boolean``.
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
        Returns ``True`` if the attribute value is of type ``long``.
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
        Returns ``True`` if the attribute value is of type ``double``.
        Otherwise, returns ``False``.

        :return:

        Examples
        --------
        ::

            attribute.is_double()
        """
        pass

    @abstractmethod
    def is_string(self) -> bool:
        """
        Returns ``True`` if the attribute value is of type ``string``.
        Otherwise, returns ``False``.

        :return:

        Examples
        --------
        ::

            attribute.is_string()
        """
        pass

    @abstractmethod
    def is_datetime(self) -> bool:
        """
        Returns ``True`` if the attribute value is of type ``datetime``.
        Otherwise, returns ``False``.

        :return:

        Examples
        --------
        ::

            attribute.is_datetime()
        """
        pass

    @abstractmethod
    def as_boolean(self) -> bool:
        """
        Returns a ``boolean`` value of the attribute. If the value has
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
        Returns a ``long`` value of the attribute. If the value has
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
        Returns a ``double`` value of the attribute. If the value has
        another type, raises an exception.

        :return:

        Examples
        --------
        ::

            attribute.as_boolean()
        """
        pass

    @abstractmethod
    def as_string(self) -> str:
        """
        Returns a ``string`` value of the attribute. If the value has
        another type, raises an exception.

        :return:

        Examples
        --------
        ::

            attribute.as_boolean()
        """
        pass

    @abstractmethod
    def as_datetime(self) -> datetime:
        """
        Returns a ``datetime`` value of the attribute. If the value has
        another type, raises an exception.

        :return:

        Examples
        --------
        ::

            attribute.as_boolean()
        """
        pass

    @abstractmethod
    def get_owners(self, transaction: TypeDBTransaction, owner_type: Optional[ThingType] = None) -> Iterator[Thing]:
        """
        Retrieves the instances that own this ``Attribute``.

        :param transaction: The current transaction
        :param owner_type: If specified, filter results for only owners
            of the given type
        :return:

        Examples
        --------
        ::

             attribute.get_owners(transaction)
            attribute.get_owners(transaction, owner_type)
        """
        pass
