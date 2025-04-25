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

from __future__ import annotations

from typing import Optional

from typedb.common.exception import TypeDBDriverException, ILLEGAL_STATE
from typedb.common.native_wrapper import NativeWrapper
from typedb.common.validation import require_non_negative
from typedb.native_driver_wrapper import query_options_new, \
    query_options_has_include_instance_types, query_options_get_include_instance_types, \
    query_options_set_include_instance_types, query_options_has_prefetch_size, \
    query_options_get_prefetch_size, query_options_set_prefetch_size, QueryOptions as NativeOptions


class QueryOptions(NativeWrapper[NativeOptions]):
    """
    TypeDB transaction options. ``QueryOptions`` object can be used to override the default server behaviour
    for executed queries.

    Options could be specified either as constructor arguments or using
    properties assignment.

    Examples
    --------

    ::

      query_options = QueryOptions(include_instance_types=True)
      query_options.prefetch_size = 10
    """

    def __init__(self, *,
                 include_instance_types: Optional[bool] = None,
                 prefetch_size: Optional[int] = None,
                 ):
        super().__init__(query_options_new())
        if include_instance_types is not None:
            self.include_instance_types = include_instance_types
        if prefetch_size is not None:
            self.prefetch_size = prefetch_size

    @property
    def _native_object_not_owned_exception(self) -> TypeDBDriverException:
        return TypeDBDriverException(ILLEGAL_STATE)

    @property
    def include_instance_types(self) -> Optional[bool]:
        """
        If set, specifies if types should be included in instance structs returned in ConceptRow answers.
        This option allows reducing the amount of unnecessary data transmitted.
        """
        return query_options_get_include_instance_types(self.native_object) \
            if query_options_has_include_instance_types(self.native_object) else None

    @include_instance_types.setter
    def include_instance_types(self, include_instance_types: bool):
        query_options_set_include_instance_types(self.native_object, include_instance_types)

    @property
    def prefetch_size(self) -> Optional[int]:
        """
        If set, specifies the number of extra query responses sent before the client side has to re-request more responses.
        Increasing this may increase performance for queries with a huge number of answers, as it can
        reduce the number of network round-trips at the cost of more resources on the server side.
        Minimal value: 1.
        """
        return query_options_get_prefetch_size(self.native_object) \
            if query_options_has_prefetch_size(self.native_object) else None

    @prefetch_size.setter
    def prefetch_size(self, prefetch_size: int):
        require_non_negative(prefetch_size, "prefetch_size")
        query_options_set_prefetch_size(self.native_object, prefetch_size)
