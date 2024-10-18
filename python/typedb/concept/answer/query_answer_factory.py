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

from typing import TYPE_CHECKING

from typedb.common.exception import TypeDBDriverException, UNEXPECTED_NATIVE_VALUE
from typedb.concept.answer.concept_document_iterator import _ConceptDocumentIterator
from typedb.concept.answer.concept_row_iterator import _ConceptRowIterator
from typedb.concept.answer.ok_query_answer import _OkQueryAnswer
from typedb.concept.answer.query_answer import _QueryAnswer
from typedb.native_driver_wrapper import \
    query_answer_is_ok, query_answer_is_concept_row_stream, query_answer_is_concept_document_stream

if TYPE_CHECKING:
    from typedb.native_driver_wrapper import QueryAnswer as NativeQueryAnswer


def wrap_query_answer(native_query_answer: NativeQueryAnswer) -> _QueryAnswer:
    if query_answer_is_ok(native_query_answer):
        return _OkQueryAnswer(native_query_answer)
    elif query_answer_is_concept_row_stream(native_query_answer):
        return _ConceptRowIterator(native_query_answer)
    elif query_answer_is_concept_document_stream(native_query_answer):
        return _ConceptDocumentIterator(native_query_answer)
    else:
        raise TypeDBDriverException(UNEXPECTED_NATIVE_VALUE)
