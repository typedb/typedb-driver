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
import grakn.protocol.ConceptProto;
import grakn.protocol.ConceptProto.RuleMethod;
import graql.lang.Graql;
import graql.lang.pattern.Pattern;

import java.util.Objects;

import static grakn.client.common.exception.ErrorMessage.Concept.MISSING_LABEL;
import static grakn.client.common.exception.ErrorMessage.Concept.MISSING_TRANSACTION;
import static grakn.common.util.Objects.className;

public class RuleImpl implements Rule {

    private final String label;
    private final Pattern when;
    private final Pattern then;
    private final int hash;

    RuleImpl(final String label, final Pattern when, final Pattern then) {
        if (label == null || label.isEmpty()) throw new GraknClientException(MISSING_LABEL);
        this.label = label;
        this.when = when;
        this.then = then;
        this.hash = Objects.hash(this.label);
    }

    public static RuleImpl of(final ConceptProto.Rule ruleProto) {
        return new RuleImpl(ruleProto.getLabel(), Graql.parsePattern(ruleProto.getWhen()), Graql.parsePattern(ruleProto.getThen()));
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public Pattern getWhen() {
        return when;
    }

    @Override
    public Pattern getThen() {
        return then;
    }

    @Override
    public RuleImpl.Remote asRemote(final Grakn.Transaction transaction) {
        return new RuleImpl.Remote(transaction, getLabel(), getWhen(), getThen());
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

        private final Grakn.Transaction transaction;
        private final String label;
        private final Pattern when;
        private final Pattern then;
        private final int hash;

        public Remote(final Grakn.Transaction transaction, final String label, final Pattern when, final Pattern then) {
            if (transaction == null) throw new GraknClientException(MISSING_TRANSACTION);
            if (label == null || label.isEmpty()) throw new GraknClientException(MISSING_LABEL);
            this.transaction = transaction;
            this.label = label;
            this.when = when;
            this.then = then;
            this.hash = Objects.hash(transaction, label);
        }

        public static RuleImpl.Remote of(final Grakn.Transaction transaction, final ConceptProto.Rule ruleProto) {
            return new RuleImpl.Remote(transaction, ruleProto.getLabel(), Graql.parsePattern(ruleProto.getWhen()), Graql.parsePattern(ruleProto.getThen()));
        }

        @Override
        public String getLabel() {
            return label;
        }

        @Override
        public Pattern getWhen() {
            return when;
        }

        @Override
        public Pattern getThen() {
            return then;
        }

        @Override
        public void setLabel(final String label) {
            execute(RuleMethod.Req.newBuilder().setRuleSetLabelReq(ConceptProto.Rule.SetLabel.Req.newBuilder().setLabel(label)));
        }

        @Override
        public void setWhen(final Pattern when) {
            execute(RuleMethod.Req.newBuilder().setRuleSetWhenReq(ConceptProto.Rule.SetWhen.Req.newBuilder().setPattern(when.toString())));
        }

        @Override
        public void setThen(final Pattern then) {
            execute(RuleMethod.Req.newBuilder().setRuleSetThenReq(ConceptProto.Rule.SetThen.Req.newBuilder().setPattern(then.toString())));
        }

        @Override
        public void delete() {
            execute(RuleMethod.Req.newBuilder().setRuleDeleteReq(ConceptProto.Rule.Delete.Req.getDefaultInstance()));
        }

        @Override
        public final boolean isDeleted() {
            return transaction.concepts().getRule(label) != null;
        }

        @Override
        public Remote asRemote(final Grakn.Transaction transaction) {
            return new RuleImpl.Remote(transaction, getLabel(), getWhen(), getThen());
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
            return transaction;
        }

        ConceptProto.RuleMethod.Res execute(final ConceptProto.RuleMethod.Req.Builder method) {
            return transaction.concepts().runRuleMethod(method.setLabel(label)).getRuleMethodRes();
        }
    }
}
