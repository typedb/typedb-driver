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

package com.typedb.driver.test.behaviour.util;

import com.typedb.driver.api.answer.JSON;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static com.typedb.driver.common.util.Double.equalsApproximate;
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
            assertTrue(String.format("Expected message '%s', but got '%s'", message, e),
                    e.toString().toLowerCase().contains(message.toLowerCase()));
        }
    }

    public static boolean JSONListMatches(List<JSON> lhs, List<JSON> rhs) {
        if (lhs.size() != rhs.size()) return false;
        Set<Integer> rhsMatches = new HashSet<>();
        for (JSON lhsItem : lhs) {
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

    public static boolean JSONListContains(List<JSON> list, JSON json) {
        return list.stream().anyMatch(listJSON -> JSONMatches(listJSON, json));
    }

    public static boolean JSONMatches(JSON lhs, JSON rhs) {
        if (lhs == null) {
            return rhs == null;
        } else if (lhs.isObject()) {
            if (rhs == null || !rhs.isObject()) return false;
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
        } else {
            return lhs.equals(rhs);
        }
    }

    public static Path createTempDir() {
        try {
            return Files.createTempDirectory("temp-" + UUID.randomUUID());
        } catch (IOException e) {
            throw new RuntimeException("Failed to create temp directory", e);
        }
    }

    public static void deleteDir(Path dir) {
        if (!Files.exists(dir)) return;

        try (var paths = Files.walk(dir)) {
            paths.sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            throw new RuntimeException("Failed to delete: " + path, e);
                        }
                    });
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete directory: " + dir, e);
        }
    }

    public static void writeFile(Path path, String content) {
        try {
            Files.write(path, content.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new RuntimeException("Failed to write to file: " + path, e);
        }
    }

    public static String readFileToString(Path path) {
        try {
            return Files.readString(path);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read file: " + path, e);
        }
    }

    public static boolean isFileEmpty(Path path) {
        try {
            return Files.size(path) == 0;
        } catch (IOException e) {
            throw new RuntimeException("Failed to check file size: " + path, e);
        }
    }

    // Can be useful for docstrings read with excessive tabulation compared to other languages
    public static String removeTwoSpacesInTabulation(String input) {
        return input.lines()
                .map(line -> line.startsWith("  ") ? line.substring(2) : line)
                .reduce((a, b) -> a + "\n" + b)
                .orElse("");
    }
}
