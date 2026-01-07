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
from typedb.native_driver_wrapper import ServerVersion as NativeServerVersion


class ServerVersion(NativeWrapper[NativeServerVersion]):
    """
    A full TypeDB server's version specification.

    Examples:
    --------
    ::

        driver.server_version()
    """

    def __init__(self, native_object: NativeServerVersion):
        super().__init__(native_object)

    @property
    def _native_object_not_owned_exception(self) -> TypeDBDriverException:
        return TypeDBDriverException(ILLEGAL_STATE)

    @property
    def distribution(self) -> str:
        """
        Returns the server's distribution.

        Examples
        --------
        ::

          server_version.distribution
        """
        return self.native_object.distribution

    @property
    def version(self) -> str:
        """
        Returns the server's version.

        Examples
        --------
        ::

          server_version.version
        """
        return self.native_object.version

    def __str__(self):
        return f"{self.distribution} {self.version}"

    def __repr__(self):
        return f"ServerVersion('{str(self)}')"
