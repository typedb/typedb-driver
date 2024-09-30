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

if TYPE_CHECKING:
    from typedb.api.concept.type.entity_type import EntityType


class Entity(Thing, ABC):
    """
    Instance of data of an entity type, representing a standalone object
    that exists in the data model independently.

    Entity does not have a value. It is usually addressed by its ownership
    over attribute instances and/or roles played in relation instances.
    """

    def is_entity(self) -> bool:
        """
        Checks if the concept is an ``Entity``.

        :return:

        Examples
        --------
        ::

            entity.is_entity()
        """
        return True

    def as_entity(self) -> Entity:
        """
        Casts the concept to ``Entity``.

        :return:

        Examples
        --------
        ::

            entity.as_entity()
        """
        return self

    @abstractmethod
    def get_type(self) -> EntityType:
        """
        Retrieves the type which this ``Entity`` belongs to.

        :return:

        Examples
        --------
        ::

            entity.get_type()
        """
        pass

    @abstractmethod
    def get_iid(self) -> str:
        """
        Retrieves the unique id of the ``Entity``.

        :return:

        Examples
        --------
        ::

            entity.get_iid()
        """
        pass
