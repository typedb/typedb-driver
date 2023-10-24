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

from abc import ABC, abstractmethod
from typing import Iterator, Optional, TYPE_CHECKING

if TYPE_CHECKING:
    from typedb.api.logic.rule import Rule
    from typedb.common.promise import Promise


class LogicManager(ABC):
    """
    Provides methods for manipulating rules in the database.
    """

    @abstractmethod
    def get_rule(self, label: str) -> Promise[Optional[Rule]]:
        """
        Retrieves the Rule that has the given label.

        :param label: The label of the Rule to create or retrieve
        :return:

        Examples:
        ---------
        ::

            transaction.logic.get_rule(label).resolve()
        """
        pass

    @abstractmethod
    def get_rules(self) -> Iterator[Rule]:
        """
        Retrieves all rules.

        :return:

        Examples:
        ---------
        ::

            transaction.logic.get_rules()
        """
        pass

    @abstractmethod
    def put_rule(self, label: str, when: str, then: str) -> Promise[Rule]:
        """
        Creates a new Rule if none exists with the given label, or replaces the existing one.

        :param label: The label of the Rule to create or replace
        :param when: The when body of the rule to create
        :param then: The then body of the rule to create
        :return:

        Examples:
        ---------
        ::

            transaction.logic.put_rule(label, when, then).resolve()
        """
        pass
