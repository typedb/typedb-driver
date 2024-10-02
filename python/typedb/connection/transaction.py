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

from typing import TYPE_CHECKING

from typedb.api.answer.query_answer import QueryAnswer
from typedb.api.connection.transaction import Transaction
from typedb.common.exception import TypeDBDriverException, TRANSACTION_CLOSED, MISSING_QUERY, TypeDBException
from typedb.common.native_wrapper import NativeWrapper
from typedb.common.promise import Promise
from typedb.concept.answer.query_answer_factory import wrap_query_answer
from typedb.native_driver_wrapper import error_code, error_message, transaction_new, transaction_query, \
    transaction_commit, \
    transaction_rollback, transaction_is_open, transaction_on_close, transaction_force_close, \
    query_answer_promise_resolve, \
    Transaction as NativeTransaction, TransactionCallbackDirector, TypeDBDriverExceptionNative, void_promise_resolve

if TYPE_CHECKING:
    from typedb.api.connection.transaction import TransactionType
    from typedb.native_driver_wrapper import Error as NativeError


class _Transaction(Transaction, NativeWrapper[NativeTransaction]):

    def __init__(self, driver: Driver, database_name: str,
                 transaction_type: TransactionType):  # , options: Options = None
        # if not options:
        #     options = Options()
        self._type = transaction_type
        # self._options = options
        try:
            super().__init__(
                transaction_new(driver.native_object, database_name, transaction_type.value))  # , options.native_object
        except TypeDBDriverExceptionNative as e:
            raise TypeDBDriverException.of(e)

    @property
    def _native_object_not_owned_exception(self) -> TypeDBDriverException:
        return TypeDBDriverException(TRANSACTION_CLOSED)

    @property
    def type(self) -> TransactionType:
        return self._type

    # @property
    # def options(self) -> Options:
    #     return self._options

    def query(self, query: str) -> Promise[QueryAnswer]:
        if not query or query.isspace():
            raise TypeDBDriverException(MISSING_QUERY)

        promise = transaction_query(self.native_object, query)
        return Promise.map(wrap_query_answer, lambda: query_answer_promise_resolve(promise))

    def is_open(self) -> bool:
        if not self._native_object.thisown:
            return False
        return transaction_is_open(self.native_object)

    def on_close(self, function: callable):
        transaction_on_close(self.native_object, _Transaction.TransactionOnClose(function).__disown__())

    class TransactionOnClose(TransactionCallbackDirector):

        def __init__(self, function: callable):
            super().__init__()
            self._function = function

        def callback(self, error: NativeError) -> None:
            self._function(TypeDBException(error_code(error), error_message(error)))

    def commit(self):
        try:
            self._native_object.thisown = 0
            void_promise_resolve(transaction_commit(self._native_object))
        except TypeDBDriverExceptionNative as e:
            raise TypeDBDriverException.of(e)

    def rollback(self):
        try:
            void_promise_resolve(transaction_rollback(self.native_object))
        except TypeDBDriverExceptionNative as e:
            raise TypeDBDriverException.of(e)

    def close(self):
        if self._native_object.thisown:
            transaction_force_close(self._native_object)

    def __enter__(self):
        return self

    def __exit__(self, exc_type, exc_val, exc_tb):
        self.close()
        if exc_tb is not None:
            return False
