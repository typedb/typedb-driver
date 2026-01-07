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
from typing import Iterable

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
from typedb.api.connection.credentials import *  # noqa # pylint: disable=unused-import
from typedb.api.connection.driver import *  # noqa # pylint: disable=unused-import
from typedb.api.connection.driver_options import *  # noqa # pylint: disable=unused-import
from typedb.api.connection.driver_tls_config import *  # noqa # pylint: disable=unused-import
from typedb.api.connection.query_options import *  # noqa # pylint: disable=unused-import
from typedb.api.connection.transaction import *  # noqa # pylint: disable=unused-import
from typedb.api.connection.transaction_options import *  # noqa # pylint: disable=unused-import
from typedb.api.database.database import *  # noqa # pylint: disable=unused-import
from typedb.api.database.database_manager import *  # noqa # pylint: disable=unused-import
from typedb.api.user.user import *  # noqa # pylint: disable=unused-import
from typedb.api.user.user_manager import *  # noqa # pylint: disable=unused-import
from typedb.common.datetime import *  # noqa # pylint: disable=unused-import
from typedb.common.duration import *  # noqa # pylint: disable=unused-import
from typedb.common.exception import *  # noqa # pylint: disable=unused-import
from typedb.connection.driver import _Driver


# Repackaging these symbols allows them to be imported from "typedb.driver"


class TypeDB:
    DEFAULT_ADDRESS = "127.0.0.1:1729"
    ADDRESSES = Union[Mapping[str, str], Iterable[str], str]

    @staticmethod
    def driver(addresses: ADDRESSES, credentials: Credentials, driver_options: DriverOptions) -> Driver:
        """
        Creates a connection to TypeDB.

        :param addresses: Address(-es) of the TypeDB server(-s).
        Can be a single string, multiple strings, or multiple pairs of strings.
        :param credentials: The credentials to connect with.
        :param driver_options: The driver connection options to connect with.
        """
        if isinstance(addresses, str):
            return _Driver(addresses, credentials, driver_options)
        elif isinstance(addresses, ABCMapping):
            return _Driver(dict(addresses), credentials, driver_options)
        else:
            return _Driver(list(addresses), credentials, driver_options)
