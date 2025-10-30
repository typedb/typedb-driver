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
    from typedb.analyze.pipeline import Pipeline
    from typedb.analyze.function import Function
    from typedb.analyze.fetch import Fetch


class AnalyzedQuery(ABC):
    """
    An AnalyzedQuery contains the server's representation of the query and preamble functions;
    as well as the result of types inferred for each variable by type-inference.
    """

    @abstractmethod
    def pipeline(self) -> "Pipeline":
        """
        A representation of the query as a Pipeline.

        :return: the Pipeline representing the query.
        """
        pass

    @abstractmethod
    def preamble(self) -> Iterator["Function"]:
        """
        A representation of the Functions in the preamble of the query.

        :return: an iterator over the analyzed functions in the preamble.
        """
        pass

    @abstractmethod
    def fetch(self) -> Optional["Fetch"]:
        """
        A representation of the Fetch stage of the query, if it has one.

        :return: an Optional containing the fetch stage if present, otherwise None
        """
        pass
