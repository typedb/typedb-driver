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
import static org.junit.Assert.assertNotNull;

public class Parameters {

    @ParameterType("true|false")
    public Boolean bool(String bool) {
        return Boolean.parseBoolean(bool);
    }

    @ParameterType("[0-9]+")
    public Integer number(String number) {
        return Integer.parseInt(number);
    }

    @ParameterType("\\d\\d\\d\\d-\\d\\d-\\d\\d \\d\\d:\\d\\d:\\d\\d")
    public LocalDateTime datetime(String dateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return LocalDateTime.parse(dateTime, formatter);
    }

    @ParameterType("entity|attribute|relation|instance")
    public Kind kind(String type) {
        return Kind.of(type);
    }

    @ParameterType("[a-zA-Z0-9-_]+")
    public String type_label(String typeLabel) {
        return typeLabel;
    }

    @ParameterType("\\$([a-zA-Z0-9]+)")
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

    @ParameterType("; fails|; parsing fails|")
    public MayError may_error(String result) {
        if (result.equals("")) {
            return MayError.FALSE;
        } else if (result.equals("; fails") || result.equals("; parsing fails")) {
            return MayError.TRUE;
        }
        return null;
    }

    public enum Kind {
        ENTITY("entity"),
        ATTRIBUTE("attribute"),
        RELATION("relation");

        private final String label;

        Kind(String label) {
            this.label = label;
        }

        public static Kind of(String label) {
            for (Kind t : Kind.values()) {
                if (t.label.equals(label)) {
                    return t;
                }
            }
            return null;
        }

        public String label() {
            return label;
        }
    }

    public enum MayError {
        TRUE(true),
        FALSE(false);

        boolean mayError;

        MayError(boolean mayError) {
            this.mayError = mayError;
        }

        public void check(Runnable function) {
            if (mayError) {
                assertThrows(function);
            } else {
                function.run();
            }
        }
    }
}
