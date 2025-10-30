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
        pass

    # Type check methods
    @abstractmethod
    def is_match(self) -> bool:
        pass

    @abstractmethod
    def is_insert(self) -> bool:
        pass

    @abstractmethod
    def is_put(self) -> bool:
        pass

    @abstractmethod
    def is_update(self) -> bool:
        pass

    @abstractmethod
    def is_delete(self) -> bool:
        pass

    @abstractmethod
    def is_select(self) -> bool:
        pass

    @abstractmethod
    def is_sort(self) -> bool:
        pass

    @abstractmethod
    def is_require(self) -> bool:
        pass

    @abstractmethod
    def is_offset(self) -> bool:
        pass

    @abstractmethod
    def is_limit(self) -> bool:
        pass

    @abstractmethod
    def is_distinct(self) -> bool:
        pass

    @abstractmethod
    def is_reduce(self) -> bool:
        pass

    # Conversion / downcast methods
    @abstractmethod
    def as_match(self) -> "PipelineStage.MatchStage":
        pass

    @abstractmethod
    def as_insert(self) -> "PipelineStage.InsertStage":
        pass

    @abstractmethod
    def as_put(self) -> "PipelineStage.PutStage":
        pass

    @abstractmethod
    def as_update(self) -> "PipelineStage.UpdateStage":
        pass

    @abstractmethod
    def as_delete(self) -> "PipelineStage.DeleteStage":
        pass

    @abstractmethod
    def as_select(self) -> "PipelineStage.SelectStage":
        pass

    @abstractmethod
    def as_sort(self) -> "PipelineStage.SortStage":
        pass

    @abstractmethod
    def as_require(self) -> "PipelineStage.RequireStage":
        pass

    @abstractmethod
    def as_offset(self) -> "PipelineStage.OffsetStage":
        pass

    @abstractmethod
    def as_limit(self) -> "PipelineStage.LimitStage":
        pass

    @abstractmethod
    def as_distinct(self) -> "PipelineStage.DistinctStage":
        pass

    @abstractmethod
    def as_reduce(self) -> "PipelineStage.ReduceStage":
        pass


class MatchStage(PipelineStage, ABC):
    """Represents a 'match' stage: match <block>"""

    @abstractmethod
    def block(self) -> "ConjunctionID":
        """The index into Pipeline.conjunctions."""
        pass


class InsertStage(PipelineStage, ABC):
    """Represents an 'insert' stage: insert <block>"""

    @abstractmethod
    def block(self) -> "ConjunctionID":
        pass


class PutStage(PipelineStage, ABC):
    """Represents a 'put' stage: put <block>"""

    @abstractmethod
    def block(self) -> "ConjunctionID":
        pass


class UpdateStage(PipelineStage, ABC):
    """Represents an 'update' stage: update <block>"""

    @abstractmethod
    def block(self) -> "ConjunctionID":
        pass


class DeleteStage(PipelineStage, ABC):
    """Represents a 'delete' stage."""

    @abstractmethod
    def block(self) -> "ConjunctionID":
        pass

    @abstractmethod
    def deleted_variables(self) -> Iterator["Variable"]:
        """The variables for which the unified concepts are to be deleted."""
        pass


class SelectStage(PipelineStage, ABC):
    """Represents a 'select' stage: select <variables>"""

    @abstractmethod
    def variables(self) -> Iterator["Variable"]:
        pass


class SortStage(PipelineStage, ABC):
    """Represents a 'sort' stage: sort <variables-and-order>"""

    @abstractmethod
    def variables(self) -> Iterator["PipelineStage.SortStage.SortVariable"]:
        pass

    class SortOrderVariant(IntEnum):
        Ascending = NativeAscending
        Descending = NativeDescending

    class SortVariable(ABC):
        """A variable and its sort order."""

        @abstractmethod
        def variable(self) -> "Variable":
            pass

        @abstractmethod
        def order(self) -> "SortOrderVariant":
            pass


class RequireStage(PipelineStage, ABC):
    """Represents a 'require' stage: require <variables>"""

    @abstractmethod
    def variables(self) -> Iterator["Variable"]:
        pass


class OffsetStage(PipelineStage, ABC):
    """Represents an 'offset' stage: offset <offset>"""

    @abstractmethod
    def offset(self) -> int:
        pass


class LimitStage(PipelineStage, ABC):
    """Represents a 'limit' stage: limit <limit>"""

    @abstractmethod
    def limit(self) -> int:
        pass


class DistinctStage(PipelineStage, ABC):
    """Represents a 'distinct' stage."""
    pass


class ReduceStage(PipelineStage, ABC):
    """Represents a 'reduce' stage: reduce <reducers> groupby <groupby>"""

    @abstractmethod
    def group_by(self) -> Iterator["Variable"]:
        """The variables to group by."""
        pass

    @abstractmethod
    def reduce_assignments(self) -> Iterator["ReduceStage.ReduceAssignment"]:
        """The reducer assignments."""
        pass

    class ReduceAssignment(ABC):
        """An assignment of a reducer to a variable."""

        @abstractmethod
        def assigned(self) -> "Variable":
            pass

        @abstractmethod
        def reducer(self) -> "Reducer":
            pass
