import { DataTable, Then, When } from "@cucumber/cucumber";
import {
    answers,
    concurrentAnswers,
    driver,
    makeQuery,
    setAnswers,
    setConcurrentAnswers, setQueryAnswerCountLimit,
    setQueryIncludeInstanceTypes,
    tx
} from "./context";
import {
    assertNotError,
    checkMayError, ConceptKind,
    ContainsOrDoesnt,
    EXPECT_ERROR_CONTAINING,
    MayError
} from "./params";
import { Concept, ConceptDocument, QueryResponseBase, QueryType, TransactionType, ValueType } from "../../../dist";
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
    if (kind !== "variable") assert.equal(concept.kind, kind);
    return concept;
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

Then('answer get row\\({int}\\) get variable{is_by_var_index}\\({var}\\){may_error}', (rowIdx: number, indexed: boolean, variable: string, mayError: boolean) => {
    if (indexed) return; // http does not have indices
    if (answers.answerType === "ok" || answers.answerType === "conceptDocuments") assert.fail("Expected conceptRow answers");
    if (mayError) assert.equal(answers.answers[rowIdx].data[variable], undefined);
    else assert.notEqual(answers.answers[rowIdx].data[variable], undefined);
});

Then('answer get row\\({int}\\) get variable{is_by_var_index}\\({var}\\) {is_or_not} empty', (rowIdx: number, indexed: boolean, variable: string, is: boolean) => {
    if (indexed) return; // http does not have indices
    if (answers.answerType === "ok" || answers.answerType === "conceptDocuments") assert.fail("Expected conceptRow answers");
    // TODO-sam: not sure if this is correct. revisit at some point
    if (is) assert.equal(answers.answers[rowIdx].data[variable], "");
    else assert.notEqual(answers.answers[rowIdx].data[variable], "");
});

Then('answer get row\\({int}\\) get variable by index\\({int}\\){may_error}', (rowIdx: number, idx: number, mayError: boolean) => {});
Then('answer get row\\({int}\\) get variable by index\\({int}\\) {is_or_not} empty', (rowIdx: number, idx: number, is: boolean) => {});

Then('answer get row\\({int}\\) get variable{is_by_var_index}\\({var}\\) as {concept_kind}{may_error}', (rowIdx: number, indexed: boolean, variable: string, kind: ConceptKind, mayError: boolean) => {
    if (indexed) return; // http does not have indices
    if (answers.answerType === "ok" || answers.answerType === "conceptDocuments") assert.fail("Expected conceptRow answers");
    if (mayError) assert.notEqual(answers.answers[rowIdx].data[variable].kind, kind);
    else assert.equal(answers.answers[rowIdx].data[variable].kind, kind);
});

Then('answer get row\\({int}\\) get {concept_kind}{is_by_var_index}\\({var}\\) is {concept_kind}: {boolean}', (rowIdx: number, varKind: ConceptKind, indexed: boolean, variable: string, checkedKind: boolean, isKind: boolean) => {
    if (indexed) return; // http does not have indices
    const concept = getRowIndexConceptKindVariable(rowIdx, variable, varKind);

    if (isKind) assert.equal(concept.kind, checkedKind);
    else assert.notEqual(concept.kind, checkedKind);
});

Then('answer get row\\({int}\\) get {concept_kind}{is_by_var_index}\\({var}\\) get type is {concept_kind}: {boolean}', (rowIdx: number, varKind: ConceptKind, indexed: boolean, variable: string, checkedKind: boolean, isKind: boolean) => {
    if (indexed) return undefined; // http does not have indices
    const concept = getRowIndexConceptKindVariable(rowIdx, variable, varKind);
    const type = conceptGetType(concept);
    if (isKind) assert.equal(type.label, checkedKind);
    else assert.notEqual(type, checkedKind);
});

function tryGetAndCheckLabel(rowIdx: number, kind: ConceptKind, indexed: boolean, variable: string, is: boolean, label: string | undefined) {
    if (indexed) return; // http does not have indices
    const concept = getRowIndexConceptKindVariable(rowIdx, variable, kind);
    let actualLabel: string | undefined = undefined;
    if ("label" in concept) actualLabel = concept.label;
    if ("type" in concept && concept.type && "label" in concept.type) actualLabel = concept.type.label;
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
    const type = conceptGetType(concept);
    assert.equal(conceptGetType(concept).label, label);
});

function tryGetAndCheckFieldInConcept(rowIdx: number, kind: ConceptKind, indexed: boolean, variable: string, fieldName: string, expectPresent: boolean) {
    if (indexed) return; // http does not have indices
    const concept = getRowIndexConceptKindVariable(rowIdx, variable, kind);
    if (expectPresent) assert.ok(fieldName in concept, `Expected concept to have ${fieldName}`);
    else assert.ok(fieldName !in concept, `Expected concept to not have ${fieldName}`);
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
    assert.equal(concept.kind, kind);
    if (concept.kind === "value" || concept.kind === "attribute") {
        if (is) assert.equal(JSON.stringify(concept.value), `${value}`);
        else assert.notEqual(JSON.stringify(concept.value), `${value}`);
    } else assert.fail(`no value in concept of kind ${concept.kind}`)
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

function getRowGetVariableCheckValueType(rowIdx: number, kind: ConceptKind, indexed: boolean, variable: string, valueType: ValueType, expectAbsent: boolean) {
    if (indexed) return; // http does not have indices
    const concept = getRowIndexConceptKindVariable(rowIdx, variable, kind);
    if (!expectAbsent) {
        assert.ok("valueType" in concept, "Expected concept to have valueType");
        assert.equal(concept.valueType, valueType);
    } else {
        assert.ok("valueType" ! in concept, "Expected concept to not have valueType");
    }
}
Then('answer get row\\({int}\\) get {concept_kind}{is_by_var_index}\\({var}\\) get {value_type}{may_error}', getRowGetVariableCheckValueType);
Then('answer get row\\({int}\\) get {concept_kind}{is_by_var_index}\\({var}\\) try get {value_type}{may_error}', getRowGetVariableCheckValueType);
Then('answer get row\\({int}\\) get {concept_kind}{is_by_var_index}\\({var}\\) try get {value_type} {is_or_not} none', getRowGetVariableCheckValueType);

function getRowGetVariableCheckValueTypeCheckValue(rowIdx: number, kind: ConceptKind, indexed: boolean, variable: string, valueType: ValueType, is: boolean, value: string) {
    if (indexed) return; // http does not have indices
    const concept = getRowIndexConceptKindVariable(rowIdx, variable, kind);
    assert.ok("valueType" in concept, "Expected concept to have valueType");
    assert.ok("value" in concept, "Expected concept to have value");
    assert.equal(concept.valueType, valueType);
    if (is) assert.equal(JSON.stringify(concept.value), value);
    else assert.notEqual(JSON.stringify(concept.value), value);
}
Then('answer get row\\({int}\\) get {concept_kind}{is_by_var_index}\\({var}\\) get {value_type} {is_or_not}: {value}', getRowGetVariableCheckValueTypeCheckValue);
Then('answer get row\\({int}\\) get {concept_kind}{is_by_var_index}\\({var}\\) try get {value_type} {is_or_not}: {value}', getRowGetVariableCheckValueTypeCheckValue);

Then('answer get row\\({int}\\) get {concept_kind}{is_by_var_index}\\({var}\\) is {value_type}: {boolean}', (rowIdx: number, kind: ConceptKind, indexed: boolean, variable: string, valueType: ValueType, is: boolean) => {
    if (indexed) return; // http does not have indices
    const concept = getRowIndexConceptKindVariable(rowIdx, variable, kind);
    assert.ok("valueType" in concept, "Expected concept to have valueType");
    if (is) assert.equal(concept.valueType, valueType);
    else assert.notEqual(concept.valueType, valueType);
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
