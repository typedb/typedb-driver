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
from typing import TYPE_CHECKING, Iterator

if TYPE_CHECKING:
    from typedb.api.analyze.conjunction_id import ConjunctionID
    from typedb.api.analyze.reducer import Reducer
    from typedb.common.enums import SortOrder
    from typedb.api.analyze.variable import Variable


class PipelineStage(ABC):
    """
    Representation of a stage in a Pipeline.
    """

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

    @abstractmethod
    def as_match(self) -> "MatchStage":
        pass

    @abstractmethod
    def as_insert(self) -> "InsertStage":
        pass

    @abstractmethod
    def as_put(self) -> "PutStage":
        pass

    @abstractmethod
    def as_update(self) -> "UpdateStage":
        pass

    @abstractmethod
    def as_delete(self) -> "DeleteStage":
        pass

    @abstractmethod
    def as_select(self) -> "SelectStage":
        pass

    @abstractmethod
    def as_sort(self) -> "SortStage":
        pass

    @abstractmethod
    def as_require(self) -> "RequireStage":
        pass

    @abstractmethod
    def as_offset(self) -> "OffsetStage":
        pass

    @abstractmethod
    def as_limit(self) -> "LimitStage":
        pass

    @abstractmethod
    def as_distinct(self) -> "DistinctStage":
        pass

    @abstractmethod
    def as_reduce(self) -> "ReduceStage":
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
    def variables(self) -> Iterator["SortStage.SortVariable"]:
        pass

    class SortVariable(ABC):
        """A variable and its sort order."""

        @abstractmethod
        def variable(self) -> "Variable":
            pass

        @abstractmethod
        def order(self) -> "SortOrder":
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
