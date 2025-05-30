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

from abc import ABC

from typedb.api.answer.query_answer import QueryAnswer
from typedb.api.answer.query_type import QueryType
from typedb.common.exception import TypeDBDriverException, ILLEGAL_STATE, NULL_NATIVE_OBJECT
from typedb.native_driver_wrapper import QueryAnswer as NativeQueryAnswer, query_answer_get_query_type


class _QueryAnswer(QueryAnswer, ABC):

    def __init__(self, query_answer: NativeQueryAnswer):
        if not query_answer:
            raise TypeDBDriverException(NULL_NATIVE_OBJECT)
        self._query_type = QueryType(query_answer_get_query_type(query_answer))

    @property
    def _native_object_not_owned_exception(self) -> TypeDBDriverException:
        return TypeDBDriverException(ILLEGAL_STATE)

    @property
    def query_type(self) -> QueryType:
        return self._query_type
