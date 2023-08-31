/*
 * Copyright (C) 2022 Vaticle
 *
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

package com.vaticle.typedb.client.test.behaviour.config;

import com.vaticle.typedb.client.api.TypeDBTransaction;
import com.vaticle.typedb.client.api.concept.type.ThingType.Annotation;
import com.vaticle.typedb.client.api.concept.value.Value;
import com.vaticle.typedb.client.common.Label;
import com.vaticle.typedb.client.common.exception.TypeDBClientException;
import io.cucumber.java.DataTableType;
import io.cucumber.java.ParameterType;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static com.vaticle.typedb.client.api.TypeDBTransaction.Type.READ;
import static com.vaticle.typedb.client.api.TypeDBTransaction.Type.WRITE;
import static com.vaticle.typedb.client.api.concept.type.ThingType.Annotation.key;
import static com.vaticle.typedb.client.api.concept.type.ThingType.Annotation.unique;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static com.vaticle.typedb.client.common.exception.ErrorMessage.Concept.UNRECOGNISED_ANNOTATION;

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

    @ParameterType("entity|attribute|relation|thing")
    public RootLabel root_label(String type) {
        return RootLabel.of(type);
    }

    @ParameterType("[a-zA-Z0-9-_]+")
    public String type_label(String typeLabel) {
        return typeLabel;
    }

    @ParameterType("[a-zA-Z0-9-_]+:[a-zA-Z0-9-_]+")
    public Label scoped_label(String roleLabel) {
        String[] labels = roleLabel.split(":");
        return Label.of(labels[0], labels[1]);
    }

    @DataTableType
    public List<Label> scoped_labels(List<String> values) {
        Iterator<String> valuesIter = values.iterator();
        String next;
        List<Label> scopedLabels = new ArrayList<>();
        while (valuesIter.hasNext() && (next = valuesIter.next()).matches("[a-zA-Z0-9-_]+:[a-zA-Z0-9-_]+")) {
            String[] labels = next.split(":");
            scopedLabels.add(Label.of(labels[0], labels[1]));
        }

        if (valuesIter.hasNext()) fail("Values do not match Scoped Labels regular expression");
        return scopedLabels;
    }

    @ParameterType("long|double|string|boolean|datetime")
    public Value.Type value_type(String type) {
        switch (type) {
            case "long":
                return Value.Type.LONG;
            case "double":
                return Value.Type.DOUBLE;
            case "string":
                return Value.Type.STRING;
            case "boolean":
                return Value.Type.BOOLEAN;
            case "datetime":
                return Value.Type.DATETIME;
            default:
                return null;
        }
    }

    @ParameterType("\\$([a-zA-Z0-9]+)")
    public String var(String variable) {
        return variable;
    }

    @ParameterType("read|write")
    public TypeDBTransaction.Type transaction_type(String type) {
        if (type.equals("read")) {
            return READ;
        } else if (type.equals("write")) {
            return WRITE;
        }
        return null;
    }

    @DataTableType
    public List<TypeDBTransaction.Type> transaction_types(List<String> values) {
        List<TypeDBTransaction.Type> typeList = new ArrayList<>();
        for (String value : values) {
            TypeDBTransaction.Type type = transaction_type(value);
            assertNotNull(type);
            typeList.add(type);
        }

        return typeList;
    }

    @ParameterType("(\\s*([\\w\\-_]+,\\s*)*[\\w\\-_]*\\s*)")
    public List<Annotation> annotations(String stringList) {
        List<String> strings = Arrays.asList(stringList.split(",\\s?"));
        List<Annotation> annotations = new ArrayList<>();
        strings.forEach(string -> {
            if (string.equals("key")) annotations.add(key());
            else if (string.equals("unique")) annotations.add(unique());
            else throw new TypeDBClientException(UNRECOGNISED_ANNOTATION, string);
        });
        return annotations;
    }

    public enum RootLabel {
        ENTITY("entity"),
        ATTRIBUTE("attribute"),
        RELATION("relation"),
        THING("thing");

        private final String label;

        RootLabel(String label) {
            this.label = label;
        }

        public static RootLabel of(String label) {
            for (RootLabel t : RootLabel.values()) {
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
}
