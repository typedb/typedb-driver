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

package com.vaticle.typedb.client.test.behaviour.concept.serialization;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonValue;
import com.vaticle.typedb.client.api.answer.ConceptMap;
import io.cucumber.java.en.Then;

import java.util.List;
import java.util.stream.Collectors;

import com.vaticle.typedb.client.test.behaviour.typeql.TypeQLSteps;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SerializationSteps {
    @Then("JSON of answer concepts matches")
    public void json_matches(String expectedJSON) {
        List<JsonValue> actual = TypeQLSteps.answers().stream().map(ConceptMap::toJSON).map(Json::parse).collect(Collectors.toList());
        List<JsonValue> expected = Json.parse(expectedJSON).asArray().values();
        assertEquals(actual.size(), expected.size());
        for(JsonValue item: expected){
            assertTrue(actual.contains(item));
            actual.remove(item);
        }
        assertEquals(actual.size(), 0);
    }
}
