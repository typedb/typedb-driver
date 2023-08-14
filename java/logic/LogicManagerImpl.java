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

package com.vaticle.typedb.client.logic;

import com.vaticle.typedb.client.api.logic.LogicManager;
import com.vaticle.typedb.client.api.logic.Rule;
import com.vaticle.typedb.client.common.exception.TypeDBClientException;
import com.vaticle.typeql.lang.pattern.Pattern;

import javax.annotation.Nullable;
import java.util.stream.Stream;

import static com.vaticle.typedb.client.common.exception.ErrorMessage.Client.TRANSACTION_CLOSED;
import static com.vaticle.typedb.client.common.exception.ErrorMessage.Concept.MISSING_LABEL;
import static com.vaticle.typedb.client.jni.typedb_client.logic_manager_get_rule;
import static com.vaticle.typedb.client.jni.typedb_client.logic_manager_get_rules;
import static com.vaticle.typedb.client.jni.typedb_client.logic_manager_put_rule;

public final class LogicManagerImpl implements LogicManager {
    final com.vaticle.typedb.client.jni.Transaction nativeTransaction;

    public LogicManagerImpl(com.vaticle.typedb.client.jni.Transaction nativeTransaction) {
        this.nativeTransaction = nativeTransaction;
    }

    @Override
    @Nullable
    public Rule getRule(String label) {
        if (label == null || label.isEmpty()) throw new TypeDBClientException(MISSING_LABEL);
        if (!nativeTransaction.isOwned()) throw new TypeDBClientException(TRANSACTION_CLOSED);
        try {
            com.vaticle.typedb.client.jni.Rule res = logic_manager_get_rule(nativeTransaction, label);
            if (res != null) return new RuleImpl(res);
            else return null;
        } catch (com.vaticle.typedb.client.jni.Error e) {
            throw new TypeDBClientException(e);
        }
    }

    @Override
    public Stream<RuleImpl> getRules() {
        if (!nativeTransaction.isOwned()) throw new TypeDBClientException(TRANSACTION_CLOSED);
        try {
            return logic_manager_get_rules(nativeTransaction).stream().map(RuleImpl::new);
        } catch (com.vaticle.typedb.client.jni.Error e) {
            throw new TypeDBClientException(e);
        }
    }

    @Override
    public Rule putRule(String label, Pattern when, Pattern then) {
        if (label == null || label.isEmpty()) throw new TypeDBClientException(MISSING_LABEL);
        if (!nativeTransaction.isOwned()) throw new TypeDBClientException(TRANSACTION_CLOSED);
        try {
            return new RuleImpl(logic_manager_put_rule(nativeTransaction, label, when.toString(), then.toString()));
        } catch (com.vaticle.typedb.client.jni.Error e) {
            throw new TypeDBClientException(e);
        }
    }
}
