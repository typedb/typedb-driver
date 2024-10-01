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

from typing import Callable

from typedb.common.exception import TypeDBDriverException
from typedb.native_driver_wrapper import TypeDBDriverExceptionNative


class IteratorWrapper:

    def __init__(self, native_iterator: object, native_next: Callable):
        self._iterator = native_iterator
        self._next = native_next

    def __iter__(self):
        return self

    def __next__(self):
        try:
            if next_item := self._next(self._iterator):
                return next_item
            raise StopIteration
        except TypeDBDriverExceptionNative as e:
            raise TypeDBDriverException.of(e)
