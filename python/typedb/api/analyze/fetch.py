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
    LeafDocument as NativeLeafDocument,
    ListDocument as NativeListDocument,
    ObjectDocument as NativeObjectDocument,
)

if TYPE_CHECKING:
    pass


class FetchVariant(IntEnum):
    LeafDocument = NativeLeafDocument
    ListDocument = NativeListDocument
    ObjectDocument = NativeObjectDocument,


class Fetch(ABC):
    """
    A representation of the 'fetch' stage of a query.
    """

    @abstractmethod
    def variant(self) -> FetchVariant:
        """
        The variant. One of `List, Leaf, Object`

        :return: fetch variant
        """
        raise NotImplementedError

    @abstractmethod
    def is_leaf(self) -> bool:
        raise NotImplementedError

    @abstractmethod
    def is_list(self) -> bool:
        raise NotImplementedError

    @abstractmethod
    def is_object(self) -> bool:
        raise NotImplementedError

    @abstractmethod
    def as_leaf(self) -> "FetchLeaf":
        """Down-casts this Fetch as a FetchLeaf variant."""
        raise NotImplementedError

    @abstractmethod
    def as_list(self) -> "FetchList":
        """Down-casts this Fetch as a FetchList variant."""
        raise NotImplementedError

    @abstractmethod
    def as_object(self) -> "FetchObject":
        """Down-casts this Fetch as a FetchObject variant."""
        raise NotImplementedError


class FetchObject(Fetch, ABC):
    """A mapping of string keys to Fetch documents."""

    @abstractmethod
    def keys(self) -> Iterator[str]:
        """The available keys of this Fetch document."""
        raise NotImplementedError

    @abstractmethod
    def get(self, key: str) -> "Fetch":
        """The Fetch object for the given key."""
        raise NotImplementedError


class FetchList(Fetch, ABC):
    """A list of Fetch documents."""

    @abstractmethod
    def element(self) -> "Fetch":
        """The element type of the list."""
        raise NotImplementedError


class FetchLeaf(Fetch, ABC):
    """The leaf of a Fetch object. Holds information on the value it can hold."""

    @abstractmethod
    def annotations(self) -> Iterator[str]:
        """The possible ValueType(s) as strings."""
        raise NotImplementedError
