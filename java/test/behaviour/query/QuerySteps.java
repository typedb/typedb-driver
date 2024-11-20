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
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static com.typedb.driver.api.concept.Concept.DECIMAL_SCALE;
import static com.typedb.driver.test.behaviour.config.Parameters.DATETIME_TZ_FORMATTERS;
import static com.typedb.driver.test.behaviour.connection.ConnectionStepsBase.threadPool;
import static com.typedb.driver.test.behaviour.connection.ConnectionStepsBase.tx;
import static com.typedb.driver.test.behaviour.util.Util.JSONListContains;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class QuerySteps {
    private static QueryAnswer queryAnswer;
    private static List<ConceptRow> collectedRows;
    private static List<JSON> collectedDocuments;
    private static List<CompletableFuture<QueryAnswer>> queryAnswersParallel = null;

    private void clearAnswers() {
        queryAnswer = null;
        if (queryAnswersParallel != null) {
            CompletableFuture.allOf(queryAnswersParallel.toArray(CompletableFuture[]::new)).join();
            queryAnswersParallel = null;
        }

        collectedRows = null;
        collectedDocuments = null;
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

    public List<Concept> getRowGetConcepts(int rowIndex) {
        ConceptRow row = collectedRows.get(rowIndex);
        return row.concepts().collect(Collectors.toList());
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

    public Optional<String> tryGetLabelOfUnwrappedConcept(Concept concept, Parameters.ConceptKind conceptKind) {
        switch (conceptKind) {
            case CONCEPT:
                return concept.tryGetLabel();
            case TYPE:
                return concept.asType().tryGetLabel();
            case INSTANCE:
                return concept.asInstance().tryGetLabel();
            case ENTITY_TYPE:
                return concept.asEntityType().tryGetLabel();
            case RELATION_TYPE:
                return concept.asRelationType().tryGetLabel();
            case ATTRIBUTE_TYPE:
                return concept.asAttributeType().tryGetLabel();
            case ROLE_TYPE:
                return concept.asRoleType().tryGetLabel();
            case ENTITY:
                return concept.asEntity().tryGetLabel();
            case RELATION:
                return concept.asRelation().tryGetLabel();
            case ATTRIBUTE:
                return concept.asAttribute().tryGetLabel();
            case VALUE:
                return concept.asValue().tryGetLabel();
            default:
                throw new AssertionError("Not covered ConceptKind: " + conceptKind);
        }
    }

    public Optional<String> tryGetIIDOfUnwrappedConcept(Concept concept, Parameters.ConceptKind conceptKind) {
        switch (conceptKind) {
            case CONCEPT:
                return concept.tryGetIID();
            case TYPE:
                return concept.asType().tryGetIID();
            case INSTANCE:
                return concept.asInstance().tryGetIID();
            case ENTITY_TYPE:
                return concept.asEntityType().tryGetIID();
            case RELATION_TYPE:
                return concept.asRelationType().tryGetIID();
            case ATTRIBUTE_TYPE:
                return concept.asAttributeType().tryGetIID();
            case ROLE_TYPE:
                return concept.asRoleType().tryGetIID();
            case ENTITY:
                return concept.asEntity().tryGetIID();
            case RELATION:
                return concept.asRelation().tryGetIID();
            case ATTRIBUTE:
                return concept.asAttribute().tryGetIID();
            case VALUE:
                return concept.asValue().tryGetIID();
            default:
                throw new AssertionError("Not covered ConceptKind: " + conceptKind);
        }
    }

    public Optional<String> tryGetValueTypeOfUnwrappedConcept(Concept concept, Parameters.ConceptKind conceptKind) {
        switch (conceptKind) {
            case CONCEPT:
                return concept.tryGetValueType();
            case TYPE:
                return concept.asType().tryGetValueType();
            case INSTANCE:
                return concept.asInstance().tryGetValueType();
            case ENTITY_TYPE:
                return concept.asEntityType().tryGetValueType();
            case RELATION_TYPE:
                return concept.asRelationType().tryGetValueType();
            case ATTRIBUTE_TYPE:
                return concept.asAttributeType().tryGetValueType();
            case ROLE_TYPE:
                return concept.asRoleType().tryGetValueType();
            case ENTITY:
                return concept.asEntity().tryGetValueType();
            case RELATION:
                return concept.asRelation().tryGetValueType();
            case ATTRIBUTE:
                return concept.asAttribute().tryGetValueType();
            case VALUE:
                return concept.asValue().tryGetValueType();
            default:
                throw new AssertionError("Not covered ConceptKind: " + conceptKind);
        }
    }

    public Optional<Value> tryGetValueOfUnwrappedConcept(Concept concept, Parameters.ConceptKind conceptKind) {
        switch (conceptKind) {
            case CONCEPT:
                return concept.tryGetValue();
            case TYPE:
                return concept.asType().tryGetValue();
            case INSTANCE:
                return concept.asInstance().tryGetValue();
            case ENTITY_TYPE:
                return concept.asEntityType().tryGetValue();
            case RELATION_TYPE:
                return concept.asRelationType().tryGetValue();
            case ATTRIBUTE_TYPE:
                return concept.asAttributeType().tryGetValue();
            case ROLE_TYPE:
                return concept.asRoleType().tryGetValue();
            case ENTITY:
                return concept.asEntity().tryGetValue();
            case RELATION:
                return concept.asRelation().tryGetValue();
            case ATTRIBUTE:
                return concept.asAttribute().tryGetValue();
            case VALUE:
                return concept.asValue().tryGetValue();
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
        switch (valueType) {
            case BOOLEAN:
                return concept.isBoolean();
            case LONG:
                return concept.isLong();
            case DOUBLE:
                return concept.isDouble();
            case DECIMAL:
                return concept.isDecimal();
            case STRING:
                return concept.isString();
            case DATE:
                return concept.isDate();
            case DATETIME:
                return concept.isDatetime();
            case DATETIME_TZ:
                return concept.isDatetimeTZ();
            case DURATION:
                return concept.isDuration();
            case STRUCT:
                return concept.isStruct();
            default:
                throw new IllegalStateException("Not covered ValueType: " + valueType);
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
                return new BigDecimal(value).setScale(DECIMAL_SCALE, RoundingMode.UNNECESSARY);
            case STRING:
                return value.substring(1, value.length() - 1).replace("\\\"", "\"");
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
                return value.getBoolean();
            case LONG:
                return value.getLong();
            case DOUBLE:
                return value.getDouble();
            case DECIMAL:
                return value.getDecimal();
            case STRING:
                return value.getString();
            case DATE:
                return value.getDate();
            case DATETIME:
                return value.getDatetime();
            case DATETIME_TZ:
                return value.getDatetimeTZ();
            case DURATION:
                return value.getDuration();
            case STRUCT:
                return value.getStruct().toString(); // compare string representations
            default:
                throw new AssertionError("Not covered ValueType: " + valueType);
        }
    }

    public Object tryGetAsValueType(Concept concept, Parameters.ConceptKind conceptKind, Parameters.ValueType valueType) {
        // Casting without explicit type declaration is enough here, otherwise it's too verbose
        Concept castedConcept;
        switch (conceptKind) {
            case CONCEPT:
                castedConcept = concept;
                break;
            case TYPE:
                castedConcept = concept.asType();
                break;
            case INSTANCE:
                castedConcept = concept.asInstance();
                break;
            case ENTITY_TYPE:
                castedConcept = concept.asEntityType();
                break;
            case RELATION_TYPE:
                castedConcept = concept.asRelationType();
                break;
            case ATTRIBUTE_TYPE:
                castedConcept = concept.asAttributeType();
                break;
            case ROLE_TYPE:
                castedConcept = concept.asRoleType();
                break;
            case ENTITY:
                castedConcept = concept.asEntity();
                break;
            case RELATION:
                castedConcept = concept.asRelation();
                break;
            case ATTRIBUTE:
                castedConcept = concept.asAttribute();
                break;
            case VALUE:
                castedConcept = concept.asValue();
                break;
            default:
                throw new AssertionError("Not covered ConceptKind: " + conceptKind);
        }

        switch (valueType) {
            case BOOLEAN:
                return castedConcept.tryGetBoolean();
            case LONG:
                return castedConcept.tryGetLong();
            case DOUBLE:
                return castedConcept.tryGetDouble();
            case DECIMAL:
                return castedConcept.tryGetDecimal();
            case STRING:
                return castedConcept.tryGetString();
            case DATE:
                return castedConcept.tryGetDate();
            case DATETIME:
                return castedConcept.tryGetDatetime();
            case DATETIME_TZ:
                return castedConcept.tryGetDatetimeTZ();
            case DURATION:
                return castedConcept.tryGetDuration();
            case STRUCT:
                Optional<Map<String, Optional<Value>>> struct = castedConcept.tryGetStruct();
                return struct.isPresent() ? Optional.of(struct.get().toString()) : struct; // compare string representations
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
    public void answer_size_is(int size) {
        collectAnswerIfNeeded();
        int answerSize;
        if (collectedRows != null) {
            answerSize = collectedRows.size();
        } else if (collectedDocuments != null) {
            answerSize = collectedDocuments.size();
        } else {
            throw new AssertionError("Query answer is not collected: the size is NULL");
        }
        assertEquals(String.format("Expected [%d] answers, but got [%d]", size, answerSize), size, answerSize);
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

    @Then("answer get row\\({integer}) query type {is_or_not}: {query_type}")
    public void answer_get_row_query_type_is(int rowIndex, Parameters.IsOrNot isOrNot, QueryType queryType) {
        collectRowsAnswerIfNeeded();
        isOrNot.compare(queryType, collectedRows.get(rowIndex).getQueryType());
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

    @Then("answer get row\\({integer}) get {concept_kind}{by_index_of_var}\\({var}) try get label: {non_semicolon}")
    public void answer_get_row_get_variable_try_get_label(int rowIndex, Parameters.ConceptKind varKind, Parameters.IsByVarIndex isByVarIndex, String var, String label) {
        collectRowsAnswerIfNeeded();
        Concept varConcept = getRowGetConcept(rowIndex, isByVarIndex, var);
        assertEquals(label, tryGetLabelOfUnwrappedConcept(varConcept, varKind).get());
    }

    @Then("answer get row\\({integer}) get {concept_kind}{by_index_of_var}\\({var}) try get label {is_or_not} none")
    public void answer_get_row_get_variable_try_get_label_is_none(int rowIndex, Parameters.ConceptKind varKind, Parameters.IsByVarIndex isByVarIndex, String var, Parameters.IsOrNot isOrNot) {
        collectRowsAnswerIfNeeded();
        Concept varConcept = getRowGetConcept(rowIndex, isByVarIndex, var);
        isOrNot.checkNone(tryGetLabelOfUnwrappedConcept(varConcept, varKind));
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
                varValueType = varConcept.asAttributeType().tryGetValueType().orElseGet(() -> "none");
                break;
            case VALUE:
                varValueType = varConcept.asValue().getType();
                break;
            default:
                throw new AssertionError("ConceptKind does not have a value type: " + varKind);
        }

        assertEquals(valueType, varValueType);
    }

    @Then("answer get row\\({integer}) get {concept_kind}{by_index_of_var}\\({var}) try get value type: {non_semicolon}")
    public void answer_get_row_get_variable_try_get_value_type(int rowIndex, Parameters.ConceptKind varKind, Parameters.IsByVarIndex isByVarIndex, String var, String valueType) {
        collectRowsAnswerIfNeeded();
        Concept varConcept = getRowGetConcept(rowIndex, isByVarIndex, var);
        String varValueType = tryGetValueTypeOfUnwrappedConcept(varConcept, varKind).get();
        assertEquals(valueType, varValueType);
    }

    @Then("answer get row\\({integer}) get {concept_kind}{by_index_of_var}\\({var}) try get value type {is_or_not} none")
    public void answer_get_row_get_variable_try_get_value_type(int rowIndex, Parameters.ConceptKind varKind, Parameters.IsByVarIndex isByVarIndex, String var, Parameters.IsOrNot isOrNot) {
        collectRowsAnswerIfNeeded();
        Concept varConcept = getRowGetConcept(rowIndex, isByVarIndex, var);
        Optional<String> varValueType = tryGetValueTypeOfUnwrappedConcept(varConcept, varKind);
        isOrNot.checkNone(varValueType);
    }

    @Then("answer get row\\({integer}) get {concept_kind}{by_index_of_var}\\({var}) get type get value type: {non_semicolon}")
    public void answer_get_row_get_variable_get_type_get_value_type(int rowIndex, Parameters.ConceptKind varKind, Parameters.IsByVarIndex isByVarIndex, String var, String valueType) {
        collectRowsAnswerIfNeeded();
        Concept varConcept = getRowGetConcept(rowIndex, isByVarIndex, var);

        String varValueType;
        switch (varKind) {
            case ATTRIBUTE:
                varValueType = varConcept.asAttribute().getType().tryGetValueType().orElseGet(() -> "none");
                break;
            default:
                throw new AssertionError("ConceptKind does not have a type with a value type: " + varKind);
        }

        assertEquals(valueType, varValueType);
    }

    @Then("answer get row\\({integer}) get {concept_kind}{by_index_of_var}\\({var}) is {value_type}: {bool}")
    public void answer_get_row_get_variable_is_value_type(int rowIndex, Parameters.ConceptKind varKind, Parameters.IsByVarIndex isByVarIndex, String var, Parameters.ValueType valueType, boolean isValueType) {
        collectRowsAnswerIfNeeded();
        Concept varConcept = getRowGetConcept(rowIndex, isByVarIndex, var);
        assertEquals(isValueType, isConceptValueType(varConcept, varKind, valueType));
    }

    @Then("answer get row\\({integer}) get {concept_kind}{by_index_of_var}\\({var}) {contains_or_doesnt} iid")
    public void answer_get_row_get_variable_get_iid_exists(int rowIndex, Parameters.ConceptKind varKind, Parameters.IsByVarIndex isByVarIndex, String var, Parameters.ContainsOrDoesnt containsOrDoesnt) {
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
        containsOrDoesnt.check(iid != null && !iid.isEmpty());
    }

    @Then("answer get row\\({integer}) get {concept_kind}{by_index_of_var}\\({var}) try get iid {is_or_not} none")
    public void answer_get_row_get_variable_try_get_iid_is_none(int rowIndex, Parameters.ConceptKind varKind, Parameters.IsByVarIndex isByVarIndex, String var, Parameters.IsOrNot isOrNot) {
        collectRowsAnswerIfNeeded();
        Optional<String> iid = tryGetIIDOfUnwrappedConcept(getRowGetConcept(rowIndex, isByVarIndex, var), varKind);
        isOrNot.checkNone(iid);
    }

    @Then("answer get row\\({integer}) get {concept_kind}{by_index_of_var}\\({var}) get {value_type}{may_error}")
    public void answer_get_row_get_variable_get_by_value_type(int rowIndex, Parameters.ConceptKind varKind, Parameters.IsByVarIndex isByVarIndex, String var, Parameters.ValueType valueType, Parameters.MayError mayError) {
        collectRowsAnswerIfNeeded();
        Value varValue = getRowGetValue(rowIndex, varKind, isByVarIndex, var);
        assertNotNull(varValue.get());
        mayError.check(() -> unwrapValueAs(varValue, valueType));
    }

    @Then("answer get row\\({integer}) get {concept_kind}{by_index_of_var}\\({var}) get value {is_or_not}: {non_semicolon}")
    public void answer_get_row_get_variable_get_value_is(int rowIndex, Parameters.ConceptKind varKind, Parameters.IsByVarIndex isByVarIndex, String var, Parameters.IsOrNot isOrNot, String value) {
        collectRowsAnswerIfNeeded();
        Value varValue = getRowGetValue(rowIndex, varKind, isByVarIndex, var);
        Parameters.ValueType valueType = Parameters.ValueType.of(varValue.getType());
        isOrNot.compare(parseExpectedValue(value, valueType == null ? Optional.empty() : Optional.of(valueType)), varValue.get());
    }

    @Then("answer get row\\({integer}) get {concept_kind}{by_index_of_var}\\({var}) get {value_type} {is_or_not}: {non_semicolon}")
    public void answer_get_row_get_variable_get_value_type_is(int rowIndex, Parameters.ConceptKind varKind, Parameters.IsByVarIndex isByVarIndex, String var, Parameters.ValueType valueType, Parameters.IsOrNot isOrNot, String value) {
        collectRowsAnswerIfNeeded();
        Value varValue = getRowGetValue(rowIndex, varKind, isByVarIndex, var);
        isOrNot.compare(parseExpectedValue(value, Optional.of(valueType)), unwrapValueAs(varValue, valueType));
    }

    @Then("answer get row\\({integer}) get {concept_kind}{by_index_of_var}\\({var}) try get {value_type} {is_or_not}: {non_semicolon}")
    public void answer_get_row_get_variable_try_get_value_type_is(int rowIndex, Parameters.ConceptKind varKind, Parameters.IsByVarIndex isByVarIndex, String var, Parameters.ValueType valueType, Parameters.IsOrNot isOrNot, String value) {
        collectRowsAnswerIfNeeded();
        Concept varConcept = getRowGetConcept(rowIndex, isByVarIndex, var);
        isOrNot.compare(Optional.of(parseExpectedValue(value, Optional.of(valueType))), tryGetAsValueType(varConcept, varKind, valueType));
    }

    @Then("answer get row\\({integer}) get {concept_kind}{by_index_of_var}\\({var}) try get {value_type} {is_or_not} none")
    public void answer_get_row_get_variable_try_get_value_type_is_none(int rowIndex, Parameters.ConceptKind varKind, Parameters.IsByVarIndex isByVarIndex, String var, Parameters.ValueType valueType, Parameters.IsOrNot isOrNot) {
        collectRowsAnswerIfNeeded();
        Concept varConcept = getRowGetConcept(rowIndex, isByVarIndex, var);
        isOrNot.checkNone(tryGetAsValueType(varConcept, varKind, valueType));
    }

    @Then("answer get row\\({integer}) get {concept_kind}{by_index_of_var}\\({var}) try get value {is_or_not}: {non_semicolon}")
    public void answer_get_row_get_variable_try_get_value_is(int rowIndex, Parameters.ConceptKind varKind, Parameters.IsByVarIndex isByVarIndex, String var, Parameters.IsOrNot isOrNot, String value) {
        collectRowsAnswerIfNeeded();
        Value varValue = tryGetValueOfUnwrappedConcept(getRowGetConcept(rowIndex, isByVarIndex, var), varKind).get();
        Parameters.ValueType valueType = Parameters.ValueType.of(varValue.getType());
        isOrNot.compare(parseExpectedValue(value, valueType == null ? Optional.empty() : Optional.of(valueType)), varValue.get());
    }

    @Then("answer get row\\({integer}) get {concept_kind}{by_index_of_var}\\({var}) try get value {is_or_not} none")
    public void answer_get_row_get_variable_try_get_value_is_none(int rowIndex, Parameters.ConceptKind varKind, Parameters.IsByVarIndex isByVarIndex, String var, Parameters.IsOrNot isOrNot) {
        collectRowsAnswerIfNeeded();
        Optional<Value> varValue = tryGetValueOfUnwrappedConcept(getRowGetConcept(rowIndex, isByVarIndex, var), varKind);
        isOrNot.checkNone(varValue);
    }

    @Then("answer get row\\({integer}) get value{by_index_of_var}\\({var}) get {is_or_not}: {non_semicolon}")
    public void answer_get_row_get_value_get_is(int rowIndex, Parameters.IsByVarIndex isByVarIndex, String var, Parameters.IsOrNot isOrNot, String value) {
        collectRowsAnswerIfNeeded();
        Value varValue = getRowGetConcept(rowIndex, isByVarIndex, var).asValue();
        Parameters.ValueType valueType = Parameters.ValueType.of(varValue.getType());
        isOrNot.compare(parseExpectedValue(value, valueType == null ? Optional.empty() : Optional.of(valueType)), varValue.get());
    }

    @Then("answer get row\\({integer}) get concepts size is: {integer}")
    public void answer_get_row_get_concepts_size_is(int rowIndex, int size) {
        collectRowsAnswerIfNeeded();
        int conceptsSize = getRowGetConcepts(rowIndex).size();
        assertEquals(String.format("Expected [%d] answers, but got [%d]", size, conceptsSize), size, conceptsSize);
    }

    @Then("answer {contains_or_doesnt} document:")
    public void answer_contains_document(Parameters.ContainsOrDoesnt containsOrDoesnt, String expectedDocument) {
        collectDocumentsAnswerIfNeeded();
        JSON expectedJSON = JSON.parse(expectedDocument);
        containsOrDoesnt.check(JSONListContains(collectedDocuments, expectedJSON));
    }
}
