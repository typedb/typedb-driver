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
import grakn.client.concept.type.Rule;
import grakn.client.concept.type.Type;
import grakn.protocol.ConceptProto;
import graql.lang.Graql;
import graql.lang.pattern.Pattern;

import javax.annotation.Nullable;
import java.util.stream.Stream;

public class RuleImpl {

    public static class Local extends TypeImpl.Local implements Rule.Local {

        public Local(ConceptProto.Type type) {
            super(type);
        }

        @Override
        public Rule.Remote asRemote(Grakn.Transaction transaction) {
            return new RuleImpl.Remote(transaction, getLabel(), isRoot());
        }
    }

    public static class Remote extends TypeImpl.Remote implements Rule.Remote {

        public Remote(final Grakn.Transaction transaction, final String label, final boolean isRoot) {
            super(transaction, label, isRoot);
        }

        @Nullable
        @Override
        public Type.Remote getSupertype() {
            return getSupertypeInternal(Type.Remote::asRule);
        }

        @Override
        public final Stream<Rule.Remote> getSupertypes() {
            return super.getSupertypes().map(Type.Remote::asRule);
        }

        @Override
        public final Stream<Rule.Remote> getSubtypes() {
            return super.getSubtypes().map(Type.Remote::asRule);
        }

        @Override
        @Nullable
        @SuppressWarnings("Duplicates") // response.getResCase() does not return the same type
        public final Pattern getWhen() {
            ConceptProto.TypeMethod.Req method = ConceptProto.TypeMethod.Req.newBuilder()
                    .setRuleWhenReq(ConceptProto.Rule.When.Req.getDefaultInstance()).build();

            ConceptProto.Rule.When.Res response = runMethod(method).getRuleWhenRes();
            switch (response.getResCase()) {
                case PATTERN:
                    return Graql.parsePattern(response.getPattern());
                default:
                case RES_NOT_SET:
                    return null;
            }
        }

        @Override
        @Nullable
        @SuppressWarnings("Duplicates") // response.getResCase() does not return the same type
        public final Pattern getThen() {
            ConceptProto.TypeMethod.Req method = ConceptProto.TypeMethod.Req.newBuilder()
                    .setRuleThenReq(ConceptProto.Rule.Then.Req.getDefaultInstance()).build();

            ConceptProto.Rule.Then.Res response = runMethod(method).getRuleThenRes();
            switch (response.getResCase()) {
                case PATTERN:
                    return Graql.parsePattern(response.getPattern());
                default:
                case RES_NOT_SET:
                    return null;
            }
        }
    }
}
