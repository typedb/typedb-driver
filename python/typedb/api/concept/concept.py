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
from typing import TYPE_CHECKING, Dict, Optional, Union

from typedb.common.datetime import Datetime
from typedb.common.duration import Duration
from typedb.common.exception import TypeDBDriverException, INVALID_CONCEPT_CASTING

if TYPE_CHECKING:
    from typedb.api.concept.instance.attribute import Attribute
    from typedb.api.concept.instance.entity import Entity
    from typedb.api.concept.instance.relation import Relation
    from typedb.api.concept.instance.instance import Instance
    from typedb.api.concept.type.attribute_type import AttributeType
    from typedb.api.concept.type.entity_type import EntityType
    from typedb.api.concept.type.relation_type import RelationType
    from typedb.api.concept.type.role_type import RoleType
    from typedb.api.concept.type.type import Type
    from typedb.api.concept.value.value import Value


class Concept(ABC):
    STRUCT = Dict[str, Optional["Value"]]
    VALUE = Union[bool, int, float, Decimal, str, date, Datetime, Duration, STRUCT]

    def is_type(self) -> bool:
        """
        Checks if the concept is a ``Type``.

        :return:

        Examples
        --------
        ::

            concept.is_type()
        """
        return False

    def is_entity_type(self) -> bool:
        """
        Checks if the concept is an ``EntityType``.

        :return:

        Examples
        --------
        ::

            concept.is_entity_type()
        """
        return False

    def is_attribute_type(self) -> bool:
        """
        Checks if the concept is an ``AttributeType``.

        :return:

        Examples
        --------
        ::

            concept.is_attribute_type()
        """
        return False

    def is_relation_type(self) -> bool:
        """
        Checks if the concept is a ``RelationType``.

        :return:

        Examples
        --------
        ::

            concept.is_relation_type()
        """
        return False

    def is_role_type(self) -> bool:
        """
        Checks if the concept is a ``RoleType``.

        :return:

        Examples
        --------
        ::

            concept.is_role_type()
        """
        return False

    def is_instance(self) -> bool:
        """
        Checks if the concept is a ``Instance``.

        :return:

        Examples
        --------
        ::

            concept.is_instance()
        """
        return False

    def is_entity(self) -> bool:
        """
        Checks if the concept is an ``Entity``.

        :return:

        Examples
        --------
        ::

            concept.is_entity()
        """
        return False

    def is_attribute(self) -> bool:
        """
        Checks if the concept is an ``Attribute``.

        :return:

        Examples
        --------
        ::

            concept.is_attribute()
        """
        return False

    def is_relation(self) -> bool:
        """
        Checks if the concept is a ``Relation``.

        :return:

        Examples
        --------
        ::

            concept.is_relation()
        """
        return False

    def is_value(self) -> bool:
        """
        Checks if the concept is a ``Value``.

        :return:

        Examples
        --------
        ::

            concept.is_value()
        """
        return False

    def as_type(self) -> Type:
        """
        Casts the concept to ``Type``.

        :return:

        Examples
        --------
        ::

            concept.as_type()
        """
        raise TypeDBDriverException(INVALID_CONCEPT_CASTING, (self.__class__.__name__, "Type"))

    def as_entity_type(self) -> EntityType:
        """
        Casts the concept to ``EntityType``.

        :return:

        Examples
        --------
        ::

            concept.as_entity_type()
        """
        raise TypeDBDriverException(INVALID_CONCEPT_CASTING, (self.__class__.__name__, "EntityType"))

    def as_attribute_type(self) -> AttributeType:
        """
        Casts the concept to ``AttributeType``.

        :return:

        Examples
        --------
        ::

            concept.as_attribute_type()
        """
        raise TypeDBDriverException(INVALID_CONCEPT_CASTING, (self.__class__.__name__, "AttributeType"))

    def as_relation_type(self) -> RelationType:
        """
        Casts the concept to ``RelationType``.

        :return:

        Examples
        --------
        ::

            concept.as_relation_type()
        """
        raise TypeDBDriverException(INVALID_CONCEPT_CASTING, (self.__class__.__name__, "RelationType"))

    def as_role_type(self) -> RoleType:
        """
        Casts the concept to ``RoleType``.

        :return:

        Examples
        --------
        ::

            concept.as_role_type()
        """
        raise TypeDBDriverException(INVALID_CONCEPT_CASTING, (self.__class__.__name__, "RoleType"))

    def as_instance(self) -> Instance:
        """
        Casts the concept to ``Instance``.

        :return:

        Examples
        --------
        ::

            concept.as_instance()
        """
        raise TypeDBDriverException(INVALID_CONCEPT_CASTING, (self.__class__.__name__, "Instance"))

    def as_entity(self) -> Entity:
        """
        Casts the concept to ``Entity``.

        :return:

        Examples
        --------
        ::

            concept.as_entity()
        """
        raise TypeDBDriverException(INVALID_CONCEPT_CASTING, (self.__class__.__name__, "Entity"))

    def as_attribute(self) -> Attribute:
        """
        Casts the concept to ``Attribute``.

        :return:

        Examples
        --------
        ::

            concept.as_attribute()
        """
        raise TypeDBDriverException(INVALID_CONCEPT_CASTING, (self.__class__.__name__, "Attribute"))

    def as_relation(self) -> Relation:
        """
        Casts the concept to ``Relation``.

        :return:

        Examples
        --------
        ::

            concept.as_relation()
        """
        raise TypeDBDriverException(INVALID_CONCEPT_CASTING, (self.__class__.__name__, "Relation"))

    def as_value(self) -> Value:
        """
        Casts the concept to ``Value``.

        :return:

        Examples
        --------
        ::

            concept.as_value()
        """
        raise TypeDBDriverException(INVALID_CONCEPT_CASTING, (self.__class__.__name__, "Value"))

    @abstractmethod
    def is_boolean(self) -> bool:
        """
        Returns ``True`` if the value which this ``Concept`` holds is of type ``boolean``
        or if this ``Concept`` is an ``AttributeType`` of type ``boolean``.
        Otherwise, returns ``False``.

        :return:

        Examples
        --------
        ::

            concept.is_boolean()
        """
        pass

    @abstractmethod
    def is_integer(self) -> bool:
        """
        Returns ``True`` if the value which this ``Concept`` holds is of type ``integer``
        or if this ``Concept`` is an ``AttributeType`` of type ``integer``.
        Otherwise, returns ``False``.

        :return:

        Examples
        --------
        ::

            concept.is_integer()
        """
        pass

    @abstractmethod
    def is_double(self) -> bool:
        """
        Returns ``True`` if the value which this ``Concept`` holds is of type ``double``
        or if this ``Concept`` is an ``AttributeType`` of type ``double``.
        Otherwise, returns ``False``.

        :return:

        Examples
        --------
        ::

            concept.is_double()
        """
        pass

    @abstractmethod
    def is_decimal(self) -> bool:
        """
        Returns ``True`` if the value which this ``Concept`` holds is of type ``decimal``
        or if this ``Concept`` is an ``AttributeType`` of type ``decimal``.
        Otherwise, returns ``False``.

        :return:

        Examples
        --------
        ::

            concept.is_decimal()
        """
        pass

    @abstractmethod
    def is_string(self) -> bool:
        """
        Returns ``True`` if the value which this ``Concept`` holds is of type ``string``
        or if this ``Concept`` is an ``AttributeType`` of type ``string``.
        Otherwise, returns ``False``.

        :return:

        Examples
        --------
        ::

            concept.is_string()
        """
        pass

    @abstractmethod
    def is_date(self) -> bool:
        """
        Returns ``True`` if the value which this ``Concept`` holds is of type ``date``
        or if this ``Concept`` is an ``AttributeType`` of type ``date``.
        Otherwise, returns ``False``.

        :return:

        Examples
        --------
        ::

            concept.is_date()
        """
        pass

    @abstractmethod
    def is_datetime(self) -> bool:
        """
        Returns ``True`` if the value which this ``Concept`` holds is of type ``datetime``
        or if this ``Concept`` is an ``AttributeType`` of type ``datetime``.
        Otherwise, returns ``False``.

        :return:

        Examples
        --------
        ::

            concept.is_datetime()
        """
        pass

    @abstractmethod
    def is_datetime_tz(self) -> bool:
        """
        Returns ``True`` if the value which this ``Concept`` holds is of type ``datetime-tz``
        or if this ``Concept`` is an ``AttributeType`` of type ``datetime-tz``.
        Otherwise, returns ``False``.

        :return:

        Examples
        --------
        ::

            concept.is_datetime_tz()
        """
        pass

    @abstractmethod
    def is_duration(self) -> bool:
        """
        Returns ``True`` if the value which this ``Concept`` holds is of type ``duration``
        or if this ``Concept`` is an ``AttributeType`` of type ``duration``.
        Otherwise, returns ``False``.

        :return:

        Examples
        --------
        ::

            concept.is_duration()
        """
        pass

    @abstractmethod
    def is_struct(self) -> bool:
        """
        Returns ``True`` if the value which this ``Concept`` holds is of type ``struct``
        or if this ``Concept`` is an ``AttributeType`` of type ``struct``.
        Otherwise, returns ``False``.

        :return:

        Examples
        --------
        ::

            concept.is_struct()
        """
        pass

    # TODO: Could be useful to have is_struct(struct_name)

    @abstractmethod
    def try_get_boolean(self) -> Optional[bool]:
        """
        Returns a ``boolean`` value of this ``Concept``.
        If it's not a ``Value`` or it has another type, raises an exception.

        :return:

        Examples
        --------
        ::

            value.try_get_boolean()
        """
        pass

    @abstractmethod
    def try_get_integer(self) -> Optional[int]:
        """
        Returns a ``integer`` value of this ``Concept``.
        If it's not a ``Value`` or it has another type, raises an exception.

        :return:

        Examples
        --------
        ::

            value.try_get_integer()
        """
        pass

    @abstractmethod
    def try_get_double(self) -> Optional[float]:
        """
        Returns a ``double`` value of this ``Concept``.
        If it's not a ``Value`` or it has another type, raises an exception.

        :return:

        Examples
        --------
        ::

            value.try_get_double()
        """
        pass

    @abstractmethod
    def try_get_decimal(self) -> Optional[Decimal]:
        """
        Returns a ``decimal`` value of this ``Concept``.
        If it's not a ``Value`` or it has another type, raises an exception.

        :return:

        Examples
        --------
        ::

            value.try_get_decimal()
        """
        pass

    @abstractmethod
    def try_get_string(self) -> Optional[str]:
        """
        Returns a ``string`` value of this ``Concept``.
        If it's not a ``Value`` or it has another type, raises an exception.

        :return:

        Examples
        --------
        ::

            value.try_get_string()
        """
        pass

    @abstractmethod
    def try_get_date(self) -> Optional[date]:
        """
        Returns a timezone naive ``date`` value of this ``Concept``.
        If it's not a ``Value`` or it has another type, raises an exception.

        :return:

        Examples
        --------
        ::

            value.try_get_date()
        """
        pass

    @abstractmethod
    def try_get_datetime(self) -> Optional[Datetime]:
        """
        Returns a timezone naive ``datetime`` value of this ``Concept``.
        If it's not a ``Value`` or it has another type, raises an exception.

        :return:

        Examples
        --------
        ::

            value.try_get_datetime()
        """
        pass

    @abstractmethod
    def try_get_datetime_tz(self) -> Optional[Datetime]:
        """
        Returns a timezone naive ``datetime_tz`` value of this ``Concept``.
        If it's not a ``Value`` or it has another type, raises an exception.

        :return:

        Examples
        --------
        ::

            value.try_get_datetime_tz()
        """
        pass

    @abstractmethod
    def try_get_duration(self) -> Optional[Duration]:
        """
        Returns a timezone naive ``duration`` value of this ``Concept``.
        If it's not a ``Value`` or it has another type, raises an exception.

        :return:

        Examples
        --------
        ::

            value.try_get_duration()
        """
        pass

    @abstractmethod
    def try_get_struct(self) -> Optional[STRUCT]:
        """
        Returns a ``struct`` value of this ``Concept`` represented as a map from field names to values.
        If it's not a ``Value`` or it has another type, raises an exception.

        :return:

        Examples
        --------
        ::

            value.try_get_struct()
        """
        pass

    @abstractmethod
    def get_label(self) -> str:
        """
        Get the label of the concept.
        If this is an ``Instance``, return the label of the type of this instance ("unknown" if type fetching is disabled).
        If this is a ``Value``, return the label of the value type of the value.
        If this is a ``Type``, return the label of the type.

        :return:

        Examples
        --------
        ::

            concept.get_label()
        """
        pass

    @abstractmethod
    def try_get_label(self) -> Optional[str]:
        """
        Get the label of the concept.
        If this is an ``Instance``, return the label of the type of this instance (``None`` if type fetching is disabled).
        Returns ``None`` if type fetching is disabled.
        If this is a ``Value``, return the label of the value type of the value.
        If this is a ``Type``, return the label of the type.

        :return:

        Examples
        --------
        ::

            concept.try_get_label()
        """
        pass

    @abstractmethod
    def try_get_iid(self) -> Optional[str]:
        """
        Retrieves the unique id of the ``Concept``. Returns ``None`` if absent.

        :return:

        Examples
        --------
        ::

            concept.try_get_iid()
        """
        pass

    @abstractmethod
    def try_get_value_type(self) -> Optional[str]:
        """
        Retrieves the ``str` describing the value type fo this ``Concept``. Returns ``None`` if absent.

        :return:

        Examples
        --------
        ::

            concept.try_get_value_type()
        """
        pass

    @abstractmethod
    def try_get_value(self) -> Optional[VALUE]:
        """
        Retrieves the value which this ``Concept`` holds. Returns ``None`` if this ``Concept`` does not hold any value.

        :return:

        Examples
        --------
        ::

            concept.try_get_value()
        """
        pass
