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

from typing import Callable, Generic, TypeVar

from typedb.common.exception import TypeDBDriverException
from typedb.native_driver_wrapper import TypeDBDriverExceptionNative

T = TypeVar('T')
U = TypeVar('U')


class Promise(Generic[T]):
    """
    A ``Promise`` represents an asynchronous network operation.

    The request it represents is performed immediately. The response is only retrieved
    once the ``Promise`` is ``resolve``\ d.
    """

    def __init__(self, inner: Callable[[], T]):
        self.inner = inner

    @classmethod
    def map(cls, ctor: Callable[[U], T], raw: Callable[[], U]) -> 'Promise[T]':
        def inner():
            if _res := raw():
                return ctor(_res)
            return None

        return cls(inner)

    def resolve(self) -> T:
        """
        Retrieves the result of the Promise.

        :return:

        Examples
        --------

        ::

            promise.resolve()
        """
        try:
            return self.inner()
        except TypeDBDriverExceptionNative as e:
            raise TypeDBDriverException.of(e)
