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

package grakn.client.answer;

import graql.lang.pattern.Pattern;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

/**
 * Reasoner explanation for inferred answers
 */
public class Explanation {

    private final Pattern pattern;
    private final List<ConceptMap> answers;

    public Explanation() {
        this.pattern = null;
        this.answers = Collections.unmodifiableList(Collections.emptyList());
    }

    public Explanation(Pattern pattern, List<ConceptMap> ans) {
        this.pattern = pattern;
        this.answers = Collections.unmodifiableList(ans);
    }

    /**
     * @return query pattern associated with this explanation
     */
    @CheckReturnValue
    @Nullable
    public Pattern getPattern() { return pattern;}

    /**
     * @return answers this explanation is dependent on
     */
    @CheckReturnValue
    public List<ConceptMap> getAnswers() { return answers;}
}
