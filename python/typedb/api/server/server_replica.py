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

if TYPE_CHECKING:
    from typedb.api.server.replica_type import ReplicaType


class ServerReplica(ABC):
    """
    The metadata and state of an individual raft replica of a driver connection.
    """

    @property
    @abstractmethod
    def address(self) -> str:
        """
        Returns the address this replica is hosted at.

        :return:

        Examples
        --------
        ::

          server_replica.address
        """
        pass

    @property
    @abstractmethod
    def replica_type(self) -> ReplicaType:
        """
        Gets the type of this replica: whether it's a primary or a secondary replica.

        :return:

        Examples
        --------
        ::

          server_replica.query_type
        """
        pass

    @property
    @abstractmethod
    def term(self) -> int:
        """
        Returns the raft protocol ‘term’ of this replica.

        :return:

        Examples
        --------
        ::

          server_replica.term()
        """
        pass
