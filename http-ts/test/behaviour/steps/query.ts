import { Then, When } from "@cucumber/cucumber";
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
import { ConceptDocument, QueryType, TransactionType } from "../../../dist";
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
When('get answers of typeql {transaction_type} query', async (_: TransactionType, query: string) => {
    await getAnswers(query);
});
Then('answer size is: {int}', (size: number) => {
    let answerLength;
    if (answers.answerType === "ok") answerLength = 0;
    else answerLength = answers.answers.length;
    assert.equal(answerLength, size);
});

async function concurrentlyGetAnswers(times: number, query: string) {
    const queries = [];
    for (let i = 0; i < times; i++) {
        queries.push(makeQuery(query).then(assertNotError));
    }
    const results = await Promise.all(queries);
    setConcurrentAnswers(results.map(x => x.ok));
}
When('concurrently get answers of typeql {transaction_type} query {int} times', async (_: TransactionType, times: number, query: string) => {
    await concurrentlyGetAnswers(times, query);
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

When('set query option include_instance_types to: {boolean}', setQueryIncludeInstanceTypes);
When('set query option answer_count_limit to: {int}', setQueryAnswerCountLimit);

Then('answer type {is_or_not}: {query_answer_type}', (type: QueryType) => assert.equal(answers.answerType, type));
Then('answer type is not: {query_answer_type}', (type: QueryType) => assert.notEqual(answers.answerType, type));
Then('answer query type is: {transaction_type}', (type: TransactionType) => assert.equal(answers.queryType, type));
Then('answer query type is not: {transaction_type}', (type: TransactionType) => assert.notEqual(answers.queryType, type));
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

Then('answer unwraps as {query_answer_type}{may_error}', (type: QueryType, mayError: boolean) => {
    if (mayError) assert.notEqual(answers.answerType, type);
    else assert.equal(answers.answerType, type);
});

function answerGetRowGetVariableGetValue(rowIdx: number, kind: ConceptKind, indexed: boolean, variable: string, is: boolean, value: string) {
    if (indexed) return;
    if (answers.answerType === "ok" || answers.answerType === "conceptDocuments") assert.fail("Expected rows answers")
    const row = answers.answers[rowIdx];
    const concept = row.data[variable];
    assert.notEqual(concept, undefined);
    assert.equal(concept.kind, kind);
    if (concept.kind === "value" || concept.kind === "attribute") {
        if (is) assert.equal(JSON.stringify(concept.value), value);
        else assert.notEqual(JSON.stringify(concept.value), value);
    } else assert.fail(`no value in concept of kind ${concept.kind}`)
}
Then(
    'answer get row\\({int}\\) get {concept_kind}{is_by_var_index}\\({var}\\) get value {is_or_not}: {value}',
    answerGetRowGetVariableGetValue
);
Then(
    'answer get row\\({int}\\) get {concept_kind}{is_by_var_index}\\({var}\\) try get value {is_or_not}: {value}',
    answerGetRowGetVariableGetValue
);
Then(
    'answer get row\\({int}\\) get value{is_by_var_index}\\({var}\\) try get value {is_or_not}: {value}',
    (rowIdx: number, indexed: boolean, variable: string, is: boolean, value: string) => answerGetRowGetVariableGetValue(rowIdx, "value", indexed, variable, is, value)
);

