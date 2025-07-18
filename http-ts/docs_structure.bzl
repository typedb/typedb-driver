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

static_function_mapping = {
    "getVariableName": "query-structure",

    "isApiError": "response",
    "isApiErrorResponse": "response",
    "isOkResponse": "response",

    "isBasicParams": "connection",
    "isTranslatedParams": "connection",
    "remoteOrigin": "connection",
}

class_nesting_prefixes = [
    "DriverParams",
    "QueryConstraint",
    "QueryVertex",
]

dir_mapping = {
    "TypeDBHttpDriver.adoc": "connection",

    "ApiErrorResponse.adoc": "response",
    "Attribute.adoc": "concept",
    "ConceptDocumentsQueryResponse.adoc": "response",
    "ConceptRow.adoc": "response",
    "ConceptRowAnswer.adoc": "response",
    "ConceptRowsQueryResponse.adoc": "response",
    "Database.adoc": "connection",
    "DatabasesListResponse.adoc": "response",
    "DriverParamsBasic.adoc": "connection",
    "DriverParamsTranslated.adoc": "connection",
    "Entity.adoc": "concept",
    "EntityType.adoc": "concept",
    "OkQueryResponse.adoc": "response",
    "QueryConstraintComparison.adoc": "query-structure",
    "QueryConstraintExpression.adoc": "query-structure",
    "QueryConstraintFunction.adoc": "query-structure",
    "QueryConstraintHas.adoc": "query-structure",
    "QueryConstraintIid.adoc": "query-structure",
    "QueryConstraintIs.adoc": "query-structure",
    "QueryConstraintIsa.adoc": "query-structure",
    "QueryConstraintIsaExact.adoc": "query-structure",
    "QueryConstraintKind.adoc": "query-structure",
    "QueryConstraintLabel.adoc": "query-structure",
    "QueryConstraintLinks.adoc": "query-structure",
    "QueryConstraintOwns.adoc": "query-structure",
    "QueryConstraintPlays.adoc": "query-structure",
    "QueryConstraintRelates.adoc": "query-structure",
    "QueryConstraintSub.adoc": "query-structure",
    "QueryConstraintSubExact.adoc": "query-structure",
    "QueryConstraintValue.adoc": "query-structure",
    "QueryOptions.adoc": "query-structure",
    "QueryResponseBase.adoc": "query-structure",
    "QueryVertexLabel.adoc": "query-structure",
    "QueryVertexValue.adoc": "query-structure",
    "QueryVertexVariable.adoc": "query-structure",
    "Relation.adoc": "concept",
    "RelationType.adoc": "concept",
    "RoleType.adoc": "concept",
    "SignInResponse.adoc": "response",
    "TransactionOpenResponse.adoc": "response",
    "TransactionOptions.adoc": "connection",
    "TranslatedAddress.adoc": "connection",
    "User.adoc": "connection",
    "UsersListResponse.adoc": "response",
    "Value.adoc": "concept",
    "VersionResponse.adoc": "response",

    "Answer.adoc": "response",
    "AnswerType.adoc": "response",
    "ApiError.adoc": "response",
    "ApiOkResponse.adoc": "response",
    "ApiResponse.adoc": "response",
    "AttributeType.adoc": "concept",
    "Concept.adoc": "concept",
    "ConceptDocument.adoc": "concept",
    "Distribution.adoc": "response",
    "DriverParams.adoc": "connection",
    "EdgeKind.adoc": "concept",
    "InstantiableType.adoc": "concept",
    "QueryConstraintAny.adoc": "query-structure",
    "QueryConstraintSpan.adoc": "query-structure",
    "QueryResponse.adoc": "query-structure",
    "QueryStructure.adoc": "query-structure",
    "QueryType.adoc": "query-structure",
    "QueryVariableInfo.adoc": "query-structure",
    "QueryVertex.adoc": "query-structure",
    "QueryVertexKind.adoc": "query-structure",
    "ThingKind.adoc": "concept",
    "TransactionType.adoc": "connection",
    "Type.adoc": "concept",
    "TypeKind.adoc": "concept",
    "ValueKind.adoc": "concept",
    "ValueType.adoc": "concept",

    "query-structureStaticFunctions.adoc": "query-structure",
    "responseStaticFunctions.adoc": "response",
    "connectionStaticFunctions.adoc": "connection",
}
