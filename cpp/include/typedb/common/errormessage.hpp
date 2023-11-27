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

extern ErrorMessage DRIVER_CLOSED;
extern ErrorMessage SESSION_CLOSED;
extern ErrorMessage TRANSACTION_CLOSED;
extern ErrorMessage TRANSACTION_CLOSED_WITH_ERRORS;
extern ErrorMessage DATABASE_DELETED;
extern ErrorMessage POSITIVE_VALUE_REQUIRED;
extern ErrorMessage MISSING_DB_NAME;

}  // namespace DriverError

namespace ConceptError {

extern ErrorMessage INVALID_CONCEPT_CASTING;
extern ErrorMessage MISSING_TRANSACTION;
extern ErrorMessage MISSING_IID;
extern ErrorMessage MISSING_LABEL;
extern ErrorMessage MISSING_VARIABLE;
extern ErrorMessage MISSING_VALUE;
extern ErrorMessage NONEXISTENT_EXPLAINABLE_CONCEPT;
extern ErrorMessage NONEXISTENT_EXPLAINABLE_OWNERSHIP;
extern ErrorMessage UNRECOGNISED_ANNOTATION;

}  // namespace ConceptError

namespace QueryError {

extern ErrorMessage VARIABLE_DOES_NOT_EXIST;
extern ErrorMessage MISSING_QUERY;

}  // namespace QueryError

namespace InternalError {

extern ErrorMessage UNEXPECTED_NATIVE_VALUE;
extern ErrorMessage ILLEGAL_STATE;
extern ErrorMessage ILLEGAL_CAST;
extern ErrorMessage NULL_NATIVE_VALUE;
extern ErrorMessage INVALID_NATIVE_HANDLE;
extern ErrorMessage ITERATOR_INVALIDATED;

}  // namespace InternalError

}  // namespace TypeDB
