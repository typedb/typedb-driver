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
from typing import TYPE_CHECKING, Optional

if TYPE_CHECKING:
    from typedb.api.concept.concept import Concept
    from typedb.api.concept.value.value import Value


class ValueGroup(ABC):
    """
    Contains an element of the group aggregate query result.
    """

    @abstractmethod
    def owner(self) -> Concept:
        """
        Retrieves the concept that is the group owner.

        :return:

        Examples
        --------
        ::

            value_group.owner()
        """
        pass

    @abstractmethod
    def value(self) -> Optional[Value]:
        """
        Retrieves the ``Value`` answer of the group.

        :return:

        Examples
        --------
        ::

            value_group.value()
        """
        pass
