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
from typing import TYPE_CHECKING

if TYPE_CHECKING:
    from typedb.api.connection.transaction import TypeDBTransaction
    from typedb.common.promise import Promise


class Rule(ABC):
    """
    Rules are a part of schema and define embedded logic.
    The reasoning engine uses rules as a set of logic to infer new data.
    A rule consists of a condition and a conclusion, and is uniquely identified by a label.
    """

    @property
    @abstractmethod
    def label(self) -> str:
        """
        The unique label of the rule.
        """
        pass

    @abstractmethod
    def set_label(self, transaction: TypeDBTransaction, new_label: str) -> Promise[None]:
        """
        Renames the label of the rule. The new label must remain unique.

        :param transaction: The current ``Transaction``
        :param new_label: The new label to be given to the rule
        :return:

        Examples:
        ---------
        ::

            rule.set_label(transaction, new_label).resolve()
        """
        pass

    @property
    @abstractmethod
    def when(self) -> str:
        """
        The statements that constitute the 'when' of the rule.
        """
        pass

    @property
    @abstractmethod
    def then(self) -> str:
        """
        The single statement that constitutes the 'then' of the rule.
        """
        pass

    @abstractmethod
    def delete(self, transaction: TypeDBTransaction) -> Promise[None]:
        """
        Deletes this rule.

        :param transaction: The current ``Transaction``
        :return:

        Examples:
        ---------
        ::

            rule.delete(transaction).resolve()
        """
        pass

    @abstractmethod
    def is_deleted(self, transaction: TypeDBTransaction) -> Promise[bool]:
        """
        Check if this rule has been deleted.

        :param transaction: The current ``Transaction``
        :return:

        Examples:
        ---------
        ::

            rule.is_deleted(transaction).resolve()
        """
        pass
