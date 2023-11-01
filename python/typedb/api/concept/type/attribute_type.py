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
from typing import Optional, TYPE_CHECKING, Iterator, Union

from typedb.api.concept.type.thing_type import ThingType
from typedb.api.concept.value.value import ValueType
from typedb.common.transitivity import Transitivity

if TYPE_CHECKING:
    from typedb.api.concept.value.value import Value
    from typedb.api.concept.type.annotation import Annotation
    from typedb.api.concept.thing.attribute import Attribute
    from typedb.api.connection.transaction import TypeDBTransaction
    from typedb.common.promise import Promise


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

    def get_value_type(self) -> ValueType:
        """
        Retrieves the ``ValueType`` of this ``AttributeType``.

        :return:

        Examples
        --------
        ::

            attribute_type.get_value_type()
        """
        return ValueType.OBJECT

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

    def is_boolean(self) -> bool:
        """
        Returns ``True`` if the value for attributes of this type is
        of type ``boolean``. Otherwise, returns ``False``.

        :return:

        Examples
        --------
        ::

            attribute_type.is_boolean()
        """
        return self.get_value_type() == ValueType.BOOLEAN

    def is_long(self) -> bool:
        """
        Returns ``True`` if the value for attributes of this type is
        of type ``long``. Otherwise, returns ``False``.

        :return:

        Examples
        --------
        ::

            attribute_type.is_long()
        """
        return self.get_value_type() == ValueType.LONG

    def is_double(self) -> bool:
        """
        Returns ``True`` if the value for attributes of this type is
        of type ``double``. Otherwise, returns ``False``.

        :return:

        Examples
        --------
        ::

            attribute_type.is_double()
        """
        return self.get_value_type() == ValueType.DOUBLE

    def is_string(self) -> bool:
        """
        Returns ``True`` if the value for attributes of this type is
        of type ``string``. Otherwise, returns ``False``.

        :return:

        Examples
        --------
        ::

            attribute_type.is_string()
        """
        return self.get_value_type() == ValueType.STRING

    def is_datetime(self) -> bool:
        """
        Returns ``True`` if the value for attributes of this type is
        of type ``datetime``. Otherwise, returns ``False``.

        :return:

        Examples
        --------
        ::

            attribute_type.is_datetime()
        """
        return self.get_value_type() == ValueType.DATETIME

    @abstractmethod
    def put(self, transaction: TypeDBTransaction, value: Union[Value, bool, int, float, str, datetime]) -> Promise[Attribute]:
        '''
        Adds and returns an ``Attribute`` of this ``AttributeType`` with the given value.

        :param transaction: The current transaction
        :param value: New ``Attribute``'s value
        :return:

        Examples
        --------
        ::

            attribute = attribute_type.put(transaction, value).resolve()
        '''
        pass

    @abstractmethod
    def get(self, transaction: TypeDBTransaction, value: Union[Value, bool, int, float, str,
                                                               datetime]) -> Promise[Optional[Attribute]]:
        '''
        Retrieves an ``Attribute`` of this ``AttributeType`` with the given value
        if such ``Attribute`` exists. Otherwise, returns ``None``.

        :param transaction: The current transaction
        :param value: ``Attribute``'s value
        :return:

        Examples
        --------
        ::

            attribute = attribute_type.get(transaction, value).resolve()
        '''
        pass

    @abstractmethod
    def get_regex(self, transaction: TypeDBTransaction) -> Promise[str]:
        """
        Retrieves the regular expression that is defined
        for this ``AttributeType``.

        :param transaction: The current transaction
        :return:

        Examples
        --------
        ::

            attribute_type.get_regex(transaction).resolve()
        """
        pass

    @abstractmethod
    def set_regex(self, transaction: TypeDBTransaction, regex: str) -> Promise[None]:
        """
        Sets a regular expression as a constraint for this ``AttributeType``.
        ``Values`` of all ``Attribute``\ s of this type (inserted earlier
        or later) should match this regex.

        Can only be applied for ``AttributeType``\ s with a ``string``
        value type.

        :param transaction: The current transaction
        :param regex: Regular expression
        :return:

        Examples
        --------
        ::

            attribute_type.set_regex(transaction, regex).resolve()
        """
        pass

    @abstractmethod
    def unset_regex(self, transaction: TypeDBTransaction) -> Promise[None]:
        """
        Removes the regular expression that is defined
        for this ``AttributeType``.

        :param transaction: The current transaction
        :return:

        Examples
        --------
        ::

            attribute_type.unset_regex(transaction).resolve()
        """
        pass

    @abstractmethod
    def set_supertype(self, transaction: TypeDBTransaction, super_attribute_type: AttributeType) -> Promise[None]:
        """
        Sets the supplied ``AttributeType`` as the supertype of
        the current ``AttributeType``.

        :param transaction: The current transaction
        :param super_attribute_type: The ``AttributeType`` to set as
            the supertype of this ``AttributeType``
        :return:

        Examples
        --------
        ::

            attribute_type.set_supertype(transaction, super_attribute_type).resolve()
        """
        pass

    @abstractmethod
    def get_subtypes_with_value_type(
        self,
        transaction: TypeDBTransaction,
        value_type: ValueType,
        transitivity: Transitivity = Transitivity.TRANSITIVE
    ) -> Iterator[AttributeType]:
        """
        Retrieves all direct and indirect (or direct only) subtypes
        of this ``AttributeType`` with given ``ValueType``.

        :param transaction: The current transaction
        :param value_type: ``ValueType`` for retrieving subtypes
        :param transitivity: ``Transitivity.TRANSITIVE`` for direct
            and indirect subtypes, ``Transitivity.EXPLICIT`` for direct
            subtypes only
        :return:

        Examples
        --------
        ::

            attribute_type.get_subtypes_with_value_type(transaction, value_type)
            attribute_type.get_subtypes_with_value_type(transaction, value_type,
                                                        Transitivity.EXPLICIT)
        """
        pass

    @abstractmethod
    def get_instances(self,
                      transaction: TypeDBTransaction,
                      transitivity: Transitivity = Transitivity.TRANSITIVE) -> Iterator[Attribute]:
        """
        Retrieves all direct and indirect (or direct only) ``Attributes``
        that are instances of this ``AttributeType``.

        :param transaction: The current transaction
        :param transitivity: ``Transitivity.TRANSITIVE`` for direct
            and indirect subtypes, ``Transitivity.EXPLICIT`` for direct
            subtypes only
        :return:

        Examples
        --------
        ::

            attribute_type.get_instances(transaction)
            attribute_type.get_instances(transaction, Transitivity.EXPLICIT)
        """
        pass

    @abstractmethod
    def get_owners(
        self,
        transaction: TypeDBTransaction,
        annotations: Optional[set[Annotation]] = None,
        transitivity: Transitivity = Transitivity.TRANSITIVE
    ) -> Iterator[ThingType]:
        """
        Retrieve all ``Things`` that own an attribute of this ``AttributeType``.
        Optionally, filtered by ``Annotation``\ s.

        :param transaction: The current transaction
        :param annotations: Only retrieve ``ThingTypes`` that have an attribute
            of this ``AttributeType`` with all given ``Annotation``\ s
        :param transitivity: ``Transitivity.TRANSITIVE`` for direct
            and inherited ownership, ``Transitivity.EXPLICIT`` for direct
            ownership only
        :return:

        Examples
        --------
        ::

            attribute_type.get_owners(transaction)
            attribute_type.get_owners(transaction, annotations=Annotation.unique(), transitivity=Transitivity.EXPLICIT)
        """
        pass
