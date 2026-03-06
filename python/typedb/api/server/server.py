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
    from typedb.api.server.replication_role import ReplicationRole


class Server(ABC):
    """
    The metadata and state of an individual raft server of a driver connection.
    """

    @property
    @abstractmethod
    def id(self) -> int:
        """
        Returns the id of this server.

        Examples
        --------
        ::

          server.id
        """
        pass

    @property
    @abstractmethod
    def address(self) -> str:
        """
        Returns the address this server is hosted at.

        Examples
        --------
        ::

          server.address
        """
        pass

    @property
    @abstractmethod
    def role(self) -> Optional[ReplicationRole]:
        """
        Returns whether this is the primary server of the raft cluster or any of the supporting types.

        Examples
        --------
        ::

          server.role
        """
        pass

    @abstractmethod
    def is_primary(self) -> bool:
        """
        Checks whether this is the primary server of the raft cluster.

        Examples
        --------
        ::

          server.is_primary()
        """
        pass

    @property
    @abstractmethod
    def term(self) -> int:
        """
        Returns the raft protocol ‘term’ of this server.

        Examples
        --------
        ::

          server.term
        """
        pass
