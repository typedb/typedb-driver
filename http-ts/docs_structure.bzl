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
    "getVariableName": "analyze",

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

    "AnalyzeOptions.adoc": "analyze",
    "AnalyzedFetch.adoc": "analyze",
    "AnalyzedFunction.adoc": "analyze",
    "AnalyzedPipeline.adoc": "analyze",
    "AnalyzedConjunction.adoc": "analyze",
    "FunctionSingleReturnSelector.adoc": "analyze",
    "FunctionReturnStructure.adoc": "analyze",
    "FetchAnnotationFieldEntry.adoc": "analyze",
    "PipelineStage.adoc": "analyze",
    "Reducer.adoc": "analyze",
    "ConjunctionAnnotations.adoc": "analyze",
    "VariableAnnotations.adoc": "analyze",

    "QueryStructureForStudio.adoc": "analyze",
    "QueryConstraintAnyForStudio.adoc": "analyze",
    "QueryConstraintExpressionForStudio.adoc": "analyze",
    "QueryConstraintLinksForStudio.adoc": "analyze",
    "ConceptRowsQueryResponseForStudio.adoc": "analyze",


    "AnalyzeResponse.adoc": "response",
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
    "QueryConstraintComparison.adoc": "analyze",
    "QueryConstraintExpression.adoc": "analyze",
    "QueryConstraintFunction.adoc": "analyze",
    "QueryConstraintHas.adoc": "analyze",
    "QueryConstraintIid.adoc": "analyze",
    "QueryConstraintIs.adoc": "analyze",
    "QueryConstraintIsa.adoc": "analyze",
    "QueryConstraintIsaExact.adoc": "analyze",
    "QueryConstraintKind.adoc": "analyze",
    "QueryConstraintLabel.adoc": "analyze",
    "QueryConstraintLinks.adoc": "analyze",
    "QueryConstraintNot.adoc": "analyze",
    "QueryConstraintOr.adoc": "analyze",
    "QueryConstraintOwns.adoc": "analyze",
    "QueryConstraintPlays.adoc": "analyze",
    "QueryConstraintRelates.adoc": "analyze",
    "QueryConstraintSub.adoc": "analyze",
    "QueryConstraintSubExact.adoc": "analyze",
    "QueryConstraintTry.adoc": "analyze",
    "QueryConstraintValue.adoc": "analyze",
    "QueryOptions.adoc": "response",
    "QueryResponseBase.adoc": "response",
    "QueryVertexLabel.adoc": "analyze",
    "QueryVertexNamedRole.adoc": "analyze",
    "QueryVertexValue.adoc": "analyze",
    "QueryVertexVariable.adoc": "analyze",
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
    "QueryConstraintAny.adoc": "analyze",
    "QueryConstraintSpan.adoc": "analyze",
    "QueryResponse.adoc": "response",
    "QueryType.adoc": "response",
    "QueryVariableInfo.adoc": "analyze",
    "QueryVertex.adoc": "analyze",
    "ThingKind.adoc": "concept",
    "TransactionType.adoc": "connection",
    "Type.adoc": "concept",
    "TypeKind.adoc": "concept",
    "ValueKind.adoc": "concept",
    "ValueType.adoc": "concept",

    "analyzeStaticFunctions.adoc": "analyze",
    "analyzed-conjunctionStaticFunctions.adoc": "analyze",
    "responseStaticFunctions.adoc": "response",
    "connectionStaticFunctions.adoc": "connection",
}
