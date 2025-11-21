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
    from typedb.analyze.conjunction import Conjunction
    from typedb.api.analyze.conjunction_id import ConjunctionID
    from typedb.analyze.pipeline_stage import PipelineStage
    from typedb.api.analyze.conjunction_id import Variable


class Pipeline(ABC):
    """
    A representation of a query pipeline.
    """

    @abstractmethod
    def stages(self) -> Iterator["PipelineStage"]:
        """A stream/iterator of the stages making up the pipeline."""
        pass

    @abstractmethod
    def get_variable_name(self, variable: "Variable") -> Optional[str]:
        """
        Gets the name of the specified variable, if it has one.

        :param variable: the variable from this pipeline
        :return: the name, if any
        """
        pass

    @abstractmethod
    def conjunction(self, conjunction_id: "ConjunctionID") -> Optional["Conjunction"]:
        """
        Gets the Conjunction corresponding to the ConjunctionID.

        :param conjunction_id: the ConjunctionID of the conjunction to retrieve
        :return: the corresponding Conjunction, if present
        """
        pass
