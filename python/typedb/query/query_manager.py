#
# Copyright (C) 2022 Vaticle
#
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
#

from __future__ import annotations

from typing import TYPE_CHECKING, Iterator, Optional

from typedb.native_driver_wrapper import (
    Transaction as NativeTransaction, TypeDBDriverExceptionNative, concept_map_group_iterator_next, concept_map_iterator_next,
    explanation_iterator_next, numeric_group_iterator_next, numeric_promise_resolve, query_define, query_delete, query_explain,
    query_insert, query_match, query_match_aggregate, query_match_group, query_match_group_aggregate, query_undefine,
    query_update, void_promise_resolve
)

from typedb.api.connection.options import TypeDBOptions
from typedb.api.query.query_manager import QueryManager
from typedb.common.exception import TypeDBDriverException, MISSING_QUERY, TRANSACTION_CLOSED
from typedb.common.iterator_wrapper import IteratorWrapper
from typedb.common.native_wrapper import NativeWrapper
from typedb.common.promise import Promise
from typedb.concept.answer.concept_map import _ConceptMap
from typedb.concept.answer.concept_map_group import _ConceptMapGroup
from typedb.concept.answer.numeric import _Numeric
from typedb.concept.answer.numeric_group import _NumericGroup
from typedb.logic.explanation import _Explanation

if TYPE_CHECKING:
    from typedb.api.answer.concept_map import ConceptMap
    from typedb.api.answer.concept_map_group import ConceptMapGroup
    from typedb.api.answer.numeric import Numeric
    from typedb.api.answer.numeric_group import NumericGroup
    from typedb.api.logic.explanation import Explanation


class _QueryManager(QueryManager, NativeWrapper[NativeTransaction]):
    def __init__(self, transaction: NativeTransaction):
        super().__init__(transaction)

    @property
    def _native_object_not_owned_exception(self) -> TypeDBDriverException:
        return TypeDBDriverException(TRANSACTION_CLOSED)

    @property
    def _native_transaction(self) -> NativeTransaction:
        return self.native_object

    def match(self, query: str, options: Optional[TypeDBOptions] = None) -> Iterator[ConceptMap]:
        if not query:
            raise TypeDBDriverException(MISSING_QUERY)
        if not options:
            options = TypeDBOptions()
        try:
            return map(
                _ConceptMap,
                IteratorWrapper(query_match(self._native_transaction, query, options.native_object), concept_map_iterator_next)
            )
        except TypeDBDriverExceptionNative as e:
            raise TypeDBDriverException.of(e)

    def match_aggregate(self, query: str, options: Optional[TypeDBOptions] = None) -> Promise[Numeric]:
        if not query:
            raise TypeDBDriverException(MISSING_QUERY)
        if not options:
            options = TypeDBOptions()
        promise = query_match_aggregate(self._native_transaction, query, options.native_object)
        return Promise.map(_Numeric, lambda: numeric_promise_resolve(promise))

    def match_group(self, query: str, options: Optional[TypeDBOptions] = None) -> Iterator[ConceptMapGroup]:
        if not query:
            raise TypeDBDriverException(MISSING_QUERY)
        if not options:
            options = TypeDBOptions()
        try:
            return map(
                _ConceptMapGroup,
                IteratorWrapper(
                    query_match_group(self._native_transaction, query, options.native_object), concept_map_group_iterator_next
                )
            )
        except TypeDBDriverExceptionNative as e:
            raise TypeDBDriverException.of(e)

    def match_group_aggregate(self, query: str, options: Optional[TypeDBOptions] = None) -> Iterator[NumericGroup]:
        if not query:
            raise TypeDBDriverException(MISSING_QUERY)
        if not options:
            options = TypeDBOptions()
        try:
            return map(
                _NumericGroup,
                IteratorWrapper(
                    query_match_group_aggregate(self._native_transaction, query, options.native_object),
                    numeric_group_iterator_next
                )
            )
        except TypeDBDriverExceptionNative as e:
            raise TypeDBDriverException.of(e)

    def insert(self, query: str, options: Optional[TypeDBOptions] = None) -> Iterator[ConceptMap]:
        if not query:
            raise TypeDBDriverException(MISSING_QUERY)
        if not options:
            options = TypeDBOptions()
        try:
            return map(
                _ConceptMap,
                IteratorWrapper(query_insert(self._native_transaction, query, options.native_object), concept_map_iterator_next)
            )
        except TypeDBDriverExceptionNative as e:
            raise TypeDBDriverException.of(e)

    def delete(self, query: str, options: Optional[TypeDBOptions] = None) -> Promise[None]:
        if not query:
            raise TypeDBDriverException(MISSING_QUERY)
        if not options:
            options = TypeDBOptions()
        promise = query_delete(self._native_transaction, query, options.native_object)
        return Promise(lambda: void_promise_resolve(promise))

    def update(self, query: str, options: Optional[TypeDBOptions] = None) -> Iterator[ConceptMap]:
        if not query:
            raise TypeDBDriverException(MISSING_QUERY)
        if not options:
            options = TypeDBOptions()
        try:
            return map(
                _ConceptMap,
                IteratorWrapper(query_update(self._native_transaction, query, options.native_object), concept_map_iterator_next)
            )
        except TypeDBDriverExceptionNative as e:
            raise TypeDBDriverException.of(e)

    def define(self, query: str, options: TypeDBOptions = None) -> Promise[None]:
        if not query:
            raise TypeDBDriverException(MISSING_QUERY)
        if not options:
            options = TypeDBOptions()
        promise = query_define(self._native_transaction, query, options.native_object)
        return Promise(lambda: void_promise_resolve(promise))

    def undefine(self, query: str, options: TypeDBOptions = None) -> Promise[None]:
        if not query:
            raise TypeDBDriverException(MISSING_QUERY)
        if not options:
            options = TypeDBOptions()
        promise = query_undefine(self._native_transaction, query, options.native_object)
        return Promise(lambda: void_promise_resolve(promise))

    def explain(self, explainable: ConceptMap.Explainable, options: Optional[TypeDBOptions] = None) -> Iterator[Explanation]:
        if not options:
            options = TypeDBOptions()
        try:
            return map(
                _Explanation,
                IteratorWrapper(
                    query_explain(self._native_transaction, explainable._native_object, options.native_object),
                    explanation_iterator_next
                )
            )
        except TypeDBDriverExceptionNative as e:
            raise TypeDBDriverException.of(e)
