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

#define ERRMSG(CODE, PREFIX, ID, MSG) {"[" CODE #ID "]", PREFIX ": " MSG};

namespace TypeDB {

namespace DriverError {

#define ERR_DRIVER(ID, MSG) ERRMSG("CCL", "Driver Error", ID, MSG)
const ErrorMessage DRIVER_CLOSED = ERR_DRIVER(4, "The driver has been closed and no further operation is allowed.");
const ErrorMessage SESSION_CLOSED = ERR_DRIVER(5, "The session has been closed and no further operation is allowed.");
const ErrorMessage TRANSACTION_CLOSED = ERR_DRIVER(6, "The transaction has been closed and no further operation is allowed.");
const ErrorMessage TRANSACTION_CLOSED_WITH_ERRORS = ERR_DRIVER(7, "The transaction has been closed with error(s): \n%s.");
const ErrorMessage DATABASE_DELETED = ERR_DRIVER(8, "The database has been deleted and no further operation is allowed.");
const ErrorMessage POSITIVE_VALUE_REQUIRED = ERR_DRIVER(9, "Value cannot be less than 1, was: '%d'.");
const ErrorMessage MISSING_DB_NAME = ERR_DRIVER(10, "Database name cannot be null.");
const ErrorMessage INVALID_JSON_CAST = ERR_DRIVER(11, "An invalid cast from json type '%s' to '%s' was attempted.");
#undef ERR_DRIVER

}  // namespace DriverError

namespace ConceptError {

#define ERR_CONCEPT(ID, MSG) ERRMSG("CCO", "Concept Error", ID, MSG)
const ErrorMessage INVALID_CONCEPT_CASTING = ERR_CONCEPT(1, "Invalid concept conversion from '%s' to '%s'.");
const ErrorMessage MISSING_TRANSACTION = ERR_CONCEPT(2, "Transaction cannot be null.");
const ErrorMessage MISSING_IID = ERR_CONCEPT(3, "IID cannot be null or empty.");
const ErrorMessage MISSING_LABEL = ERR_CONCEPT(4, "Label cannot be null or empty.");
const ErrorMessage MISSING_VARIABLE = ERR_CONCEPT(5, "Variable name cannot be null or empty.");
const ErrorMessage MISSING_VALUE = ERR_CONCEPT(6, "Value cannot be null.");
const ErrorMessage NONEXISTENT_EXPLAINABLE_CONCEPT = ERR_CONCEPT(7, "The concept identified by '%s' is not explainable.");
const ErrorMessage NONEXISTENT_EXPLAINABLE_OWNERSHIP = ERR_CONCEPT(8, "The ownership by owner '%s' of attribute '%s' is not explainable.");
const ErrorMessage UNRECOGNISED_ANNOTATION = ERR_CONCEPT(9, "The annotation '%s' is not recognised");
#undef ERR_CONCEPT

}  // namespace ConceptError

namespace QueryError {

#define ERR_QUERY(ID, MSG) ERRMSG("CQY", "Query Error", ID, MSG)
const ErrorMessage VARIABLE_DOES_NOT_EXIST = ERR_QUERY(1, "The variable '%s' does not exist.");
const ErrorMessage MISSING_QUERY = ERR_QUERY(2, "Query cannot be null or empty");
#undef ERR_QUERY

}  // namespace QueryError

namespace InternalError {

#define ERR_INTERNAL(ID, MSG) ERRMSG("CIN", "C++ Internal Error", ID, MSG)
const ErrorMessage UNEXPECTED_NATIVE_VALUE = ERR_INTERNAL(1, "Unexpected native value encountered!");
const ErrorMessage ILLEGAL_STATE = ERR_INTERNAL(2, "Illegal state has been reached! (%s : %d)");
const ErrorMessage ILLEGAL_CAST = ERR_INTERNAL(3, "Illegal casting operation to '%s'.");
const ErrorMessage NULL_NATIVE_VALUE = ERR_INTERNAL(4, "Unhandled null pointer to a native object encountered!");
const ErrorMessage INVALID_NATIVE_HANDLE = ERR_INTERNAL(5, "The object does not have a valid native handle. It may have been:  uninitialised, moved or disposed.");
const ErrorMessage ITERATOR_INVALIDATED = ERR_INTERNAL(6, "Dereferenced iterator which has reached end (or was invalidated by a move).");
#undef ERR_INTERNAL

}  // namespace InternalError

}  // namespace TypeDB
#undef ERRMSG
