import { defineParameterType } from "@cucumber/cucumber";
import { AnswerType, ApiOkResponse, ApiResponse, isApiErrorResponse, isOkResponse, QueryType, TransactionType, ValueType } from "../../../dist";
import assert from "assert";

export type MayError = boolean | string;
export const EXPECT_ERROR_CONTAINING = "; fails with a message containing: {string}"
defineParameterType({
    name: "may_error",
    regexp: /|; fails|; parsing fails/,
    transformer: s => !!s
});
export function checkMayError<T>(mayError: MayError): (res: ApiResponse<T>) => ApiResponse<T> {
    return (res) => {
        if (mayError === false) return assertNotError(res);
        if (mayError === true) {
            if (isApiErrorResponse(res)) return res;
            else assert.fail(JSON.stringify(res.ok));
        }
        if (isApiErrorResponse(res) && res.err.message.includes(mayError)) return res;
        else assert.fail(`\nExpected: ${mayError}\nReceived: ${JSON.stringify(res)}`);
    }
}
export function assertNotError<T>(res: ApiResponse<T>): ApiOkResponse<T> {
    if (isOkResponse(res)) return res;
    else assert.fail(res.err.message);
}

export type ContainsOrDoesnt = "contains" | "does not contain";
defineParameterType({
    name: "contains_or_doesnt",
    regexp: /(contains|does not contain)/,
    transformer: s => s as ContainsOrDoesnt
});
export function checkContainsOrDoesnt(containsOrDoesnt: ContainsOrDoesnt, target: string, list: string[]) {
    if (containsOrDoesnt === "contains") {
        assert.ok(list.includes(target), `${target} not found in ${list.toString()}`)
    } else {
        assert.ok(!list.includes(target), `${target} unexpectedly found in ${list.toString()}`)
    }
}

defineParameterType({
    name: "transaction_type",
    regexp: /(schema|write|read)/,
    transformer: s => s as TransactionType
});

defineParameterType({
    name: "query_type",
    regexp: /(schema|write|read)/,
    transformer: s => s as QueryType
});

function stringToAnswerType(s: string): AnswerType {
    if (s === "ok") return "ok";
    if (s === "concept documents") return "conceptDocuments";
    if (s === "concept rows") return "conceptRows";
    throw `Cannot convert ${s} to answer type`;
}
defineParameterType({
    name: "query_answer_type",
    regexp: /(ok|concept documents|concept rows)/,
    transformer: stringToAnswerType
});

defineParameterType({
    name: "boolean",
    regexp: /(true|false)/,
    transformer: s => {
        if (s === "true") return true
        else if (s === "false") return false
        else throw "Invalid boolean"
    }
});

export type ConceptKind = "concept" | "variable" | "type" | "instance" | "entityType" | "relationType" | "attributeType" | "roleType" | "entity" | "relation" | "attribute" | "value"
defineParameterType({
    name: "concept_kind",
    regexp: /(concept|variable|type|instance|entity type|relation type|attribute type|role type|entity|relation|attribute|value)/,
    transformer: s => s.split(" ")
        .map((x, i) =>{
            if (i === 0) return x;
            else return x[0].toUpperCase() + x.substring(1, undefined);
        }).join("") as ConceptKind
});

defineParameterType({
    name: "is_by_var_index",
    regexp: /(| by index of variable)/,
    transformer: s => !!s
});

defineParameterType({
    name: "var",
    regexp: /.*/,
    transformer: s => s,
});

defineParameterType({
    name: "is_or_not",
    regexp: /(is|is not)/,
    transformer: s => !s.includes('not')
});

defineParameterType({
    name: "value",
    regexp: /.*/,
    transformer: s => s,
});

defineParameterType({
    name: "value_type",
    regexp: /boolean|integer|double|decimal|string|date|datetime|datetime-tz|duration|struct/,
    transformer: s => s as ValueType,
});
