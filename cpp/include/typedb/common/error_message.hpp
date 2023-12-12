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

#ifdef _MSC_VER
#ifdef _COMPILING_TYPEDB_DRIVER
#define DECLSPEC_DLL __declspec(dllexport)
#else
#define DECLSPEC_DLL __declspec(dllimport)
#endif
#else
#define DECLSPEC_DLL __attribute__((visibility("default")))
#endif

namespace TypeDB {

struct DECLSPEC_DLL ErrorMessage {
    const char* code;
    const char* formatString;
};

namespace DriverError {

DECLSPEC_DLL extern const ErrorMessage DRIVER_CLOSED;
DECLSPEC_DLL extern const ErrorMessage SESSION_CLOSED;
DECLSPEC_DLL extern const ErrorMessage TRANSACTION_CLOSED;
DECLSPEC_DLL extern const ErrorMessage TRANSACTION_CLOSED_WITH_ERRORS;
DECLSPEC_DLL extern const ErrorMessage DATABASE_DELETED;
DECLSPEC_DLL extern const ErrorMessage POSITIVE_VALUE_REQUIRED;
DECLSPEC_DLL extern const ErrorMessage MISSING_DB_NAME;
DECLSPEC_DLL extern const ErrorMessage INVALID_JSON_CAST;
DECLSPEC_DLL extern const ErrorMessage CALLBACK_EXCEPTION;

}  // namespace DriverError

namespace ConceptError {

DECLSPEC_DLL extern const ErrorMessage INVALID_CONCEPT_CASTING;
DECLSPEC_DLL extern const ErrorMessage MISSING_TRANSACTION;
DECLSPEC_DLL extern const ErrorMessage MISSING_IID;
DECLSPEC_DLL extern const ErrorMessage MISSING_LABEL;
DECLSPEC_DLL extern const ErrorMessage MISSING_VARIABLE;
DECLSPEC_DLL extern const ErrorMessage MISSING_VALUE;
DECLSPEC_DLL extern const ErrorMessage NONEXISTENT_EXPLAINABLE_CONCEPT;
DECLSPEC_DLL extern const ErrorMessage NONEXISTENT_EXPLAINABLE_OWNERSHIP;
DECLSPEC_DLL extern const ErrorMessage UNRECOGNISED_ANNOTATION;

}  // namespace ConceptError

namespace QueryError {

DECLSPEC_DLL extern const ErrorMessage VARIABLE_DOES_NOT_EXIST;
DECLSPEC_DLL extern const ErrorMessage MISSING_QUERY;

}  // namespace QueryError

namespace InternalError {

DECLSPEC_DLL extern const ErrorMessage UNEXPECTED_NATIVE_VALUE;
DECLSPEC_DLL extern const ErrorMessage ILLEGAL_STATE;
DECLSPEC_DLL extern const ErrorMessage ILLEGAL_CAST;
DECLSPEC_DLL extern const ErrorMessage NULL_NATIVE_VALUE;
DECLSPEC_DLL extern const ErrorMessage INVALID_NATIVE_HANDLE;
DECLSPEC_DLL extern const ErrorMessage ITERATOR_INVALIDATED;

}  // namespace InternalError

}  // namespace TypeDB
#undef DECLSPEC_DLL
