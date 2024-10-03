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

from typing import Union, Any

from typedb.native_driver_wrapper import TypeDBDriverExceptionNative


class TypeDBDriverException(RuntimeError):
    """
    Exceptions raised by the driver.

    Examples
    --------

    ::

        try:
            transaction.commit()
        except TypeDBDriverException as err:
            print("Error:", err)
    """

    def __init__(self, msg: Union[ErrorMessage, str], params: Any = None):
        if isinstance(msg, str):
            self.message = msg
            self.error_message = None
        else:
            self.message = msg.message(params)
            self.error_message = msg

        super().__init__(self.message)

    @staticmethod
    def of(exception: TypeDBDriverExceptionNative) -> TypeDBDriverException:
        """
        :meta private:
        """
        return TypeDBDriverException(str(exception))


class ErrorMessage:
    """
    :meta private:
    """

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


class DriverErrorMessage(ErrorMessage):
    """
    :meta private:
    """

    def __init__(self, code: int, message: str):
        super(DriverErrorMessage, self).__init__(code_prefix="PDR", code_number=code, message_prefix="Driver Error",
                                                 message_body=message)


DRIVER_CLOSED = DriverErrorMessage(1, "The driver has been closed and no further operation is allowed.")
TRANSACTION_CLOSED = DriverErrorMessage(2, "The transaction has been closed and no further operation is allowed.")
DATABASE_DELETED = DriverErrorMessage(3, "The database '%s' has been deleted and no further operation is allowed.")
MISSING_DB_NAME = DriverErrorMessage(4, "Database name cannot be empty.")
POSITIVE_VALUE_REQUIRED = DriverErrorMessage(5, "Value should be positive, was: '%d'.")
CLOUD_CREDENTIAL_INCONSISTENT = DriverErrorMessage(6, "TLS disabled but the Root CA path provided.")
UNIMPLEMENTED = DriverErrorMessage(7, "This operation is not implemented yet.")


class ConceptErrorMessage(ErrorMessage):
    """
    :meta private:
    """

    def __init__(self, code: int, message: str):
        super(ConceptErrorMessage, self).__init__(code_prefix="PCO", code_number=code,
                                                  message_prefix="Concept Error", message_body=message)


INVALID_CONCEPT_CASTING = ConceptErrorMessage(1, "Invalid concept conversion from '%s' to '%s'.")
INVALID_QUERY_ANSWER_CASTING = ConceptErrorMessage(2, "Invalid query answer conversion from '%s' to '%s'.")
MISSING_VARIABLE = ConceptErrorMessage(3, "Variable name cannot be null or empty.")


class QueryErrorMessage(ErrorMessage):
    """
    :meta private:
    """

    def __init__(self, code: int, message: str):
        super(QueryErrorMessage, self).__init__(code_prefix="PQR", code_number=code,
                                                message_prefix="Query Error", message_body=message)


VARIABLE_DOES_NOT_EXIST = QueryErrorMessage(1, "The variable '%s' does not exist.")
MISSING_QUERY = QueryErrorMessage(2, "Query cannot be null or empty.")


class InternalErrorMessage(ErrorMessage):
    """
    :meta private:
    """

    def __init__(self, code: int, message: str):
        super(InternalErrorMessage, self).__init__(code_prefix="PIN", code_number=code,
                                                   message_prefix="Python Internal Error", message_body=message)


UNEXPECTED_NATIVE_VALUE = InternalErrorMessage(1, "Unexpected native value encountered!")
ILLEGAL_STATE = InternalErrorMessage(2, "Illegal state has been reached!")
ILLEGAL_CAST = InternalErrorMessage(3, "Illegal casting operation to '%s'.")
NULL_NATIVE_OBJECT = InternalErrorMessage(4, "Unhandled null pointer to a native object encountered!")


class TypeDBException(Exception):
    """
    :meta private:
    """

    def __init__(self, code: str, message: str):
        super().__init__(code, message)
        self._code = code
        self._message = message

    def __str__(self):
        return "%s %s" % (self._code, self._message)
