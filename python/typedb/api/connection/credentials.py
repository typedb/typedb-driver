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

from typedb.common.exception import TypeDBDriverException, ILLEGAL_STATE
from typedb.common.native_wrapper import NativeWrapper
from typedb.native_driver_wrapper import credentials_new, Credentials as NativeCredentials


class Credentials(NativeWrapper[NativeCredentials]):
    """
    User credentials and TLS encryption settings for connecting to TypeDB Server.

    Examples:
    --------
    ::

        credentials = Credentials(username, password)
    """

    def __init__(self, username: str, password: str):
        super().__init__(credentials_new(username, password))

    @property
    def _native_object_not_owned_exception(self) -> TypeDBDriverException:
        return TypeDBDriverException(ILLEGAL_STATE)
