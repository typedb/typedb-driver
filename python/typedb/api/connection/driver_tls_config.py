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
from typedb.common.validation import require_non_negative, require_non_null
from typedb.native_driver_wrapper import driver_tls_config_new_disabled, \
    driver_tls_config_new_enabled_with_native_root_ca, \
    driver_tls_config_new_enabled_with_root_ca_path, driver_tls_config_is_enabled, driver_tls_config_has_root_ca_path, \
    driver_tls_config_get_root_ca_path, DriverTlsConfig as NativeDriverTlsConfig


class DriverTlsConfig(NativeWrapper[NativeDriverTlsConfig]):
    """
    TLS configuration for the TypeDB driver.

    ``DriverTlsConfig`` represents a fully constructed and validated TLS configuration.
    If TLS is enabled, the underlying TLS config is built eagerly at construction time,
    ensuring that no connection attempt can observe a partially-configured TLS state.

    The driver defaults to using TLS with **native system trust roots**.
    This matches typical system and container deployments while still allowing
    explicit opt-out or custom PKI configuration.
    """

    def __init__(self, native_object: NativeDriverTlsConfig):
        super().__init__(native_object)

    @property
    def _native_object_not_owned_exception(self) -> TypeDBDriverException:
        return TypeDBDriverException(ILLEGAL_STATE)

    @staticmethod
    def disabled() -> "DriverTlsConfig":
        """
        Creates a TLS configuration with TLS disabled.
        WARNING: Disabling TLS causes credentials and data to be transmitted in plaintext.

        Examples
        --------
        ::

          tls_config = DriverTlsConfig.disabled()
        """
        return DriverTlsConfig(driver_tls_config_new_disabled())

    @staticmethod
    def enabled_with_native_root_ca() -> "DriverTlsConfig":
        """
        Creates a TLS configuration enabled with system native trust roots.

        Examples
        --------
        ::

          tls_config = DriverTlsConfig.enabled_with_native_root_ca()
        """
        return DriverTlsConfig(driver_tls_config_new_enabled_with_native_root_ca())

    @staticmethod
    def enabled_with_root_ca(tls_root_ca_path: str) -> "DriverTlsConfig":
        """
        Creates a TLS configuration enabled with a custom root CA certificate bundle (PEM).

        :param tls_root_ca_path: the path to PEM-encoded root CA certificate bundle

        Examples
        --------
        ::

          tls_config = DriverTlsConfig.enabled_with_root_ca("path/to/ca-certificate.pem")
        """
        require_non_null(tls_root_ca_path, "tls_root_ca_path")
        return DriverTlsConfig(driver_tls_config_new_enabled_with_root_ca_path(tls_root_ca_path))

    @property
    def is_enabled(self) -> bool:
        """
        Returns whether TLS is enabled.

        Examples
        --------
        ::

          tls_config.is_enabled
        """
        return driver_tls_config_is_enabled(self.native_object)

    @property
    def root_ca_path(self) -> Optional[str]:
        """
        Returns the configured custom root CA path, if present.
        If TLS is enabled with native roots (or disabled), this will be ``None``.

        Examples
        --------
        ::

          tls_config.root_ca_path
        """
        return driver_tls_config_get_root_ca_path(self.native_object) \
            if driver_tls_config_has_root_ca_path(self.native_object) else None
