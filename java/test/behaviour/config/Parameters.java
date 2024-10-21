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

package com.typedb.driver.test.behaviour.config;

import com.typedb.driver.api.Transaction;
import io.cucumber.java.DataTableType;
import io.cucumber.java.ParameterType;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static com.typedb.driver.api.Transaction.Type.READ;
import static com.typedb.driver.api.Transaction.Type.SCHEMA;
import static com.typedb.driver.api.Transaction.Type.WRITE;
import static com.typedb.driver.test.behaviour.util.Util.assertThrows;
import static com.typedb.driver.test.behaviour.util.Util.assertThrowsWithMessage;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class Parameters {

    @ParameterType("true|false")
    public Boolean bool(String bool) {
        return Boolean.parseBoolean(bool);
    }

    @ParameterType("[0-9]+")
    public Integer integer(String number) {
        return Integer.parseInt(number);
    }

    @ParameterType("[^;]+")
    public String non_semicolon(String non_semicolon) {
        return non_semicolon;
    }

    @ParameterType("\\d\\d\\d\\d-\\d\\d-\\d\\d \\d\\d:\\d\\d:\\d\\d")
    public LocalDateTime datetime(String dateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return LocalDateTime.parse(dateTime, formatter);
    }

    @ParameterType("concept|variable|type|instance|entity type|relation type|attribute type|role type|entity|relation|attribute|value")
    public ConceptKind concept_kind(String type) {
        return ConceptKind.of(type);
    }

    @ParameterType("([a-zA-Z0-9]*)")
    public String var(String variable) {
        return variable;
    }

    @ParameterType("read|write|schema")
    public Transaction.Type transaction_type(String type) {
        if (type.equals("read")) {
            return READ;
        } else if (type.equals("write")) {
            return WRITE;
        } else if (type.equals("schema")) {
            return SCHEMA;
        }
        return null;
    }

    @DataTableType
    public List<Transaction.Type> transaction_types(List<String> values) {
        List<Transaction.Type> typeList = new ArrayList<>();
        for (String value : values) {
            Transaction.Type type = transaction_type(value);
            assertNotNull(type);
            typeList.add(type);
        }

        return typeList;
    }

    @ParameterType("ok|concept rows|concept documents")
    public QueryAnswerType query_answer_type(String value) {
        return QueryAnswerType.of(value);
    }

    @ParameterType("|; fails|; parsing fails|; fails with a message containing: \".*\"")
    public MayError may_error(String value) {
        if (value.equals("")) {
            return new MayError(false);
        } else if (value.equals("; fails") || value.equals("; parsing fails")) {
            return new MayError(true);
        } else if (value.startsWith("; fails with a message containing:")) {
            String pattern = value.substring("; fails with a message containing: ".length()).replaceAll("^\"|\"$", "");
            return new MayError(true, pattern);
        }
        return null;
    }

    @ParameterType("is|is not")
    public IsOrNot is_or_not(String value) {
        if (value.equals("is")) {
            return IsOrNot.IS;
        } else if (value.equals("is not")) {
            return IsOrNot.IS_NOT;
        }
        return null;
    }

    public enum ConceptKind {
        CONCEPT("concept"),
        TYPE("type"),
        INSTANCE("instance"),
        ENTITY_TYPE("entity type"),
        RELATION_TYPE("relation type"),
        ATTRIBUTE_TYPE("attribute type"),
        ROLE_TYPE("role type"),
        ENTITY("entity"),
        RELATION("relation"),
        ATTRIBUTE("attribute"),
        VALUE("value");

        private final String name;

        ConceptKind(String name) {
            this.name = name;
        }

        public static ConceptKind of(String name) {
            for (ConceptKind t : ConceptKind.values()) {
                if (t.name.equals(name)) {
                    return t;
                }
            }
            return null;
        }

        public String toString() {
            return name;
        }
    }

    public enum QueryAnswerType {
        OK("ok"),
        CONCEPT_ROWS("concept rows"),
        CONCEPT_DOCUMENTS("concept documents");

        private final String name;

        QueryAnswerType(String name) {
            this.name = name;
        }

        public static QueryAnswerType of(String name) {
            for (QueryAnswerType type : QueryAnswerType.values()) {
                if (type.name.equals(name)) {
                    return type;
                }
            }
            return null;
        }

        public String toString() {
            return name;
        }
    }

    public class MayError {
        final boolean mayError;
        final String message;

        public MayError(boolean mayError) {
            this(mayError, "");
        }

        public MayError(boolean mayError, String message) {
            this.mayError = mayError;
            this.message = message;
        }

        public void check(Runnable function) {
            if (mayError) {
                if (message.isEmpty()) {
                    assertThrows(function);
                } else {
                    assertThrowsWithMessage(function, message);
                }
            } else {
                function.run();
            }
        }
    }

    public enum IsOrNot {
        IS(true),
        IS_NOT(false);

        private final boolean is;

        IsOrNot(boolean is) {
            this.is = is;
        }

        public boolean toBoolean() {
            return is;
        }

        public void check(boolean toCheck) {
            assertEquals(is, toCheck);
        }
    }
}
