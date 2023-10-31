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

import {isDeepStrictEqual} from "util";
import assert = require("assert");
import {JSONArray, JSONObject, JSON} from "../../../dist";

export async function assertThrows(testfunc: () => Promise<unknown>): Promise<void> {
    try {
        await testfunc();
    } catch {
        // Failed successfully
        return
    }
    assert.fail();
}

export async function assertThrowsWithMessage(testfunc: () => Promise<unknown>, message: string): Promise<void> {
    try {
        await testfunc();
    } catch (error) {
        assert(error.toString().toLowerCase().includes(message.toLowerCase()));
        return
    }
    assert.fail();
}

export function splitString(value: string, separator: string, limit: number): string[] {
    const arr = value.split(separator);
    return arr.slice(0, limit).concat(arr.slice(limit).join(separator));
}


export function JSONEqualsUnordered(first: JSON, second: JSON): boolean {
    if (Array.isArray(first) && Array.isArray(second)) {
        return JSONArrayEquals(first, second);
    } else if (first instanceof Object && second instanceof Object) {
        return JSONObjectEquals(first as JSONObject, second as JSONObject);
    } else {
        return first === second;
    }
}

export function JSONArrayEquals(first: JSONArray, second: JSONArray): boolean {
    if (first.length != second.length) return false;
    let secondCopy = [...second];
    for (let f of first) {
        let matched = false;
        for (let i = 0; i < secondCopy.length; i++) {
            let s = secondCopy[i];
            if (JSONEqualsUnordered(f, s)) {
                matched = true;
                secondCopy.splice(i, 1);
                break;
            }
        }
        if (!matched) return false;
    }
    return secondCopy.length == 0;
}

export function JSONObjectEquals(first: JSONObject, second: JSONObject): boolean {
    if (Object.keys(first).length != Object.keys(second).length) return false;
    for (const key in first) {
        let firstValue = first[key];
        let secondValue = second[key];
        if (secondValue == null || !JSONEqualsUnordered(firstValue, secondValue)) {
            return false;
        }
    }
    return true;
}
