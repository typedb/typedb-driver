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
from typing import TYPE_CHECKING, Iterator

if TYPE_CHECKING:
    from typedb.api.concept.type.type import Type


class VariableAnnotations(ABC):
    """
    Holds typing information about a variable (instance/type/value annotations).
    """

    @abstractmethod
    def is_instance(self) -> bool:
        """Returns True if this variable is an Instance variable."""
        pass

    @abstractmethod
    def is_type(self) -> bool:
        """Returns True if this variable is a Type variable."""
        pass

    @abstractmethod
    def is_value(self) -> bool:
        """Returns True if this variable is a Value variable."""
        pass

    @abstractmethod
    def as_instance(self) -> Iterator["Type"]:
        """The possible Types of instances this variable can hold."""
        pass

    @abstractmethod
    def as_type(self) -> Iterator["Type"]:
        """The possible types this variable can hold."""
        pass

    @abstractmethod
    def as_value(self) -> Iterator[str]:
        """The possible ValueType(s) of values this variable can hold."""
        pass
