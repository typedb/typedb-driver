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

from typedb.common.exception import TypeDBDriverException, CLOUD_CREDENTIAL_INCONSISTENT, ILLEGAL_STATE
from typedb.common.native_wrapper import NativeWrapper
from typedb.native_driver_wrapper import credential_new, Credential as NativeCredential


class Credential(NativeWrapper[NativeCredential]):
    """
    User credentials and TLS encryption settings for connecting to TypeDB Cloud. Arguments:
    1) username: The name of the user to connect as. 2) password: The password for the user.
    3) tls_root_ca_path: Path to the CA certificate to use for authenticating server certificates.
    4) tls_enabled: Specify whether the connection to TypeDB Cloud must be done over TLS.

    Examples:
    --------
    ::

        # Creates a credential using the specified username and password.
        credential = Credential(username, password)

        # Creates a credential as above, but with TLS and the specified CA to authenticate server certificates.
        credential = Credential(username, password, tls_enabled=True, tls_root_ca_path="path/to/ca-certificate.pem")
    """

    def __init__(self, username: str, password: str, *, tls_root_ca_path: Optional[str] = None,
                 tls_enabled: bool = False):
        if tls_root_ca_path is not None and not tls_enabled:
            raise TypeDBDriverException(CLOUD_CREDENTIAL_INCONSISTENT)
        super().__init__(credential_new(username, password, tls_root_ca_path, tls_enabled))

    @property
    def _native_object_not_owned_exception(self) -> TypeDBDriverException:
        return TypeDBDriverException(ILLEGAL_STATE)
