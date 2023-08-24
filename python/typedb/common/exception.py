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

from typing import Union, Any

from typedb.native_client_wrapper import TypeDBClientException


class TypeDBClientExceptionExt(TypeDBClientException):

    def __init__(self, msg: Union[ErrorMessage, str], cause: BaseException = None, params: Any = None):
        if isinstance(msg, str):
            self.message = msg
            self.error_message = None
        else:
            self.message = msg.message(params)
            self.error_message = msg

        self.__cause__ = cause
        super(TypeDBClientException, self).__init__(self.message)

    @staticmethod
    def of(error_message: ErrorMessage, params: Any = None):
        return TypeDBClientExceptionExt(msg=error_message, cause=None, params=params)


class ErrorMessage:

    def __init__(self, code_prefix: str, code_number: int, message_prefix: str, message_body: str):
        self._code_prefix = code_prefix
        self._code_number = code_number
        self._message = message_prefix + ": " + message_body

    def code(self) -> str:
        return self._code_prefix + str(self._code_number).zfill(2)

    def message(self, params: Any) -> str:
        return self._message % params if params else self._message

    def __str__(self):
        return "[%s] %s" % (self.code(), self._message)


class ClientErrorMessage(ErrorMessage):

    def __init__(self, code: int, message: str):
        super(ClientErrorMessage, self).__init__(code_prefix="CLI", code_number=code, message_prefix="Client Error",
                                                 message_body=message)


CLIENT_CLOSED = ClientErrorMessage(1, "The client has been closed and no further operation is allowed.")
SESSION_CLOSED = ClientErrorMessage(2, "The session has been closed and no further operation is allowed.")
TRANSACTION_CLOSED = ClientErrorMessage(3, "The transaction has been closed and no further operation is allowed.")
DATABASE_DELETED = ClientErrorMessage(4, "The database '%s' has been deleted and no further operation is allowed.")
MISSING_DB_NAME = ClientErrorMessage(5, "Database name cannot be empty.")
POSITIVE_VALUE_REQUIRED = ClientErrorMessage(6, "Value should be positive, was: '%d'.")
CLUSTER_CREDENTIAL_INCONSISTENT = ClientErrorMessage(7, "TLS disabled but the Root CA path provided.")


class ConceptErrorMessage(ErrorMessage):

    def __init__(self, code: int, message: str):
        super(ConceptErrorMessage, self).__init__(code_prefix="CON", code_number=code,
                                                  message_prefix="Concept Error", message_body=message)


INVALID_CONCEPT_CASTING = ConceptErrorMessage(1, "Invalid concept conversion from '%s' to '%s'.")
MISSING_IID = ConceptErrorMessage(2, "IID cannot be null or empty.")
MISSING_LABEL = ConceptErrorMessage(3, "Label cannot be null or empty.")
MISSING_VARIABLE = ConceptErrorMessage(4, "Variable name cannot be null or empty.")
MISSING_VALUE = ConceptErrorMessage(5, "Value cannot be null or empty.")
NONEXISTENT_EXPLAINABLE_CONCEPT = ConceptErrorMessage(6, "The concept identified by '%s' is not explainable.")
NONEXISTENT_EXPLAINABLE_OWNERSHIP = ConceptErrorMessage(7, "The ownership by owner '%s' of attribute '%s' "
                                                        "is not explainable.")
GET_HAS_WITH_MULTIPLE_FILTERS = ConceptErrorMessage(8, "Only one filter can be applied at a time to get_has. "
                                                    "The possible filters are: [attribute_type, attribute_types, "
                                                    "annotations]")
UNRECOGNISED_ANNOTATION = ConceptErrorMessage(9, "The annotation '%s' is not recognised.")


class QueryErrorMessage(ErrorMessage):

    def __init__(self, code: int, message: str):
        super(QueryErrorMessage, self).__init__(code_prefix="QRY", code_number=code,
                                                message_prefix="Query Error", message_body=message)


VARIABLE_DOES_NOT_EXIST = QueryErrorMessage(1, "The variable '%s' does not exist.")
MISSING_QUERY = QueryErrorMessage(2, "Query cannot be null or empty.")


class InternalErrorMessage(ErrorMessage):

    def __init__(self, code: int, message: str):
        super(InternalErrorMessage, self).__init__(code_prefix="PIN", code_number=code,
                                                   message_prefix="Python Internal Error", message_body=message)


UNEXPECTED_NATIVE_VALUE = InternalErrorMessage(1, "Unexpected native value encountered!")
ILLEGAL_STATE = InternalErrorMessage(2, "Illegal state has been reached!")
ILLEGAL_CAST = InternalErrorMessage(3, "Illegal casting operation to '%s'.")
NULL_NATIVE_OBJECT = InternalErrorMessage(4, "Unhandled null pointer to a native object encountered!")


class TypeDBException(Exception):

    def __init__(self, code: str, message: str):
        super().__init__(code, message)
        self._code = code
        self._message = message

    def __str__(self):
        return "%s %s" % (self._code, self._message)
