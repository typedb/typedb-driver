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
    from typedb.api.analyze.variable import Variable
    from typedb.api.analyze.variable_annotations import VariableAnnotations


class Given(ABC):
    """
    A representation of the 'given' stage of a query.
    """

    @abstractmethod
    def variables(self) -> Iterator["Variable"]:
        """
        :return: the variables declared in the given stage.
        """
        pass

    @abstractmethod
    def variable_annotations(self, variable: "Variable") -> Optional["VariableAnnotations"]:
        """
        :return: the inferred type annotations for the specified variable.
        """
        pass
