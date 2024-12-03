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

from collections.abc import Mapping as ABCMapping
from typing import Iterable, Mapping, Union

from typedb.api.answer.concept_document_iterator import *  # noqa # pylint: disable=unused-import
from typedb.api.answer.concept_row import *  # noqa # pylint: disable=unused-import
from typedb.api.answer.concept_row_iterator import *  # noqa # pylint: disable=unused-import
from typedb.api.answer.query_answer import *  # noqa # pylint: disable=unused-import
from typedb.api.answer.query_type import *  # noqa # pylint: disable=unused-import
from typedb.api.concept.concept import *  # noqa # pylint: disable=unused-import
from typedb.api.concept.instance.attribute import *  # noqa # pylint: disable=unused-import
from typedb.api.concept.instance.entity import *  # noqa # pylint: disable=unused-import
from typedb.api.concept.instance.instance import *  # noqa # pylint: disable=unused-import
from typedb.api.concept.instance.relation import *  # noqa # pylint: disable=unused-import
from typedb.api.concept.type.attribute_type import *  # noqa # pylint: disable=unused-import
from typedb.api.concept.type.entity_type import *  # noqa # pylint: disable=unused-import
from typedb.api.concept.type.relation_type import *  # noqa # pylint: disable=unused-import
from typedb.api.concept.type.role_type import *  # noqa # pylint: disable=unused-import
from typedb.api.concept.type.type import *  # noqa # pylint: disable=unused-import
from typedb.api.concept.value import *  # noqa # pylint: disable=unused-import
from typedb.api.connection.connection_settings import *
from typedb.api.connection.credentials import *
from typedb.api.connection.database import *  # noqa # pylint: disable=unused-import
from typedb.api.connection.driver import *
# from typedb.api.connection.options import *  # noqa # pylint: disable=unused-import
from typedb.api.connection.transaction import *  # noqa # pylint: disable=unused-import
from typedb.api.user.user import *  # noqa # pylint: disable=unused-import
from typedb.common.datetime import *  # noqa # pylint: disable=unused-import
from typedb.common.duration import *  # noqa # pylint: disable=unused-import
from typedb.common.exception import *  # noqa # pylint: disable=unused-import
from typedb.connection.driver import _Driver


# Repackaging these symbols allows them to be imported from "typedb.driver"


class TypeDB:
    DEFAULT_ADDRESS = "localhost:1729"

    @staticmethod
    def core_driver(address: str, credentials: Credentials, connection_settings: ConnectionSettings) -> Driver:
        """
        Creates a connection to TypeDB.

        :param address: Address of the TypeDB server.
        :param credentials: The credentials to connect with.
        :param connection_settings: The connection settings to connect with.
        :return:
        """
        return _Driver.core(address, credentials, connection_settings)

    @staticmethod
    def cloud_driver(addresses: Union[Mapping[str, str], Iterable[str], str], credentials: Credentials,
                     connection_settings: ConnectionSettings) -> Driver:
        """
        Creates a connection to TypeDB Cloud, authenticating with the provided credentials.

        :param addresses: TypeDB server addresses as a single string, a list of strings, or as an address translation mapping.
        :param credentials: The credentials to connect with.
        :param connection_settings: The connection settings to connect with.
        :return:
        """
        if isinstance(addresses, str):
            return _Driver.cloud([addresses], credentials, credentials, connection_settings)
        elif isinstance(addresses, ABCMapping):
            return _Driver.cloud(dict(addresses), credentials, credentials, connection_settings)
        else:
            return _Driver.cloud(list(addresses), credentials, credentials, connection_settings)
