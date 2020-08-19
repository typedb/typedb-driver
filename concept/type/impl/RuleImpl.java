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

import grakn.client.Grakn.Transaction;
import grakn.client.concept.type.Rule;
import grakn.client.concept.type.Type;
import grakn.client.exception.GraknClientException;
import grakn.protocol.ConceptProto;
import graql.lang.Graql;
import graql.lang.pattern.Pattern;

import javax.annotation.Nullable;
import java.util.stream.Stream;

public class RuleImpl {
    /**
     * Client implementation of Rule
     */
    public static class Local extends TypeImpl.Local implements Rule.Local {

        public Local(ConceptProto.Type type) {
            super(type);
        }
    }

    /**
     * Client implementation of Rule
     */
    public static class Remote extends TypeImpl.Remote implements Rule.Remote {

        public Remote(Transaction tx, String label) {
            super(tx, label);
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
                case NULL:
                    return null;
                case PATTERN:
                    return Graql.parsePattern(response.getPattern());
                default:
                    throw GraknClientException.unreachableStatement("Unexpected response " + response);
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
                case NULL:
                    return null;
                case PATTERN:
                    return Graql.parsePattern(response.getPattern());
                default:
                    throw GraknClientException.unreachableStatement("Unexpected response " + response);
            }
        }
    }
}
