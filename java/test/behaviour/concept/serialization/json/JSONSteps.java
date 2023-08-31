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

package com.vaticle.typedb.client.test.behaviour.concept.serialization.json;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import com.vaticle.typedb.client.api.answer.ConceptMap;
import io.cucumber.java.en.Then;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.vaticle.typedb.client.test.behaviour.typeql.TypeQLSteps;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class JSONSteps {
    @Then("JSON serialization of answers matches")
    public void json_matches(String expectedJSON) {
        List<JsonValue> actual = TypeQLSteps.answers().stream().map(ConceptMap::toJSON).collect(Collectors.toList());
        List<JsonValue> expected = Json.parse(expectedJSON).asArray().values();
        assertEquals(actual.size(), expected.size());
        for (JsonValue expectedItem: expected) {
            boolean foundMatch = false;
            for (JsonValue actualItem: actual) {
                if (JSONValuesAreEqual(expectedItem, actualItem)) {
                    actual.remove(actualItem);
                    foundMatch = true;
                    break;
                }
            }
            assertTrue("No matches found for [" + expectedItem + "] in the list of answers.", foundMatch);
        }
    }

    private boolean JSONValuesAreEqual(JsonValue left, JsonValue right) {
        if (Objects.equals(left, right)) return true;
        if (left == null || right == null) return false;
        if (left.isObject() && right.isObject()) return JSONObjectsAreEqual(left.asObject(), right.asObject());
        if (left.isArray() && right.isArray()) return JSONArraysAreEqual(left.asArray(), right.asArray());
        return false;
    }

    private boolean JSONObjectsAreEqual(JsonObject left, JsonObject right) {
        if (left.size() != right.size()) return false;
        return left.names().stream().allMatch((name) -> JSONValuesAreEqual(left.get(name), right.get(name)));
    }

    private boolean JSONArraysAreEqual(JsonArray left, JsonArray right) {
        if (left.size() != right.size()) return false;
        for (int i = 0; i < left.size(); i++)
            if (!JSONValuesAreEqual(left.get(i), right.get(i))) return false;
        return true;
    }
}
