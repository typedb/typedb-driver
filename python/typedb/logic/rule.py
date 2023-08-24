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

from typing import TYPE_CHECKING

from typedb.native_client_wrapper import rule_get_when, rule_get_then, rule_get_label, rule_set_label, rule_delete, \
    rule_is_deleted, rule_to_string, Rule as NativeRule

from typedb.api.logic.rule import Rule
from typedb.common.exception import TypeDBClientExceptionExt, MISSING_LABEL, NULL_NATIVE_OBJECT, ILLEGAL_STATE
from typedb.common.native_wrapper import NativeWrapper

if TYPE_CHECKING:
    from typedb.connection.transaction import _Transaction


class _Rule(Rule, NativeWrapper[NativeRule]):

    def __init__(self, rule: NativeRule):
        if not rule:
            raise TypeDBClientExceptionExt(NULL_NATIVE_OBJECT)
        super().__init__(rule)
        self._rule = rule
        self._when = rule_get_when(self._rule)
        self._then = rule_get_then(self._rule)

    @property
    def _native_object_not_owned_exception(self) -> TypeDBClientExceptionExt:
        return TypeDBClientExceptionExt.of(ILLEGAL_STATE)

    @property
    def label(self) -> str:
        return rule_get_label(self.native_object)

    def set_label(self, transaction: _Transaction, new_label: str) -> None:
        if not new_label:
            raise TypeDBClientExceptionExt(MISSING_LABEL)
        rule_set_label(transaction.logic, self.native_object, new_label)

    @property
    def when(self) -> str:
        return self._when

    @property
    def then(self) -> str:
        return self._then

    def delete(self, transaction: _Transaction) -> None:
        rule_delete(transaction.logic, self.native_object)

    def is_deleted(self, transaction: _Transaction) -> bool:
        return rule_is_deleted(transaction.logic, self.native_object)

    def __repr__(self):
        return rule_to_string(self.native_object)

    def __eq__(self, other):
        if other is self:
            return True
        if not other or type(self) != type(other):
            return False
        return self.label == other.label

    def __hash__(self):
        return hash(self.label)
