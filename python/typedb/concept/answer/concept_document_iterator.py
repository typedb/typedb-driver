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

import json
from typing import TYPE_CHECKING, Iterator, Optional

from typedb.api.answer.concept_document_iterator import ConceptDocumentIterator
from typedb.common.iterator_wrapper import IteratorWrapper
from typedb.concept.answer.query_answer import _QueryAnswer
from typedb.native_driver_wrapper import query_answer_into_documents, string_iterator_next

if TYPE_CHECKING:
    from typedb.native_driver_wrapper import QueryAnswer as NativeQueryAnswer


class _ConceptDocumentIterator(_QueryAnswer, ConceptDocumentIterator):

    def __init__(self, query_answer: NativeQueryAnswer):
        super().__init__(query_answer)
        self.iterator = map(json.loads,
                            IteratorWrapper(query_answer_into_documents(query_answer), string_iterator_next))

    def __iter__(self) -> Iterator[Optional[dict]]:
        return iter(self.iterator)

    def __next__(self) -> Optional[dict]:
        return next(self.iterator)
