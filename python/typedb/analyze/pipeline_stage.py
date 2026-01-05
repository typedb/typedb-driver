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
from typing import TYPE_CHECKING, Iterator

from typedb.common.iterator_wrapper import IteratorWrapper
from typedb.common.native_wrapper import NativeWrapper
from typedb.common.exception import TypeDBDriverException, ILLEGAL_STATE, INVALID_STAGE_CASTING, \
    UNEXPECTED_NATIVE_VALUE, NULL_NATIVE_OBJECT

from typedb.api.analyze.pipeline_stage import (
    PipelineStage,
    MatchStage, InsertStage, PutStage, UpdateStage, DeleteStage,
    SelectStage, SortStage, RequireStage, OffsetStage, LimitStage, DistinctStage, ReduceStage
)
from typedb.common.enums import SortOrder
from typedb.analyze.conjunction_id import _ConjunctionID
from typedb.analyze.reducer import _Reducer
from typedb.analyze.variable import _Variable
from typedb.analyze.variants import PipelineStageVariant

from typedb.native_driver_wrapper import (
    PipelineStage as NativePipelineStage,
    ReduceAssignment as NativeReduceAssignment,
    SortVariable as NativeSortVariable,
    pipeline_stage_variant,
    pipeline_stage_get_block,
    pipeline_stage_delete_get_deleted_variables,
    variable_iterator_next,
    pipeline_stage_select_get_variables,
    variable_iterator_next,
    pipeline_stage_sort_get_sort_variables,
    sort_variable_iterator_next,
    sort_variable_get_variable,
    sort_variable_get_order,
    pipeline_stage_require_get_variables,
    variable_iterator_next,
    pipeline_stage_offset_get_offset,
    pipeline_stage_limit_get_limit,
    pipeline_stage_reduce_get_groupby,
    pipeline_stage_string_repr,
    pipeline_stage_reduce_get_reducer_assignments,
    pipeline_stage_iterator_next,
    reduce_assignment_get_assigned,
    reduce_assignment_get_reducer,
    reduce_assignment_iterator_next,
    variable_iterator_next,
)

if TYPE_CHECKING:
    from typedb.api.analyze.conjunction_id import ConjunctionID
    from typedb.api.analyze.reducer import Reducer
    from typedb.api.analyze.variable import Variable


class _PipelineStage(PipelineStage, NativeWrapper[NativePipelineStage], ABC):

    def __init__(self, pipeline_stage: NativePipelineStage):
        if not pipeline_stage:
            raise TypeDBDriverException(NULL_NATIVE_OBJECT)
        super().__init__(pipeline_stage)

    @staticmethod
    def of(native_object: NativePipelineStage):
        variant = PipelineStageVariant(pipeline_stage_variant(native_object))
        if variant == PipelineStageVariant.Match:
            return _MatchStage(native_object)
        elif variant == PipelineStageVariant.Insert:
            return _InsertStage(native_object)
        elif variant == PipelineStageVariant.Put:
            return _PutStage(native_object)
        elif variant == PipelineStageVariant.Update:
            return _UpdateStage(native_object)
        elif variant == PipelineStageVariant.Delete:
            return _DeleteStage(native_object)
        elif variant == PipelineStageVariant.Select:
            return _SelectStage(native_object)
        elif variant == PipelineStageVariant.Sort:
            return _SortStage(native_object)
        elif variant == PipelineStageVariant.Require:
            return _RequireStage(native_object)
        elif variant == PipelineStageVariant.Offset:
            return _OffsetStage(native_object)
        elif variant == PipelineStageVariant.Limit:
            return _LimitStage(native_object)
        elif variant == PipelineStageVariant.Distinct:
            return _DistinctStage(native_object)
        elif variant == PipelineStageVariant.Reduce:
            return _ReduceStage(native_object)
        else:
            raise TypeDBDriverException(UNEXPECTED_NATIVE_VALUE)

    @property
    def _native_object_not_owned_exception(self) -> TypeDBDriverException:
        return TypeDBDriverException(ILLEGAL_STATE)

    def variant(self) -> PipelineStageVariant:
        return PipelineStageVariant(pipeline_stage_variant(self.native_object))

    def is_match(self) -> bool:
        return False

    def is_insert(self) -> bool:
        return False

    def is_put(self) -> bool:
        return False

    def is_update(self) -> bool:
        return False

    def is_delete(self) -> bool:
        return False

    def is_select(self) -> bool:
        return False

    def is_sort(self) -> bool:
        return False

    def is_require(self) -> bool:
        return False

    def is_offset(self) -> bool:
        return False

    def is_limit(self) -> bool:
        return False

    def is_distinct(self) -> bool:
        return False

    def is_reduce(self) -> bool:
        return False

    def as_match(self):
        raise TypeDBDriverException(INVALID_STAGE_CASTING, (self.__class__.__name__, "MatchStage"))

    def as_insert(self):
        raise TypeDBDriverException(INVALID_STAGE_CASTING, (self.__class__.__name__, "InsertStage"))

    def as_put(self):
        raise TypeDBDriverException(INVALID_STAGE_CASTING, (self.__class__.__name__, "PutStage"))

    def as_update(self):
        raise TypeDBDriverException(INVALID_STAGE_CASTING, (self.__class__.__name__, "UpdateStage"))

    def as_delete(self):
        raise TypeDBDriverException(INVALID_STAGE_CASTING, (self.__class__.__name__, "DeleteStage"))

    def as_select(self):
        raise TypeDBDriverException(INVALID_STAGE_CASTING, (self.__class__.__name__, "SelectStage"))

    def as_sort(self):
        raise TypeDBDriverException(INVALID_STAGE_CASTING, (self.__class__.__name__, "SortStage"))

    def as_require(self):
        raise TypeDBDriverException(INVALID_STAGE_CASTING, (self.__class__.__name__, "RequireStage"))

    def as_offset(self):
        raise TypeDBDriverException(INVALID_STAGE_CASTING, (self.__class__.__name__, "OffsetStage"))

    def as_limit(self):
        raise TypeDBDriverException(INVALID_STAGE_CASTING, (self.__class__.__name__, "LimitStage"))

    def as_distinct(self):
        raise TypeDBDriverException(INVALID_STAGE_CASTING, (self.__class__.__name__, "DistinctStage"))

    def as_reduce(self):
        raise TypeDBDriverException(INVALID_STAGE_CASTING, (self.__class__.__name__, "ReduceStage"))

    def __repr__(self):
        return pipeline_stage_string_repr(self.native_object)


class _MatchStage(MatchStage, _PipelineStage):
    def __init__(self, native):
        super().__init__(native)

    def is_match(self) -> bool:
        return True

    def as_match(self):
        return self

    def block(self) -> "ConjunctionID":
        return _ConjunctionID(pipeline_stage_get_block(self.native_object))


class _InsertStage(InsertStage, _PipelineStage):
    def __init__(self, native):
        super().__init__(native)

    def is_insert(self) -> bool:
        return True

    def as_insert(self):
        return self

    def block(self) -> "ConjunctionID":
        return _ConjunctionID(pipeline_stage_get_block(self.native_object))


class _PutStage(PutStage, _PipelineStage):
    def __init__(self, native):
        super().__init__(native)

    def is_put(self) -> bool:
        return True

    def as_put(self):
        return self

    def block(self) -> "ConjunctionID":
        return _ConjunctionID(pipeline_stage_get_block(self.native_object))


class _UpdateStage(UpdateStage, _PipelineStage):
    def __init__(self, native):
        super().__init__(native)

    def is_update(self) -> bool:
        return True

    def as_update(self):
        return self

    def block(self) -> "ConjunctionID":
        return _ConjunctionID(pipeline_stage_get_block(self.native_object))


class _DeleteStage(DeleteStage, _PipelineStage):
    def __init__(self, native):
        super().__init__(native)

    def is_delete(self) -> bool:
        return True

    def as_delete(self):
        return self

    def block(self) -> "ConjunctionID":
        return _ConjunctionID(pipeline_stage_get_block(self.native_object))

    def deleted_variables(self) -> Iterator["Variable"]:
        native_iter = pipeline_stage_delete_get_deleted_variables(self.native_object)
        return map(_Variable, IteratorWrapper(native_iter, variable_iterator_next))


class _SelectStage(SelectStage, _PipelineStage):
    def __init__(self, native):
        super().__init__(native)

    def is_select(self) -> bool:
        return True

    def as_select(self):
        return self

    def variables(self) -> Iterator["Variable"]:
        native_iter = pipeline_stage_select_get_variables(self.native_object)
        return map(_Variable, IteratorWrapper(native_iter, variable_iterator_next))


class _SortStage(SortStage, _PipelineStage):
    def __init__(self, native):
        super().__init__(native)

    def is_sort(self) -> bool:
        return True

    def as_sort(self):
        return self

    def variables(self) -> Iterator["SortStage.SortVariable"]:
        native_iter = pipeline_stage_sort_get_sort_variables(self.native_object)
        return map(
            _SortStage._SortVariable,
            IteratorWrapper(native_iter, sort_variable_iterator_next)
        )

    class _SortVariable(SortStage.SortVariable, NativeWrapper[NativeSortVariable]):
        def __init__(self, native):
            super().__init__(native)

        @property
        def _native_object_not_owned_exception(self) -> TypeDBDriverException:
            return TypeDBDriverException(ILLEGAL_STATE)

        def variable(self) -> "Variable":
            return _Variable(sort_variable_get_variable(self.native_object))

        def order(self) -> "SortOrder":
            return SortOrder(sort_variable_get_order(self.native_object))


class _RequireStage(RequireStage, _PipelineStage):
    def __init__(self, native):
        super().__init__(native)

    def is_require(self) -> bool:
        return True

    def as_require(self):
        return self

    def variables(self) -> Iterator["Variable"]:
        native_iter = pipeline_stage_require_get_variables(self.native_object)
        return map(_Variable, IteratorWrapper(native_iter, variable_iterator_next))


class _OffsetStage(OffsetStage, _PipelineStage):
    def __init__(self, native):
        super().__init__(native)

    def is_offset(self) -> bool:
        return True

    def as_offset(self):
        return self

    def offset(self) -> int:
        return pipeline_stage_offset_get_offset(self.native_object)


class _LimitStage(LimitStage, _PipelineStage):
    def __init__(self, native):
        super().__init__(native)

    def is_limit(self) -> bool:
        return True

    def as_limit(self):
        return self

    def limit(self) -> int:
        return pipeline_stage_limit_get_limit(self.native_object)


class _DistinctStage(DistinctStage, _PipelineStage):
    def __init__(self, native):
        super().__init__(native)

    def is_distinct(self) -> bool:
        return True

    def as_distinct(self):
        return self


class _ReduceStage(ReduceStage, _PipelineStage):
    def __init__(self, native):
        super().__init__(native)

    def is_reduce(self) -> bool:
        return True

    def as_reduce(self):
        return self

    def group_by(self) -> Iterator["Variable"]:
        native_iter = pipeline_stage_reduce_get_groupby(self.native_object)
        return map(_Variable, IteratorWrapper(native_iter, variable_iterator_next))

    def reduce_assignments(self) -> Iterator["ReduceStage.ReduceAssignment"]:
        native_iter = pipeline_stage_reduce_get_reducer_assignments(self.native_object)
        return map(
            _ReduceStage._ReduceAssignment,
            IteratorWrapper(native_iter, reduce_assignment_iterator_next)
        )

    class _ReduceAssignment(ReduceStage.ReduceAssignment, NativeWrapper[NativeReduceAssignment]):
        def __init__(self, reduce_assignment: NativeReduceAssignment):
            if not reduce_assignment:
                raise TypeDBDriverException(NULL_NATIVE_OBJECT)
            super().__init__(reduce_assignment)

        @property
        def _native_object_not_owned_exception(self) -> TypeDBDriverException:
            return TypeDBDriverException(ILLEGAL_STATE)

        def assigned(self) -> "Variable":
            return _Variable(reduce_assignment_get_assigned(self.native_object))

        def reducer(self) -> "Reducer":
            native_red = reduce_assignment_get_reducer(self.native_object)
            return _Reducer(native_red)
