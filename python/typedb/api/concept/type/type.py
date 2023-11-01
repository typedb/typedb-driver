#
# Copyright (C) 2022 Vaticle
#
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
#

from __future__ import annotations

from abc import ABC, abstractmethod
from typing import Iterator, Mapping, Optional, TYPE_CHECKING

from typedb.api.concept.concept import Concept
from typedb.common.transitivity import Transitivity

if TYPE_CHECKING:
    from typedb.api.connection.transaction import TypeDBTransaction
    from typedb.common.label import Label
    from typedb.common.promise import Promise


class Type(Concept, ABC):
    @abstractmethod
    def get_label(self) -> Label:
        """
        Retrieves the unique label of the type.

        :return:

        Examples
        --------
        ::

            type_.get_label()
        """
        pass

    @abstractmethod
    def set_label(self, transaction: TypeDBTransaction, new_label: Label) -> Promise[None]:
        """
        Renames the label of the type. The new label must remain unique.

        :param transaction: The current transaction
        :param new_label: The new ``Label`` to be given to the type.
        :return:

        Examples
        --------
        ::

            type_.set_label(transaction, new_label).resolve()
        """
        pass

    @abstractmethod
    def is_root(self) -> bool:
        """
        Checks if the type is a root type.

        :return:

        Examples
        --------
        ::

            type_.is_root()
        """
        pass

    @abstractmethod
    def is_abstract(self) -> bool:
        """
        Checks if the type is prevented from having data instances (i.e., ``abstract``).

        :return:

        Examples
        --------
        ::

            type_.is_abstract()
        """
        pass

    def is_type(self) -> bool:
        """
        Checks if the concept is a ``Type``.

        :return:

        Examples
        --------
        ::

            type_.is_type()
        """
        return True

    @abstractmethod
    def get_supertype(self, transaction: TypeDBTransaction) -> Promise[Optional[Type]]:
        """
        Retrieves the most immediate supertype of the type.

        :param transaction: The current transaction
        :return:

        Examples
        --------
        ::

            type_.get_supertype(transaction).resolve()
        """
        pass

    @abstractmethod
    def get_supertypes(self, transaction: TypeDBTransaction) -> Iterator[Type]:
        """
        Retrieves all supertypes of the type.

        :param transaction: The current transaction
        :return:

        Examples
        --------
        ::

            type_.get_supertypes(transaction)
        """
        pass

    @abstractmethod
    def get_subtypes(
        self,
        transaction: TypeDBTransaction,
        transitivity: Transitivity = Transitivity.TRANSITIVE,
    ) -> Iterator[Type]:
        """
        Retrieves all direct and indirect (or direct only) subtypes of the type.

        :param transaction: The current transaction
        :param transitivity: ``Transitivity.TRANSITIVE`` for direct
            and indirect subtypes, ``Transitivity.EXPLICIT`` for direct
            subtypes only
        :return:

        Examples
        --------
        ::

            type_.get_subtypes(transaction)
            type_.get_subtypes(transaction, Transitivity.EXPLICIT)
        """
        pass

    @abstractmethod
    def delete(self, transaction: TypeDBTransaction) -> Promise[None]:
        """
        Deletes this type from the database.

        :param transaction: The current transaction
        :return:

        Examples
        --------
        ::

            type_.delete(transaction).resolve()
        """
        pass
