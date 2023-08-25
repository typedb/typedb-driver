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
    from typedb.common.label import Label
    from typedb.api.connection.transaction import TypeDBTransaction


class Type(Concept, ABC):

    @abstractmethod
    def get_label(self) -> Label:
        pass

    @abstractmethod
    def set_label(self, transaction: TypeDBTransaction, new_label: Label) -> None:
        pass

    @abstractmethod
    def is_root(self) -> bool:
        pass
    
    @abstractmethod
    def is_abstract(self) -> bool:
        pass

    def is_type(self) -> bool:
        return True

    def to_json(self) -> Mapping[str, str]:
        return {"label": self.get_label().scoped_name()}

    @abstractmethod
    def get_supertype(self, transaction: TypeDBTransaction) -> Optional[Type]:
        pass

    @abstractmethod
    def get_supertypes(self, transaction: TypeDBTransaction) -> Iterator[Type]:
        pass

    @abstractmethod
    def get_subtypes(self, transaction: TypeDBTransaction, transitivity: Transitivity = Transitivity.TRANSITIVE
                     ) -> Iterator[Type]:
        pass

    @abstractmethod
    def delete(self, transaction: TypeDBTransaction) -> None:
        pass
