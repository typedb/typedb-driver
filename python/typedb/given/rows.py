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

from typing import Iterator, Optional, TYPE_CHECKING

from typedb.api.given.rows import GivenRows
from typedb.common.exception import TypeDBDriverException, ILLEGAL_STATE
from typedb.common.native_wrapper import NativeWrapper
from typedb.native_driver_wrapper import (
    given_rows_new, given_rows_push,
    given_row_new, given_row_set_index_to_concept, given_row_set_index_to_empty,
    QueryGivenRows as NativeQueryGivenRows,
)

if TYPE_CHECKING:
    from typedb.api.concept.concept import Concept


class _GivenRows(GivenRows, NativeWrapper[NativeQueryGivenRows]):
    """
    Contains multiple input rows for a query.
    """

    @property
    def _native_object_not_owned_exception(self) -> TypeDBDriverException:
        return TypeDBDriverException(ILLEGAL_STATE)

    @staticmethod
    def build(rows: Iterator[Iterator[Optional["Concept"]]]) -> "_GivenRows":
        rows_list = [list(row) for row in rows]
        native_rows = given_rows_new(len(rows_list))
        for row_list in rows_list:
            native_row = given_row_new(len(row_list))
            for i, concept in enumerate(row_list):
                if concept is None:
                    given_row_set_index_to_empty(native_row, i)
                else:
                    # Disown the Python proxy: Rust takes ownership via take_ownership()
                    concept._native_object.thisown = 0
                    given_row_set_index_to_concept(native_row, i, concept._native_object)
            # Disown the row: given_rows_push takes ownership via take_ownership()
            native_row.thisown = 0
            given_rows_push(native_rows, native_row)
        return _GivenRows(native_rows)
