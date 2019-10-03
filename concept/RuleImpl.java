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

package grakn.client.concept;

import grakn.client.GraknClient;
import grakn.client.concept.api.Concept;
import grakn.client.concept.api.ConceptId;
import grakn.client.concept.api.Rule;
import grakn.client.exception.GraknClientException;
import grakn.protocol.session.ConceptProto;
import graql.lang.Graql;
import graql.lang.pattern.Pattern;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nullable;
import java.util.stream.Stream;

/**
 * Client implementation of Rule
 */
public class RuleImpl extends SchemaConceptImpl<Rule> implements Rule {

    RuleImpl(GraknClient.Transaction tx, ConceptId id) {
        super(tx, id);
    }

    @Nullable
    @SuppressWarnings("Duplicates") // response.getResCase() does not return the same type
    public final Pattern when() {
        ConceptProto.Method.Req method = ConceptProto.Method.Req.newBuilder()
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

    @Nullable
    @SuppressWarnings("Duplicates") // response.getResCase() does not return the same type
    public final Pattern then() {
        ConceptProto.Method.Req method = ConceptProto.Method.Req.newBuilder()
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

    @Override
    final Rule asCurrentBaseType(Concept other) {
        return other.asRule();
    }

    @Override
    final boolean equalsCurrentBaseType(Concept other) {
        return other.isRule();
    }

}
