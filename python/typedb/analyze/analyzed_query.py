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

from typedb.api.analyze.analyzed_query import AnalyzedQuery
from typedb.common.exception import TypeDBDriverException, ILLEGAL_STATE, NULL_NATIVE_OBJECT
from typedb.common.iterator_wrapper import IteratorWrapper
from typedb.common.native_wrapper import NativeWrapper

from typedb.analyze.pipeline import _Pipeline
from typedb.analyze.function import _Function
from typedb.analyze.fetch import _Fetch

from typedb.native_driver_wrapper import AnalyzedQuery as NativeAnalyzedQuery, \
    analyzed_preamble, analyzed_query_pipeline, analyzed_fetch, \
    function_iterator_next

if TYPE_CHECKING:
    from typedb.api.analyze.pipeline import Pipeline
    from typedb.api.analyze.function import Function
    from typedb.api.analyze.fetch import Fetch


class _AnalyzedQuery(AnalyzedQuery, NativeWrapper[NativeAnalyzedQuery]):
    def __init__(self, analyzed_query: NativeAnalyzedQuery):
        if not analyzed_query:
            raise TypeDBDriverException(NULL_NATIVE_OBJECT)
        super().__init__(analyzed_query)

    @property
    def _native_object_not_owned_exception(self) -> TypeDBDriverException:
        return TypeDBDriverException(ILLEGAL_STATE)

    def pipeline(self) -> "Pipeline":
        return _Pipeline(analyzed_query_pipeline(self.native_object))

    def preamble(self) -> Iterator["Function"]:
        return map(_Function, IteratorWrapper(analyzed_preamble(self.native_object), function_iterator_next))

    def fetch(self) -> Optional["Fetch"]:
        native_fetch = analyzed_fetch(self.native_object)
        if native_fetch is None:
            return None
        else:
            return _Fetch.of(native_fetch)
