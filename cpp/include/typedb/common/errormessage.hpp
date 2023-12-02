/*
 * Copyright (C) 2022 Vaticle
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

#pragma once

namespace TypeDB {

struct ErrorMessage {
    const char* code;
    const char* formatString;
};

namespace DriverError {

extern const ErrorMessage DRIVER_CLOSED;
extern const ErrorMessage SESSION_CLOSED;
extern const ErrorMessage TRANSACTION_CLOSED;
extern const ErrorMessage TRANSACTION_CLOSED_WITH_ERRORS;
extern const ErrorMessage DATABASE_DELETED;
extern const ErrorMessage POSITIVE_VALUE_REQUIRED;
extern const ErrorMessage MISSING_DB_NAME;
extern const ErrorMessage INVALID_JSON_CAST;

}  // namespace DriverError

namespace ConceptError {

extern const ErrorMessage INVALID_CONCEPT_CASTING;
extern const ErrorMessage MISSING_TRANSACTION;
extern const ErrorMessage MISSING_IID;
extern const ErrorMessage MISSING_LABEL;
extern const ErrorMessage MISSING_VARIABLE;
extern const ErrorMessage MISSING_VALUE;
extern const ErrorMessage NONEXISTENT_EXPLAINABLE_CONCEPT;
extern const ErrorMessage NONEXISTENT_EXPLAINABLE_OWNERSHIP;
extern const ErrorMessage UNRECOGNISED_ANNOTATION;

}  // namespace ConceptError

namespace QueryError {

extern const ErrorMessage VARIABLE_DOES_NOT_EXIST;
extern const ErrorMessage MISSING_QUERY;

}  // namespace QueryError

namespace InternalError {

extern const ErrorMessage UNEXPECTED_NATIVE_VALUE;
extern const ErrorMessage ILLEGAL_STATE;
extern const ErrorMessage ILLEGAL_CAST;
extern const ErrorMessage NULL_NATIVE_VALUE;
extern const ErrorMessage INVALID_NATIVE_HANDLE;
extern const ErrorMessage ITERATOR_INVALIDATED;

}  // namespace InternalError

}  // namespace TypeDB
