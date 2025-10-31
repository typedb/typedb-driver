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
    pass


class Fetch(ABC):
    """
    A representation of the 'fetch' stage of a query.
    """

    @abstractmethod
    def is_leaf(self) -> bool:
        pass

    @abstractmethod
    def is_list(self) -> bool:
        pass

    @abstractmethod
    def is_object(self) -> bool:
        pass

    @abstractmethod
    def as_leaf(self) -> "FetchLeaf":
        """Down-casts this Fetch as a FetchLeaf variant."""
        pass

    @abstractmethod
    def as_list(self) -> "FetchList":
        """Down-casts this Fetch as a FetchList variant."""
        pass

    @abstractmethod
    def as_object(self) -> "FetchObject":
        """Down-casts this Fetch as a FetchObject variant."""
        pass


class FetchObject(Fetch, ABC):
    """A mapping of string keys to Fetch documents."""

    @abstractmethod
    def keys(self) -> Iterator[str]:
        """The available keys of this Fetch document."""
        pass

    @abstractmethod
    def get(self, key: str) -> "Fetch":
        """The Fetch object for the given key."""
        pass


class FetchList(Fetch, ABC):
    """A list of Fetch documents."""

    @abstractmethod
    def element(self) -> "Fetch":
        """The element type of the list."""
        pass


class FetchLeaf(Fetch, ABC):
    """The leaf of a Fetch object. Holds information on the value it can hold."""

    @abstractmethod
    def annotations(self) -> Iterator[str]:
        """The possible ValueType(s) as strings."""
        pass
