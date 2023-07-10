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

import com.vaticle.typedb.client.api.TypeDBTransaction;
import com.vaticle.typedb.client.api.logic.Rule;
import com.vaticle.typeql.lang.TypeQL;
import com.vaticle.typeql.lang.pattern.Conjunction;
import com.vaticle.typeql.lang.pattern.Pattern;
import com.vaticle.typeql.lang.pattern.variable.ThingVariable;

import static com.vaticle.typedb.client.jni.typedb_client.rule_delete;
import static com.vaticle.typedb.client.jni.typedb_client.rule_get_label;
import static com.vaticle.typedb.client.jni.typedb_client.rule_get_then;
import static com.vaticle.typedb.client.jni.typedb_client.rule_get_when;
import static com.vaticle.typedb.client.jni.typedb_client.rule_is_deleted;
import static com.vaticle.typedb.client.jni.typedb_client.rule_set_label;

public class RuleImpl implements Rule {
    com.vaticle.typedb.client.jni.Rule rule;

    RuleImpl(com.vaticle.typedb.client.jni.Rule rule) {
        this.rule = rule;
    }

    @Override
    public String getLabel() {
        return rule_get_label(rule);
    }

    @Override
    public Conjunction<? extends Pattern> getWhen() {
        return TypeQL.parsePattern(rule_get_when(rule)).asConjunction();
    }

    @Override
    public ThingVariable<?> getThen() {
        return TypeQL.parseVariable(rule_get_then(rule)).asThing();
    }

    @Override
    public void setLabel(TypeDBTransaction transaction, String newLabel) {
        rule_set_label(((LogicManagerImpl) transaction.logic()).transaction, rule, newLabel);
    }

    @Override
    public void delete(TypeDBTransaction transaction) {
        rule_delete(((LogicManagerImpl) transaction.logic()).transaction, rule);
    }

    @Override
    public final boolean isDeleted(TypeDBTransaction transaction) {
        return rule_is_deleted(((LogicManagerImpl) transaction.logic()).transaction, rule);
    }

    @Override
    public String toString() {
        return rule_to_string(rule);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RuleImpl that = (RuleImpl) o;
        return this.getLabel().equals(that.getLabel());
    }

    @Override
    public int hashCode() {
        return rule.hashCode(); // FIXME
    }
}
