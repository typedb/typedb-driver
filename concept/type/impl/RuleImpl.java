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
import graql.lang.Graql;
import graql.lang.pattern.Pattern;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.stream.Stream;

import static grakn.client.common.exception.ErrorMessage.Concept.BAD_ENCODING;
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
            final ConceptProto.RuleMethod.Req method = ConceptProto.RuleMethod.Req.newBuilder()
                    .setRuleSetLabelReq(ConceptProto.Rule.SetLabel.Req.newBuilder()
                            .setLabel(label)).build();
            execute(method);
        }

        @Override
        public void setWhen(final Pattern when) {
            // TODO
            implement this method
        }

        @Override
        public void setThen(final Pattern then) {
            // TODO
            implement this method
        }

        @Override
        public void delete() {
            // TODO
            implement this method
        }

        @Override
        public boolean isDeleted() {
            // TODO
            implement this method
        }

        @Override
        public Remote asRemote(final Grakn.Transaction transaction) {
            return new RuleImpl.Remote(transaction, getLabel(), getWhen(), getThen());
        }

        Grakn.Transaction tx() {
            return transaction;
        }

        ConceptProto.RuleMethod.Res execute(final ConceptProto.RuleMethod.Req ruleMethod) {
            return transaction.concepts().runRuleMethod(getLabel(), ruleMethod).getTypeMethodRes();
        }
    }
}
