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
from typing import TYPE_CHECKING

from typedb.api.concept.concept import Concept

if TYPE_CHECKING:
    from typedb.common.label import Label


class Type(Concept, ABC):

    @abstractmethod
    def get_label(self) -> Label:
        """
        Retrieves the unique label of the type.

        :return:

        Examples
        --------
        ::

            type_.get_label()
        """
        pass

    def is_type(self) -> bool:
        """
        Checks if the concept is a ``Type``.

        :return:

        Examples
        --------
        ::

            type_.is_type()
        """
        return True
