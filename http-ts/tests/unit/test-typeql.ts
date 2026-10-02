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

import { test } from "node:test";
import assert from "node:assert/strict";
import { TypeQL, typeql } from "../../dist/index.cjs";

test("typeql keeps literal text as is", () => {
    assert.equal(typeql`match $x isa person;`.text, "match $x isa person;");
});

test("typeql interpolates fragments", () => {
    const query = typeql`match ${TypeQL.variable("x")} isa ${TypeQL.label("person")}, has name == $name;`;
    assert.equal(query.text, "match $x isa person, has name == $name;");
});

test("typeql rejects any interpolated value that isn't a fragment", () => {
    for (const value of [`"; delete $x;`, 42, null, undefined, { text: "match" }]) {
        assert.throws(() => typeql`match $x has name == ${value as unknown as TypeQL};`, /only interpolates TypeQL fragments/);
    }
});

test("typeql can't be called as a plain function", () => {
    const strings = ["match $x isa person;"] as unknown as TemplateStringsArray;
    assert.throws(() => typeql(strings), /template tag/);
});

test("TypeQL can't be constructed directly", () => {
    assert.throws(() => new TypeQL("match", Symbol("TypeQL")), /built with typeql/);
});

test("TypeQL.label accepts type labels, scoped role labels and other identifiers", () => {
    for (const name of ["person", "first-name", "date_of_birth", "_internal", "p2", "café", "friendship:friend"]) {
        assert.equal(TypeQL.label(name).text, name);
    }
});

test("TypeQL.label rejects anything else", () => {
    for (const name of ["", "1st", "-x", "$x", "a b", "a;", "a:b:c", ":a", "a:", "name == \"x\"", "a\nb", "match", "friendship:has"]) {
        assert.throws(() => TypeQL.label(name), /Not a TypeQL label/);
    }
    assert.throws(() => TypeQL.label(42 as unknown as string), /Not a TypeQL label/);
});

test("TypeQL.variable prefixes a valid variable name with $", () => {
    for (const name of ["x", "first-name", "p_2", "1st", "café"]) {
        assert.equal(TypeQL.variable(name).text, `$${name}`);
    }
});

test("TypeQL.variable rejects anything else", () => {
    for (const name of ["", "$x", "_", "_x", "-x", "a b", "a;", "a:b"]) {
        assert.throws(() => TypeQL.variable(name), /Not a TypeQL variable name/);
    }
});

test("TypeQL.join joins fragments with a fragment separator", () => {
    assert.equal(TypeQL.join([typeql`"name": $x.name`, typeql`"age": $x.age`], typeql`, `).text, `"name": $x.name, "age": $x.age`);
    assert.equal(TypeQL.join([], typeql`, `).text, "");
});

test("TypeQL.join rejects anything that isn't a fragment", () => {
    assert.throws(() => TypeQL.join(["x" as unknown as TypeQL], typeql`, `), /only interpolates TypeQL fragments/);
    assert.throws(() => TypeQL.join([typeql`a`], ", " as unknown as TypeQL), /only interpolates TypeQL fragments/);
});
