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

if TYPE_CHECKING:
    from typedb.api.concept.type import Type
    from typedb.api.concept.value import Value

class ConstraintVertex(ABC):
    """
    The answer to a TypeDB query is a set of concepts which satisfy the constraints in the query.
    A ConstraintVertex is either a variable, or some identifier of the concept.
    """

    @abstractmethod
    def is_variable(self) -> bool:
        """Checks if this vertex is a variable."""
        raise NotImplementedError

    @abstractmethod
    def is_label(self) -> bool:
        """Checks if this vertex is a label."""
        raise NotImplementedError

    @abstractmethod
    def is_value(self) -> bool:
        """Checks if this vertex is a value."""
        raise NotImplementedError

    @abstractmethod
    def is_named_role(self) -> bool:
        """Checks if this vertex is a named role."""
        raise NotImplementedError

    @abstractmethod
    def as_variable(self) -> Variable:
        """Down-casts this vertex to a variable."""
        raise NotImplementedError

    @abstractmethod
    def as_label(self) -> Type:
        """
        Down-casts this vertex to a type label.

        :return: the type representation of this vertex
        :raises: IllegalStateError / appropriate exception if not a label
        """
        raise NotImplementedError

    @abstractmethod
    def as_value(self) -> Value:
        """Down-casts this vertex to a value."""
        raise NotImplementedError

    @abstractmethod
    def as_named_role_get_variable(self) -> "Variable":
        """
        Down-casts the variable as a NamedRoleVertex and gets the associated variable.
        """
        raise NotImplementedError

    @abstractmethod
    def as_named_role_get_name(self) -> str:
        """
        Down-casts the variable as a NamedRoleVertex and gets the associated name.
        """
        raise NotImplementedError