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

package com.vaticle.typedb.driver.api.answer;

import javax.annotation.CheckReturnValue;

/**
 * Stores an aggregate query answer.
 */
public interface Numeric {
    /**
     * Checks if the type of an aggregate answer is a <code>long</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * numeric.isLong();
     * </pre>
     */
    @CheckReturnValue
    boolean isLong();

    /**
     * Checks if the type of an aggregate answer is a <code>double</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * numeric.isDouble();
     * </pre>
     */
    @CheckReturnValue
    boolean isDouble();

    /**
     * Checks if the aggregate answer is not a number.
     *
     * <h3>Examples</h3>
     * <pre>
     * numeric.isNaN();
     * </pre>
     */
    @CheckReturnValue
    boolean isNaN();

    /**
     * Retrieves numeric value of an aggregate answer as a <code>long</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * numeric.asLong();
     * </pre>
     */
    @CheckReturnValue
    long asLong();

    /**
     * Retrieves numeric value of an aggregate answer as a <code>double</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * numeric.asDouble();
     * </pre>
     */
    @CheckReturnValue
    double asDouble();

    /**
     * Retrieves numeric value of an aggregate answer.
     *
     * <h3>Examples</h3>
     * <pre>
     * numeric.asNumber();
     * </pre>
     */
    @CheckReturnValue
    Number asNumber();

    /**
     * Retrieves a string representation of an aggregate answer.
     *
     * <h3>Examples</h3>
     * <pre>
     * numeric.toString();
     * </pre>
     */
    @Override
    @CheckReturnValue
    String toString();
}
