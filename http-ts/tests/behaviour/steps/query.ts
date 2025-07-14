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

import { DataTable, Then, When } from "@cucumber/cucumber";
import {
    answers,
    concurrentAnswers,
    makeQuery,
    setAnswers,
    setConcurrentAnswers, setQueryAnswerCountLimit,
    setQueryIncludeInstanceTypes,
} from "./context";
import {
    assertNotError,
    checkMayError, ConceptKind,
    ContainsOrDoesnt,
    EXPECT_ERROR_CONTAINING,
    MayError, parseValue
} from "./params";
import { Concept, ConceptDocument, QueryType, ValueType } from "../../../dist";
import assert from "assert";

const runQuery = async (mayError: MayError, query: string) =>  {
    await makeQuery(query).then(checkMayError(mayError));
}
When("typeql schema query{may_error}", runQuery);
When(`typeql schema query${EXPECT_ERROR_CONTAINING}`, runQuery);
When("typeql write query{may_error}", runQuery);
When(`typeql write query${EXPECT_ERROR_CONTAINING}`, runQuery);
When("typeql read query{may_error}", runQuery);
When(`typeql read query${EXPECT_ERROR_CONTAINING}`, runQuery);

async function getAnswers(query: string) {
    const results = await makeQuery(query).then(assertNotError);
    setAnswers(results.ok);
}

function conceptGetType(concept: Concept) {
    if (concept.kind === "entity") return concept.type;
    if (concept.kind === "relation") return concept.type;
    if (concept.kind === "attribute") return concept.type;
    assert.fail("Only instances can have types");
}

function getRowIndexConceptKindVariable(rowIdx: number, variable: string, kind: ConceptKind): Concept | undefined {
    if (answers.answerType === "ok" || answers.answerType === "conceptDocuments") assert.fail("Expected conceptRow answers");
    const concept = answers.answers[rowIdx].data[variable];
    assert.ok(checkConceptKind(concept, kind), `Expected concept to be ${kind} but was ${concept.kind}`);
    return concept;
}

function checkConceptKind(concept: Concept, kind: ConceptKind) {
    if (kind === "concept") { return true }
    else if (kind === "type") return concept.kind === "entityType" || concept.kind === "relationType" || concept.kind === "attributeType" || concept.kind === "roleType";
    else if (kind === "instance") return concept.kind === "entity" || concept.kind === "relation" || concept.kind === "attribute";
    else return concept.kind === kind;
}

When('get answers of typeql {query_type} query', async (_: QueryType, query: string) => await getAnswers(query));
When('concurrently get answers of typeql {query_type} query {int} times', async (_: QueryType, times: number, query: string) => {
    const queries = [];
    for (let i = 0; i < times; i++) {
        queries.push(makeQuery(query).then(assertNotError));
    }
    const results = await Promise.all(queries);
    setConcurrentAnswers(results.map(x => x.ok));
});

When('set query option include_instance_types to: {boolean}', setQueryIncludeInstanceTypes);
When('set query option answer_count_limit to: {int}', setQueryAnswerCountLimit);

Then('answer type {is_or_not}: {query_answer_type}', (is: boolean, type: QueryType) => {
    if (is) assert.equal(answers.answerType, type);
    else assert.notEqual(answers.answerType, type);
});

Then('answer unwraps as {query_answer_type}{may_error}', (type: QueryType, mayError: boolean) => {
    if (mayError) assert.notEqual(answers.answerType, type);
    else assert.equal(answers.answerType, type);
});

Then('answer size is: {int}', (size: number) => {
    let answerLength;
    if (answers.answerType === "ok") answerLength = 0;
    else answerLength = answers.answers.length;
    assert.equal(answerLength, size);
});

Then('concurrently process {int} row(s) from answers{may_error}', (count: number, mayError: boolean) => {
    // Cannot actually process them because they are already collected. But we can at least check that these rows exist
    for (const answer of concurrentAnswers) {
        if (answer.answerType === "ok") assert.fail("No rows in 'ok' answer");
        for (let i = 0; i < count; i++) {
            if (mayError) assert.equal(answer.answers.pop(), undefined)
            else assert.notEqual(answer.answers.pop(), undefined);
        }
    }
});

Then('answer column names are:', (names: DataTable) => {
    if (answers.answerType === "ok" || answers.answerType === "conceptDocuments") assert.fail("Expected conceptRow answers");
    const expectedColumnNames = names.raw().map(x => x[0]);
    expectedColumnNames.sort();
    const actualColumnNames = Object.keys(answers.answers[0].data);
    actualColumnNames.sort();
    assert.deepEqual(expectedColumnNames, actualColumnNames);
});

Then('answer query type {is_or_not}: {query_type}', (is: boolean, type: QueryType) => {
    if (is) assert.equal(answers.queryType, type);
    else assert.notEqual(answers.queryType, type);
});

Then('answer get row\\({int}\\) query type {is_or_not}: {query_type}', (_: number, is: boolean, type: QueryType) => {
    if (answers.answerType === "ok" || answers.answerType === "conceptDocuments") assert.fail("Expected conceptRow answers");
    if (is) assert.equal(answers.queryType, type);
    else assert.notEqual(answers.queryType, type);
});

function getRowGetVariable(rowIdx: number, indexed: boolean, variable: string, mayError: boolean) {
    if (indexed) return; // http does not have indices
    if (answers.answerType === "ok" || answers.answerType === "conceptDocuments") assert.fail("Expected conceptRow answers");
    if (mayError) assert.equal(answers.answers[rowIdx].data[variable], undefined);
    else assert.notEqual(answers.answers[rowIdx].data[variable], undefined);
}

Then('answer get row\\({int}\\) get variable{is_by_var_index}\\({var}\\){may_error}', getRowGetVariable);
Then(`answer get row\\({int}\\) get variable{is_by_var_index}\\({var}\\)${EXPECT_ERROR_CONTAINING}`, getRowGetVariable);

Then('answer get row\\({int}\\) get variable{is_by_var_index}\\({var}\\) {is_or_not} empty', (rowIdx: number, indexed: boolean, variable: string, is: boolean) => {
    if (indexed) return; // http does not have indices
    if (answers.answerType === "ok" || answers.answerType === "conceptDocuments") assert.fail("Expected conceptRow answers");
    if (is) assert.equal(answers.answers[rowIdx].data[variable], "");
    else assert.notEqual(answers.answers[rowIdx].data[variable], "");
});

Then('answer get row\\({int}\\) get variable by index\\({int}\\){may_error}', (rowIdx: number, idx: number, mayError: boolean) => {});
Then(`answer get row\\({int}\\) get variable by index\\({int}\\)${EXPECT_ERROR_CONTAINING}`, (rowIdx: number, idx: number, mayError: boolean) => {});
Then('answer get row\\({int}\\) get variable by index\\({int}\\) {is_or_not} empty', (rowIdx: number, idx: number, is: boolean) => {});

function getRowGetVariableAsConceptKind(rowIdx: number, indexed: boolean, variable: string, kind: ConceptKind, mayError: MayError) {
    if (indexed) return; // http does not have indices
    if (answers.answerType === "ok" || answers.answerType === "conceptDocuments") assert.fail("Expected conceptRow answers");
    if (mayError) assert.notEqual(answers.answers[rowIdx].data[variable].kind, kind);
    else assert.equal(answers.answers[rowIdx].data[variable].kind, kind);
}
Then('answer get row\\({int}\\) get variable{is_by_var_index}\\({var}\\) as {concept_kind}{may_error}', getRowGetVariableAsConceptKind);
Then(`answer get row\\({int}\\) get variable{is_by_var_index}\\({var}\\) as {concept_kind}${EXPECT_ERROR_CONTAINING}`, getRowGetVariableAsConceptKind);

Then('answer get row\\({int}\\) get {concept_kind}{is_by_var_index}\\({var}\\) is {concept_kind}: {boolean}', (rowIdx: number, varKind: ConceptKind, indexed: boolean, variable: string, checkedKind: ConceptKind, isKind: boolean) => {
    if (indexed) return; // http does not have indices
    const concept = getRowIndexConceptKindVariable(rowIdx, variable, varKind);

    const kindMatches = checkConceptKind(concept, checkedKind);
    if (isKind) assert.ok(kindMatches, `Expected kind ${checkedKind} but got ${concept.kind}`);
    else assert.ok(!kindMatches, `Expected kind ${checkedKind} to not match ${concept.kind}, but it did`);
});

Then('answer get row\\({int}\\) get {concept_kind}{is_by_var_index}\\({var}\\) get type is {concept_kind}: {boolean}', (rowIdx: number, varKind: ConceptKind, indexed: boolean, variable: string, checkedKind: ConceptKind, isKind: boolean) => {
    if (indexed) return undefined; // http does not have indices
    const concept = getRowIndexConceptKindVariable(rowIdx, variable, varKind);
    const type = conceptGetType(concept);
    if (isKind) assert.equal(type.kind, checkedKind);
    else assert.notEqual(type.kind, checkedKind);
});

function tryGetAndCheckLabel(rowIdx: number, kind: ConceptKind, indexed: boolean, variable: string, is: boolean, label: string | undefined) {
    if (indexed) return; // http does not have indices
    const concept = getRowIndexConceptKindVariable(rowIdx, variable, kind);
    let actualLabel: string | undefined = undefined;
    if ("label" in concept) actualLabel = concept.label;
    else if ("type" in concept && concept.type && "label" in concept.type) actualLabel = concept.type.label;
    else if ("valueType" in concept) actualLabel = concept.valueType
    if (is) assert.equal(actualLabel, label);
    else assert.notEqual(actualLabel, label);
}
Then('answer get row\\({int}\\) get {concept_kind}{is_by_var_index}\\({var}\\) try get label {is_or_not} none', (rowIdx: number, kind: ConceptKind, indexed: boolean, variable: string, is: boolean) => {
    tryGetAndCheckLabel(rowIdx, kind, indexed, variable, is, undefined);
});
Then('answer get row\\({int}\\) get {concept_kind}{is_by_var_index}\\({var}\\) try get label: {word}', (rowIdx: number, kind: ConceptKind, indexed: boolean, variable: string, label: string) => {
    tryGetAndCheckLabel(rowIdx, kind, indexed, variable, true, label);
});
Then('answer get row\\({int}\\) get {concept_kind}{is_by_var_index}\\({var}\\) get label: {word}', (rowIdx: number, kind: ConceptKind, indexed: boolean, variable: string, label: string) => {
    tryGetAndCheckLabel(rowIdx, kind, indexed, variable, true, label);
});

Then('answer get row\\({int}\\) get {concept_kind}{is_by_var_index}\\({var}\\) get type get label: {word}', (rowIdx: number, kind: ConceptKind, indexed: boolean, variable: string, label: string) => {
    if (indexed) return; // http does not have indices
    const concept = getRowIndexConceptKindVariable(rowIdx, variable, kind);
    assert.equal(conceptGetType(concept).label, label);
});

function tryGetAndCheckFieldInConcept(rowIdx: number, kind: ConceptKind, indexed: boolean, variable: string, fieldName: string, expectPresent: boolean) {
    if (indexed) return; // http does not have indices
    const concept = getRowIndexConceptKindVariable(rowIdx, variable, kind);
    if (expectPresent) assert.ok(fieldName in concept && concept[fieldName], `Expected concept to have ${fieldName}`);
    else assert.ok(fieldName !in concept || !concept[fieldName], `Expected concept to not have ${fieldName}`);
}

Then('answer get row\\({int}\\) get {concept_kind}{is_by_var_index}\\({var}\\) {contains_or_doesnt} iid', (rowIdx: number, kind: ConceptKind, indexed: boolean, variable: string, containsOrDoesnt: ContainsOrDoesnt) => {
    tryGetAndCheckFieldInConcept(rowIdx, kind, indexed, variable, "iid", containsOrDoesnt === "contains");
});

Then('answer get row\\({int}\\) get {concept_kind}{is_by_var_index}\\({var}\\) try get iid {is_or_not} none', (rowIdx: number, kind: ConceptKind, indexed: boolean, variable: string, is: boolean) => {
    tryGetAndCheckFieldInConcept(rowIdx, kind, indexed, variable, "iid", !is);
});

Then('answer get row\\({int}\\) get {concept_kind}{is_by_var_index}\\({var}\\) try get value type {is_or_not} none', (rowIdx: number, kind: ConceptKind, indexed: boolean, variable: string, is: boolean) => {
    tryGetAndCheckFieldInConcept(rowIdx, kind, indexed, variable, "valueType", !is);
});

function getRowIndexConceptKindVariableCheckValueType(rowIdx: number, kind: ConceptKind, indexed: boolean, variable: string, valueType: string) {
    if (indexed) return; // http does not have indices
    const concept = getRowIndexConceptKindVariable(rowIdx, variable, kind);
    if ("valueType" in concept) assert.equal(concept.valueType, valueType);
    else assert.equal("none", valueType);
}
Then('answer get row\\({int}\\) get {concept_kind}{is_by_var_index}\\({var}\\) try get value type: {word}', getRowIndexConceptKindVariableCheckValueType);
Then('answer get row\\({int}\\) get {concept_kind}{is_by_var_index}\\({var}\\) get value type: {word}', getRowIndexConceptKindVariableCheckValueType);

Then('answer get row\\({int}\\) get {concept_kind}{is_by_var_index}\\({var}\\) get type get value type: {word}', getRowIndexConceptKindVariableCheckValueType);

function answerGetRowGetVariableGetValue(rowIdx: number, kind: ConceptKind, indexed: boolean, variable: string, is: boolean, value: string | undefined) {
    if (indexed) return; // http does not have indices
    const concept = getRowIndexConceptKindVariable(rowIdx, variable, kind);
    assert.notEqual(concept, undefined);
    checkConceptKind(concept, kind);
    const values = getExpectedAndActualValue(concept, value);
    if (is) assert.deepEqual(values.actual, values.expected);
    else assert.notDeepEqual(values.actual, values.expected);
}
function getExpectedAndActualValue(concept: Concept, value: string | undefined): { expected: any, actual: any } {
    if (value === undefined) {
        if ("valueType" in concept && "value" in concept) return { actual: concept.value, expected: value };
        else return { expected: value, actual: undefined };
    }
    if ("valueType" in concept && "value" in concept) {
        const valueType = concept.valueType;
        const expected = parseValue(value, valueType);
        const actual = parseValue(concept.value, valueType);
        return { expected, actual };
    }
    return { expected: value, actual: value };
}
Then('answer get row\\({int}\\) get {concept_kind}{is_by_var_index}\\({var}\\) try get value {is_or_not} none', (rowIdx: number, kind: ConceptKind, indexed: boolean, variable: string, is: boolean) => {
    answerGetRowGetVariableGetValue(rowIdx, kind, indexed, variable, is, undefined);
})
Then(
    'answer get row\\({int}\\) get {concept_kind}{is_by_var_index}\\({var}\\) get value {is_or_not}: {value}',
    answerGetRowGetVariableGetValue
);
Then(
    'answer get row\\({int}\\) get {concept_kind}{is_by_var_index}\\({var}\\) try get value {is_or_not}: {value}',
    answerGetRowGetVariableGetValue
);
Then('answer get row\\({int}\\) get value{is_by_var_index}\\({var}\\) get {is_or_not}: {value}', (rowIdx: number, indexed: boolean, variable: string, is: boolean, value: string) => {
    answerGetRowGetVariableGetValue(rowIdx, "value", indexed, variable, is, value);
});

function getRowGetVariableCheckValueType(rowIdx: number, kind: ConceptKind, indexed: boolean, variable: string, valueType: ValueType, mayError: MayError) {
    if (indexed) return; // http does not have indices
    const concept = getRowIndexConceptKindVariable(rowIdx, variable, kind);
    if (!mayError) {
        assert.ok("valueType" in concept, "Expected concept to have valueType");
        assert.equal(concept.valueType, valueType);
    } else {
        assert.ok("valueType" ! in concept || concept["valueType"] !== valueType, "Expected concept to not have valueType");
    }
}
Then('answer get row\\({int}\\) get {concept_kind}{is_by_var_index}\\({var}\\) get {value_type}{may_error}', getRowGetVariableCheckValueType);
Then(`answer get row\\({int}\\) get {concept_kind}{is_by_var_index}\\({var}\\) get {value_type}${EXPECT_ERROR_CONTAINING}`, getRowGetVariableCheckValueType);
Then('answer get row\\({int}\\) get {concept_kind}{is_by_var_index}\\({var}\\) try get {value_type}{may_error}', getRowGetVariableCheckValueType);
Then(`answer get row\\({int}\\) get {concept_kind}{is_by_var_index}\\({var}\\) try get {value_type}${EXPECT_ERROR_CONTAINING}`, getRowGetVariableCheckValueType);
Then('answer get row\\({int}\\) get {concept_kind}{is_by_var_index}\\({var}\\) try get {value_type} {is_or_not} none', getRowGetVariableCheckValueType);

function getRowGetVariableCheckValueTypeCheckValue(rowIdx: number, kind: ConceptKind, indexed: boolean, variable: string, valueType: ValueType, is: boolean, value: string) {
    if (indexed) return; // http does not have indices
    const concept = getRowIndexConceptKindVariable(rowIdx, variable, kind);
    assert.ok("valueType" in concept, "Expected concept to have valueType");
    assert.ok("value" in concept, "Expected concept to have value");
    assert.equal(concept.valueType, valueType);
    const actualValue = parseValue(concept.value, valueType);
    const expectedValue = parseValue(value, valueType);
    if (is) assert.deepEqual(actualValue, expectedValue);
    else assert.notDeepEqual(actualValue, expectedValue);
}
Then('answer get row\\({int}\\) get {concept_kind}{is_by_var_index}\\({var}\\) get {value_type} {is_or_not}: {value}', getRowGetVariableCheckValueTypeCheckValue);
Then('answer get row\\({int}\\) get {concept_kind}{is_by_var_index}\\({var}\\) try get {value_type} {is_or_not}: {value}', getRowGetVariableCheckValueTypeCheckValue);

Then('answer get row\\({int}\\) get {concept_kind}{is_by_var_index}\\({var}\\) is {value_type}: {boolean}', (rowIdx: number, kind: ConceptKind, indexed: boolean, variable: string, valueType: ValueType, is: boolean) => {
    if (indexed) return; // http does not have indices
    const concept = getRowIndexConceptKindVariable(rowIdx, variable, kind);
    if (is) {
        assert.ok("valueType" in concept, "Expected concept to have valueType");
        if (valueType === "struct") {
            // structs have a valueType of the actual struct name, so we instead check for the value NOT being any of the other types
            assert.notEqual(concept.valueType, "boolean");
            assert.notEqual(concept.valueType, "integer");
            assert.notEqual(concept.valueType, "double");
            assert.notEqual(concept.valueType, "decimal");
            assert.notEqual(concept.valueType, "string");
            assert.notEqual(concept.valueType, "date");
            assert.notEqual(concept.valueType, "datetime");
            assert.notEqual(concept.valueType, "datetime-tz");
            assert.notEqual(concept.valueType, "duration");
        } else {
            assert.equal(concept.valueType, valueType);
        }
    } else if ("valueType" in concept) assert.notEqual(concept.valueType, valueType);
});

Then('answer get row\\({int}\\) get concepts size is: {int}', (rowIdx: number, size: number) => {
    if (answers.answerType === "ok" || answers.answerType === "conceptDocuments") assert.fail("Expected conceptRow answers");
    assert.equal(Object.keys(answers.answers[rowIdx].data).length, size);
});

Then('answer {contains_or_doesnt} document:', (contains_or_doesnt: ContainsOrDoesnt, document: string) => {
    if (answers.answerType === "ok" || answers.answerType === "conceptRows") assert.fail("Expected document answers")
    const expected = JSON.parse(document);
    if (contains_or_doesnt === "does not contain") {
        assert.ok(!documentPresentInAnswers(expected, answers.answers), `Found ${expected} in ${JSON.stringify(answers.answers)}`);
    } else {
        assert.ok(documentPresentInAnswers(expected, answers.answers), `Did not find ${expected} in ${JSON.stringify(answers.answers)}`);
    }
});

function documentPresentInAnswers(document: any, answers: ConceptDocument[]) {
    return answers.some((x) => {
        try {
            assert.deepEqual(x, document);
            return true;
        } catch (_) {
            return false;
        }
    });
}
