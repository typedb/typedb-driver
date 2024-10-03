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

from typing import Optional


class Label:
    """
    A ``Label`` holds the uniquely identifying name of a type.

    It consists of an optional ``scope``, and a ``name``, represented ``scope:name``.
    The scope is used only used to distinguish between role-types of the same name declared in different relation types.
    """

    def __init__(self, scope: Optional[str], name: str):
        self._scope = scope
        self._name = name

    @staticmethod
    def of(*args: str) -> Label:
        """
        Creates a Label from a specified name, or scoped name.

        :param args: If a single string is provided, this is interpreted as the label name.
            If a pair of strings is provided, the first string is the scope and the second string is the name.
        :return:

        Examples
        --------

        ::

            Label.of("entity")
            Label.of("relation", "role")
        """
        return Label(scope=args[0], name=args[1]) if len(args) == 2 else Label(scope=None, name=args[0])

    @property
    def scope(self) -> Optional[str]:
        """ The scope part of the label """
        return self._scope

    @property
    def name(self) -> str:
        """ The name part of the label """
        return self._name

    def scoped_name(self) -> str:
        """
        Returns the string representation of the scoped name.

        :return:

        Examples
        --------

        ::

            label.scoped_name()
        """
        return "%s:%s" % (self.scope, self.name) if self.scope else self.name

    def __str__(self):
        return self.scoped_name()

    def __repr__(self):
        return f"Label('{str(self)}')"

    def __hash__(self):
        return hash((self.name, self.scope))

    def __eq__(self, other):
        if other is self:
            return True
        if other is None or not isinstance(other, self.__class__):
            return False
        return self.scope == other.scope and self.name == other.name
