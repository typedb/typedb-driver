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

from datetime import datetime
from typing import Optional, Iterator, TYPE_CHECKING, Union, Any

from typedb.native_client_wrapper import attribute_type_set_supertype, attribute_type_get_supertype, \
    attribute_type_get_supertypes, attribute_type_get_subtypes, attribute_type_get_subtypes_with_value_type, \
    attribute_type_get_instances, attribute_type_get_owners, attribute_type_put, attribute_type_get, \
    attribute_type_get_regex, attribute_type_set_regex, attribute_type_unset_regex, attribute_type_get_value_type, \
    concept_iterator_next

from typedb.api.concept.type.attribute_type import AttributeType
from typedb.api.concept.value.value import ValueType
from typedb.common.iterator_wrapper import IteratorWrapper
from typedb.common.transitivity import Transitivity
from typedb.concept.concept_factory import wrap_attribute, wrap_thing_type
from typedb.concept.type.thing_type import _ThingType
from typedb.concept.value.value import _Value

if TYPE_CHECKING:
    from typedb.api.concept.type.annotation import Annotation
    from typedb.api.concept.value.value import Value
    from typedb.concept.thing.attribute import _Attribute
    from typedb.connection.transaction import _Transaction


class _AttributeType(AttributeType, _ThingType):

    def get_value_type(self) -> ValueType:
        return ValueType.of(attribute_type_get_value_type(self.native_object))

    def __eq__(self, other):
        if other is self:
            return True
        # root "attribute" should always be equal to itself regardless of which value class it holds
        if not other or not isinstance(other, _AttributeType):
            return False
        return self.get_label() == other.get_label()

    def __hash__(self):
        return super(_AttributeType, self).__hash__()

    def set_supertype(self, transaction: _Transaction, attribute_type: _AttributeType) -> None:
        attribute_type_set_supertype(transaction.native_object, self.native_object,
                                     attribute_type.native_object)

    def get_supertype(self, transaction: _Transaction) -> Optional[_AttributeType]:
        if res := attribute_type_get_supertype(transaction.native_object, self.native_object):
            return _AttributeType(res)
        return None

    def get_supertypes(self, transaction: _Transaction) -> Iterator[_AttributeType]:
        return map(_AttributeType,
                   IteratorWrapper(attribute_type_get_supertypes(transaction.native_object, self.native_object),
                                   concept_iterator_next))

    def get_subtypes(self, transaction: _Transaction, transitivity: Transitivity = Transitivity.TRANSITIVE
                     ) -> Iterator[_AttributeType]:
        return map(_AttributeType,
                   IteratorWrapper(attribute_type_get_subtypes(transaction.native_object, self.native_object,
                                                               transitivity.value),
                                   concept_iterator_next))

    def get_subtypes_with_value_type(self, transaction: _Transaction, value_type: ValueType,
                                     transitivity: Transitivity = Transitivity.TRANSITIVE
                                     ) -> Iterator[_AttributeType]:
        return map(_AttributeType,
                   IteratorWrapper(attribute_type_get_subtypes_with_value_type(transaction.native_object,
                                                                               self.native_object,
                                                                               value_type.native_object,
                                                                               transitivity.value),
                                   concept_iterator_next))

    def get_instances(self, transaction: _Transaction, transitivity: Transitivity = Transitivity.TRANSITIVE
                      ) -> Iterator[_Attribute]:
        return map(wrap_attribute,
                   IteratorWrapper(attribute_type_get_instances(transaction.native_object, self.native_object,
                                                                transitivity.value),
                                   concept_iterator_next))

    def get_owners(self, transaction: _Transaction,
                   annotations: Optional[set[Annotation]] = None,
                   transitivity: Transitivity = Transitivity.TRANSITIVE) -> Iterator[Any]:
        annotations_array = [anno.native_object for anno in annotations] if annotations else []
        return map(wrap_thing_type,
                   IteratorWrapper(attribute_type_get_owners(transaction.native_object, self.native_object,
                                                             transitivity.value, annotations_array),
                                   concept_iterator_next))

    def put(self, transaction: _Transaction, value: Union[Value, bool, int, float, str, datetime]) -> _Attribute:
        return wrap_attribute(attribute_type_put(transaction.native_object, self.native_object,
                                                 _Value.of(value).native_object))

    def get(self, transaction: _Transaction, value: Union[Value, bool, int, float, str, datetime]
            ) -> Optional[_Attribute]:
        if res := attribute_type_get(transaction.native_object, self.native_object, _Value.of(value).native_object):
            return wrap_attribute(res)
        return None

    def get_regex(self, transaction: _Transaction) -> str:
        return attribute_type_get_regex(transaction.native_object, self.native_object)

    def set_regex(self, transaction: _Transaction, regex: str) -> None:
        attribute_type_set_regex(transaction.native_object, self.native_object, regex)

    def unset_regex(self, transaction: _Transaction) -> None:
        attribute_type_unset_regex(transaction.native_object, self.native_object)
