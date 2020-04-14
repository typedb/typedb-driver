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

package grakn.client.test.behaviour.graql;

import com.google.common.collect.Iterators;
import grakn.client.GraknClient;
import grakn.client.answer.Answer;
import grakn.client.answer.ConceptMap;
import grakn.client.answer.Explanation;
import grakn.client.concept.Attribute;
import grakn.client.concept.Concept;
import grakn.client.concept.Rule;
import grakn.client.test.behaviour.connection.ConnectionSteps;
import graql.lang.Graql;
import graql.lang.pattern.Conjunction;
import graql.lang.query.GraqlDefine;
import graql.lang.query.GraqlGet;
import graql.lang.query.GraqlInsert;
import graql.lang.query.GraqlQuery;
import graql.lang.query.GraqlUndefine;
import graql.lang.statement.Variable;
import io.cucumber.java.After;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class GraqlSteps {

    private static GraknClient.Session session = null;
    private static GraknClient.Transaction tx = null;

    private static List<ConceptMap> answers;
    HashMap<String, UniquenessCheck> identifierChecks = new HashMap<>();
    private Map<String, Map<String, String>> rules;

    @After
    public void close_transaction() {
        tx.close();
    }

    @Given("transaction is initialised")
    public void transaction_is_initialised() {
        session = Iterators.getOnlyElement(ConnectionSteps.sessions.iterator());
        tx = session.transaction().write();
        assertTrue(tx.isOpen());
    }

    @Given("the integrity is validated")
    public void integrity_is_validated(){

        // TODO

    }

    @Given("graql define")
    public void graql_define(String defineQueryStatements) {
        GraqlDefine graqlQuery = Graql.parse(String.join("\n", defineQueryStatements)).asDefine();
        tx.execute(graqlQuery);
        tx.commit();
        tx = session.transaction().write();
    }

    @Given("graql undefine")
    public void graql_undefine(String undefineQueryStatements) {
        GraqlUndefine graqlQuery = Graql.parse(String.join("\n", undefineQueryStatements)).asUndefine();
        tx.execute(graqlQuery);
        tx.commit();
        tx = session.transaction().write();
    }

    @Given("graql undefine throws")
    public void graql_undefine_throws(String undefineQueryStatements) {
        GraqlUndefine graqlQuery = Graql.parse(String.join("\n", undefineQueryStatements)).asUndefine();
        boolean threw = false;
        try {
            tx.execute(graqlQuery);
            tx.commit();
        } catch (RuntimeException e) {
            threw = true;
        } finally {
            tx.close();
            tx = session.transaction().write();
        }

        assertTrue(threw);
    }

    @Given("graql insert")
    public void graql_insert(String insertQueryStatements) {
        GraqlQuery graqlQuery = Graql.parse(String.join("\n", insertQueryStatements));
        tx.execute(graqlQuery);
        tx.commit();
        tx = session.transaction().write();
    }


    @Given("graql insert throws")
    public void graql_insert_throws(String insertQueryStatements) {
        GraqlQuery graqlQuery = Graql.parse(String.join("\n", insertQueryStatements));
        boolean threw = false;
        try {
            tx.execute(graqlQuery);
            tx.commit();
        } catch (RuntimeException e) {
            threw = true;
        } finally {
            tx.close();
            tx = session.transaction().write();
        }
        assertTrue(threw);
    }

    @When("get answers of graql query")
    public void graql_query(String graqlQueryStatements) {
        GraqlQuery graqlQuery = Graql.parse(String.join("\n", graqlQueryStatements));
        if (graqlQuery instanceof GraqlGet) {
            answers = tx.execute(graqlQuery.asGet());
        } else if (graqlQuery instanceof GraqlInsert) {
            answers = tx.execute(graqlQuery.asInsert());
        } else {
            // TODO specialise exception
            throw new RuntimeException("Only match-get and inserted supported for now");
        }
    }

    @Then("answer size is: {number}")
    public void answer_quantity_assertion(int expectedAnswers) {
        assertEquals(expectedAnswers, answers.size());
    }

    @Then("concept identifier symbols are")
    public void concept_identifier_symbols_are(Map<String, Map<String, String>> identifiers) {
        for (Map.Entry<String, Map<String, String>> entry : identifiers.entrySet()) {
            String identifier = entry.getKey();
            String check = entry.getValue().get("check");
            String value = entry.getValue().get("value");

            switch (check) {
                case "key":
                    identifierChecks.put(identifier, new KeyUniquenessCheck(value));
                    break;
                case "value":
                    identifierChecks.put(identifier, new ValueUniquenessCheck(value));
                    break;
                case "label":
                    identifierChecks.put(identifier, new LabelUniquenessCheck(value));
                    break;
                default:
                    throw new RuntimeException(String.format("Unrecognised identifier check \"%s\"", check));
            }
        }
    }

    private interface UniquenessCheck {
        boolean check(Concept concept);
    }

    public static class LabelUniquenessCheck implements UniquenessCheck {

        private final String label;

        LabelUniquenessCheck(String label) {
            this.label = label;
        }

        @Override
        public boolean check(Concept concept) {
            return concept.isType() && label.equals(concept.asType().label().toString());
        }
    }

    public static class AttributeUniquenessCheck {

        protected final String type;
        protected final String value;

        AttributeUniquenessCheck(String typeAndValue) {
            String[] s = typeAndValue.split(":");
            assert 2 == s.length : String.format("A check for attribute uniqueness should be given in the format \"type:value\", but received %s", typeAndValue);
            type = s[0];
            value = s[1];
        }
    }

    public static class ValueUniquenessCheck extends AttributeUniquenessCheck implements UniquenessCheck {
        ValueUniquenessCheck(String typeAndValue) {
            super(typeAndValue);
        }

        public boolean check(Concept concept) {
            return concept.isAttribute()
            && type.equals(concept.asAttribute().type().label().toString())
            && value.equals(concept.asAttribute().value().toString());
        }
    }

    public static class KeyUniquenessCheck extends AttributeUniquenessCheck implements UniquenessCheck {
        KeyUniquenessCheck(String typeAndValue) {
            super(typeAndValue);
        }

        /**
         * Check that the given key is in the concept's keys
         * @param concept to check
         * @return whether the given key matches a key belonging to the concept
         */
        @Override
        public boolean check(Concept concept) {
            if(!concept.isThing()) { return false; }

            Set<Attribute<?>> keys = concept.asThing().keys().collect(Collectors.toSet());
            HashMap<String, String> keyMap = new HashMap<>();

            for (Attribute<?> key : keys) {
                keyMap.put(
                        key.type().label().toString(),
                        key.value().toString());
            }
            return value.equals(keyMap.get(type));
        }
    }

    @Then("uniquely identify answer concepts by symbols")
    public void uniquely_identify_answer_concepts_by_symbols(List<Map<String, String>> answersIdentifiers) {
      assert answersIdentifiers.size() == answers.size() : String.format("The number of identifier entries (rows) should match the number of answers, but found %d identifier entries and %d answers", answersIdentifiers.size(), answers.size());

        for (ConceptMap answer : answers) {
            List<Map<String, String>> matchingIdentifiers = matchingAnswers(answersIdentifiers, answer);
            assert 1 == matchingIdentifiers.size() : String.format("An identifier entry (row) should match 1-to-1 to an answer, but there were %d matching identifier entries for answer with variables %s", matchingIdentifiers.size(), answer.map().keySet().toString());
        }
    }

    private List<Map<String, String>> matchingAnswers(List<Map<String, String>> answersIdentifiers, ConceptMap answer) {
        List<Map<String, String>> matchingIdentifiers = new ArrayList<>();

        for (Map<String, String> answerIdentifiers : answersIdentifiers) {

            if (matchAnswer(answerIdentifiers, answer)) {
                matchingIdentifiers.add(answerIdentifiers);
            }
        }
        return matchingIdentifiers;
    }

    private boolean matchAnswer(Map<String, String> answerIdentifiers, ConceptMap answer) {
        for (Map.Entry<String, String> entry : answerIdentifiers.entrySet()) {
            String varName = entry.getKey();
            String identifier = entry.getValue();

            if(!answer.map().containsKey(new Variable(varName))){
                return false;
            }

            if(!identifierChecks.get(identifier).check(answer.get(varName))) {
                return false;
            }
        }
        return true;
    }

    @Then("rules are")
    public void rules_are(Map<String, Map<String, String>> rules) {
        this.rules = rules;
    }

    @Then("answers contain explanation tree")
    public void answers_contain_explanation_tree(Map<Integer, Map<String, String>> explanationTree) {
        checkExplanationEntry(answers, explanationTree, 0);
    }

    private void checkExplanationEntry(List<ConceptMap> answers, Map<Integer, Map<String, String>> explanationTree, Integer entryId) {
        Map<String, String> explanationEntry = explanationTree.get(entryId);
        String[] vars = explanationEntry.get("vars").split(", ");
        String[] identifiers = explanationEntry.get("identifiers").split(", ");
        String[] children = explanationEntry.get("children").split(", ");

        if (vars.length != identifiers.length) {
            throw new RuntimeException(String.format("vars and identifiers should correspond. Found %d vars and %s identifiers", vars.length, identifiers.length));
        }

        Map<String, String> answerIdentifiers = IntStream.range(0, vars.length).boxed().collect(Collectors.toMap(i -> vars[i], i -> identifiers[i]));

        Optional<ConceptMap> matchingAnswer = answers.stream().filter(answer -> matchAnswer(answerIdentifiers, answer)).findFirst();

        assert matchingAnswer.isPresent() : String.format("No answer found for explanation entry %d that satisfies the vars and identifiers given", entryId);

        ConceptMap answer = matchingAnswer.get();

        String queryWithIds = applyQueryTemplate(explanationEntry.get("pattern"), answer);
        Conjunction<?> queryWithIdsConj = Graql.and(Graql.parsePatternList(queryWithIds));
        assert queryWithIdsConj.equals(answer.queryPattern()) : String.format("Pattern is not as expected for explanation entry %d.\nExpected: %s\nActual: %s", entryId, queryWithIdsConj, answer.queryPattern());
        assert queryWithIdsConj.equals(answer.queryPattern()) : String.format("Pattern is not as expected for explanation entry %d.\nExpected: %s\nActual: %s", entryId, queryWithIdsConj, answer.queryPattern());

        String expectedRule = explanationEntry.get("rule");
        boolean hasExplanation = answer.hasExplanation();

        if (expectedRule.equals("lookup")) {
            assert !hasExplanation : String.format("Explanation entry %d is declared as a lookup, but an explanation was found", entryId);
            String[] expectedChildren = {"-"};
            assert Arrays.equals(expectedChildren, children) : String.format("Explanation entry %d is declared as a lookup, and so it should have no children, indicated as \"-\", but got children %s instead", entryId, Arrays.toString(children));
        } else {

            Explanation explanation = answer.explanation();
            List<ConceptMap> explAnswers = explanation.getAnswers();

            assert children.length == explAnswers.size() : String.format("Explanation entry %d should have as many children as it has answers. Instead, %d children were declared, and %d answers were found.", entryId, children.length, explAnswers.size());

            if (expectedRule.equals("join")) {
                assert explanation.getRule() == null : String.format("Explanation entry %d is declared as a join, and should not have a rule attached, but one was found", entryId);
            } else {
                // rule
                Rule rule = explanation.getRule();
                String ruleLabel = rule.label().toString();
                assert (expectedRule.equals(ruleLabel)) : String.format("Incorrect rule label for explanation entry %d with rule %s.\nExpected: %s\nActual: %s", entryId, ruleLabel, expectedRule, ruleLabel);

                Map<String, String> expectedRuleDefinition = rules.get(expectedRule);
                String when = Objects.requireNonNull(rule.when()).toString();
                assert expectedRuleDefinition.get("when").equals(when) : String.format("Incorrect rule body (when) for explanation entry %d with rule %s.\nExpected: %s\nActual: %s", entryId, ruleLabel, expectedRuleDefinition.get("when"), when);

                String then = Objects.requireNonNull(rule.then()).toString();
                assert expectedRuleDefinition.get("then").equals(then) : String.format("Incorrect rule head (then) for explanation entry %d with rule %s.\nExpected: %s\nActual: %s", entryId, ruleLabel, expectedRuleDefinition.get("then"), then);
            }
            for (String child : children) {
                // Recurse
                checkExplanationEntry(explAnswers, explanationTree, Integer.valueOf(child));
            }
        }
    }

    @Then("answers are labeled") // TODO Update this with the latest structure
    public void answers_satisfy_labels(List<Map<String, String>> conceptLabels) {
        assertEquals(conceptLabels.size(), answers.size());

        for (ConceptMap answer : answers) {

            // convert the concept map into a map from variable to type label
            Map<String, String> answerAsLabels = new HashMap<>();
            answer.map().forEach((var, concept) -> answerAsLabels.put(var.name(), concept.asSchemaConcept().label().toString()));

            int matchingAnswers = 0;
            for (Map<String, String> expectedLabels : conceptLabels) {
                if (expectedLabels.equals(answerAsLabels)) {
                    matchingAnswers++;
                }
            }

            // we expect exactly one matching answer from the expected answer set
            assertEquals(1, matchingAnswers);
        }
    }

    @Then("each answer satisfies")
    public void each_answer_satisfies(String templatedGraqlQuery) {
        String templatedQuery = String.join("\n", templatedGraqlQuery);
        for (ConceptMap answer : answers) {
            String query = applyQueryTemplate(templatedQuery, answer);
            GraqlQuery graqlQuery = Graql.parse(query);
            List<? extends Answer> answers = tx.execute(graqlQuery);
            assertEquals(1, answers.size());
        }
    }

    private String applyQueryTemplate(String template, ConceptMap templateFiller) {
        // find shortest matching strings between <>
        Pattern pattern = Pattern.compile("<.+?>");
        Matcher matcher = pattern.matcher(template);

        StringBuilder builder = new StringBuilder();
        int i = 0;
        while (matcher.find()) {
            String matched = matcher.group(0);
            String requiredVariable = variableFromTemplatePlaceholder(matched.substring(1, matched.length() - 1));

            builder.append(template.substring(i, matcher.start()));
            if (templateFiller.map().containsKey(new Variable(requiredVariable))) {

                Concept concept = templateFiller.get(requiredVariable);
                String conceptId = concept.id().toString();
                builder.append(conceptId);

            } else {
                throw new RuntimeException(String.format("No ID available for template placeholder: %s", matched));
            }
            i = matcher.end();
        }
        builder.append(template.substring(i));
        return builder.toString();
    }

    private String variableFromTemplatePlaceholder(String placeholder) {
        if (placeholder.endsWith(".id")) {
            String stripped = placeholder.replace(".id", "");
            String withoutPrefix = stripped.replace("answer.", "");
            return withoutPrefix;
        } else {
            throw new RuntimeException("Cannot replace template not based on ID");
        }
    }
}
