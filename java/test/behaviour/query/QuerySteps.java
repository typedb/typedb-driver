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

package com.typedb.driver.test.behaviour.query;

import com.typedb.driver.api.QueryType;
import com.typedb.driver.api.answer.ConceptDocumentIterator;
import com.typedb.driver.api.answer.ConceptRow;
import com.typedb.driver.api.answer.ConceptRowIterator;
import com.typedb.driver.api.answer.JSON;
import com.typedb.driver.api.answer.OkQueryAnswer;
import com.typedb.driver.api.answer.QueryAnswer;
import com.typedb.driver.api.concept.Concept;
import com.typedb.driver.api.concept.instance.Attribute;
import com.typedb.driver.api.concept.instance.Entity;
import com.typedb.driver.api.concept.instance.Instance;
import com.typedb.driver.api.concept.instance.Relation;
import com.typedb.driver.api.concept.type.AttributeType;
import com.typedb.driver.api.concept.type.EntityType;
import com.typedb.driver.api.concept.type.RelationType;
import com.typedb.driver.api.concept.type.RoleType;
import com.typedb.driver.api.concept.type.Type;
import com.typedb.driver.api.concept.value.Value;
import com.typedb.driver.common.Duration;
import com.typedb.driver.test.behaviour.config.Parameters;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static com.typedb.driver.test.behaviour.config.Parameters.DATETIME_TZ_FORMATTERS;
import static com.typedb.driver.test.behaviour.connection.ConnectionStepsBase.threadPool;
import static com.typedb.driver.test.behaviour.connection.ConnectionStepsBase.tx;
import static com.typedb.driver.test.behaviour.util.Util.JSONListMatches;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class QuerySteps {
    private static QueryAnswer queryAnswer;
    private static List<ConceptRow> collectedRows;
    private static List<JSON> collectedDocuments;
    private static List<CompletableFuture<QueryAnswer>> queryAnswersParallel = null;
    private static List<JSON> fetchAnswers;

    private void clearAnswers() {
        queryAnswer = null;
        if (queryAnswersParallel != null) {
            CompletableFuture.allOf(queryAnswersParallel.toArray(CompletableFuture[]::new)).join();
            queryAnswersParallel = null;
        }

        collectedRows = null;
        collectedDocuments = null;

        fetchAnswers = null;
    }

    private void collectAnswerIfNeeded() {
        if (collectedRows != null || collectedDocuments != null) {
            return;
        }

        if (queryAnswer.isConceptRows()) {
            collectedRows = queryAnswer.asConceptRows().stream().collect(Collectors.toList());
        } else if (queryAnswer.isConceptDocuments()) {
            collectedDocuments = queryAnswer.asConceptDocuments().stream().collect(Collectors.toList());
        } else {
            throw new AssertionError("Query answer is not collectable");
        }
    }

    private void collectRowsAnswerIfNeeded() {
        collectAnswerIfNeeded();
        assertNotNull("Expected to collect ConceptRows, but the answer is not ConceptRows", collectedRows);
    }

    private void collectDocumentsAnswerIfNeeded() {
        collectAnswerIfNeeded();
        assertNotNull("Expected to collect ConceptDocuments, but the answer is not ConceptDocuments", collectedDocuments);
    }

    public void assertAnswerSize(int expectedAnswers) {
        int answerSize;
        if (collectedRows != null) {
            answerSize = collectedRows.size();
        } else if (collectedDocuments != null) {
            answerSize = collectedDocuments.size();
        } else {
            throw new AssertionError("Query answer is not collected: the size is NULL");
        }
        assertEquals(String.format("Expected [%d] answers, but got [%d]", expectedAnswers, answerSize), expectedAnswers, answerSize);
    }

    public Concept getRowGetConcept(int rowIndex, Parameters.IsByVarIndex isByVarIndex, String var) {
        ConceptRow row = collectedRows.get(rowIndex);
        switch (isByVarIndex) {
            case IS:
                return row.getIndex(row.columnNames().collect(Collectors.toList()).indexOf(var));
            case IS_NOT:
                return row.get(var);
            default:
                throw new AssertionError("Unexpected isByVarIndex: " + isByVarIndex);
        }
    }

    public Value getRowGetValue(int rowIndex, Parameters.ConceptKind varKind, Parameters.IsByVarIndex isByVarIndex, String var) {
        Concept concept = getRowGetConcept(rowIndex, isByVarIndex, var);
        switch (varKind) {
            case ATTRIBUTE:
                return concept.asAttribute().getValue();
            case VALUE:
                return concept.asValue();
            default:
                throw new IllegalStateException("ConceptKind does not have values: " + varKind);
        }
    }

    public boolean isConceptKind(Concept concept, Parameters.ConceptKind checkedKind) {
        switch (checkedKind) {
            case CONCEPT:
                return true;
            case TYPE:
                return concept.isType();
            case INSTANCE:
                return concept.isInstance();
            case ENTITY_TYPE:
                return concept.isEntityType();
            case RELATION_TYPE:
                return concept.isRelationType();
            case ATTRIBUTE_TYPE:
                return concept.isAttributeType();
            case ROLE_TYPE:
                return concept.isRoleType();
            case ENTITY:
                return concept.isEntity();
            case RELATION:
                return concept.isRelation();
            case ATTRIBUTE:
                return concept.isAttribute();
            case VALUE:
                return concept.isValue();
            default:
                throw new AssertionError("Not covered ConceptKind: " + checkedKind);
        }
    }

    // We want to call the checks on specific interfaces, not "Concept"s, so the boilerplate is needed
    public boolean isUnwrappedConceptKind(Concept concept, Parameters.ConceptKind conceptKind, Parameters.ConceptKind checkedKind) {
        switch (conceptKind) {
            case CONCEPT:
                switch (checkedKind) {
                    case CONCEPT:
                        return true;
                    case TYPE:
                        return concept.isType();
                    case INSTANCE:
                        return concept.isInstance();
                    case ENTITY_TYPE:
                        return concept.isEntityType();
                    case RELATION_TYPE:
                        return concept.isRelationType();
                    case ATTRIBUTE_TYPE:
                        return concept.isAttributeType();
                    case ROLE_TYPE:
                        return concept.isRoleType();
                    case ENTITY:
                        return concept.isEntity();
                    case RELATION:
                        return concept.isRelation();
                    case ATTRIBUTE:
                        return concept.isAttribute();
                    case VALUE:
                        return concept.isValue();
                    default:
                        throw new AssertionError("Not covered ConceptKind: " + checkedKind);
                }
            case TYPE:
                Type type = concept.asType();
                switch (checkedKind) {
                    case CONCEPT:
                        return true;
                    case TYPE:
                        return type.isType();
                    case INSTANCE:
                        return type.isInstance();
                    case ENTITY_TYPE:
                        return type.isEntityType();
                    case RELATION_TYPE:
                        return type.isRelationType();
                    case ATTRIBUTE_TYPE:
                        return type.isAttributeType();
                    case ROLE_TYPE:
                        return type.isRoleType();
                    case ENTITY:
                        return type.isEntity();
                    case RELATION:
                        return type.isRelation();
                    case ATTRIBUTE:
                        return type.isAttribute();
                    case VALUE:
                        return type.isValue();
                    default:
                        throw new AssertionError("Not covered ConceptKind: " + checkedKind);
                }
            case INSTANCE:
                Instance instance = concept.asInstance();
                switch (checkedKind) {
                    case CONCEPT:
                        return true;
                    case TYPE:
                        return instance.isType();
                    case INSTANCE:
                        return instance.isInstance();
                    case ENTITY_TYPE:
                        return instance.isEntityType();
                    case RELATION_TYPE:
                        return instance.isRelationType();
                    case ATTRIBUTE_TYPE:
                        return instance.isAttributeType();
                    case ROLE_TYPE:
                        return instance.isRoleType();
                    case ENTITY:
                        return instance.isEntity();
                    case RELATION:
                        return instance.isRelation();
                    case ATTRIBUTE:
                        return instance.isAttribute();
                    case VALUE:
                        return instance.isValue();
                    default:
                        throw new AssertionError("Not covered ConceptKind: " + checkedKind);
                }
            case ENTITY_TYPE:
                EntityType entityType = concept.asEntityType();
                switch (checkedKind) {
                    case CONCEPT:
                        return true;
                    case TYPE:
                        return entityType.isType();
                    case INSTANCE:
                        return entityType.isInstance();
                    case ENTITY_TYPE:
                        return entityType.isEntityType();
                    case RELATION_TYPE:
                        return entityType.isRelationType();
                    case ATTRIBUTE_TYPE:
                        return entityType.isAttributeType();
                    case ROLE_TYPE:
                        return entityType.isRoleType();
                    case ENTITY:
                        return entityType.isEntity();
                    case RELATION:
                        return entityType.isRelation();
                    case ATTRIBUTE:
                        return entityType.isAttribute();
                    case VALUE:
                        return entityType.isValue();
                    default:
                        throw new AssertionError("Not covered ConceptKind: " + checkedKind);
                }
            case RELATION_TYPE:
                RelationType relationType = concept.asRelationType();
                switch (checkedKind) {
                    case CONCEPT:
                        return true;
                    case TYPE:
                        return relationType.isType();
                    case INSTANCE:
                        return relationType.isInstance();
                    case ENTITY_TYPE:
                        return relationType.isEntityType();
                    case RELATION_TYPE:
                        return relationType.isRelationType();
                    case ATTRIBUTE_TYPE:
                        return relationType.isAttributeType();
                    case ROLE_TYPE:
                        return relationType.isRoleType();
                    case ENTITY:
                        return relationType.isEntity();
                    case RELATION:
                        return relationType.isRelation();
                    case ATTRIBUTE:
                        return relationType.isAttribute();
                    case VALUE:
                        return relationType.isValue();
                    default:
                        throw new AssertionError("Not covered ConceptKind: " + checkedKind);
                }
            case ATTRIBUTE_TYPE:
                AttributeType attributeType = concept.asAttributeType();
                switch (checkedKind) {
                    case CONCEPT:
                        return true;
                    case TYPE:
                        return attributeType.isType();
                    case INSTANCE:
                        return attributeType.isInstance();
                    case ENTITY_TYPE:
                        return attributeType.isEntityType();
                    case RELATION_TYPE:
                        return attributeType.isRelationType();
                    case ATTRIBUTE_TYPE:
                        return attributeType.isAttributeType();
                    case ROLE_TYPE:
                        return attributeType.isRoleType();
                    case ENTITY:
                        return attributeType.isEntity();
                    case RELATION:
                        return attributeType.isRelation();
                    case ATTRIBUTE:
                        return attributeType.isAttribute();
                    case VALUE:
                        return attributeType.isValue();
                    default:
                        throw new AssertionError("Not covered ConceptKind: " + checkedKind);
                }
            case ROLE_TYPE:
                RoleType roleType = concept.asRoleType();
                switch (checkedKind) {
                    case CONCEPT:
                        return true;
                    case TYPE:
                        return roleType.isType();
                    case INSTANCE:
                        return roleType.isInstance();
                    case ENTITY_TYPE:
                        return roleType.isEntityType();
                    case RELATION_TYPE:
                        return roleType.isRelationType();
                    case ATTRIBUTE_TYPE:
                        return roleType.isAttributeType();
                    case ROLE_TYPE:
                        return roleType.isRoleType();
                    case ENTITY:
                        return roleType.isEntity();
                    case RELATION:
                        return roleType.isRelation();
                    case ATTRIBUTE:
                        return roleType.isAttribute();
                    case VALUE:
                        return roleType.isValue();
                    default:
                        throw new AssertionError("Not covered ConceptKind: " + checkedKind);
                }
            case ENTITY:
                Entity entity = concept.asEntity();
                switch (checkedKind) {
                    case CONCEPT:
                        return true;
                    case TYPE:
                        return entity.isType();
                    case INSTANCE:
                        return entity.isInstance();
                    case ENTITY_TYPE:
                        return entity.isEntityType();
                    case RELATION_TYPE:
                        return entity.isRelationType();
                    case ATTRIBUTE_TYPE:
                        return entity.isAttributeType();
                    case ROLE_TYPE:
                        return entity.isRoleType();
                    case ENTITY:
                        return entity.isEntity();
                    case RELATION:
                        return entity.isRelation();
                    case ATTRIBUTE:
                        return entity.isAttribute();
                    case VALUE:
                        return entity.isValue();
                    default:
                        throw new AssertionError("Not covered ConceptKind: " + checkedKind);
                }
            case RELATION:
                Relation relation = concept.asRelation();
                switch (checkedKind) {
                    case CONCEPT:
                        return true;
                    case TYPE:
                        return relation.isType();
                    case INSTANCE:
                        return relation.isInstance();
                    case ENTITY_TYPE:
                        return relation.isEntityType();
                    case RELATION_TYPE:
                        return relation.isRelationType();
                    case ATTRIBUTE_TYPE:
                        return relation.isAttributeType();
                    case ROLE_TYPE:
                        return relation.isRoleType();
                    case ENTITY:
                        return relation.isEntity();
                    case RELATION:
                        return relation.isRelation();
                    case ATTRIBUTE:
                        return relation.isAttribute();
                    case VALUE:
                        return relation.isValue();
                    default:
                        throw new AssertionError("Not covered ConceptKind: " + checkedKind);
                }
            case ATTRIBUTE:
                Attribute attribute = concept.asAttribute();
                switch (checkedKind) {
                    case CONCEPT:
                        return true;
                    case TYPE:
                        return attribute.isType();
                    case INSTANCE:
                        return attribute.isInstance();
                    case ENTITY_TYPE:
                        return attribute.isEntityType();
                    case RELATION_TYPE:
                        return attribute.isRelationType();
                    case ATTRIBUTE_TYPE:
                        return attribute.isAttributeType();
                    case ROLE_TYPE:
                        return attribute.isRoleType();
                    case ENTITY:
                        return attribute.isEntity();
                    case RELATION:
                        return attribute.isRelation();
                    case ATTRIBUTE:
                        return attribute.isAttribute();
                    case VALUE:
                        return attribute.isValue();
                    default:
                        throw new AssertionError("Not covered ConceptKind: " + checkedKind);
                }
            case VALUE:
                Value value = concept.asValue();
                switch (checkedKind) {
                    case CONCEPT:
                        return true;
                    case TYPE:
                        return value.isType();
                    case INSTANCE:
                        return value.isInstance();
                    case ENTITY_TYPE:
                        return value.isEntityType();
                    case RELATION_TYPE:
                        return value.isRelationType();
                    case ATTRIBUTE_TYPE:
                        return value.isAttributeType();
                    case ROLE_TYPE:
                        return value.isRoleType();
                    case ENTITY:
                        return value.isEntity();
                    case RELATION:
                        return value.isRelation();
                    case ATTRIBUTE:
                        return value.isAttribute();
                    case VALUE:
                        return value.isValue();
                    default:
                        throw new AssertionError("Not covered ConceptKind: " + checkedKind);
                }
            default:
                throw new AssertionError("Not covered ConceptKind: " + conceptKind);
        }
    }

    public void unwrapConceptAs(Concept concept, Parameters.ConceptKind conceptKind) {
        switch (conceptKind) {
            case CONCEPT:
                break;
            case TYPE:
                assertNotNull(concept.asType());
                break;
            case INSTANCE:
                assertNotNull(concept.asInstance());
                break;
            case ENTITY_TYPE:
                assertNotNull(concept.asEntityType());
                break;
            case RELATION_TYPE:
                assertNotNull(concept.asRelationType());
                break;
            case ATTRIBUTE_TYPE:
                assertNotNull(concept.asAttributeType());
                break;
            case ROLE_TYPE:
                assertNotNull(concept.asRoleType());
                break;
            case ENTITY:
                assertNotNull(concept.asEntity());
                break;
            case RELATION:
                assertNotNull(concept.asRelation());
                break;
            case ATTRIBUTE:
                assertNotNull(concept.asAttribute());
                break;
            case VALUE:
                assertNotNull(concept.asValue());
                break;
            default:
                throw new AssertionError("Not covered ConceptKind: " + conceptKind);
        }
    }

    public String getLabelOfUnwrappedConcept(Concept concept, Parameters.ConceptKind conceptKind) {
        switch (conceptKind) {
            case CONCEPT:
                return concept.getLabel();
            case TYPE:
                return concept.asType().getLabel();
            case INSTANCE:
                return concept.asInstance().getLabel();
            case ENTITY_TYPE:
                return concept.asEntityType().getLabel();
            case RELATION_TYPE:
                return concept.asRelationType().getLabel();
            case ATTRIBUTE_TYPE:
                return concept.asAttributeType().getLabel();
            case ROLE_TYPE:
                return concept.asRoleType().getLabel();
            case ENTITY:
                return concept.asEntity().getLabel();
            case RELATION:
                return concept.asRelation().getLabel();
            case ATTRIBUTE:
                return concept.asAttribute().getLabel();
            case VALUE:
                return concept.asValue().getLabel();
            default:
                throw new AssertionError("Not covered ConceptKind: " + conceptKind);
        }
    }

    public String getLabelOfUnwrappedConceptsType(Concept concept, Parameters.ConceptKind conceptKind) {
        switch (conceptKind) {
            case INSTANCE:
                return concept.asInstance().getType().getLabel();
            case ENTITY:
                return concept.asEntity().getType().getLabel();
            case RELATION:
                return concept.asRelation().getType().getLabel();
            case ATTRIBUTE:
                return concept.asAttribute().getType().getLabel();
            default:
                throw new AssertionError("ConceptKind does not have a type: " + conceptKind);
        }
    }

    private boolean isConceptValueType(Concept concept, Parameters.ConceptKind varKind, Parameters.ValueType valueType) {
        switch (varKind) {
            case ATTRIBUTE_TYPE:
                AttributeType varAttributeType = concept.asAttributeType();
                switch (valueType) {
                    case BOOLEAN:
                        return varAttributeType.isBoolean();
                    case LONG:
                        return varAttributeType.isLong();
                    case DOUBLE:
                        return varAttributeType.isDouble();
                    case DECIMAL:
                        return varAttributeType.isDecimal();
                    case STRING:
                        return varAttributeType.isString();
                    case DATE:
                        return varAttributeType.isDate();
                    case DATETIME:
                        return varAttributeType.isDatetime();
                    case DATETIME_TZ:
                        return varAttributeType.isDatetimeTZ();
                    case DURATION:
                        return varAttributeType.isDuration();
                    case STRUCT:
                        return varAttributeType.isStruct();
                    default:
                        throw new IllegalStateException("Not covered ValueType: " + valueType);
                }
            case ATTRIBUTE:
                Attribute varAttribute = concept.asAttribute();
                switch (valueType) {
                    case BOOLEAN:
                        return varAttribute.isBoolean();
                    case LONG:
                        return varAttribute.isLong();
                    case DOUBLE:
                        return varAttribute.isDouble();
                    case DECIMAL:
                        return varAttribute.isDecimal();
                    case STRING:
                        return varAttribute.isString();
                    case DATE:
                        return varAttribute.isDate();
                    case DATETIME:
                        return varAttribute.isDatetime();
                    case DATETIME_TZ:
                        return varAttribute.isDatetimeTZ();
                    case DURATION:
                        return varAttribute.isDuration();
                    case STRUCT:
                        return varAttribute.isStruct();
                    default:
                        throw new IllegalStateException("Not covered ValueType: " + valueType);
                }
            case VALUE:
                Value varValue = concept.asValue();
                switch (valueType) {
                    case BOOLEAN:
                        return varValue.isBoolean();
                    case LONG:
                        return varValue.isLong();
                    case DOUBLE:
                        return varValue.isDouble();
                    case DECIMAL:
                        return varValue.isDecimal();
                    case STRING:
                        return varValue.isString();
                    case DATE:
                        return varValue.isDate();
                    case DATETIME:
                        return varValue.isDatetime();
                    case DATETIME_TZ:
                        return varValue.isDatetimeTZ();
                    case DURATION:
                        return varValue.isDuration();
                    case STRUCT:
                        return varValue.isStruct();
                    default:
                        throw new IllegalStateException("Not covered ValueType: " + valueType);
                }
            default:
                throw new AssertionError("ConceptKind does not have a value or a value type: " + varKind);
        }
    }

    public Object parseExpectedValue(String value, Optional<Parameters.ValueType> valueTypeOpt) {
        Parameters.ValueType valueType;
        valueType = valueTypeOpt.orElse(Parameters.ValueType.STRUCT);

        switch (valueType) {
            case BOOLEAN:
                return Boolean.parseBoolean(value);
            case LONG:
                return Long.parseLong(value);
            case DOUBLE:
                return Double.parseDouble(value);
            case DECIMAL:
                return new BigDecimal(value);
            case STRING:
                return value.substring(1, value.length() - 1).replaceAll("\\\"", "\"");
            case DATE:
                return LocalDate.parse(value);
            case DATETIME:
                return LocalDateTime.parse(value);
            case DATETIME_TZ:
                for (DateTimeFormatter formatter : DATETIME_TZ_FORMATTERS) {
                    try {
                        return ZonedDateTime.parse(value, formatter);
                    } catch (DateTimeParseException e) {
                        // Continue to the next formatter if parsing fails
                    }
                }
                throw new AssertionError("DatetimeTZ format is not supported");
            case DURATION:
                return Duration.parse(value);
            case STRUCT:
                return value; // compare string representations
            default:
                throw new AssertionError("Not covered ValueType: " + valueType);
        }
    }

    public Object unwrapValueAs(Value value, Parameters.ValueType valueType) {
        switch (valueType) {
            case BOOLEAN:
                return value.asBoolean();
            case LONG:
                return value.asLong();
            case DOUBLE:
                return value.asDouble();
            case DECIMAL:
                return value.asDecimal();
            case STRING:
                return value.asString();
            case DATE:
                return value.asDate();
            case DATETIME:
                return value.asDatetime();
            case DATETIME_TZ:
                return value.asDatetimeTZ();
            case DURATION:
                return value.asDuration();
            case STRUCT:
                return value.asStruct().toString(); // compare string representations
            default:
                throw new AssertionError("Not covered ValueType: " + valueType);
        }
    }

    @Given("typeql write query{may_error}")
    @Given("typeql read query{may_error}")
    @Given("typeql schema query{may_error}")
    public void typeql_query(Parameters.MayError mayError, String query) {
        clearAnswers();
        mayError.check(() -> tx().query(query).resolve());
    }

    @Given("get answers of typeql write query{may_error}")
    @Given("get answers of typeql read query{may_error}")
    @Given("get answers of typeql schema query{may_error}")
    public void get_answers_of_typeql_query(Parameters.MayError mayError, String query) {
        clearAnswers();
        mayError.check(() -> queryAnswer = tx().query(query).resolve());
    }

    @Given("concurrently get answers of typeql write query {integer} times")
    @Given("concurrently get answers of typeql read query {integer} times")
    @Given("concurrently get answers of typeql schema query {integer} times")
    public void concurrently_get_answers_of_typeql_query_count_times(int count, String query) {
        clearAnswers();
        queryAnswersParallel = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            queryAnswersParallel.add(CompletableFuture.supplyAsync(() -> tx().query(query).resolve(), threadPool));
        }
    }

    @Then("answer type {is_or_not}: {query_answer_type}")
    public void answer_type_is(Parameters.IsOrNot isOrNot, Parameters.QueryAnswerType queryAnswerType) {
        switch (queryAnswerType) {
            case OK:
                isOrNot.check(queryAnswer.isOk());
                break;
            case CONCEPT_ROWS:
                isOrNot.check(queryAnswer.isConceptRows());
                break;
            case CONCEPT_DOCUMENTS:
                isOrNot.check(queryAnswer.isConceptDocuments());
                break;
            default:
                throw new AssertionError("Unknown query answer type: " + queryAnswerType);
        }
    }

    @Then("answer unwraps as {query_answer_type}{may_error}")
    public void answer_unwraps_as(Parameters.QueryAnswerType queryAnswerType, Parameters.MayError mayError) {
        mayError.check(() -> {
            switch (queryAnswerType) {
                case OK:
                    OkQueryAnswer _ok = queryAnswer.asOk();
                    break;
                case CONCEPT_ROWS:
                    ConceptRowIterator _rows = queryAnswer.asConceptRows();
                    break;
                case CONCEPT_DOCUMENTS:
                    ConceptDocumentIterator _documents = queryAnswer.asConceptDocuments();
                    break;
                default:
                    throw new AssertionError("Unknown query answer type: " + queryAnswerType);
            }
        });
    }

    @Then("answer query type {is_or_not}: {query_type}")
    public void answer_type_is(Parameters.IsOrNot isOrNot, QueryType queryType) {
        isOrNot.compare(queryType, queryAnswer.getQueryType());
    }

    @Then("answer size is: {integer}")
    public void answer_size_is(int expectedAnswers) {
        collectAnswerIfNeeded();
        assertAnswerSize(expectedAnswers);
    }

    @Then("answer column names are:")
    public void answer_column_names_are(List<String> names) {
        collectAnswerIfNeeded();
        List<String> answerColumnNames = collectedRows.get(0).columnNames().sorted().collect(Collectors.toList());
        assertEquals(names.stream().sorted().collect(Collectors.toList()), answerColumnNames);
    }

    @Given("concurrently process {integer} row(s) from answers{may_error}")
    public void concurrently_process_count_rows_from_answers(int count, Parameters.MayError mayError) {
        queryAnswersParallel = new ArrayList<>();

        List<CompletableFuture<Object>> jobs = queryAnswersParallel.stream()
                .map(futureAnswer -> futureAnswer.thenComposeAsync(answer -> {
                    ConceptRowIterator iterator = answer.asConceptRows();
                    List<ConceptRow> rows = new ArrayList<>();

                    for (int i = 0; i < count; i++) {
                        try {
                            rows.add(iterator.next());
                        } catch (NoSuchElementException e) {
                            mayError.check(() -> {
                                throw e;
                            });
                        }
                    }

                    return null;
                }, threadPool))
                .collect(Collectors.toList());

        CompletableFuture.allOf(jobs.toArray(new CompletableFuture[0])).join();
    }

    @Then("answer get row\\({integer}) get variable{by_index_of_var}\\({var}){may_error}")
    public void answer_get_row_get_variable_is_kind(int rowIndex, Parameters.IsByVarIndex isByVarIndex, String var, Parameters.MayError mayError) {
        collectRowsAnswerIfNeeded();
        mayError.check(() -> getRowGetConcept(rowIndex, isByVarIndex, var));
    }

    @Then("answer get row\\({integer}) get variable{by_index_of_var}\\({var}) as {concept_kind}{may_error}")
    public void answer_get_row_get_variable_is_kind(int rowIndex, Parameters.IsByVarIndex isByVarIndex, String var, Parameters.ConceptKind varKind, Parameters.MayError mayError) {
        collectRowsAnswerIfNeeded();
        Concept varConcept = getRowGetConcept(rowIndex, isByVarIndex, var);
        mayError.check(() -> unwrapConceptAs(varConcept, varKind));
    }

    @Then("answer get row\\({integer}) get {concept_kind}{by_index_of_var}\\({var}) is {concept_kind}: {bool}")
    public void answer_get_row_get_variable_is_kind(int rowIndex, Parameters.ConceptKind varKind, Parameters.IsByVarIndex isByVarIndex, String var, Parameters.ConceptKind checkedKind, boolean isCheckedKind) {
        collectRowsAnswerIfNeeded();
        Concept varConcept = getRowGetConcept(rowIndex, isByVarIndex, var);
        assertEquals(isCheckedKind, isUnwrappedConceptKind(varConcept, varKind, checkedKind));
    }

    @Then("answer get row\\({integer}) get {concept_kind}{by_index_of_var}\\({var}) get type is {concept_kind}: {bool}")
    public void answer_get_row_get_variable_get_type_is_kind(int rowIndex, Parameters.ConceptKind varKind, Parameters.IsByVarIndex isByVarIndex, String var, Parameters.ConceptKind checkedKind, boolean isCheckedKind) {
        collectRowsAnswerIfNeeded();
        Concept varConcept = getRowGetConcept(rowIndex, isByVarIndex, var);
        boolean isTypeKind;
        switch (varKind) {
            case INSTANCE:
                isTypeKind = isConceptKind(varConcept.asInstance().getType(), checkedKind);
                break;
            case ENTITY:
                isTypeKind = isConceptKind(varConcept.asEntity().getType(), checkedKind);
                break;
            case RELATION:
                isTypeKind = isConceptKind(varConcept.asRelation().getType(), checkedKind);
                break;
            case ATTRIBUTE:
                isTypeKind = isConceptKind(varConcept.asAttribute().getType(), checkedKind);
                break;
            default:
                throw new AssertionError("ConceptKind does not have a type: " + varKind);
        }
        assertEquals(isCheckedKind, isTypeKind);
    }

    @Then("answer get row\\({integer}) get {concept_kind}{by_index_of_var}\\({var}) get label: {non_semicolon}")
    public void answer_get_row_get_variable_get_label(int rowIndex, Parameters.ConceptKind varKind, Parameters.IsByVarIndex isByVarIndex, String var, String label) {
        collectRowsAnswerIfNeeded();
        Concept varConcept = getRowGetConcept(rowIndex, isByVarIndex, var);
        assertEquals(label, getLabelOfUnwrappedConcept(varConcept, varKind));
    }

    @Then("answer get row\\({integer}) get {concept_kind}{by_index_of_var}\\({var}) get type get label: {non_semicolon}")
    public void answer_get_row_get_variable_get_type_get_label(int rowIndex, Parameters.ConceptKind varKind, Parameters.IsByVarIndex isByVarIndex, String var, String label) {
        collectRowsAnswerIfNeeded();
        Concept varConcept = getRowGetConcept(rowIndex, isByVarIndex, var);
        assertEquals(label, getLabelOfUnwrappedConceptsType(varConcept, varKind));
    }

    @Then("answer get row\\({integer}) get {concept_kind}{by_index_of_var}\\({var}) get value type: {non_semicolon}")
    public void answer_get_row_get_variable_get_value_type(int rowIndex, Parameters.ConceptKind varKind, Parameters.IsByVarIndex isByVarIndex, String var, String valueType) {
        collectRowsAnswerIfNeeded();
        Concept varConcept = getRowGetConcept(rowIndex, isByVarIndex, var);

        String varValueType;
        switch (varKind) {
            case ATTRIBUTE_TYPE:
                varValueType = varConcept.asAttributeType().getValueType();
                break;
            case VALUE:
                varValueType = varConcept.asValue().getType();
                break;
            default:
                throw new AssertionError("ConceptKind does not have a value type: " + varKind);
        }

        assertEquals(valueType, varValueType);
    }

    @Then("answer get row\\({integer}) get {concept_kind}{by_index_of_var}\\({var}) get type get value type: {non_semicolon}")
    public void answer_get_row_get_variable_get_type_get_value_type(int rowIndex, Parameters.ConceptKind varKind, Parameters.IsByVarIndex isByVarIndex, String var, String valueType) {
        collectRowsAnswerIfNeeded();
        Concept varConcept = getRowGetConcept(rowIndex, isByVarIndex, var);

        String varValueType;
        switch (varKind) {
            case ATTRIBUTE:
                varValueType = varConcept.asAttribute().getType().getValueType();
                break;
            default:
                throw new AssertionError("ConceptKind does not have a type with a value type: " + varKind);
        }

        assertEquals(valueType, varValueType);
    }

    @Then("answer get row\\({integer}) get attribute type{by_index_of_var}\\({var}) is untyped: {bool}")
    public void answer_get_row_get_attribute_type_is_untyped(int rowIndex, Parameters.IsByVarIndex isByVarIndex, String var, boolean isUntyped) {
        collectRowsAnswerIfNeeded();
        Concept varConcept = getRowGetConcept(rowIndex, isByVarIndex, var);
        assertEquals(isUntyped, varConcept.asAttributeType().isUntyped());
    }

    @Then("answer get row\\({integer}) get {concept_kind}{by_index_of_var}\\({var}) is {value_type}: {bool}")
    public void answer_get_row_get_variable_is_value_type(int rowIndex, Parameters.ConceptKind varKind, Parameters.IsByVarIndex isByVarIndex, String var, Parameters.ValueType valueType, boolean isValueType) {
        collectRowsAnswerIfNeeded();
        Concept varConcept = getRowGetConcept(rowIndex, isByVarIndex, var);
        assertEquals(isValueType, isConceptValueType(varConcept, varKind, valueType));
    }

    @Then("answer get row\\({integer}) get {concept_kind}{by_index_of_var}\\({var}) get iid {exists_or_doesnt}")
    public void answer_get_row_get_variable_get_iid_exists(int rowIndex, Parameters.ConceptKind varKind, Parameters.IsByVarIndex isByVarIndex, String var, Parameters.ExistsOrDoesnt existsOrDoesnt) {
        collectRowsAnswerIfNeeded();
        Concept varConcept = getRowGetConcept(rowIndex, isByVarIndex, var);
        String iid;
        switch (varKind) {
            case ENTITY:
                iid = varConcept.asEntity().getIID();
                break;
            case RELATION:
                iid = varConcept.asRelation().getIID();
                break;
            default:
                throw new IllegalStateException("ConceptKind does not have iids: " + varKind);
        }
        existsOrDoesnt.check(iid != null && !iid.isEmpty());
    }

    @Then("answer get row\\({integer}) get {concept_kind}{by_index_of_var}\\({var}) get value {is_or_not}: {non_semicolon}")
    public void answer_get_row_get_variable_get_value_is(int rowIndex, Parameters.ConceptKind varKind, Parameters.IsByVarIndex isByVarIndex, String var, Parameters.IsOrNot isOrNot, String value) {
        collectRowsAnswerIfNeeded();
        Value varValue = getRowGetValue(rowIndex, varKind, isByVarIndex, var);
        Parameters.ValueType valueType = Parameters.ValueType.of(varValue.getType());
        isOrNot.compare(parseExpectedValue(value, valueType == null ? Optional.empty() : Optional.of(valueType)), varValue.asUntyped());
    }

    @Then("answer get row\\({integer}) get {concept_kind}{by_index_of_var}\\({var}) as {value_type}{may_error}")
    public void answer_get_row_get_variable_as_value_type(int rowIndex, Parameters.ConceptKind varKind, Parameters.IsByVarIndex isByVarIndex, String var, Parameters.ValueType valueType, Parameters.MayError mayError) {
        collectRowsAnswerIfNeeded();
        Value varValue = getRowGetValue(rowIndex, varKind, isByVarIndex, var);
        assertNotNull(varValue.asUntyped());
        mayError.check(() -> unwrapValueAs(varValue, valueType));
    }

    @Then("answer get row\\({integer}) get {concept_kind}{by_index_of_var}\\({var}) as {value_type} {is_or_not}: {non_semicolon}")
    public void answer_get_row_get_variable_as_value_type_is(int rowIndex, Parameters.ConceptKind varKind, Parameters.IsByVarIndex isByVarIndex, String var, Parameters.ValueType valueType, Parameters.IsOrNot isOrNot, String value) {
        collectRowsAnswerIfNeeded();
        Value varValue = getRowGetValue(rowIndex, varKind, isByVarIndex, var);
        isOrNot.compare(parseExpectedValue(value, Optional.of(valueType)), unwrapValueAs(varValue, valueType));
    }

    // TODO: Refactor
    @Then("fetch answers are")
    public void fetch_answers_are(String expectedJSON) {
        JSON expected = JSON.parse(expectedJSON);
        assertTrue("Fetch response is a list of JSON objects, but the behaviour test expects something else", expected.isArray());
        assertTrue(JSONListMatches(fetchAnswers, expected.asArray()));
    }
}
