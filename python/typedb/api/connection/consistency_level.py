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

from abc import ABC
from typing import Optional, Union

from typedb.common.exception import TypeDBDriverException, UNEXPECTED_NATIVE_VALUE, ILLEGAL_STATE
from typedb.common.native_wrapper import NativeWrapper
from typedb.native_driver_wrapper import consistency_level_strong, consistency_level_eventual, \
    consistency_level_replica_dependent, ConsistencyLevel as NativeConsistencyLevel, \
    Strong as NativeStrong, Eventual as NativeEventual, ReplicaDependent as NativeReplicaDependent


class ConsistencyLevel(NativeWrapper[NativeConsistencyLevel], ABC):
    """
    Consistency levels of operations against a distributed server. All driver methods have default
    recommended values, however, most of the operations can be configured in order to potentially
    speed up the execution (introducing risks of stale data) or test a specific replica.
    This setting does not affect clusters with a single node.
    """

    @staticmethod
    def of(native_value: NativeConsistencyLevel) -> Union[None, "Strong", "Eventual", "ReplicaDependent"]:
        if native_value is None:
            return None

        if native_value.tag == NativeStrong:
            return Strong()
        elif native_value.tag == NativeEventual:
            return Eventual()
        elif native_value.tag == NativeReplicaDependent:
            return ReplicaDependent(native_value.address)
        else:
            raise TypeDBDriverException(UNEXPECTED_NATIVE_VALUE)

    @staticmethod
    def native_value(consistency_level: Optional["ConsistencyLevel"]) -> NativeConsistencyLevel:
        return None if consistency_level is None else consistency_level.native_object

    def is_strong(self) -> bool:
        return isinstance(self, Strong)

    def is_eventual(self) -> bool:
        return isinstance(self, Eventual)

    def is_replica_dependent(self) -> bool:
        return isinstance(self, ReplicaDependent)


class Strong(ConsistencyLevel):
    """
    Strong consistency level.
    Strongest consistency, always up-to-date due to the guarantee of the primary replica usage.
    May require more time for operation execution.
    """

    def __init__(self):
        super().__init__(consistency_level_strong())

    @property
    def _native_object_not_owned_exception(self) -> TypeDBDriverException:
        return TypeDBDriverException(ILLEGAL_STATE)

    def __str__(self):
        return "Strong"


class Eventual(ConsistencyLevel):
    """
    Eventual consistency level.
    Allow stale reads from any replica and execution orchestration through a non-primary replica.
    Does not guarantee latest writes, but is eventually faster compared to other consistency levels.
    Note that the target replica can redirect the request, if needed.
    """

    def __init__(self):
        super().__init__(consistency_level_eventual())

    @property
    def _native_object_not_owned_exception(self) -> TypeDBDriverException:
        return TypeDBDriverException(ILLEGAL_STATE)

    def __str__(self):
        return "Eventual"


class ReplicaDependent(ConsistencyLevel):
    """
    Replica dependent consistency level.
    The operation is executed against the provided replica address only. Its guarantees depend
    on the replica selected. Note that the target replica can redirect the request, if needed.
    """

    def __init__(self, address: str):
        super().__init__(consistency_level_replica_dependent(address))
        self._address = address

    @property
    def _native_object_not_owned_exception(self) -> TypeDBDriverException:
        return TypeDBDriverException(ILLEGAL_STATE)

    @property
    def address(self) -> str:
        """
        Retrieves the address of the replica this consistency level depends on.
        """
        return self._address

    def __str__(self):
        return f"ReplicaDependent({self._address})"


ConsistencyLevel.Strong = Strong
ConsistencyLevel.Eventual = Eventual
ConsistencyLevel.ReplicaDependent = ReplicaDependent
