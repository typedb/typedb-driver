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

import com.vaticle.typedb.driver.api.TypeDBTransaction;
import com.vaticle.typeql.lang.pattern.Pattern;

import javax.annotation.CheckReturnValue;

/**
 * Rules are a part of schema and define embedded logic.
 * The reasoning engine uses rules as a set of logic to infer new data.
 * A rule consists of a condition and a conclusion, and is uniquely identified by a label.
 */
public interface Rule {

    /**
     * Retrieves the unique label of the rule.
     */
    @CheckReturnValue
    String getLabel();

    /**
     * The statements that constitute the ‘when’ of the rule.
     */
    @CheckReturnValue
    Pattern getWhen();

    /**
     * The single statement that constitutes the ‘then’ of the rule.
     */
    @CheckReturnValue
    Pattern getThen();

    /**
     * Renames the label of the rule. The new label must remain unique.
     *
     * <h3>Examples</h3>
     * <pre>
     * rule.setLabel(transaction, newLabel)
     * </pre>
     *
     * @param transaction The current <code>Transaction</code>
     * @param label The new label to be given to the rule
     */
    void setLabel(TypeDBTransaction transaction, String label);

    /**
     * Deletes this rule.
     *
     * <h3>Examples</h3>
     * <pre>
     * rule.delete(transaction)
     * </pre>
     *
     * @param transaction The current <code>Transaction</code>
     */
    void delete(TypeDBTransaction transaction);

    /**
     * Check if this rule has been deleted.
     *
     * <h3>Examples</h3>
     * <pre>
     * rule.isDeleted(transaction)
     * </pre>
     *
     * @param transaction The current <code>Transaction</code>
     */
    @CheckReturnValue
    boolean isDeleted(TypeDBTransaction transaction);
}
