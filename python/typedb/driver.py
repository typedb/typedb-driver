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

from typedb.api.answer.concept_row import *  # noqa # pylint: disable=unused-import
from typedb.api.answer.value_group import *  # noqa # pylint: disable=unused-import
from typedb.api.concept.concept import *  # noqa # pylint: disable=unused-import
from typedb.api.concept.thing.attribute import *  # noqa # pylint: disable=unused-import
from typedb.api.concept.thing.entity import *  # noqa # pylint: disable=unused-import
from typedb.api.concept.thing.relation import *  # noqa # pylint: disable=unused-import
from typedb.api.concept.thing.thing import *  # noqa # pylint: disable=unused-import
from typedb.api.concept.type.attribute_type import *  # noqa # pylint: disable=unused-import
from typedb.api.concept.type.entity_type import *  # noqa # pylint: disable=unused-import
from typedb.api.concept.type.relation_type import *  # noqa # pylint: disable=unused-import
from typedb.api.concept.type.role_type import *  # noqa # pylint: disable=unused-import
from typedb.api.concept.type.thing_type import *  # noqa # pylint: disable=unused-import
from typedb.api.concept.type.type import *  # noqa # pylint: disable=unused-import
from typedb.api.concept.value import *  # noqa # pylint: disable=unused-import
# from typedb.api.connection.credential import *
from typedb.api.connection.database import *  # noqa # pylint: disable=unused-import
from typedb.api.connection.driver import *
# from typedb.api.connection.options import *  # noqa # pylint: disable=unused-import
from typedb.api.connection.transaction import *  # noqa # pylint: disable=unused-import
# from typedb.api.user.user import *  # noqa # pylint: disable=unused-import
from typedb.common.exception import *  # noqa # pylint: disable=unused-import
from typedb.common.label import *  # noqa # pylint: disable=unused-import
from typedb.connection.driver import _Driver


# Repackaging these symbols allows them to be imported from "typedb.driver"


class TypeDB:
    DEFAULT_ADDRESS = "localhost:1729"

    @staticmethod
    def core_driver(address: str) -> Driver:
        """
        Creates a connection to TypeDB.

        :param address: Address of the TypeDB server.
        :return:
        """
        return _Driver([address])

    # @staticmethod
    # def cloud_driver(addresses: Union[Mapping[str, str], Iterable[str], str], credential: Credential) -> Driver:
    #     """
    #     Creates a connection to TypeDB Cloud, authenticating with the provided credentials.
    #
    #     :param addresses:
    #     :param credential:
    #     :return:
    #     """
    #     if isinstance(addresses, str):
    #         return _Driver([addresses], credential)
    #     elif isinstance(addresses, ABCMapping):
    #         return _Driver(dict(addresses), credential)
    #     else:
    #         return _Driver(list(addresses), credential)
