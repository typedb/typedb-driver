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

package com.vaticle.typedb.driver.logic;

import com.vaticle.typedb.driver.api.TypeDBTransaction;
import com.vaticle.typedb.driver.api.logic.Rule;
import com.vaticle.typedb.driver.common.NativeObject;
import com.vaticle.typedb.driver.common.Promise;
import com.vaticle.typedb.driver.common.exception.TypeDBDriverException;
import com.vaticle.typeql.lang.TypeQL;
import com.vaticle.typeql.lang.pattern.Conjunction;
import com.vaticle.typeql.lang.pattern.Pattern;
import com.vaticle.typeql.lang.pattern.statement.ThingStatement;

import javax.annotation.CheckReturnValue;

import static com.vaticle.typedb.driver.common.exception.ErrorMessage.Driver.TRANSACTION_CLOSED;
import static com.vaticle.typedb.driver.common.exception.ErrorMessage.Concept.MISSING_LABEL;
import static com.vaticle.typedb.driver.jni.typedb_driver.rule_delete;
import static com.vaticle.typedb.driver.jni.typedb_driver.rule_get_label;
import static com.vaticle.typedb.driver.jni.typedb_driver.rule_get_then;
import static com.vaticle.typedb.driver.jni.typedb_driver.rule_get_when;
import static com.vaticle.typedb.driver.jni.typedb_driver.rule_is_deleted;
import static com.vaticle.typedb.driver.jni.typedb_driver.rule_set_label;
import static com.vaticle.typedb.driver.jni.typedb_driver.rule_to_string;

public class RuleImpl extends NativeObject<com.vaticle.typedb.driver.jni.Rule> implements Rule {
    private int hash = 0;

    private final Conjunction<? extends Pattern> when;
    private final ThingStatement<?> then;

    RuleImpl(com.vaticle.typedb.driver.jni.Rule rule) {
        super(rule);
        when = TypeQL.parsePattern(rule_get_when(nativeObject)).asConjunction();
        then = TypeQL.parseStatement(rule_get_then(nativeObject)).asThing();
    }

    @Override
    public String getLabel() {
        return rule_get_label(nativeObject);
    }

    @Override
    public Conjunction<? extends Pattern> getWhen() {
        return when;
    }

    @Override
    public ThingStatement<?> getThen() {
        return then;
    }

    @Override
    @CheckReturnValue
    public Promise<Void> setLabel(TypeDBTransaction transaction, String newLabel) {
        if (newLabel == null || newLabel.isEmpty()) throw new TypeDBDriverException(MISSING_LABEL);
        return new Promise<>(rule_set_label(nativeTransaction(transaction), nativeObject, newLabel));
    }

    @Override
    @CheckReturnValue
    public Promise<Void> delete(TypeDBTransaction transaction) {
        return new Promise<>(rule_delete(nativeTransaction(transaction), nativeObject));
    }

    @Override
    @CheckReturnValue
    public final Promise<Boolean> isDeleted(TypeDBTransaction transaction) {
        return new Promise<>(rule_is_deleted(nativeTransaction(transaction), nativeObject));
    }

    private static com.vaticle.typedb.driver.jni.Transaction nativeTransaction(TypeDBTransaction transaction) {
        com.vaticle.typedb.driver.jni.Transaction nativeTransaction = ((LogicManagerImpl) transaction.logic()).nativeTransaction;
        if (!nativeTransaction.isOwned()) throw new TypeDBDriverException(TRANSACTION_CLOSED);
        return nativeTransaction;
    }

    @Override
    public String toString() {
        return rule_to_string(nativeObject);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        RuleImpl that = (RuleImpl) obj;
        return this.getLabel().equals(that.getLabel());
    }

    @Override
    public int hashCode() {
        if (hash == 0) hash = getLabel().hashCode();
        return hash;
    }
}
