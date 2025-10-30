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
from typing import TYPE_CHECKING, Iterator

from typedb.native_driver_wrapper import (
    StreamReturn as NativeStreamReturn,
    SingleReturn as NativeSingleReturn,
    CheckReturn as NativeCheckReturn,
    ReduceReturn as NativeReduceReturn,
)

if TYPE_CHECKING:
    from typedb.analyze.pipeline import Pipeline
    from typedb.analyze.variable_annotations import VariableAnnotations
    from typedb.analyze.reducer import Reducer


class ReturnOperationVariant(IntEnum):
    StreamReturn = NativeStreamReturn
    SingleReturn = NativeSingleReturn
    CheckReturn = NativeCheckReturn
    ReduceReturn = NativeReduceReturn


class Function(ABC):
    """
    Holds a representation of a function, and the result of type-inference for each variable.
    """

    @abstractmethod
    def body(self) -> "Pipeline":
        """Gets the pipeline which forms the body of the function."""
        raise NotImplementedError

    @abstractmethod
    def argument_variables(self) -> Iterator["Variable"]:
        """Gets the variables which are the arguments of the function."""
        raise NotImplementedError

    @abstractmethod
    def return_operation(self) -> "ReturnOperation":
        """Gets the return operation of the function."""
        raise NotImplementedError

    @abstractmethod
    def argument_annotations(self) -> Iterator["VariableAnnotations"]:
        """Gets the type annotations for each argument of the function."""
        raise NotImplementedError

    @abstractmethod
    def return_annotations(self) -> Iterator["VariableAnnotations"]:
        """Gets the type annotations for each concept returned by the function."""
        raise NotImplementedError


class ReturnOperation(ABC):
    @abstractmethod
    def variant(self) -> "ReturnOperationVariant":
        raise NotImplementedError

    @abstractmethod
    def is_stream(self) -> bool:
        raise NotImplementedError

    @abstractmethod
    def is_single(self) -> bool:
        raise NotImplementedError

    @abstractmethod
    def is_check(self) -> bool:
        raise NotImplementedError

    @abstractmethod
    def is_reduce(self) -> bool:
        raise NotImplementedError

    @abstractmethod
    def as_stream(self) -> "ReturnOperationStream":
        raise NotImplementedError

    @abstractmethod
    def as_single(self) -> "ReturnOperationSingle":
        raise NotImplementedError

    @abstractmethod
    def as_check(self) -> "ReturnOperationCheck":
        raise NotImplementedError

    @abstractmethod
    def as_reduce(self) -> "ReturnOperationReduce":
        raise NotImplementedError


class ReturnOperationStream(ReturnOperation, ABC):
    @abstractmethod
    def variables(self) -> Iterator["Variable"]:
        """Gets the variables in the returned row."""
        raise NotImplementedError


class ReturnOperationSingle(ReturnOperation, ABC):
    @abstractmethod
    def variables(self) -> Iterator["Variable"]:
        """Gets the variables in the returned row."""
        raise NotImplementedError

    @abstractmethod
    def selector(self) -> str:
        """Gets the selector that determines how the operation selects the row."""
        raise NotImplementedError


class ReturnOperationCheck(ReturnOperation, ABC):
    pass


class ReturnOperationReduce(ReturnOperation, ABC):
    @abstractmethod
    def reducers(self) -> Iterator["Reducer"]:
        """Gets the reducers used to compute the aggregations."""
        raise NotImplementedError
