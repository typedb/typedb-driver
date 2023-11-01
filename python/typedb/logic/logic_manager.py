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

from typedb.native_driver_wrapper import (
    Transaction as NativeTransaction, TypeDBDriverExceptionNative, logic_manager_get_rule, logic_manager_get_rules,
    logic_manager_put_rule, rule_iterator_next, rule_promise_resolve
)

from typedb.api.logic.logic_manager import LogicManager
from typedb.api.logic.rule import Rule
from typedb.common.exception import TypeDBDriverException, MISSING_LABEL, TRANSACTION_CLOSED
from typedb.common.iterator_wrapper import IteratorWrapper
from typedb.common.native_wrapper import NativeWrapper
from typedb.common.promise import Promise
from typedb.logic.rule import _Rule


def _not_blank_label(label: str) -> str:
    if not label or label.isspace():
        raise TypeDBDriverException(MISSING_LABEL)
    return label


class _LogicManager(LogicManager, NativeWrapper[NativeTransaction]):
    def __init__(self, transaction: NativeTransaction):
        super().__init__(transaction)

    @property
    def _native_object_not_owned_exception(self) -> TypeDBDriverException:
        return TypeDBDriverException(TRANSACTION_CLOSED)

    @property
    def _native_transaction(self) -> NativeTransaction:
        return self.native_object

    def get_rule(self, label: str) -> Promise[Optional[Rule]]:
        promise = logic_manager_get_rule(self._native_transaction, _not_blank_label(label))
        return Promise.map(_Rule, lambda: rule_promise_resolve(promise))

    def get_rules(self):
        try:
            return map(_Rule, IteratorWrapper(logic_manager_get_rules(self._native_transaction), rule_iterator_next))
        except TypeDBDriverExceptionNative as e:
            raise TypeDBDriverException.of(e)

    def put_rule(self, label: str, when: str, then: str) -> Promise[Rule]:
        promise = logic_manager_put_rule(self._native_transaction, _not_blank_label(label), when, then)
        return Promise.map(_Rule, lambda: rule_promise_resolve(promise))
