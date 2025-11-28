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

from typing import TYPE_CHECKING, Iterator, Optional

from typedb.common.iterator_wrapper import IteratorWrapper
from typedb.common.native_wrapper import NativeWrapper
from typedb.common.exception import TypeDBDriverException, ILLEGAL_STATE

from typedb.api.analyze.conjunction import Conjunction
from typedb.analyze.variable_annotations import _VariableAnnotations
from typedb.analyze.constraint import _Constraint
from typedb.analyze.variable import _Variable

from typedb.native_driver_wrapper import (
    Conjunction as NativeConjunction,
    conjunction_get_constraints,
    constraint_with_span_iterator_next,
    conjunction_get_annotated_variables,
    variable_iterator_next,
    conjunction_get_variable_annotations,
)

if TYPE_CHECKING:
    from typedb.api.analyze.constraint import Constraint
    from typedb.api.analyze.variable_annotations import VariableAnnotations
    from typedb.api.analyze.variable import Variable


class _Conjunction(Conjunction, NativeWrapper[NativeConjunction]):
    @property
    def _native_object_not_owned_exception(self) -> TypeDBDriverException:
        return TypeDBDriverException(ILLEGAL_STATE)

    def constraints(self) -> Iterator["Constraint"]:
        native_iter = conjunction_get_constraints(self.native_object)
        return map(_Constraint.of,
                   IteratorWrapper(native_iter, constraint_with_span_iterator_next))

    def annotated_variables(self) -> Iterator["Variable"]:
        native_iter = conjunction_get_annotated_variables(self.native_object)
        return map(_Variable, IteratorWrapper(native_iter, variable_iterator_next))

    def variable_annotations(self, variable: "Variable") -> Optional["VariableAnnotations"]:
        native_annotations = conjunction_get_variable_annotations(self.native_object, variable.native_object)
        return None if native_annotations is None else _VariableAnnotations(native_annotations)
