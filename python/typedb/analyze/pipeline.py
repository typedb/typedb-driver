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

from typing import TYPE_CHECKING, Iterator, Optional

from typedb.common.iterator_wrapper import IteratorWrapper
from typedb.common.native_wrapper import NativeWrapper
from typedb.common.exception import TypeDBDriverException, ILLEGAL_STATE, NULL_NATIVE_OBJECT

from typedb.api.analyze.pipeline import Pipeline

from typedb.analyze.conjunction import _Conjunction
from typedb.analyze.pipeline_stage import _PipelineStage

from typedb.native_driver_wrapper import (
    Pipeline as NativePipeline,
    pipeline_stages,
    pipeline_stage_iterator_next,
    variable_get_name,
    pipeline_get_conjunction,
)

if TYPE_CHECKING:
    from typedb.api.analyze.conjunction import Conjunction
    from typedb.api.analyze.conjunction_id import ConjunctionID
    from typedb.api.analyze.pipeline_stage import PipelineStage
    from typedb.api.analyze.variable import Variable


class _Pipeline(Pipeline, NativeWrapper[NativePipeline]):

    def __init__(self, pipeline: NativePipeline):
        if not pipeline:
            raise TypeDBDriverException(NULL_NATIVE_OBJECT)
        super().__init__(pipeline)

    @property
    def _native_object_not_owned_exception(self) -> TypeDBDriverException:
        return TypeDBDriverException(ILLEGAL_STATE)

    def stages(self) -> Iterator["PipelineStage"]:
        native_iter = pipeline_stages(self.native_object)
        return map(_PipelineStage.of, IteratorWrapper(native_iter, pipeline_stage_iterator_next))

    def get_variable_name(self, variable: "Variable") -> Optional[str]:
        name = variable_get_name(self.native_object, variable.native_object)
        return None if name is None else name

    def conjunction(self, conjunction_id: "ConjunctionID") -> Optional["Conjunction"]:
        native_conj = pipeline_get_conjunction(self.native_object, conjunction_id.native_object)
        return None if native_conj is None else _Conjunction(native_conj)
