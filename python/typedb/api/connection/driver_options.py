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
from typedb.native_driver_wrapper import driver_options_get_is_tls_enabled, driver_options_get_tls_root_ca_path, \
    driver_options_has_tls_root_ca_path, driver_options_new, driver_options_set_is_tls_enabled, \
    driver_options_set_tls_root_ca_path, DriverOptions as NativeDriverOptions


class DriverOptions(NativeWrapper[NativeDriverOptions]):
    """
    TypeDB driver options. ``DriverOptions`` are used to specify the driver's connection behavior.

    Options could be specified either as constructor arguments or using
    properties assignment.

    Examples:
    --------
    ::

      driver_options = DriverOptions(tls_enabled=True)
      driver_options.tls_root_ca_path = "path/to/ca-certificate.pem"
    """

    def __init__(self, *,
                 is_tls_enabled: Optional[bool] = None,
                 tls_root_ca_path: Optional[str] = None,
                 ):
        super().__init__(driver_options_new())
        if is_tls_enabled is not None:
            self.is_tls_enabled = is_tls_enabled
        if tls_root_ca_path is not None:
            self.tls_root_ca_path = tls_root_ca_path

    @property
    def _native_object_not_owned_exception(self) -> TypeDBDriverException:
        return TypeDBDriverException(ILLEGAL_STATE)

    @property
    def is_tls_enabled(self) -> bool:
        """
        Returns the value set for the TLS flag in this ``DriverOptions`` object.
        Specifies whether the connection to TypeDB must be done over TLS.
        """
        return driver_options_get_is_tls_enabled(self.native_object)

    @is_tls_enabled.setter
    def is_tls_enabled(self, is_tls_enabled: bool):
        driver_options_set_is_tls_enabled(self.native_object, is_tls_enabled)

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
