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
from typing import TYPE_CHECKING, Iterator, Optional

from typedb.api.concept.type.type import Type
from typedb.common.transitivity import Transitivity

if TYPE_CHECKING:
    from typedb.api.concept.type.annotation import Annotation
    from typedb.api.concept.thing.thing import Thing
    from typedb.api.concept.type.role_type import RoleType
    from typedb.api.concept.value.value import ValueType
    from typedb.api.concept.type.attribute_type import AttributeType
    from typedb.api.connection.transaction import TypeDBTransaction


class ThingType(Type, ABC):

    def is_thing_type(self) -> bool:
        return True

    @abstractmethod
    def get_supertype(self, transaction: TypeDBTransaction) -> Optional[ThingType]:
        pass

    @abstractmethod
    def get_supertypes(self, transaction: TypeDBTransaction) -> Iterator[ThingType]:
        pass

    @abstractmethod
    def get_subtypes(self, transaction: TypeDBTransaction, transitivity: Transitivity = Transitivity.TRANSITIVE
                     ) -> Iterator[ThingType]:
        pass

    @abstractmethod
    def get_instances(self, transaction: TypeDBTransaction, transitivity: Transitivity = Transitivity.TRANSITIVE
                      ) -> Iterator[Thing]:
        pass

    @abstractmethod
    def set_abstract(self, transaction: TypeDBTransaction) -> None:
        pass

    @abstractmethod
    def unset_abstract(self, transaction: TypeDBTransaction) -> None:
        pass

    @abstractmethod
    def set_plays(self, transaction: TypeDBTransaction, role_type: RoleType,
                  overriden_type: Optional[RoleType] = None) -> None:
        pass

    @abstractmethod
    def unset_plays(self, transaction: TypeDBTransaction, role_type: RoleType) -> None:
        pass

    @abstractmethod
    def set_owns(self, transaction: TypeDBTransaction, attribute_type: AttributeType,
                 overridden_type: Optional[AttributeType] = None,
                 annotations: Optional[set[Annotation]] = None) -> None:
        pass

    @abstractmethod
    def unset_owns(self, transaction: TypeDBTransaction, attribute_type: AttributeType) -> None:
        pass

    @abstractmethod
    @abstractmethod
    def get_plays(self, transaction: TypeDBTransaction, transitivity: Transitivity = Transitivity.TRANSITIVE
                  ) -> Iterator[RoleType]:
        pass

    @abstractmethod
    def get_plays_overridden(self, transaction: TypeDBTransaction, role_type: RoleType) -> Optional[RoleType]:
        pass

    def get_owns(self, transaction: TypeDBTransaction, value_type: Optional[ValueType] = None,
                 transitivity: Transitivity = Transitivity.TRANSITIVE, annotations: Optional[set[Annotation]] = None
                 ) -> Iterator[AttributeType]:
        pass

    @abstractmethod
    def get_owns_overridden(self, transaction: TypeDBTransaction, attribute_type: AttributeType
                            ) -> Optional[AttributeType]:
        pass

    @abstractmethod
    def get_syntax(self, transaction: TypeDBTransaction) -> str:
        pass
