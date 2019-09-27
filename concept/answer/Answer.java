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

import javax.annotation.CheckReturnValue;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * An object that contains the answer of every Graql Query.
 */
public abstract class Answer {

    /**
     * @return an explanation object indicating how this answer was obtained
     */
    @Nullable
    @CheckReturnValue
    public abstract Explanation explanation();

    /**
     * @return all explanations taking part in the derivation of this answer
     */
    @CheckReturnValue
    public Set<Explanation> explanations() {
        if (this.explanation() == null) return Collections.emptySet();
        Set<Explanation> explanations = new HashSet<>();
        explanations.add(this.explanation());
        this.explanation().getAnswers().forEach(ans -> explanations.addAll(ans.explanations()));
        return explanations;
    }
}
