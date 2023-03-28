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

import {Then} from "@cucumber/cucumber";
import {answers} from "../../../typeql/TypeQLSteps"
import assert = require("assert");
import {isDeepStrictEqual} from "util";

Then("JSON serialization of answers matches", async (expectedJSON: string) => {
    const expected = JSON.parse(expectedJSON);
    const actual = answers.map((conceptMap) => conceptMap.toJSONRecord());
    assertUnorderedDeepStrictEqual(actual, expected);
});

function assertUnorderedDeepStrictEqual<T>(actual: unknown[], expected: T[]): asserts actual is T[] {
    assert.strictEqual(actual.length, expected.length,
        `The number of answers (${actual.length}) should match the number of expected answers (${expected.length})`);
    const matches: number[] = [];
    for (const item of actual) {
        let foundMatch = false;
        for (let index = 0; index < expected.length; index++) {
            if (matches.includes(index)) continue;
            if (isDeepStrictEqual(item, expected[index])) {
                foundMatch = true;
                matches.push(index);
                break;
            }
        }
        assert.ok(foundMatch, `No matches found for [${JSON.stringify(item)}] in the expected list of answers.`);
    }
}
