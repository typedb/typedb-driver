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

from typedb.native_client_wrapper import Annotation as NativeAnnotation, annotation_new_key, annotation_new_unique, \
    annotation_is_key, annotation_is_unique, annotation_to_string, annotation_equals


class Annotation:

    def __init__(self, annotation: NativeAnnotation):
        self._native_object = annotation

    @property
    def native_object(self) -> NativeAnnotation:
        return self._native_object

    @staticmethod
    def key() -> Annotation:
        return Annotation(annotation_new_key())

    @staticmethod
    def unique() -> Annotation:
        return Annotation(annotation_new_unique())

    def is_key(self) -> bool:
        return annotation_is_key(self.native_object)

    def is_unique(self) -> bool:
        return annotation_is_unique(self.native_object)

    def __str__(self):
        return annotation_to_string(self.native_object)

    def __repr__(self):
        return f"Annotation({self.native_object})"

    def __hash__(self):
        return hash((self.is_key(), self.is_unique()))

    def __eq__(self, other):
        return isinstance(other, Annotation) \
            and isinstance(self.native_object, NativeAnnotation) \
            and isinstance(other.native_object, NativeAnnotation) \
            and annotation_equals(self.native_object, other.native_object)
