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

from abc import ABC, abstractmethod
from enum import IntEnum
from typing import TYPE_CHECKING, Iterator, Optional

from typedb.native_driver_wrapper import (
    Match as NativeMatch,
    Insert as NativeInsert,
    Put as NativePut,
    Update as NativeUpdate,
    Delete as NativeDelete,
    Select as NativeSelect,
    Sort as NativeSort,
    Require as NativeRequire,
    Offset as NativeOffset,
    Limit as NativeLimit,
    Distinct as NativeDistinct,
    Reduce as NativeReduce,

    Ascending as NativeAscending,
    Descending as NativeDescending,
)


if TYPE_CHECKING:
    from typedb.native_driver_wrapper import (
        ConjunctionID,
        Variable,
    )
    from typedb.api.analyze.reducer import Reducer

class PipelineStageVariant(IntEnum):
    Match = NativeMatch
    Insert = NativeInsert
    Put = NativePut
    Update = NativeUpdate
    Delete = NativeDelete
    Select = NativeSelect
    Sort = NativeSort
    Require = NativeRequire
    Offset = NativeOffset
    Limit = NativeLimit
    Distinct = NativeDistinct
    Reduce = NativeReduce


class PipelineStage(ABC):
    """
    Representation of a stage in a Pipeline.
    """

    @abstractmethod
    def variant(self) -> PipelineStageVariant:
        """Return the pipeline stage variant."""
        raise NotImplementedError

    # Type check methods
    @abstractmethod
    def is_match(self) -> bool:
        raise NotImplementedError

    @abstractmethod
    def is_insert(self) -> bool:
        raise NotImplementedError

    @abstractmethod
    def is_put(self) -> bool:
        raise NotImplementedError

    @abstractmethod
    def is_update(self) -> bool:
        raise NotImplementedError

    @abstractmethod
    def is_delete(self) -> bool:
        raise NotImplementedError

    @abstractmethod
    def is_select(self) -> bool:
        raise NotImplementedError

    @abstractmethod
    def is_sort(self) -> bool:
        raise NotImplementedError

    @abstractmethod
    def is_require(self) -> bool:
        raise NotImplementedError

    @abstractmethod
    def is_offset(self) -> bool:
        raise NotImplementedError

    @abstractmethod
    def is_limit(self) -> bool:
        raise NotImplementedError

    @abstractmethod
    def is_distinct(self) -> bool:
        raise NotImplementedError

    @abstractmethod
    def is_reduce(self) -> bool:
        raise NotImplementedError

    # Conversion / downcast methods
    @abstractmethod
    def as_match(self) -> "PipelineStage.MatchStage":
        raise NotImplementedError

    @abstractmethod
    def as_insert(self) -> "PipelineStage.InsertStage":
        raise NotImplementedError

    @abstractmethod
    def as_put(self) -> "PipelineStage.PutStage":
        raise NotImplementedError

    @abstractmethod
    def as_update(self) -> "PipelineStage.UpdateStage":
        raise NotImplementedError

    @abstractmethod
    def as_delete(self) -> "PipelineStage.DeleteStage":
        raise NotImplementedError

    @abstractmethod
    def as_select(self) -> "PipelineStage.SelectStage":
        raise NotImplementedError

    @abstractmethod
    def as_sort(self) -> "PipelineStage.SortStage":
        raise NotImplementedError

    @abstractmethod
    def as_require(self) -> "PipelineStage.RequireStage":
        raise NotImplementedError

    @abstractmethod
    def as_offset(self) -> "PipelineStage.OffsetStage":
        raise NotImplementedError

    @abstractmethod
    def as_limit(self) -> "PipelineStage.LimitStage":
        raise NotImplementedError

    @abstractmethod
    def as_distinct(self) -> "PipelineStage.DistinctStage":
        raise NotImplementedError

    @abstractmethod
    def as_reduce(self) -> "PipelineStage.ReduceStage":
        raise NotImplementedError

class MatchStage(PipelineStage, ABC):
    """Represents a 'match' stage: match <block>"""
    @abstractmethod
    def block(self) -> "ConjunctionID":
        """The index into Pipeline.conjunctions."""
        raise NotImplementedError

class InsertStage(PipelineStage, ABC):
    """Represents an 'insert' stage: insert <block>"""
    @abstractmethod
    def block(self) -> "ConjunctionID":
        raise NotImplementedError

class PutStage(PipelineStage, ABC):
    """Represents a 'put' stage: put <block>"""
    @abstractmethod
    def block(self) -> "ConjunctionID":
        raise NotImplementedError

class UpdateStage(PipelineStage, ABC):
    """Represents an 'update' stage: update <block>"""
    @abstractmethod
    def block(self) -> "ConjunctionID":
        raise NotImplementedError

class DeleteStage(PipelineStage, ABC):
    """Represents a 'delete' stage."""
    @abstractmethod
    def block(self) -> "ConjunctionID":
        raise NotImplementedError

    @abstractmethod
    def deleted_variables(self) -> Iterator["Variable"]:
        """The variables for which the unified concepts are to be deleted."""
        raise NotImplementedError

class SelectStage(PipelineStage, ABC):
    """Represents a 'select' stage: select <variables>"""
    @abstractmethod
    def variables(self) -> Iterator["Variable"]:
        raise NotImplementedError

class SortStage(PipelineStage, ABC):
    """Represents a 'sort' stage: sort <variables-and-order>"""
    @abstractmethod
    def variables(self) -> Iterator["PipelineStage.SortStage.SortVariable"]:
        raise NotImplementedError

    class SortOrderVariant(IntEnum):
        Ascending = NativeAscending
        Descending = NativeDescending

    class SortVariable(ABC):
        """A variable and its sort order."""
        @abstractmethod
        def variable(self) -> "Variable":
            raise NotImplementedError

        @abstractmethod
        def order(self) -> "SortOrderVariant":
            raise NotImplementedError

class RequireStage(PipelineStage, ABC):
    """Represents a 'require' stage: require <variables>"""
    @abstractmethod
    def variables(self) -> Iterator["Variable"]:
        raise NotImplementedError

class OffsetStage(PipelineStage, ABC):
    """Represents an 'offset' stage: offset <offset>"""
    @abstractmethod
    def offset(self) -> int:
        raise NotImplementedError

class LimitStage(PipelineStage, ABC):
    """Represents a 'limit' stage: limit <limit>"""
    @abstractmethod
    def limit(self) -> int:
        raise NotImplementedError

class DistinctStage(PipelineStage, ABC):
    """Represents a 'distinct' stage."""
    pass

class ReduceStage(PipelineStage, ABC):
    """Represents a 'reduce' stage: reduce <reducers> groupby <groupby>"""
    @abstractmethod
    def group_by(self) -> Iterator["Variable"]:
        """The variables to group by."""
        raise NotImplementedError

    @abstractmethod
    def reduce_assignments(self) -> Iterator["ReduceStage.ReduceAssignment"]:
        """The reducer assignments."""
        raise NotImplementedError

    class ReduceAssignment(ABC):
        """An assignment of a reducer to a variable."""
        @abstractmethod
        def assigned(self) -> "Variable":
            raise NotImplementedError

        @abstractmethod
        def reducer(self) -> "Reducer":
            raise NotImplementedError