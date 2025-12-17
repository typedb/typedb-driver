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

from typing import Optional

from typedb.common.exception import TypeDBDriverException, ILLEGAL_STATE
from typedb.common.native_wrapper import NativeWrapper
from typedb.common.validation import require_non_negative
from typedb.native_driver_wrapper import driver_options_get_tls_enabled, driver_options_get_tls_root_ca_path, \
    driver_options_has_tls_root_ca_path, driver_options_new, driver_options_set_tls_enabled, \
    driver_options_set_tls_root_ca_path, driver_options_get_use_replication, driver_options_set_use_replication, \
    driver_options_get_primary_failover_retries, driver_options_set_primary_failover_retries, \
    driver_options_get_replica_discovery_attempts, driver_options_set_replica_discovery_attempts, \
    driver_options_has_replica_discovery_attempts, DriverOptions as NativeDriverOptions


class DriverOptions(NativeWrapper[NativeDriverOptions]):
    """
    TypeDB driver options. ``DriverOptions`` are used to specify the driver's connection behavior.

    Options could be specified either as constructor arguments or using
    properties assignment.

    Examples:
    --------
    ::

      driver_options = DriverOptions(is_tls_enabled=True)
      driver_options.tls_root_ca_path = "path/to/ca-certificate.pem"
    """

    def __init__(self, *,
                 is_tls_enabled: Optional[bool] = None,
                 tls_root_ca_path: Optional[str] = None,
                 use_replication: Optional[bool] = None,
                 primary_failover_retries: Optional[int] = None,
                 replica_discovery_attempts: Optional[int] = None,
                 ):
        super().__init__(driver_options_new())
        if is_tls_enabled is not None:
            self.is_tls_enabled = is_tls_enabled
        if tls_root_ca_path is not None:
            self.tls_root_ca_path = tls_root_ca_path
        if use_replication is not None:
            self.use_replication = use_replication
        if primary_failover_retries is not None:
            self.primary_failover_retries = primary_failover_retries
        if replica_discovery_attempts is not None:
            self.replica_discovery_attempts = replica_discovery_attempts

    @property
    def _native_object_not_owned_exception(self) -> TypeDBDriverException:
        return TypeDBDriverException(ILLEGAL_STATE)

    @property
    def is_tls_enabled(self) -> bool:
        """
        Returns the value set for the TLS flag in this ``DriverOptions`` object.
        Specifies whether the connection to TypeDB must be done over TLS.
        WARNING: Setting this to false will make the driver sending passwords as plaintext. Defaults to True.
        """
        return driver_options_get_tls_enabled(self.native_object)

    @is_tls_enabled.setter
    def is_tls_enabled(self, is_tls_enabled: bool):
        driver_options_set_tls_enabled(self.native_object, is_tls_enabled)

    @property
    def tls_root_ca_path(self) -> Optional[str]:
        """
        Returns the TLS root CA set in this ``DriverOptions`` object.
        Specifies the root CA used in the TLS config for server certificates authentication.
        Uses system roots if None is set.
        """
        return driver_options_get_tls_root_ca_path(self.native_object) \
            if driver_options_has_tls_root_ca_path(self.native_object) else None

    @tls_root_ca_path.setter
    def tls_root_ca_path(self, tls_root_ca_path: Optional[str]):
        driver_options_set_tls_root_ca_path(self.native_object, tls_root_ca_path)

    @property
    def use_replication(self) -> bool:
        """
        Returns the value set for the replication usage flag in this ``DriverOptions`` object.
        Specifies whether the connection to TypeDB can use cluster replicas provided by the server
        or it should be limited to a single configured address. Defaults to True.
        """
        return driver_options_get_use_replication(self.native_object)

    @use_replication.setter
    def use_replication(self, use_replication: bool):
        driver_options_set_use_replication(self.native_object, use_replication)

    @property
    def primary_failover_retries(self) -> int:
        """
        Returns the value set for the primary failover retries limit in this ``DriverOptions`` object.
        Limits the number of attempts to redirect a strongly consistent request to another
        primary replica in case of a failure due to the change of replica roles. Defaults to 1.
        """
        return driver_options_get_primary_failover_retries(self.native_object)

    @primary_failover_retries.setter
    def primary_failover_retries(self, primary_failover_retries: int):
        require_non_negative(primary_failover_retries, "primary_failover_retries")
        driver_options_set_primary_failover_retries(self.native_object, primary_failover_retries)

    @property
    def replica_discovery_attempts(self) -> Optional[int]:
        """
        Returns the value set for the replica discovery attempts limit in this ``DriverOptions`` object.
        Limits the number of driver attempts to discover a single working replica to perform an
        operation in case of a replica unavailability. Every replica is tested once, which means
        that at most:
        - {limit} operations are performed if the limit <= the number of replicas.
        - {number of replicas} operations are performed if the limit > the number of replicas.
        - {number of replicas} operations are performed if the limit is None.
        Affects every eventually consistent operation, including redirect failover, when the new
        primary replica is unknown. If not set, the maximum (practically unlimited) value is used.
        """
        return driver_options_get_replica_discovery_attempts(self.native_object) \
            if driver_options_has_replica_discovery_attempts(self.native_object) else None

    @replica_discovery_attempts.setter
    def replica_discovery_attempts(self, replica_discovery_attempts: int):
        require_non_negative(replica_discovery_attempts, "replica_discovery_attempts")
        driver_options_set_replica_discovery_attempts(self.native_object, replica_discovery_attempts)
