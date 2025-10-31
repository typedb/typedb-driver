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

from typing import TYPE_CHECKING, Iterator, Optional

from typedb.api.analyze.fetch import Fetch, FetchLeaf, FetchList, FetchObject
from typedb.common.exception import TypeDBDriverException, ILLEGAL_STATE, UNEXPECTED_NATIVE_VALUE, \
    INVALID_FETCH_CASTING, NULL_NATIVE_OBJECT
from typedb.common.iterator_wrapper import IteratorWrapper
from typedb.common.native_wrapper import NativeWrapper
from typedb.analyze.variants import FetchVariant

from typedb.native_driver_wrapper import (
    Fetch as NativeFetch,
    LeafDocument, ListDocument, ObjectDocument,
    fetch_variant, fetch_leaf_annotations, fetch_list_element, fetch_object_fields, fetch_object_get_field,
    string_iterator_next
)

if TYPE_CHECKING:
    pass


class _Fetch(Fetch, NativeWrapper[NativeFetch]):
    def __init__(self, fetch: NativeFetch):
        if not fetch:
            raise TypeDBDriverException(NULL_NATIVE_OBJECT)
        super().__init__(fetch)

    @staticmethod
    def of(native_fetch: NativeFetch) -> "Fetch":
        variant = fetch_variant(native_fetch)
        if variant == LeafDocument:
            return _FetchLeaf(native_fetch)
        elif variant == ListDocument:
            return _FetchList(native_fetch)
        elif variant == ObjectDocument:
            return _FetchObject(native_fetch)
        else:
            raise TypeDBDriverException(UNEXPECTED_NATIVE_VALUE)

    @property
    def _native_object_not_owned_exception(self) -> TypeDBDriverException:
        return TypeDBDriverException(ILLEGAL_STATE)

    def variant(self) -> FetchVariant:
        return FetchVariant(fetch_variant(self.native_object))

    def is_leaf(self) -> bool:
        return False

    def is_list(self) -> bool:
        return False

    def is_object(self) -> bool:
        return False

    def as_leaf(self) -> "FetchLeaf":
        raise TypeDBDriverException(INVALID_FETCH_CASTING, (self.__class__.__name__, "Fetch.FetchLeaf"))

    def as_list(self) -> "FetchList":
        raise TypeDBDriverException(INVALID_FETCH_CASTING, (self.__class__.__name__, "Fetch.FetchList"))

    def as_object(self) -> "FetchObject":
        raise TypeDBDriverException(INVALID_FETCH_CASTING, (self.__class__.__name__, "Fetch.FetchObject"))


class _FetchObject(FetchObject, _Fetch):
    def is_object(self) -> bool:
        return True

    def as_object(self) -> "FetchObject":
        return self

    def keys(self) -> Iterator[str]:
        native_iter = fetch_object_fields(self.native_object)
        return IteratorWrapper(native_iter, string_iterator_next)

    def get(self, key: str) -> "Fetch":
        return _Fetch.of(fetch_object_get_field(self.native_object, key))


class _FetchList(FetchList, _Fetch):
    def is_list(self) -> bool:
        return True

    def as_list(self) -> "FetchList":
        return self

    def element(self) -> "Fetch":
        return _Fetch.of(fetch_list_element(self.native_object))


class _FetchLeaf(FetchLeaf, _Fetch):
    def is_leaf(self) -> bool:
        return True

    def as_leaf(self) -> "FetchLeaf":
        return self

    def annotations(self) -> Iterator[str]:
        return IteratorWrapper(fetch_leaf_annotations(self.native_object), string_iterator_next)
