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
    from typedb.api.concept.type.relation_type import RelationType


class Relation(Thing, ABC):
    """
    Relation is an instance of a relation type and can be uniquely addressed
    by a combination of its type, owned attributes and role players.
    """

    def is_relation(self) -> bool:
        """
        Checks if the concept is a ``Relation``.

        :return:

        Examples
        --------
        ::

            relation.is_relation()
        """
        return True

    def as_relation(self) -> Relation:
        """
        Casts the concept to ``Relation``.

        :return:

        Examples
        --------
        ::

            relation.as_relation()
        """
        return self

    @abstractmethod
    def get_type(self) -> RelationType:
        """
        Retrieves the type which this ``Relation`` belongs to.

        :return:

        Examples
        --------
        ::

            relation.get_type()
        """
        pass

    @abstractmethod
    def get_iid(self) -> str:
        """
        Retrieves the unique id of the ``Relation``.

        :return:

        Examples
        --------
        ::

            relation.get_iid()
        """
        pass
