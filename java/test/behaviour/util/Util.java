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

package com.vaticle.typedb.driver.test.behaviour.util;

import com.vaticle.typedb.driver.api.answer.JSON;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.vaticle.typedb.common.util.Double.equalsApproximate;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class Util {

    public static void assertThrows(Runnable function) {
        try {
            function.run();
            fail();
        } catch (RuntimeException e) {
            assertTrue(true);
        }
    }

    public static void assertThrowsWithMessage(Runnable function, String message) {
        try {
            function.run();
            fail();
        } catch (RuntimeException e) {
            assert e.toString().toLowerCase().contains(message.toLowerCase());
        }
    }

    public static boolean JSONListMatches(List<JSON> lhs, List<JSON> rhs) {
            if (lhs.size() != rhs.size()) return false;
            Set<Integer> rhsMatches = new HashSet<>();
            for (JSON lhsItem: lhs) {
                for (int i = 0; i < rhs.size(); i++) {
                    if (rhsMatches.contains(i)) continue;
                    JSON rhsItem = rhs.get(i);
                    if (JSONMatches(lhsItem, rhsItem)) {
                        rhsMatches.add(i);
                        break;
                    }
                }
            }
            return rhsMatches.size() == rhs.size();
    }

    public static boolean JSONMatches(JSON lhs, JSON rhs) {
        if (lhs.isObject()) {
            if (!rhs.isObject()) return false;
            Map<String, JSON> lhsMap = lhs.asObject();
            Map<String, JSON> rhsMap = rhs.asObject();
            if (lhsMap.size() != rhsMap.size()) return false;
            return lhsMap.keySet().stream().allMatch(key -> JSONMatches(lhsMap.get(key), rhsMap.get(key)));
        } else if (lhs.isArray()) {
            if (!rhs.isArray()) return false;
            return JSONListMatches(lhs.asArray(), rhs.asArray());
        } else if (lhs.isNumber()) {
            if (!rhs.isNumber()) return false;
            return equalsApproximate(lhs.asNumber(), rhs.asNumber());
        } else if (lhs.isString()) {
            if (!rhs.isString()) return false;
            return lhs.asString().equals(rhs.asString());
        } else if (lhs.isBoolean()) {
            if (!rhs.isBoolean()) return false;
            return lhs.asBoolean() == rhs.asBoolean();
        }
        throw new IllegalStateException("Encountered unexpected JSON value");
    }
}
