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
    from typedb.api.analyze.pipeline import Pipeline
    from typedb.api.analyze.variable_annotations import VariableAnnotations
    from typedb.api.analyze.reducer import Reducer
    from typedb.api.analyze.Variable import Variable


class Function(ABC):
    """
    Holds a representation of a function, and the result of type-inference for each variable.
    """

    @abstractmethod
    def body(self) -> "Pipeline":
        """Gets the pipeline which forms the body of the function."""
        pass

    @abstractmethod
    def argument_variables(self) -> Iterator["Variable"]:
        """Gets the variables which are the arguments of the function."""
        pass

    @abstractmethod
    def return_operation(self) -> "ReturnOperation":
        """Gets the return operation of the function."""
        pass

    @abstractmethod
    def argument_annotations(self) -> Iterator["VariableAnnotations"]:
        """Gets the type annotations for each argument of the function."""
        pass

    @abstractmethod
    def return_annotations(self) -> Iterator["VariableAnnotations"]:
        """Gets the type annotations for each concept returned by the function."""
        pass


class ReturnOperation(ABC):
    @abstractmethod
    def is_stream(self) -> bool:
        pass

    @abstractmethod
    def is_single(self) -> bool:
        pass

    @abstractmethod
    def is_check(self) -> bool:
        pass

    @abstractmethod
    def is_reduce(self) -> bool:
        pass

    @abstractmethod
    def as_stream(self) -> "ReturnOperationStream":
        pass

    @abstractmethod
    def as_single(self) -> "ReturnOperationSingle":
        pass

    @abstractmethod
    def as_check(self) -> "ReturnOperationCheck":
        pass

    @abstractmethod
    def as_reduce(self) -> "ReturnOperationReduce":
        pass


class ReturnOperationStream(ReturnOperation, ABC):
    @abstractmethod
    def variables(self) -> Iterator["Variable"]:
        """Gets the variables in the returned row."""
        pass


class ReturnOperationSingle(ReturnOperation, ABC):
    @abstractmethod
    def variables(self) -> Iterator["Variable"]:
        """Gets the variables in the returned row."""
        pass

    @abstractmethod
    def selector(self) -> str:
        """Gets the selector that determines how the operation selects the row."""
        pass


class ReturnOperationCheck(ReturnOperation, ABC):
    pass


class ReturnOperationReduce(ReturnOperation, ABC):
    @abstractmethod
    def reducers(self) -> Iterator["Reducer"]:
        """Gets the reducers used to compute the aggregations."""
        pass
