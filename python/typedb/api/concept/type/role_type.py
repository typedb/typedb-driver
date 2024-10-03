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

from abc import ABC
from typing import TYPE_CHECKING

from typedb.api.concept.type.type import Type

if TYPE_CHECKING:
    pass


class RoleType(Type, ABC):
    """
    Roles are special internal types used by relations. We can not create
    an instance of a role in a database. But we can set an instance
    of another type (role player) to play a role in a particular instance
    of a relation type.

    Roles allow a schema to enforce logical constraints on types
    of role players.
    """

    def is_role_type(self) -> bool:
        """
        Checks if the concept is a ``RoleType``.

        :return:

        Examples
        --------
        ::

            role_type.is_role_type()
        """
        return True

    def as_role_type(self) -> RoleType:
        """
        Casts the concept to ``RoleType``.

        :return:

        Examples
        --------
        ::

            role_type.as_role_type()
        """
        return self
