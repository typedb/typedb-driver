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

package grakn.client.concept.type.impl;

import grakn.client.Grakn;
import grakn.client.common.exception.GraknClientException;
import grakn.client.concept.type.Rule;
import grakn.client.rpc.RPCTransaction;
import grakn.protocol.ConceptProto;
import grakn.protocol.TransactionProto;
import graql.lang.Graql;
import graql.lang.pattern.Conjunction;
import graql.lang.pattern.Pattern;
import graql.lang.pattern.variable.ThingVariable;

import java.util.Objects;

import static grakn.client.common.exception.ErrorMessage.Concept.MISSING_LABEL;
import static grakn.client.common.exception.ErrorMessage.Concept.MISSING_TRANSACTION;
import static grakn.common.util.Objects.className;

public class RuleImpl implements Rule {

    private final String label;
    private final Conjunction<? extends Pattern> when;
    private final ThingVariable<?> then;
    private final int hash;

    RuleImpl(final String label, final Conjunction<? extends Pattern> when, final ThingVariable<?> then) {
        if (label == null || label.isEmpty()) throw new GraknClientException(MISSING_LABEL);
        this.label = label;
        this.when = when;
        this.then = then;
        this.hash = Objects.hash(this.label);
    }

    public static RuleImpl of(final ConceptProto.Rule ruleProto) {
        return new RuleImpl(ruleProto.getLabel(), Graql.and(Graql.parsePatterns(ruleProto.getWhen())), Graql.parseVariable(ruleProto.getThen()).asThing());
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
    public RuleImpl.Remote asRemote(final Grakn.Transaction transaction) {
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
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final RuleImpl that = (RuleImpl) o;
        return this.label.equals(that.label);
    }

    @Override
    public int hashCode() {
        return hash;
    }

    public static class Remote implements Rule.Remote {

        final RPCTransaction rpcTransaction;
        private final String label;
        private final Conjunction<? extends Pattern> when;
        private final ThingVariable<?> then;
        private final int hash;

        public Remote(final Grakn.Transaction transaction, final String label, final Conjunction<? extends Pattern> when, final ThingVariable<?> then) {
            if (transaction == null) throw new GraknClientException(MISSING_TRANSACTION);
            if (label == null || label.isEmpty()) throw new GraknClientException(MISSING_LABEL);
            this.rpcTransaction = (RPCTransaction) transaction;
            this.label = label;
            this.when = when;
            this.then = then;
            this.hash = Objects.hash(transaction, label);
        }

        public static RuleImpl.Remote of(final Grakn.Transaction transaction, final ConceptProto.Rule ruleProto) {
            return new RuleImpl.Remote(transaction, ruleProto.getLabel(), Graql.and(Graql.parsePatterns(ruleProto.getWhen())), Graql.parseVariable(ruleProto.getThen()).asThing());
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
        public void setLabel(final String label) {
            execute(ConceptProto.Rule.Req.newBuilder().setRuleSetLabelReq(ConceptProto.Rule.SetLabel.Req.newBuilder().setLabel(label)));
        }

        @Override
        public void delete() {
            execute(ConceptProto.Rule.Req.newBuilder().setRuleDeleteReq(ConceptProto.Rule.Delete.Req.getDefaultInstance()));
        }

        @Override
        public final boolean isDeleted() {
            return rpcTransaction.concepts().getRule(label) != null;
        }

        @Override
        public Remote asRemote(final Grakn.Transaction transaction) {
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
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            final RuleImpl.Remote that = (RuleImpl.Remote) o;
            return this.label.equals(that.label);
        }

        @Override
        public int hashCode() {
            return hash;
        }

        final Grakn.Transaction tx() {
            return rpcTransaction;
        }

        ConceptProto.Rule.Res execute(final ConceptProto.Rule.Req.Builder method) {
            final TransactionProto.Transaction.Req.Builder request = TransactionProto.Transaction.Req.newBuilder()
                    .setRuleReq(method.setLabel(label));
            return rpcTransaction.execute(request).getRuleRes();
        }
    }
}
