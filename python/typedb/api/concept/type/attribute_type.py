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
from datetime import datetime
from typing import Optional, TYPE_CHECKING, Iterator, Union

from typedb.api.concept.type.thing_type import ThingType
from typedb.api.concept.value.value import ValueType
from typedb.common.transitivity import Transitivity

if TYPE_CHECKING:
    from typedb.api.concept.value.value import Value
    from typedb.api.concept.type.annotation import Annotation
    from typedb.api.concept.thing.attribute import Attribute
    from typedb.api.connection.transaction import TypeDBTransaction


class AttributeType(ThingType, ABC):

    def get_value_type(self) -> ValueType:
        return ValueType.OBJECT

    def as_attribute_type(self) -> AttributeType:
        return self

    def is_attribute_type(self) -> bool:
        return True

    def is_boolean(self) -> bool:
        return self.get_value_type() == ValueType.BOOLEAN

    def is_long(self) -> bool:
        return self.get_value_type() == ValueType.LONG

    def is_double(self) -> bool:
        return self.get_value_type() == ValueType.DOUBLE

    def is_string(self) -> bool:
        return self.get_value_type() == ValueType.STRING

    def is_datetime(self) -> bool:
        return self.get_value_type() == ValueType.DATETIME

    @abstractmethod
    def put(self, transaction: TypeDBTransaction, value: Union[Value, bool, int, float, str, datetime]) -> Attribute:
        pass

    @abstractmethod
    def get(self, transaction: TypeDBTransaction, value: Union[Value, bool, int, float, str, datetime]
            ) -> Optional[Attribute]:
        pass

    @abstractmethod
    def get_regex(self, transaction: TypeDBTransaction) -> str:
        pass

    @abstractmethod
    def set_regex(self, transaction: TypeDBTransaction, regex: str) -> None:
        pass

    @abstractmethod
    def unset_regex(self, transaction: TypeDBTransaction) -> None:
        pass

    @abstractmethod
    def set_supertype(self, transaction: TypeDBTransaction, attribute_type: AttributeType) -> None:
        pass

    @abstractmethod
    def get_subtypes_with_value_type(self, transaction: TypeDBTransaction, value_type: ValueType,
                                     transitivity: Transitivity = Transitivity.TRANSITIVE
                                     ) -> Iterator[AttributeType]:
        pass

    @abstractmethod
    def get_instances(self, transaction: TypeDBTransaction, transitivity: Transitivity = Transitivity.TRANSITIVE
                      ) -> Iterator[Attribute]:
        pass

    @abstractmethod
    def get_owners(self, transaction: TypeDBTransaction, annotations: Optional[set[Annotation]] = None,
                   transitivity: Transitivity = Transitivity.TRANSITIVE) -> Iterator[ThingType]:
        pass
