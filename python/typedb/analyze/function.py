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

from abc import ABC
from typing import TYPE_CHECKING, Iterator

from typedb.common.exception import TypeDBDriverException, UNEXPECTED_NATIVE_VALUE, NULL_NATIVE_OBJECT, ILLEGAL_STATE, \
    INVALID_RETURN_OPERATION_CASTING
from typedb.common.iterator_wrapper import IteratorWrapper
from typedb.common.native_wrapper import NativeWrapper
from typedb.native_driver_wrapper import (
    Function as NativeFunction,
    ReturnOperation as NativeReturnOperation,
    function_body,
    function_argument_variables,
    function_return_operation,
    function_argument_annotations,
    function_return_annotations,

    return_operation_variant,
    return_operation_stream_variables,
    return_operation_single_selector,
    return_operation_single_variables,
    return_operation_reducers,

    reducer_iterator_next,
    variable_iterator_next,
    variable_annotations_iterator_next,
)

from typedb.api.analyze.function import (
    Function, ReturnOperation,
    ReturnOperationStream, ReturnOperationSingle, ReturnOperationCheck, ReturnOperationReduce,
)

from typedb.analyze.pipeline import _Pipeline
from typedb.analyze.variable_annotations import _VariableAnnotations
from typedb.analyze.reducer import _Reducer
from typedb.analyze.variable import _Variable
from typedb.analyze.variants import ReturnOperationVariant

if TYPE_CHECKING:
    from typedb.api.analyze.pipeline import Pipeline
    from typedb.api.analyze.variable_annotations import VariableAnnotations
    from typedb.api.analyze.reducer import Reducer
    from typedb.api.analyze.variable import Variable


class _Function(Function, NativeWrapper[NativeFunction]):
    def __init__(self, function: NativeFunction):
        if not function:
            raise TypeDBDriverException(NULL_NATIVE_OBJECT)
        super().__init__(function)

    @staticmethod
    def _return_operation_of(op: NativeReturnOperation):
        variant = ReturnOperationVariant(return_operation_variant(op))
        if variant == ReturnOperationVariant.StreamReturn:
            return _ReturnOperationStream(op)
        elif variant == ReturnOperationVariant.SingleReturn:
            return _ReturnOperationSingle(op)
        elif variant == ReturnOperationVariant.CheckReturn:
            return _ReturnOperationCheck(op)
        elif variant == ReturnOperationVariant.ReduceReturn:
            return _ReturnOperationReduce(op)
        else:
            raise TypeDBDriverException(UNEXPECTED_NATIVE_VALUE)

    @property
    def _native_object_not_owned_exception(self) -> TypeDBDriverException:
        return TypeDBDriverException(ILLEGAL_STATE)

    def body(self) -> "Pipeline":
        return _Pipeline(function_body(self.native_object))

    def argument_variables(self) -> Iterator["Variable"]:
        native_iter = function_argument_variables(self.native_object)
        return map(_Variable, IteratorWrapper(native_iter, variable_iterator_next))

    def return_operation(self) -> "ReturnOperation":
        return _Function._return_operation_of(function_return_operation(self.native_object))

    def argument_annotations(self) -> Iterator["VariableAnnotations"]:
        iterator = IteratorWrapper(function_argument_annotations(self.native_object),
                                   variable_annotations_iterator_next)
        return map(_VariableAnnotations, iterator)

    def return_annotations(self) -> Iterator["VariableAnnotations"]:
        iterator = IteratorWrapper(function_return_annotations(self.native_object), variable_annotations_iterator_next)
        return map(_VariableAnnotations, iterator)


class _ReturnOperation(ReturnOperation, NativeWrapper[NativeReturnOperation], ABC):
    def __init__(self, return_operation: NativeReturnOperation):
        if not return_operation:
            raise TypeDBDriverException(NULL_NATIVE_OBJECT)
        super().__init__(return_operation)

    @property
    def _native_object_not_owned_exception(self) -> TypeDBDriverException:
        return TypeDBDriverException(ILLEGAL_STATE)

    def variant(self) -> "ReturnOperationVariant":
        return ReturnOperationVariant(return_operation_variant(self.native_object))

    def is_stream(self) -> bool:
        return False

    def is_single(self) -> bool:
        return False

    def is_check(self) -> bool:
        return False

    def is_reduce(self) -> bool:
        return False

    def as_stream(self) -> "ReturnOperationStream":
        raise TypeDBDriverException(INVALID_RETURN_OPERATION_CASTING,
                                    (self.__class__.__name__, "ReturnOperationStream"))

    def as_single(self) -> "ReturnOperationSingle":
        raise TypeDBDriverException(INVALID_RETURN_OPERATION_CASTING,
                                    (self.__class__.__name__, "ReturnOperationSingle"))

    def as_check(self) -> "ReturnOperationCheck":
        raise TypeDBDriverException(INVALID_RETURN_OPERATION_CASTING, (self.__class__.__name__, "ReturnOperationCheck"))

    def as_reduce(self) -> "ReturnOperationReduce":
        raise TypeDBDriverException(INVALID_RETURN_OPERATION_CASTING,
                                    (self.__class__.__name__, "ReturnOperationReduce"))


class _ReturnOperationStream(_ReturnOperation, ReturnOperationStream):
    def __init__(self, stream: NativeReturnOperation):
        super().__init__(stream)

    def is_stream(self) -> bool:
        return True

    def as_stream(self):
        return self

    def variables(self) -> Iterator["Variable"]:
        return map(_Variable,
                   IteratorWrapper(return_operation_stream_variables(self.native_object), variable_iterator_next))


class _ReturnOperationSingle(_ReturnOperation, ReturnOperationSingle):
    def __init__(self, single: NativeReturnOperation):
        super().__init__(single)

    def is_single(self) -> bool:
        return True

    def as_single(self):
        return self

    def variables(self) -> Iterator["Variable"]:
        return map(_Variable,
                   IteratorWrapper(return_operation_single_variables(self.native_object), variable_iterator_next))

    def selector(self) -> str:
        return return_operation_single_selector(self.native_object)


class _ReturnOperationCheck(_ReturnOperation, ReturnOperationCheck):
    def __init__(self, check: NativeReturnOperation):
        super().__init__(check)

    def is_check(self) -> bool:
        return True

    def as_check(self):
        return self


class _ReturnOperationReduce(_ReturnOperation, ReturnOperationReduce):
    def __init__(self, reduce: NativeReturnOperation):
        super().__init__(reduce)

    def is_reduce(self) -> bool:
        return True

    def as_reduce(self):
        return self

    def reducers(self) -> Iterator["Reducer"]:
        iterator = IteratorWrapper(return_operation_reducers(self.native_object), reducer_iterator_next)
        return map(_Reducer, iterator)
