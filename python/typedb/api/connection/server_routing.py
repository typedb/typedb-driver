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
from typedb.native_driver_wrapper import server_routing_auto, \
    server_routing_direct, ServerRouting as NativeServerRouting, \
    Auto as NativeAuto, Direct as NativeDirect


class ServerRouting(NativeWrapper[NativeServerRouting], ABC):
    """
    Server routing directive for operations against a distributed server. All driver methods have
    default recommended values, however, some operations can be configured in order to
    target a specific server in the cluster. This setting does not affect clusters with a single node.
    """

    @staticmethod
    def of(native_value: NativeServerRouting) -> Union[None, "Auto", "Direct"]:
        if native_value is None:
            return None

        if native_value.tag == NativeAuto:
            return Auto()
        elif native_value.tag == NativeDirect:
            return Direct(native_value.address)
        else:
            raise TypeDBDriverException(UNEXPECTED_NATIVE_VALUE)

    @staticmethod
    def native_value(server_routing: Optional["ServerRouting"]) -> NativeServerRouting:
        return None if server_routing is None else server_routing.native_object

    def is_auto(self) -> bool:
        return isinstance(self, Auto)

    def is_direct(self) -> bool:
        return isinstance(self, Direct)


class Auto(ServerRouting):
    """
    Automatic server routing. Driver automatically selects the server (primary in clusters).
    """

    def __init__(self):
        super().__init__(server_routing_auto())

    @property
    def _native_object_not_owned_exception(self) -> TypeDBDriverException:
        return TypeDBDriverException(ILLEGAL_STATE)

    def __str__(self):
        return "Auto"


class Direct(ServerRouting):
    """
    Route to a specific known server at the given address. Mostly used for debugging purposes.
    """

    def __init__(self, address: str):
        super().__init__(server_routing_direct(address))
        self._address = address

    @property
    def _native_object_not_owned_exception(self) -> TypeDBDriverException:
        return TypeDBDriverException(ILLEGAL_STATE)

    @property
    def address(self) -> str:
        """
        Retrieves the address of the server this routing targets.
        """
        return self._address

    def __str__(self):
        return f"Direct({self._address})"


ServerRouting.Auto = Auto
ServerRouting.Direct = Direct
