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

from typedb.api.answer.concept_tree_iterator import ConceptTreeIterator
from typedb.common.exception import TypeDBDriverException, UNIMPLEMENTED
from typedb.concept.answer.query_answer import _QueryAnswer

if TYPE_CHECKING:
    from typedb.native_driver_wrapper import QueryAnswer as NativeQueryAnswer


class _ConceptTreeIterator(_QueryAnswer, ConceptTreeIterator):

    def __init__(self, query_answer: NativeQueryAnswer):
        super().__init__(query_answer)
        raise TypeDBDriverException(UNIMPLEMENTED)

    def __iter__(self):
        # return iter(self.iterator)
        raise TypeDBDriverException(UNIMPLEMENTED)

    def __next__(self):
        # return next(self.iterator)
        raise TypeDBDriverException(UNIMPLEMENTED)
