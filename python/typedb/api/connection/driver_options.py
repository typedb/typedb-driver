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
from typedb.native_driver_wrapper import driver_options_new, DriverOptions as NativeDriverOptions


class DriverOptions(NativeWrapper[NativeDriverOptions]):
    """
    User credentials and TLS encryption settings for connecting to TypeDB Server. Arguments:
    1) is_tls_enabled: Specify whether the connection to TypeDB Cloud must be done over TLS.
    2) tls_root_ca_path: Path to the CA certificate to use for authenticating server certificates.

    Examples:
    --------
    ::

        driver_options = DriverOptions(tls_enabled=True, tls_root_ca_path="path/to/ca-certificate.pem")
    """

    def __init__(self, is_tls_enabled: bool = False, tls_root_ca_path: Optional[str] = None):
        super().__init__(driver_options_new(is_tls_enabled, tls_root_ca_path))

    @property
    def _native_object_not_owned_exception(self) -> TypeDBDriverException:
        return TypeDBDriverException(ILLEGAL_STATE)
