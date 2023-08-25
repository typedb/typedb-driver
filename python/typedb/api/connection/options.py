#
# Copyright (C) 2022 Vaticle
#
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
#

from __future__ import annotations

from typing import Optional

from typedb.native_client_wrapper import options_new, options_has_infer, options_get_infer, options_set_infer, \
    options_get_trace_inference, options_has_trace_inference, options_set_trace_inference, options_get_explain, \
    options_has_explain, options_set_explain, options_has_parallel, options_get_parallel, options_set_parallel, \
    options_get_prefetch, options_has_prefetch, options_set_prefetch, options_has_prefetch_size, \
    options_get_prefetch_size, options_set_prefetch_size, options_get_session_idle_timeout_millis, \
    options_has_session_idle_timeout_millis, options_set_session_idle_timeout_millis, \
    options_has_transaction_timeout_millis, options_get_transaction_timeout_millis, \
    options_set_transaction_timeout_millis, options_get_schema_lock_acquire_timeout_millis, \
    options_has_schema_lock_acquire_timeout_millis, options_set_schema_lock_acquire_timeout_millis, \
    options_set_read_any_replica, options_get_read_any_replica, options_has_read_any_replica, Options as NativeOptions

from typedb.common.exception import TypeDBClientExceptionExt, ILLEGAL_STATE, POSITIVE_VALUE_REQUIRED
from typedb.common.native_wrapper import NativeWrapper


class TypeDBOptions(NativeWrapper[NativeOptions]):

    def __init__(self, *,
                 infer: Optional[bool] = None,
                 trace_inference: Optional[bool] = None,
                 explain: Optional[bool] = None,
                 parallel: Optional[bool] = None,
                 prefetch: Optional[bool] = None,
                 prefetch_size: Optional[int] = None,
                 session_idle_timeout_millis: Optional[int] = None,
                 transaction_timeout_millis: Optional[int] = None,
                 schema_lock_acquire_timeout_millis: Optional[int] = None,
                 read_any_replica: Optional[bool] = None,
                 ):
        super().__init__(options_new())
        if infer is not None:
            self.infer = infer
        if trace_inference is not None:
            self.trace_inference = trace_inference
        if explain is not None:
            self.explain = explain
        if parallel is not None:
            self.parallel = parallel
        if prefetch is not None:
            self.prefetch = prefetch
        if prefetch_size is not None:
            self.prefetch_size = prefetch_size
        if session_idle_timeout_millis is not None:
            self.session_idle_timeout_millis = session_idle_timeout_millis
        if transaction_timeout_millis is not None:
            self.transaction_timeout_millis = transaction_timeout_millis
        if schema_lock_acquire_timeout_millis is not None:
            self.schema_lock_acquire_timeout_millis = schema_lock_acquire_timeout_millis
        if read_any_replica is not None:
            self.read_any_replica = read_any_replica

    @property
    def _native_object_not_owned_exception(self) -> TypeDBClientExceptionExt:
        return TypeDBClientExceptionExt.of(ILLEGAL_STATE)

    @property
    def infer(self) -> Optional[bool]:
        return options_get_infer(self.native_object) if options_has_infer(self.native_object) else None

    @infer.setter
    def infer(self, infer: bool):
        options_set_infer(self.native_object, infer)

    @property
    def trace_inference(self) -> Optional[bool]:
        return options_get_trace_inference(self.native_object) if options_has_trace_inference(self.native_object) \
            else None

    @trace_inference.setter
    def trace_inference(self, trace_inference: bool):
        options_set_trace_inference(self.native_object, trace_inference)

    @property
    def explain(self) -> Optional[bool]:
        return options_get_explain(self.native_object) if options_has_explain(self.native_object) else None

    @explain.setter
    def explain(self, explain: bool):
        options_set_explain(self.native_object, explain)

    @property
    def parallel(self) -> Optional[bool]:
        return options_get_parallel(self.native_object) if options_has_parallel(self.native_object) else None

    @parallel.setter
    def parallel(self, parallel: bool):
        options_set_parallel(self.native_object, parallel)

    @property
    def prefetch(self) -> Optional[bool]:
        return options_get_prefetch(self.native_object) if options_has_prefetch(self.native_object) else None

    @prefetch.setter
    def prefetch(self, prefetch: bool):
        options_set_prefetch(self.native_object, prefetch)

    @property
    def prefetch_size(self) -> Optional[int]:
        return options_get_prefetch_size(self.native_object) if options_has_prefetch_size(self.native_object) else None

    @prefetch_size.setter
    def prefetch_size(self, prefetch_size: int):
        if prefetch_size < 1:
            raise TypeDBClientExceptionExt.of(POSITIVE_VALUE_REQUIRED, prefetch_size)
        options_set_prefetch_size(self.native_object, prefetch_size)

    @property
    def session_idle_timeout_millis(self) -> Optional[int]:
        return options_get_session_idle_timeout_millis(self.native_object) \
            if options_has_session_idle_timeout_millis(self.native_object) else None

    @session_idle_timeout_millis.setter
    def session_idle_timeout_millis(self, session_idle_timeout_millis: int):
        if session_idle_timeout_millis < 1:
            raise TypeDBClientExceptionExt.of(POSITIVE_VALUE_REQUIRED, session_idle_timeout_millis)
        options_set_session_idle_timeout_millis(self.native_object, session_idle_timeout_millis)

    @property
    def transaction_timeout_millis(self) -> Optional[int]:
        return options_get_transaction_timeout_millis(self.native_object) \
            if options_has_transaction_timeout_millis(self.native_object) else None

    @transaction_timeout_millis.setter
    def transaction_timeout_millis(self, transaction_timeout_millis: int):
        if transaction_timeout_millis < 1:
            raise TypeDBClientExceptionExt.of(POSITIVE_VALUE_REQUIRED, transaction_timeout_millis)
        options_set_transaction_timeout_millis(self.native_object, transaction_timeout_millis)

    @property
    def schema_lock_acquire_timeout_millis(self) -> Optional[int]:
        return options_get_schema_lock_acquire_timeout_millis(self.native_object) \
            if options_has_schema_lock_acquire_timeout_millis(self.native_object) else None

    @schema_lock_acquire_timeout_millis.setter
    def schema_lock_acquire_timeout_millis(self, schema_lock_acquire_timeout_millis: int):
        if schema_lock_acquire_timeout_millis < 1:
            raise TypeDBClientExceptionExt.of(POSITIVE_VALUE_REQUIRED, schema_lock_acquire_timeout_millis)
        options_set_schema_lock_acquire_timeout_millis(self.native_object, schema_lock_acquire_timeout_millis)

    @property
    def read_any_replica(self) -> Optional[bool]:
        return options_get_read_any_replica(self.native_object) if options_has_read_any_replica(self.native_object) \
            else None

    @read_any_replica.setter
    def read_any_replica(self, read_any_replice: bool):
        options_set_read_any_replica(self.native_object, read_any_replice)
