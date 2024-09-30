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
from typing import TYPE_CHECKING, Iterator, Union, Optional

from typedb.api.concept.type.thing_type import ThingType
from typedb.common.transitivity import Transitivity

if TYPE_CHECKING:
    from typedb.api.concept.thing.relation import Relation
    from typedb.api.concept.type.role_type import RoleType
    from typedb.api.connection.transaction import Transaction
    from typedb.common.promise import Promise


class RelationType(ThingType, ABC):
    """
    Relation types (or subtypes of the relation root type) represent relationships
    between types. Relation types have roles.

    Other types can play roles in relations if it’s mentioned in their definition.

    A relation type must specify at least one role.
    """

    def is_relation_type(self) -> bool:
        """
        Checks if the concept is a ``RelationType``.

        :return:

        Examples
        --------
        ::

            relation_type.is_relation_type()
        """
        return True

    def as_relation_type(self) -> RelationType:
        """
        Casts the concept to ``RelationType``.

        :return:

        Examples
        --------
        ::

            relation_type.as_relation_type()
        """
        return self
