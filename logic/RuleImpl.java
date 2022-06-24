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
import com.vaticle.typedb.client.common.exception.TypeDBClientException;
import com.vaticle.typedb.protocol.LogicProto;
import com.vaticle.typeql.lang.TypeQL;
import com.vaticle.typeql.lang.pattern.Conjunction;
import com.vaticle.typeql.lang.pattern.Pattern;
import com.vaticle.typeql.lang.pattern.variable.ThingVariable;

import java.util.Objects;

import static com.vaticle.typedb.client.common.exception.ErrorMessage.Concept.MISSING_LABEL;
import static com.vaticle.typedb.client.common.exception.ErrorMessage.Concept.MISSING_TRANSACTION;
import static com.vaticle.typedb.client.common.rpc.RequestBuilder.Rule.deleteReq;
import static com.vaticle.typedb.client.common.rpc.RequestBuilder.Rule.setLabelReq;
import static com.vaticle.typedb.common.util.Objects.className;

public class RuleImpl implements Rule {

    private final String label;
    private final Conjunction<? extends Pattern> when;
    private final ThingVariable<?> then;
    private final int hash;

    RuleImpl(String label, Conjunction<? extends Pattern> when, ThingVariable<?> then) {
        if (label == null || label.isEmpty()) throw new TypeDBClientException(MISSING_LABEL);
        this.label = label;
        this.when = when;
        this.then = then;
        this.hash = Objects.hash(this.label);
    }

    public static RuleImpl of(LogicProto.Rule ruleProto) {
        return new RuleImpl(
                ruleProto.getLabel(),
                TypeQL.parsePattern(ruleProto.getWhen()).asConjunction(),
                TypeQL.parseVariable(ruleProto.getThen()).asThing()
        );
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public Conjunction<? extends Pattern> getWhen() {
        return when;
    }

    @Override
    public ThingVariable<?> getThen() {
        return then;
    }

    @Override
    public RuleImpl.Remote asRemote(TypeDBTransaction transaction) {
        return new RuleImpl.Remote(transaction, getLabel(), getWhen(), getThen());
    }

    @Override
    public boolean isRemote() {
        return false;
    }

    @Override
    public String toString() {
        return className(this.getClass()) + "[label: " + label + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RuleImpl that = (RuleImpl) o;
        return this.label.equals(that.label);
    }

    @Override
    public int hashCode() {
        return hash;
    }

    public static class Remote implements Rule.Remote {

        final TypeDBTransaction.Extended transactionExt;
        private String label;
        private final Conjunction<? extends Pattern> when;
        private final ThingVariable<?> then;
        private final int hash;

        public Remote(TypeDBTransaction transaction, String label, Conjunction<? extends Pattern> when, ThingVariable<?> then) {
            if (transaction == null) throw new TypeDBClientException(MISSING_TRANSACTION);
            if (label == null || label.isEmpty()) throw new TypeDBClientException(MISSING_LABEL);
            this.transactionExt = (TypeDBTransaction.Extended) transaction;
            this.label = label;
            this.when = when;
            this.then = then;
            this.hash = Objects.hash(transaction, label);
        }

        @Override
        public String getLabel() {
            return label;
        }

        @Override
        public Conjunction<? extends Pattern> getWhen() {
            return when;
        }

        @Override
        public ThingVariable<?> getThen() {
            return then;
        }

        @Override
        public void setLabel(String newLabel) {
            transactionExt.execute(setLabelReq(label, newLabel));
            this.label = newLabel;
        }

        @Override
        public void delete() {
            transactionExt.execute(deleteReq(label));
        }

        @Override
        public final boolean isDeleted() {
            return transactionExt.logic().getRule(label) != null;
        }

        @Override
        public Remote asRemote(TypeDBTransaction transaction) {
            return new RuleImpl.Remote(transaction, getLabel(), getWhen(), getThen());
        }

        @Override
        public boolean isRemote() {
            return true;
        }

        @Override
        public String toString() {
            return className(this.getClass()) + "[label: " + label + "]";
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            RuleImpl.Remote that = (RuleImpl.Remote) o;
            return this.transactionExt.equals(that.transactionExt) && this.label.equals(that.label);
        }

        @Override
        public int hashCode() {
            return hash;
        }
    }
}
