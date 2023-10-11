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
    from typedb.api.answer.concept_map import ConceptMap
    from typedb.api.logic.rule import Rule


class Explanation(ABC):
    """
    An explanation of which rule was used for inferring the explained concept, the condition of the rule,
    the conclusion of the rule, and the mapping of variables between the query and the rule's conclusion.
    """

    @abstractmethod
    def rule(self) -> Rule:
        """
        Retrieves the Rule for this Explanation.

        :return:

        Examples:
        ---------
        ::

            explanation.rule()
        """
        pass

    @abstractmethod
    def conclusion(self) -> ConceptMap:
        """
        Retrieves the Conclusion for this Explanation.

        :return:

        Examples:
        ---------
        ::

            explanation.conclusion()
        """
        pass

    @abstractmethod
    def condition(self) -> ConceptMap:
        """
        Retrieves the Condition for this Explanation.

        :return:

        Examples:
        ---------
        ::

            explanation.condition()
        """
        pass

    @abstractmethod
    def query_variables(self) -> set[str]:
        """
        Retrieves the query variables for this ``Explanation``.

        :return:

        Examples:
        ---------
        ::

            explanation.query_variables()
        """
        pass

    @abstractmethod
    def query_variable_mapping(self, var: str) -> set[str]:
        """
        Retrieves the rule variables corresponding to the query variable `var` for this ``Explanation``.

        :param var: The query variable to map to rule variables.
        :return:

        Examples:
        ---------
        ::

            explanation.variable_mapping(var)
        """
        pass
