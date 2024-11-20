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

import com.typedb.driver.api.QueryType;
import com.typedb.driver.api.Transaction;
import io.cucumber.java.DataTableType;
import io.cucumber.java.ParameterType;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static com.typedb.driver.api.Transaction.Type.READ;
import static com.typedb.driver.api.Transaction.Type.SCHEMA;
import static com.typedb.driver.api.Transaction.Type.WRITE;
import static com.typedb.driver.test.behaviour.util.Util.assertThrows;
import static com.typedb.driver.test.behaviour.util.Util.assertThrowsWithMessage;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
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

    @ParameterType("read|write|schema")
    public QueryType query_type(String type) {
        if (type.equals("read")) {
            return QueryType.READ;
        } else if (type.equals("write")) {
            return QueryType.WRITE;
        } else if (type.equals("schema")) {
            return QueryType.SCHEMA;
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


    @ParameterType("boolean|long|double|decimal|string|date|datetime|datetime-tz|duration|struct")
    public ValueType value_type(String value) {
        return ValueType.of(value);
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

    @ParameterType("contains|does not contain")
    public ContainsOrDoesnt contains_or_doesnt(String value) {
        if (value.equals("contains")) {
            return ContainsOrDoesnt.DOES;
        } else if (value.equals("does not contain")) {
            return ContainsOrDoesnt.DOES_NOT;
        }
        return null;
    }

    @ParameterType("| by index of variable")
    public IsByVarIndex by_index_of_var(String value) {
        if (value.equals(" by index of variable")) {
            return IsByVarIndex.IS;
        } else if (value.equals("")) {
            return IsByVarIndex.IS_NOT;
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
            if (name.equals("variable")) {
                return ConceptKind.CONCEPT;
            }
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

    public enum ValueType {
        BOOLEAN("boolean"),
        LONG("long"),
        DOUBLE("double"),
        DECIMAL("decimal"),
        STRING("string"),
        DATE("date"),
        DATETIME("datetime"),
        DATETIME_TZ("datetime-tz"),
        DURATION("duration"),
        STRUCT("struct");

        private final String name;

        ValueType(String name) {
            this.name = name;
        }

        public static ValueType of(String name) {
            for (ValueType v : ValueType.values()) {
                if (v.name.equals(name)) {
                    return v;
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

        public void compare(Object lhs, Object rhs) {
            if (is) assertEquals(lhs, rhs);
            else assertNotEquals(lhs, rhs);
        }

        public void check(boolean toCheck) {
            assertEquals(is, toCheck);
        }

        public void checkNone(Object object) {
            if (is) assertEquals("expected none", object, Optional.empty());
            else assertNotEquals("expected not none", object, Optional.empty());
        }
    }

    public enum ContainsOrDoesnt {
        DOES(true),
        DOES_NOT(false);

        private final boolean does;

        ContainsOrDoesnt(boolean is) {
            this.does = is;
        }

        public boolean toBoolean() {
            return does;
        }

        public void check(boolean toCheck) {
            assertEquals(does, toCheck);
        }
    }

    public enum IsByVarIndex {
        IS(true),
        IS_NOT(false);

        private final boolean is;

        IsByVarIndex(boolean is) {
            this.is = is;
        }

        public boolean toBoolean() {
            return is;
        }

        public void check(boolean toCheck) {
            assertEquals(is, toCheck);
        }
    }

    public static final List<DateTimeFormatter> DATETIME_TZ_FORMATTERS = Arrays.asList(
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSSZ"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss VV"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSS VV"),
            DateTimeFormatter.ISO_ZONED_DATE_TIME
    );
}
