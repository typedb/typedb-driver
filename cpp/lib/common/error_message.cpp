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
#include "typedb/common/error_message.hpp"

#ifdef _MSC_VER
#define DECLSPEC_DLL __declspec(dllexport)
#else
#define DECLSPEC_DLL __attribute__((visibility("default")))
#endif

#define ERRMSG(CODE, PREFIX, ID, MSG) {"[" CODE #ID "]", PREFIX ": " MSG};

namespace TypeDB {

namespace DriverError {

#define ERR_DRIVER(ID, MSG) ERRMSG("DRI", "Driver Error", ID, MSG)
DECLSPEC_DLL extern const ErrorMessage DRIVER_CLOSED = ERR_DRIVER(1, "The driver has been closed and no further operation is allowed.");
DECLSPEC_DLL extern const ErrorMessage SESSION_CLOSED = ERR_DRIVER(2, "The session has been closed and no further operation is allowed.");
DECLSPEC_DLL extern const ErrorMessage TRANSACTION_CLOSED = ERR_DRIVER(3, "The transaction has been closed and no further operation is allowed.");
DECLSPEC_DLL extern const ErrorMessage TRANSACTION_CLOSED_WITH_ERRORS = ERR_DRIVER(4, "The transaction has been closed with error(s): \n%s.");
DECLSPEC_DLL extern const ErrorMessage DATABASE_DELETED = ERR_DRIVER(5, "The database has been deleted and no further operation is allowed.");
DECLSPEC_DLL extern const ErrorMessage POSITIVE_VALUE_REQUIRED = ERR_DRIVER(6, "Value cannot be less than 1, was: '%d'.");
DECLSPEC_DLL extern const ErrorMessage MISSING_DB_NAME = ERR_DRIVER(7, "Database name cannot be null.");
DECLSPEC_DLL extern const ErrorMessage INVALID_JSON_CAST = ERR_DRIVER(8, "An invalid cast from json type '%s' to '%s' was attempted.");
DECLSPEC_DLL extern const ErrorMessage CALLBACK_EXCEPTION = ERR_DRIVER(9, "An exception occurred when executing a callback: %s");
#undef ERR_DRIVER

}  // namespace DriverError

namespace ConceptError {

#define ERR_CONCEPT(ID, MSG) ERRMSG("CON", "Concept Error", ID, MSG)
DECLSPEC_DLL extern const ErrorMessage INVALID_CONCEPT_CASTING = ERR_CONCEPT(1, "Invalid concept conversion from '%s' to '%s'.");
DECLSPEC_DLL extern const ErrorMessage MISSING_TRANSACTION = ERR_CONCEPT(2, "Transaction cannot be null.");
DECLSPEC_DLL extern const ErrorMessage MISSING_IID = ERR_CONCEPT(3, "IID cannot be null or empty.");
DECLSPEC_DLL extern const ErrorMessage MISSING_LABEL = ERR_CONCEPT(4, "Label cannot be null or empty.");
DECLSPEC_DLL extern const ErrorMessage MISSING_VARIABLE = ERR_CONCEPT(5, "Variable name cannot be null or empty.");
DECLSPEC_DLL extern const ErrorMessage MISSING_VALUE = ERR_CONCEPT(6, "Value cannot be null.");
DECLSPEC_DLL extern const ErrorMessage NONEXISTENT_EXPLAINABLE_CONCEPT = ERR_CONCEPT(7, "The concept identified by '%s' is not explainable.");
DECLSPEC_DLL extern const ErrorMessage NONEXISTENT_EXPLAINABLE_OWNERSHIP = ERR_CONCEPT(8, "The ownership by owner '%s' of attribute '%s' is not explainable.");
DECLSPEC_DLL extern const ErrorMessage UNRECOGNISED_ANNOTATION = ERR_CONCEPT(9, "The annotation '%s' is not recognised.");
#undef ERR_CONCEPT

}  // namespace ConceptError

namespace QueryError {

#define ERR_QUERY(ID, MSG) ERRMSG("QRY", "Query Error", ID, MSG)
DECLSPEC_DLL extern const ErrorMessage VARIABLE_DOES_NOT_EXIST = ERR_QUERY(1, "The variable '%s' does not exist.");
DECLSPEC_DLL extern const ErrorMessage MISSING_QUERY = ERR_QUERY(2, "Query cannot be null or empty.");
#undef ERR_QUERY

}  // namespace QueryError

namespace InternalError {

#define ERR_INTERNAL(ID, MSG) ERRMSG("INT", "C++ Internal Error", ID, MSG)
DECLSPEC_DLL extern const ErrorMessage UNEXPECTED_NATIVE_VALUE = ERR_INTERNAL(1, "Unexpected native value encountered!");
DECLSPEC_DLL extern const ErrorMessage ILLEGAL_STATE = ERR_INTERNAL(2, "Illegal state has been reached! (%s : %d).");
DECLSPEC_DLL extern const ErrorMessage ILLEGAL_CAST = ERR_INTERNAL(3, "Illegal casting operation to '%s'.");
DECLSPEC_DLL extern const ErrorMessage NULL_NATIVE_VALUE = ERR_INTERNAL(4, "Unhandled null pointer to a native object encountered!");
DECLSPEC_DLL extern const ErrorMessage INVALID_NATIVE_HANDLE = ERR_INTERNAL(5, "The object does not have a valid native handle. It may have been:  uninitialised, moved or disposed.");
DECLSPEC_DLL extern const ErrorMessage ITERATOR_INVALIDATED = ERR_INTERNAL(6, "Dereferenced iterator which has reached end (or was invalidated by a move).");
#undef ERR_INTERNAL

}  // namespace InternalError

}  // namespace TypeDB
#undef ERRMSG
#undef DECLSPEC_DLL
