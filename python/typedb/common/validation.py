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

from typedb.common.exception import TypeDBDriverException, NON_NEGATIVE_VALUE_REQUIRED, NON_NULL_VALUE_REQUIRED, \
    POSITIVE_VALUE_REQUIRED


def require_non_null(obj, field_name):
    """
    Validates that the provided object is not None.

    :param obj: The object to check.
    :param field_name: The name of the checked field for error context.
    :raises TypeDBDriverException: If the object is None.
    """
    if obj is None:
        raise TypeDBDriverException(NON_NULL_VALUE_REQUIRED, field_name)


def require_positive(value, field_name):
    """
    Validates that the provided value is positive.

    :param value: The numeric value to check.
    :param field_name: The name of the checked field for error context.
    :raises TypeDBDriverException: If the value is not positive or None.
    """
    require_non_null(value, field_name)
    if value < 1:
        raise TypeDBDriverException(POSITIVE_VALUE_REQUIRED, field_name, value)


def require_non_negative(value, field_name):
    """
    Validates that the provided value is non-negative.

    :param value: The numeric value to check.
    :param field_name: The name of the checked field for error context.
    :raises TypeDBDriverException: If the value is negative or None.
    """
    require_non_null(value, field_name)
    if value < 0:
        raise TypeDBDriverException(NON_NEGATIVE_VALUE_REQUIRED, field_name, value)
