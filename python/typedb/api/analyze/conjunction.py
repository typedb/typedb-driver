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

from abc import ABC, abstractmethod
from typing import TYPE_CHECKING, Iterator, Optional

if TYPE_CHECKING:
    from typedb.analyze.constraint import Constraint
    from typedb.analyze.variable_annotations import VariableAnnotations
    from typedb.analyze.variable import Variable


class Conjunction(ABC):
    """
    A representation of the constraints involved in the query, and types inferred for each variable.
    """

    @abstractmethod
    def constraints(self) -> Iterator["Constraint"]:
        """
        The Constraint(s) in the conjunction.

        :return: an iterator over constraints
        """
        pass

    @abstractmethod
    def annotated_variables(self) -> Iterator["Variable"]:
        """
        The variables that have annotations in this conjunction.

        :return: an iterator over annotated variables
        """
        pass

    @abstractmethod
    def variable_annotations(self, variable: "Variable") -> Optional["VariableAnnotations"]:
        """
        Gets the annotations for a specific variable in this conjunction.

        :param variable: the variable to get annotations for
        :return: the variable's annotations
        """
        pass
