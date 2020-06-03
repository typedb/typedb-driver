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
import grakn.client.concept.Concept;
import grakn.client.concept.Rule;
import grakn.client.concept.thing.Attribute;
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
import java.util.HashSet;
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

    @Given("graql define without commit")
    public void graql_define_without_commit(String defineQueryStatements) {
        GraqlDefine graqlQuery = Graql.parse(String.join("\n", defineQueryStatements)).asDefine();
        tx.execute(graqlQuery);
    }

    @Given("graql define throws")
    public void graql_define_throws(String defineQueryStatements) {
        boolean threw = false;
        try {
            GraqlDefine graqlQuery = Graql.parse(String.join("\n", defineQueryStatements)).asDefine();
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
            throw new ScenarioDefinitionException("Only match-get and inserted supported for now");
        }
    }

    @Then("answer size is: {number}")
    public void answer_quantity_assertion(int expectedAnswers) {
        assertEquals(expectedAnswers, answers.size());
    }

    @Then("concept identifiers are")
    public void concept_identifiers_are(Map<String, Map<String, String>> identifiers) {
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
                    throw new ScenarioDefinitionException(String.format("Unrecognised identifier check \"%s\"", check));
            }
        }
    }

    @Then("uniquely identify answer concepts")
    public void uniquely_identify_answer_concepts(List<Map<String, String>> answersIdentifiers) {
        assertEquals(
                String.format("The number of identifier entries (rows) should match the number of answers, but found %d identifier entries and %d answers",
                        answersIdentifiers.size(), answers.size()),
                answersIdentifiers.size(), answers.size()
        );

        for (ConceptMap answer : answers) {
            List<Map<String, String>> matchingIdentifiers = new ArrayList<>();

            for (Map<String, String> answerIdentifiers : answersIdentifiers) {

                if (matchAnswer(answerIdentifiers, answer)) {
                    matchingIdentifiers.add(answerIdentifiers);
                }
            }
            assertEquals(
                    String.format("An identifier entry (row) should match 1-to-1 to an answer, but there were %d matching identifier entries for answer with variables %s",
                            matchingIdentifiers.size(), answer.map().keySet().toString()),
                    1, matchingIdentifiers.size()
            );
        }
    }

    private boolean matchAnswer(Map<String, String> answerIdentifiers, ConceptMap answer) {

        if (!(answerIdentifiers).keySet().equals(answer.map().keySet().stream().map(Variable::name).collect(Collectors.toSet()))) {
            return false;
        }

        for (Map.Entry<String, String> entry : answerIdentifiers.entrySet()) {
            String varName = entry.getKey();
            String identifier = entry.getValue();

            if(!identifierChecks.containsKey(identifier)) {
                throw new ScenarioDefinitionException(String.format("Identifier \"%s\" hasn't previously been declared", identifier));
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
            throw new ScenarioDefinitionException(String.format("vars and identifiers do not correspond for explanation entry %d. Found %d vars and %s identifiers", entryId, vars.length, identifiers.length));
        }

        Map<String, String> answerIdentifiers = IntStream.range(0, vars.length).boxed().collect(Collectors.toMap(i -> vars[i], i -> identifiers[i]));

        Optional<ConceptMap> matchingAnswer = answers.stream().filter(answer -> matchAnswer(answerIdentifiers, answer)).findFirst();

        assertTrue(String.format("No answer found for explanation entry %d that satisfies the vars and identifiers given", entryId), matchingAnswer.isPresent());
        ConceptMap answer = matchingAnswer.get();

        String queryWithIds = applyQueryTemplate(explanationEntry.get("pattern"), answer);
        Conjunction<?> queryWithIdsConj = Graql.and(Graql.parsePatternList(queryWithIds));
        assertEquals(
                String.format("Explanation entry %d has an incorrect pattern.\nExpected: %s\nActual: %s", entryId, queryWithIdsConj, answer.queryPattern()),
                queryWithIdsConj, answer.queryPattern()
        );

        String expectedRule = explanationEntry.get("rule");
        boolean hasExplanation = answer.hasExplanation();

        if (expectedRule.equals("lookup")) {

            assertFalse(String.format("Explanation entry %d is declared as a lookup, but an explanation was found", entryId), hasExplanation);

            String[] expectedChildren = {"-"};
            assertArrayEquals(String.format("Explanation entry %d is declared as a lookup, and so it should have no children, indicated as \"-\", but got children %s instead", entryId, Arrays.toString(children)), expectedChildren, children);
        } else {

            Explanation explanation = answer.explanation();
            List<ConceptMap> explAnswers = explanation.getAnswers();

            assertEquals(String.format("Explanation entry %d should have as many children as it has answers. Instead, %d children were declared, and %d answers were found.", entryId, children.length, explAnswers.size()), children.length, explAnswers.size());

            if (expectedRule.equals("join")) {
                assertNull(String.format("Explanation entry %d is declared as a join, and should not have a rule attached, but one was found", entryId), explanation.getRule());
            } else {
                // rule
                Rule.Remote rule = explanation.getRule().asRemote(tx);
                String ruleLabel = rule.label().toString();
                assertEquals(String.format("Incorrect rule label for explanation entry %d with rule %s.\nExpected: %s\nActual: %s", entryId, ruleLabel, expectedRule, ruleLabel), expectedRule, ruleLabel);

                Map<String, String> expectedRuleDefinition = rules.get(expectedRule);
                String when = Objects.requireNonNull(rule.when()).toString();
                assertEquals(String.format("Incorrect rule body (when) for explanation entry %d with rule %s.\nExpected: %s\nActual: %s", entryId, ruleLabel, expectedRuleDefinition.get("when"), when), expectedRuleDefinition.get("when"), when);

                String then = Objects.requireNonNull(rule.then()).toString();
                assertEquals(String.format("Incorrect rule head (then) for explanation entry %d with rule %s.\nExpected: %s\nActual: %s", entryId, ruleLabel, expectedRuleDefinition.get("then"), then), expectedRuleDefinition.get("then"), then);
            }
            for (String child : children) {
                // Recurse
                checkExplanationEntry(explAnswers, explanationTree, Integer.valueOf(child));
            }
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
                throw new ScenarioDefinitionException(String.format("No ID available for template placeholder: %s", matched));
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
            throw new ScenarioDefinitionException("Cannot replace template not based on ID");
        }
    }

    private static class ScenarioDefinitionException extends RuntimeException {
        ScenarioDefinitionException(String message) {
            super(message);
        }
    }

    private interface UniquenessCheck {
        boolean check(Concept<?> concept);
    }

    public static class LabelUniquenessCheck implements UniquenessCheck {

        private final String label;

        LabelUniquenessCheck(String label) {
            this.label = label;
        }

        @Override
        public boolean check(Concept concept) {
            if (concept.isType()) {
                return label.equals(concept.asType().label().toString());
            } else if (concept.isRole()) {
                return label.equals(concept.asRole().label().toString());
            } else if (concept.isRule()) {
                return label.equals(concept.asRule().label().toString());
            } else {
                throw new ScenarioDefinitionException("Concept was checked for label uniqueness, but it is neither a Role nor a Type nor a Rule.");
            }
        }
    }

    public static class AttributeUniquenessCheck {

        protected final String type;
        protected final String value;

        AttributeUniquenessCheck(String typeAndValue) {
            String[] s = typeAndValue.split(":");
            assertEquals(
                    String.format("A check for attribute uniqueness should be given in the format \"type:value\", but received %s", typeAndValue),
                    2, s.length
            );
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
        public boolean check(Concept<?> concept) {
            if(!concept.isThing()) { return false; }

            Set<Attribute.Remote<?>> keys = concept.asThing().asRemote(tx).keys().collect(Collectors.toSet());

            HashMap<String, String> keyMap = new HashMap<>();

            for (Attribute<?> key : keys) {
                keyMap.put(
                        key.type().label().toString(),
                        key.value().toString());
            }
            return value.equals(keyMap.get(type));
        }
    }
}
