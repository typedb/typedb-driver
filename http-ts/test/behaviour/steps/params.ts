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

export type ConceptKind = "concept" | "type" | "instance" | "entityType" | "relationType" | "attributeType" | "roleType" | "entity" | "relation" | "attribute" | "value"
defineParameterType({
    name: "concept_kind",
    regexp: /(concept|variable|type|instance|entity type|relation type|attribute type|role type|entity|relation|attribute|value)/,
    transformer: s => s.split(" ")
        .map((x, i) =>{
            if (x === "variable") return "concept";
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
    regexp: /.*?/,
    transformer: s => s,
});

export function parseValue(value: string, valueType: ValueType) {
    switch (valueType) {
        case "boolean": return JSON.parse(value);
        case "integer": return parseInt(value);
        case "double": return parseFloat(value);
        case "decimal": {
            const stripped = value.replace("dec", "");
            return parseFloat(stripped);
        }
        case "string": {
            const unescaped = unescapeString(value);
            if (unescaped.startsWith("\"") && unescaped.endsWith("\""))
                return unescaped.substring(1, unescaped.length - 1);
            else return unescaped;
        }
        case "date": return new Date(value);
        case "datetime": return parseDateTime(value);
        case "datetime-tz": {
            const split = value.split(" ");
            const dateTime = split[0].split("+")[0].replace("Z", "");
            const tz = (split[1] ?? value.split("+")[1] ?? "Z").replace(":", "");
            return `${parseDateTime(dateTime)} ` + tz;
        }
        case "duration": return parseDuration(value);
        case "struct": return JSON.parse(value);
    }
}

function unescapeString(value: string) {
    if (value.includes("\\\"")) return unescapeString(value.replace("\\\"", "\""));
    else return value;
}

function parseDateTime(value: string): string {
    const split = value.split('.');
    const date = new Date(split[0]);
    const dateString = date.toISOString().replace("Z", "");
    if (split.length > 1) return `${dateString}${split[1].slice(3)}`;
    else return `${dateString}000000`;
}

const iso8601DurationRegex = /(-)?P(?:([.,\d]+)Y)?(?:([.,\d]+)M)?(?:([.,\d]+)W)?(?:([.,\d]+)D)?(?:T(?:([.,\d]+)H)?(?:([.,\d]+)M)?(?:([.,\d]+)S)?)?/;
function parseDuration(duration: string) {
    const matches = duration.match(iso8601DurationRegex);

    const sign = matches[1] ? -1 : 1;
    const seconds = matches[8] ? parseFloat(matches[8]) : 0;
    const minutes = (matches[7] ? parseFloat(matches[7]) : 0) + Math.floor(seconds/60);
    const hours = (matches[6] ? parseFloat(matches[6]) : 0) + Math.floor(minutes/60);
    const days = (matches[5] ? parseFloat(matches[5]) : 0) + Math.floor(hours/24);
    const weeks = (matches[4] ? parseFloat(matches[4]) : 0) + Math.floor(days/7);
    const months = (matches[3] ? parseFloat(matches[3]) : 0);
    const years = (matches[2] ? parseFloat(matches[2]) : 0) + Math.floor(months/12) + Math.floor(weeks/52);

    return {
        sign,
        seconds: seconds % 60,
        minutes: minutes % 60,
        hours: hours % 24,
        days: days % 7,
        weeks: weeks % 52,
        months: months % 12,
        years
    }
}

defineParameterType({
    name: "value_type",
    regexp: /boolean|integer|double|decimal|string|date|datetime|datetime-tz|duration|struct/,
    transformer: s => s as ValueType,
});
