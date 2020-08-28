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

package grakn.client.concept.answer;

import grakn.client.Grakn.Transaction;
import grakn.client.concept.type.Rule;
import grakn.client.concept.type.Type;
import grakn.protocol.AnswerProto;
import grakn.protocol.ConceptProto;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * An Explanation can be retrieved using a specific ConceptMap. The concept map's pattern and variable mapping
 * correspond to a satisified `then` clause of a rule.
 * The corresponding Explanation, contains the concept maps that
 * satisfy the `when` clause of a rule that was satisfied.
 */
public class Explanation {

    private final List<ConceptMap> answers;
    private final Rule.Remote rule;

    public Explanation(List<ConceptMap> ans, @Nullable Rule.Remote rule) {
        this.answers = Collections.unmodifiableList(ans);
        this.rule = rule;
    }

    public static Explanation of(final Transaction tx, final AnswerProto.Explanation.Res res) {
        final List<ConceptMap> answers = new ArrayList<>();
        res.getExplanationList().forEach(explanationMap -> answers.add(ConceptMap.of(tx, explanationMap)));
        final ConceptProto.Type ruleProto = res.getRule();
        final Rule.Remote rule = res.hasRule() ? Type.Remote.of(tx.concepts(), ruleProto).asRule() : null;
        return new Explanation(answers, rule);
    }

    /**
     * @return answers this explanation is dependent on
     */
    @CheckReturnValue
    public List<ConceptMap> getAnswers() { return answers;}

    @CheckReturnValue
    public Rule.Remote getRule() {
        return rule;
    }
}
