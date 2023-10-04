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

package com.vaticle.typedb.driver.api.logic;

import com.vaticle.typeql.lang.pattern.Pattern;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nullable;
import java.util.stream.Stream;

/**
 * Provides methods for manipulating rules in the database.
 */
public interface LogicManager {

    /**
     * Retrieves the Rule that has the given label.
     *
     * <h3>Examples</h3>
     * <pre>
     * transaction.logic().getRule(label)
     * </pre>
     *
     * @param label The label of the Rule to create or retrieve
     */
    @Nullable
    @CheckReturnValue
    Rule getRule(String label);

    /**
     * Retrieves all rules.
     *
     * <h3>Examples</h3>
     * <pre>
     * transaction.logic().getRules()
     * </pre>
     */
    @CheckReturnValue
    Stream<? extends Rule> getRules();

    /**
     * Creates a new Rule if none exists with the given label, or replaces the existing one.
     *
     * <h3>Examples</h3>
     * <pre>
     * transaction.logic().putRule(label, when, then)
     * </pre>
     *
     * @param label The label of the Rule to create or replace
     * @param when The when body of the rule to create
     * @param then The then body of the rule to create
     */
    Rule putRule(String label, Pattern when, Pattern then);
}
