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

from typedb.api.concept.given_rows import GivenRows
from typedb.common.exception import TypeDBDriverException, ILLEGAL_STATE
from typedb.common.native_wrapper import NativeWrapper
from typedb.native_driver_wrapper import (
    GivenRows as NativeGivenRows,
    given_rows_header_builder_new, given_rows_header_builder_push, given_rows_header_builder_finish,
    given_rows_builder_new, given_rows_builder_start_new_row, given_rows_builder_commit_row,
    given_rows_builder_finish,
    given_rows_builder_set_index_to_concept, given_rows_builder_set_index_to_empty,
    given_rows_builder_set_variable_to_concept, given_rows_builder_set_variable_to_empty,
)


class _GivenRows(GivenRows, NativeWrapper[NativeGivenRows]):
    def __init__(self, native: NativeGivenRows):
        self._native = native

    @property
    def _native_object_not_owned_exception(self) -> TypeDBDriverException:
        return TypeDBDriverException(ILLEGAL_STATE)

    @staticmethod
    def of(variables, rows) -> GivenRows:
        header_builder = given_rows_header_builder_new(len(variables))
        for variable in variables:
            given_rows_header_builder_push(header_builder, variable)
        header_builder.thisown = 0
        header = given_rows_header_builder_finish(header_builder)
        rows_builder = given_rows_builder_new(header, len(rows))
        for row in rows:
            given_rows_builder_start_new_row(rows_builder)
            for i, concept in enumerate(row):
                if concept is None:
                    given_rows_builder_set_index_to_empty(rows_builder, i)
                else:
                    given_rows_builder_set_index_to_concept(rows_builder, i, concept._native_object)
            given_rows_builder_commit_row(rows_builder)
        rows_builder.thisown = 0
        return _GivenRows(given_rows_builder_finish(rows_builder))

    @staticmethod
    def of_map(rows) -> GivenRows:
        from typedb.api.concept.concept import Concept
        from typedb.concept.value.value import _Value
        all_variables: set = set()
        for row in rows:
            all_variables.update(row.keys())
        variables = list(all_variables)
        header_builder = given_rows_header_builder_new(len(variables))
        for variable in variables:
            given_rows_header_builder_push(header_builder, variable)
        header_builder.thisown = 0
        header = given_rows_header_builder_finish(header_builder)
        rows_builder = given_rows_builder_new(header, len(rows))
        for row in rows:
            given_rows_builder_start_new_row(rows_builder)
            for variable, val in row.items():
                if val is None:
                    given_rows_builder_set_variable_to_empty(rows_builder, variable)
                else:
                    concept = val if isinstance(val, Concept) else _Value.try_convert_to_value(val)
                    given_rows_builder_set_variable_to_concept(rows_builder, variable, concept._native_object)
            given_rows_builder_commit_row(rows_builder)
        rows_builder.thisown = 0
        return _GivenRows(given_rows_builder_finish(rows_builder))
