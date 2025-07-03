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

from abc import ABC, abstractmethod
from typing import Optional

from typedb.native_driver_wrapper import consistency_level_strong, consistency_level_eventual, \
    consistency_level_replica_dependant, ConsistencyLevel as NativeConsistencyLevel

class ConsistencyLevel(ABC):
    """
    Consistency levels of operations against a distributed server. All driver methods have default
    recommended values, however, readonly operations can be configured in order to potentially
    speed up the execution (introducing risks of stale data) or test a specific replica.
    This setting does not affect clusters with a single node.
    """

    @abstractmethod
    def native_object(self):
        pass

    @staticmethod
    def native_value(consistency_level: Optional["ConsistencyLevel"]) -> NativeConsistencyLevel:
        return None if consistency_level is None else consistency_level.native_object()

    def is_strong(self) -> bool:
        return isinstance(self, ConsistencyLevel.Strong)

    def is_eventual(self) -> bool:
        return isinstance(self, ConsistencyLevel.Eventual)

    def is_replica_dependant(self) -> bool:
        return isinstance(self, ConsistencyLevel.ReplicaDependant)

    class Strong(ABC):
        """
        The strongest consistency level. Always up-to-date due to the guarantee of the primary replica usage.
        May require more time for operation execution.
        """

        def native_object(self):
            return consistency_level_strong()

        def __str__(self):
            return "Strong"

    class Eventual(ABC):
        """
        Eventual consistency level. Allow stale reads from any replica. May not reflect latest writes.
        The execution may be eventually faster compared to other consistency levels.
        """

        def native_object(self):
            return consistency_level_eventual()

        def __str__(self):
            return "Eventual"

    class ReplicaDependant(ABC):
        """
        Replica dependant consistency level. The operation is executed against the provided replica address only.
        Its guarantees depend on the replica selected.
        """

        def __init__(self, address: str):
            self._address = address

        def native_object(self):
            return consistency_level_replica_dependant(self._address)

        """
        Retrieves the address of the replica this consistency level depends on.
        """
        @property
        def address(self) -> str:
            return self._address

        def __str__(self):
            return f"ReplicaDependant({self._address})"
