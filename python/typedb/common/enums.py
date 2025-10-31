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

from enum import IntEnum

from typedb.native_driver_wrapper import (
    comparator_get_name,

    Isa as NativeIsa,
    Has as NativeHas,
    Links as NativeLinks,
    Sub as NativeSub,
    Owns as NativeOwns,
    Relates as NativeRelates,
    Plays as NativePlays,
    FunctionCall as NativeFunctionCall,
    Expression as NativeExpression,
    Is as NativeIs,
    Iid as NativeIid,
    Comparison as NativeComparison,
    KindOf as NativeKindOf,
    Label as NativeLabel,
    Value as NativeValue,
    Or as NativeOr,
    Not as NativeNot,
    Try as NativeTry,

    Exact as NativeExact,
    Subtypes as NativeSubtypes,

    Equal as NativeEqual,
    NotEqual as NativeNotEqual,
    LessThan as NativeLessThan,
    LessOrEqual as NativeLessOrEqual,
    Greater as NativeGreater,
    GreaterOrEqual as NativeGreaterOrEqual,
    Like as NativeLike,
    Contains as NativeContains,

    Entity as NativeEntity,
    Relation as NativeRelation,
    Attribute as NativeAttribute,
    Role as NativeRole,

    LeafDocument as NativeLeafDocument,
    ListDocument as NativeListDocument,
    ObjectDocument as NativeObjectDocument,

    StreamReturn as NativeStreamReturn,
    SingleReturn as NativeSingleReturn,
    CheckReturn as NativeCheckReturn,
    ReduceReturn as NativeReduceReturn,

    Match as NativeMatch,
    Insert as NativeInsert,
    Put as NativePut,
    Update as NativeUpdate,
    Delete as NativeDelete,
    Select as NativeSelect,
    Sort as NativeSort,
    Require as NativeRequire,
    Offset as NativeOffset,
    Limit as NativeLimit,
    Distinct as NativeDistinct,
    Reduce as NativeReduce,

    Ascending as NativeAscending,
    Descending as NativeDescending,

    InstanceAnnotations as NativeInstanceAnnotations,
    TypeAnnotations as NativeTypeAnnotations,
    ValueAnnotations as NativeValueAnnotations,
)

class ConstraintExactness(IntEnum):
    Exact = NativeExact
    Subtypes = NativeSubtypes


class Comparator(IntEnum):
    Equal = NativeEqual
    NotEqual = NativeNotEqual
    LessThan = NativeLessThan
    LessOrEqual = NativeLessOrEqual
    Greater = NativeGreater
    GreaterOrEqual = NativeGreaterOrEqual
    Like = NativeLike
    Contains = NativeContains

    def symbol(self):
        return comparator_get_name(self)

    def __str__(self):
        return self.symbol()


class ConstraintVariant(IntEnum):
    Isa = NativeIsa
    Has = NativeHas
    Links = NativeLinks
    Sub = NativeSub
    Owns = NativeOwns
    Relates = NativeRelates
    Plays = NativePlays
    FunctionCall = NativeFunctionCall
    Expression = NativeExpression
    Is = NativeIs
    Iid = NativeIid
    Comparison = NativeComparison
    KindOf = NativeKindOf
    Label = NativeLabel
    Value = NativeValue
    Or = NativeOr
    Not = NativeNot
    Try = NativeTry


class FetchVariant(IntEnum):
    LeafDocument = NativeLeafDocument
    ListDocument = NativeListDocument
    ObjectDocument = NativeObjectDocument,


class KindVariant(IntEnum):
    Entity = NativeEntity
    Relation = NativeRelation
    Attribute = NativeAttribute
    Role = NativeRole


class PipelineStageVariant(IntEnum):
    Match = NativeMatch
    Insert = NativeInsert
    Put = NativePut
    Update = NativeUpdate
    Delete = NativeDelete
    Select = NativeSelect
    Sort = NativeSort
    Require = NativeRequire
    Offset = NativeOffset
    Limit = NativeLimit
    Distinct = NativeDistinct
    Reduce = NativeReduce


class ReturnOperationVariant(IntEnum):
    StreamReturn = NativeStreamReturn
    SingleReturn = NativeSingleReturn
    CheckReturn = NativeCheckReturn
    ReduceReturn = NativeReduceReturn


class SortOrderVariant(IntEnum):
    Ascending = NativeAscending
    Descending = NativeDescending


class VariableAnnotationsVariant(IntEnum):
    InstanceAnnotations = NativeInstanceAnnotations
    TypeAnnotations = NativeTypeAnnotations
    ValueAnnotations = NativeValueAnnotations
