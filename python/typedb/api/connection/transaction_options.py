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

from typing import Optional

from typedb.common.exception import TypeDBDriverException, ILLEGAL_STATE
from typedb.common.native_wrapper import NativeWrapper
from typedb.common.validation import require_positive
from typedb.native_driver_wrapper import transaction_options_new, \
    transaction_options_has_transaction_timeout_millis, transaction_options_get_transaction_timeout_millis, \
    transaction_options_set_transaction_timeout_millis, transaction_options_get_schema_lock_acquire_timeout_millis, \
    transaction_options_has_schema_lock_acquire_timeout_millis, \
    transaction_options_set_schema_lock_acquire_timeout_millis, TransactionOptions as NativeOptions


class TransactionOptions(NativeWrapper[NativeOptions]):
    """
    TypeDB transaction options. ``TransactionOptions`` object can be used to override the default server behaviour
    for opened transactions.

    Options could be specified either as constructor arguments or using
    properties assignment.

    Examples
    --------

    ::

      transaction_options = TransactionOptions(transaction_timeout_millis=20_000)
      transaction_options.schema_lock_acquire_timeout_millis = 50_000
    """

    def __init__(self, *,
                 transaction_timeout_millis: Optional[int] = None,
                 schema_lock_acquire_timeout_millis: Optional[int] = None,
                 ):
        super().__init__(transaction_options_new())
        if transaction_timeout_millis is not None:
            self.transaction_timeout_millis = transaction_timeout_millis
        if schema_lock_acquire_timeout_millis is not None:
            self.schema_lock_acquire_timeout_millis = schema_lock_acquire_timeout_millis

    @property
    def _native_object_not_owned_exception(self) -> TypeDBDriverException:
        return TypeDBDriverException(ILLEGAL_STATE)

    @property
    def transaction_timeout_millis(self) -> Optional[int]:
        """
        If set, specifies a timeout for killing transactions automatically, preventing
        memory leaks in unclosed transactions.
        """
        return transaction_options_get_transaction_timeout_millis(self.native_object) \
            if transaction_options_has_transaction_timeout_millis(self.native_object) else None

    @transaction_timeout_millis.setter
    def transaction_timeout_millis(self, transaction_timeout_millis: int):
        require_positive(transaction_timeout_millis, "transaction_timeout_millis")
        transaction_options_set_transaction_timeout_millis(self.native_object, transaction_timeout_millis)

    @property
    def schema_lock_acquire_timeout_millis(self) -> Optional[int]:
        """
        If set, specifies how long the driver should wait if opening a transaction
        is blocked by a schema write lock.
        """
        return transaction_options_get_schema_lock_acquire_timeout_millis(self.native_object) \
            if transaction_options_has_schema_lock_acquire_timeout_millis(self.native_object) else None

    @schema_lock_acquire_timeout_millis.setter
    def schema_lock_acquire_timeout_millis(self, schema_lock_acquire_timeout_millis: int):
        require_positive(schema_lock_acquire_timeout_millis, "schema_lock_acquire_timeout_millis")
        transaction_options_set_schema_lock_acquire_timeout_millis(self.native_object,
                                                                   schema_lock_acquire_timeout_millis)
