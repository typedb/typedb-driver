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

import com.vaticle.typedb.driver.api.logic.LogicManager;
import com.vaticle.typedb.driver.api.logic.Rule;
import com.vaticle.typedb.driver.common.Promise;
import com.vaticle.typedb.driver.common.exception.TypeDBDriverException;
import com.vaticle.typeql.lang.pattern.Pattern;

import javax.annotation.CheckReturnValue;
import java.util.stream.Stream;

import static com.vaticle.typedb.driver.common.exception.ErrorMessage.Driver.TRANSACTION_CLOSED;
import static com.vaticle.typedb.driver.common.exception.ErrorMessage.Concept.MISSING_LABEL;
import static com.vaticle.typedb.driver.jni.typedb_driver.logic_manager_get_rule;
import static com.vaticle.typedb.driver.jni.typedb_driver.logic_manager_get_rules;
import static com.vaticle.typedb.driver.jni.typedb_driver.logic_manager_put_rule;

public final class LogicManagerImpl implements LogicManager {
    final com.vaticle.typedb.driver.jni.Transaction nativeTransaction;

    public LogicManagerImpl(com.vaticle.typedb.driver.jni.Transaction nativeTransaction) {
        this.nativeTransaction = nativeTransaction;
    }

    @Override
    @CheckReturnValue
    public Promise<Rule> getRule(String label) {
        if (label == null || label.isEmpty()) throw new TypeDBDriverException(MISSING_LABEL);
        if (!nativeTransaction.isOwned()) throw new TypeDBDriverException(TRANSACTION_CLOSED);
        try {
            com.vaticle.typedb.driver.jni.RulePromise promise = logic_manager_get_rule(nativeTransaction, label);
            return new Promise<>(() -> {
                com.vaticle.typedb.driver.jni.Rule res = promise.get();
                if (res != null) return new RuleImpl(res);
                else return null;
            });
        } catch (com.vaticle.typedb.driver.jni.Error e) {
            throw new TypeDBDriverException(e);
        }
    }

    @Override
    public Stream<RuleImpl> getRules() {
        if (!nativeTransaction.isOwned()) throw new TypeDBDriverException(TRANSACTION_CLOSED);
        try {
            return logic_manager_get_rules(nativeTransaction).stream().map(RuleImpl::new);
        } catch (com.vaticle.typedb.driver.jni.Error e) {
            throw new TypeDBDriverException(e);
        }
    }

    @Override
    @CheckReturnValue
    public Promise<Rule> putRule(String label, Pattern when, Pattern then) {
        if (label == null || label.isEmpty()) throw new TypeDBDriverException(MISSING_LABEL);
        if (!nativeTransaction.isOwned()) throw new TypeDBDriverException(TRANSACTION_CLOSED);
        try {
            com.vaticle.typedb.driver.jni.RulePromise promise = logic_manager_put_rule(nativeTransaction, label, when.toString(), then.toString());
            return new Promise<>(() -> new RuleImpl(promise.get()));
        } catch (com.vaticle.typedb.driver.jni.Error e) {
            throw new TypeDBDriverException(e);
        }
    }
}
