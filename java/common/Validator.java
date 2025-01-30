/*
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

package com.typedb.driver.common;

import com.typedb.driver.common.exception.TypeDBDriverException;

import static com.typedb.driver.common.exception.ErrorMessage.Driver.NON_NEGATIVE_VALUE_REQUIRED;
import static com.typedb.driver.common.exception.ErrorMessage.Driver.NON_NULL_VALUE_REQUIRED;
import static com.typedb.driver.common.exception.ErrorMessage.Driver.POSITIVE_VALUE_REQUIRED;

public class Validator {
    /**
     * Validates that the provided object is not null.
     *
     * @param obj       The object to check.
     * @param fieldName The name of the checked field for error context.
     * @throws TypeDBDriverException if the object is null.
     */
    public static void requireNonNull(Object obj, String fieldName) {
        if (obj == null) {
            throw new TypeDBDriverException(NON_NULL_VALUE_REQUIRED, fieldName);
        }
    }

    /**
     * Validates that the provided value is positive.
     *
     * @param value     The long value to check.
     * @param fieldName The name of the checked field for error context.
     * @throws TypeDBDriverException if the value is not positive.
     */
    public static void requirePositive(long value, String fieldName) {
        if (value < 1) {
            throw new TypeDBDriverException(POSITIVE_VALUE_REQUIRED, fieldName, value);
        }
    }

    /**
     * Validates that the provided value is non-negative.
     *
     * @param value     The long value to check.
     * @param fieldName The name of the checked field for error context.
     * @throws TypeDBDriverException if the value is negative.
     */
    public static void requireNonNegative(long value, String fieldName) {
        if (value < 0) {
            throw new TypeDBDriverException(NON_NEGATIVE_VALUE_REQUIRED, fieldName, value);
        }
    }
}
