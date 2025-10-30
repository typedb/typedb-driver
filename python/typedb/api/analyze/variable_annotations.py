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
from enum import IntEnum
from typing import TYPE_CHECKING, Iterator

from typedb.native_driver_wrapper import (
    InstanceAnnotations as NativeInstanceAnnotations,
    TypeAnnotations as NativeTypeAnnotations,
    ValueAnnotations as NativeValueAnnotations,
)

if TYPE_CHECKING:
    from typedb.api.concept.type import Type


class VariableAnnotationsVariant(IntEnum):
    InstanceAnnotations = NativeInstanceAnnotations
    TypeAnnotations = NativeTypeAnnotations
    ValueAnnotations = NativeValueAnnotations


class VariableAnnotations(ABC):
    """
    Holds typing information about a variable (instance/type/value annotations).
    """

    @abstractmethod
    def variant(self) -> "VariableAnnotationsVariant":
        """The variant indicates whether this is an instance variable, type variable, or value variable."""
        raise NotImplementedError

    @abstractmethod
    def is_instance(self) -> bool:
        """Returns True if this variable is an Instance variable."""
        raise NotImplementedError

    @abstractmethod
    def is_type(self) -> bool:
        """Returns True if this variable is a Type variable."""
        raise NotImplementedError

    @abstractmethod
    def is_value(self) -> bool:
        """Returns True if this variable is a Value variable."""
        raise NotImplementedError

    @abstractmethod
    def as_instance(self) -> Iterator["Type"]:
        """The possible Types of instances this variable can hold."""
        raise NotImplementedError

    @abstractmethod
    def as_type(self) -> Iterator["Type"]:
        """The possible types this variable can hold."""
        raise NotImplementedError

    @abstractmethod
    def as_value(self) -> Iterator[str]:
        """The possible ValueType(s) of values this variable can hold."""
        raise NotImplementedError
