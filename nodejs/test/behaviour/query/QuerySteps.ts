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

import {Then, When} from "@cucumber/cucumber";
import DataTable from "@cucumber/cucumber/lib/models/data_table";
import {fail} from "assert";
import {Attribute, Concept, ConceptMap, ConceptMapGroup, JSONObject, JSONArray, JSON, ThingType, Value, ValueGroup} from "../../../dist";
import {parseBool} from "../config/Parameters";
import {tx} from "../connection/ConnectionStepsBase";
import {JSONEqualsUnordered, assertThrows, assertThrowsWithMessage, splitString, JSONArrayEquals} from "../util/Util";
import assert = require("assert");
import Annotation = ThingType.Annotation;
import ValueType = Concept.ValueType;

export let answers: ConceptMap[] = [];
let fetchAnswers: JSONObject[] = [];
let valueAnswer: Value | null;
let answerGroups: ConceptMapGroup[] = []
let valueAnswerGroups: ValueGroup[] = []

function clearAnswers() {
    answers.length = 0;
    fetchAnswers.length = 0;
    valueAnswer = null;
    answerGroups.length = 0;
    valueAnswerGroups.length = 0;
}

When("typeql define", async (query: string) => {
    await tx().query.define(query);
});

Then("typeql define; throws exception containing {string}", async (exceptionString: string, query: string) => {
    await assertThrowsWithMessage(async () => await tx().query.define(query), exceptionString);
});

Then("typeql define; throws exception", async (query: string) => {
    await assertThrows(async () => await tx().query.define(query));
});

When("typeql undefine", async (query: string) => {
    await tx().query.undefine(query);
});

Then("typeql undefine; throws exception containing {string}", async (exceptionString: string, query: string) => {
    await assertThrowsWithMessage(async () => await tx().query.undefine(query), exceptionString);
});

Then("typeql undefine; throws exception", async (query: string) => {
    await assertThrows(async () => await tx().query.undefine(query));
});

When("typeql insert", async (query: string) => {
    await tx().query.insert(query).collect();
});

Then("typeql insert; throws exception containing {string}", async (exceptionString: string, query: string) => {
    await assertThrowsWithMessage(async () => await tx().query.insert(query).first(), exceptionString);
});

Then("typeql insert; throws exception", async (query: string) => {
    await assertThrows(async () => await tx().query.insert(query).first());
});

When("typeql delete", async (query: string) => {
    await tx().query.delete(query);
});

Then("typeql delete; throws exception containing {string}", async (exceptionString: string, query: string) => {
    await assertThrowsWithMessage(async () => await tx().query.delete(query), exceptionString);
});

Then("typeql delete; throws exception", async (query: string) => {
    await assertThrows(async () => await tx().query.delete(query));
});

When("typeql update", async (query: string) => {
    await tx().query.update(query).collect();
});

Then("typeql update; throws exception containing {string}", async (exceptionString: string, query: string) => {
    await assertThrowsWithMessage(async () => await tx().query.update(query).first(), exceptionString);
});

Then("typeql update; throws exception", async (query: string) => {
    await assertThrows(async () => await tx().query.update(query).first());
});

When("get answers of typeql insert", async (query: string) => {
    clearAnswers();
    answers = await tx().query.insert(query).collect();
});

When("get answers of typeql update", async (query: string) => {
    clearAnswers();
    answers = await tx().query.update(query).collect();
});

When("get answers of typeql get", async (query: string) => {
    clearAnswers();
    answers = await tx().query.get(query).collect();
});

When("get answers of typeql fetch", async (query: string) => {
    clearAnswers();
    fetchAnswers = await tx().query.fetch(query).collect();
});

When("typeql fetch; throws exception", async (query: string) => {
    await assertThrows(async () => await tx().query.fetch(query).collect());
});

Then("typeql get; throws exception", async (query: string) => {
    await assertThrows(async () => await tx().query.get(query).first());
});

Then("typeql get; throws exception containing {string}", async (error: string, query: string) => {
    await assertThrowsWithMessage(async () => await tx().query.get(query).first(), error);
});

When("get answer of typeql get aggregate", async (query: string) => {
    clearAnswers();
    valueAnswer = await tx().query.getAggregate(query);
});

When("get answers of typeql get group", async (query: string) => {
    clearAnswers();
    answerGroups = await tx().query.getGroup(query).collect();
});

When("typeql get group; throws exception", async (query: string) => {
    await assertThrows(async () => await tx().query.getGroup(query).first());
})

When("get answers of typeql get group aggregate", async (query: string) => {
    clearAnswers();
    valueAnswerGroups = await tx().query.getGroupAggregate(query).collect();
});

When("typeql get aggregate; throws exception", async (query: string) => {
    await assertThrows(async () => await tx().query.getAggregate(query));

})

Then("answer size is: {number}", async (expectedAnswers: number) => {
    assert.strictEqual(answers.length, expectedAnswers, `Expected [${expectedAnswers}], but got [${answers.length}]`);
});

Then("rules contain: {type_label}", async (ruleLabel: string) => {
    for await (const rule of (await tx().logic.getRules())) {
        if (rule.label === ruleLabel) return;
    }
    assert.fail();
});

Then("rules do not contain: {type_label}", async (ruleLabel: string) => {
    for await (const rule of (await tx().logic.getRules())) {
        if (rule.label === ruleLabel) assert.fail();
    }
});

interface ConceptMatcher {
    matches(concept: Concept): Promise<boolean>;
}

class TypeLabelMatcher implements ConceptMatcher {
    private readonly _label: string;

    constructor(label: string) {
        this._label = label;
    }

    protected get label(): string {
        return this._label;
    }

    async matches(concept: Concept): Promise<boolean> {
        if (concept.isType()) return this.label == concept.asType().label.scopedName;
        else throw new TypeError("A Concept was matched by label, but it is not a Type.");
    }
}

abstract class AttributeMatcher implements ConceptMatcher {
    private readonly _typeLabel: string;
    private readonly _value: string;

    constructor(typeAndValue: string) {
        const s = typeAndValue.match(/([\w|-]+):(.+)/).slice(1);
        assert.strictEqual(s.length, 2, `[${typeAndValue}] is not a valid attribute identifier. It should have format "typeLabel:value".`);
        [this._typeLabel, this._value] = s;
    }

    protected get typeLabel(): string {
        return this._typeLabel;
    }

    protected get value(): string {
        return this._value;
    }

    check(attribute: Attribute) {
        switch (attribute.valueType) {
            case ValueType.BOOLEAN:
                return attribute.value === parseBool(this.value);
            case ValueType.LONG:
                return attribute.value === parseInt(this.value);
            case ValueType.DOUBLE:
                return attribute.value === parseFloat(this.value);
            case ValueType.STRING:
                return attribute.value === this.value;
            case ValueType.DATETIME: {
                const date = new Date(this.value)
                const userTimezoneOffset = date.getTimezoneOffset() * 60000;
                return (attribute.value as Date).getTime() === new Date(date.getTime() - userTimezoneOffset).getTime();
            }
            default:
                throw new Error(`Unrecognised value type ${attribute.constructor.name}`);
        }
    }

    abstract matches(concept: Concept): Promise<boolean>;
}

class AttributeValueMatcher extends AttributeMatcher {

    async matches(concept: Concept): Promise<boolean> {
        if (!concept.isAttribute()) return false;

        const attribute = concept.asAttribute();

        if (this.typeLabel !== attribute.type.label.scopedName) return false;

        return this.check(attribute);
    }
}

class ThingKeyMatcher extends AttributeMatcher {
    async matches(concept: Concept): Promise<boolean> {
        if (!concept.isThing()) return false;

        const keys = await concept.asThing().getHas(tx(), [Annotation.KEY]).collect();

        for (const key of keys) {
            if (key.type.label.scopedName === this.typeLabel) {
                return this.check(key);
            }
        }

        return false;
    }
}

class ValueMatcher implements ConceptMatcher {
    private readonly _valueType: string;
    private readonly _value: string;

    constructor(typeAndValue: string) {
        const s = typeAndValue.match(/([\w|-]+):(.+)/).slice(1);
        assert.strictEqual(s.length, 2, `[${typeAndValue}] is not a valid attribute identifier. It should have format "valueType:value".`);
        [this._valueType, this._value] = s;
    }

    protected get valueType(): string {
        return this._valueType;
    }

    protected get value(): string {
        return this._value;
    }

    check(value: Value) {
        if (value.isBoolean()) return value.asBoolean() === parseBool(this.value);
        else if (value.isLong()) return value.asLong() === parseInt(this.value);
        else if (value.isDouble()) return value.asDouble() === parseFloat(this.value);
        else if (value.isString()) return value.asString() === this.value;
        else if (value.isDateTime()) return value.asDateTime().getTime() === new Date(this.value).getTime();
        else throw new Error(`Unrecognised value type ${value.valueType}`);
    }

    async matches(concept: Concept): Promise<boolean> {
        if (!concept.isValue()) return false;
        const value = concept.asValue();
        if (this.valueType !== value.valueType.name()) return false;
        return this.check(value);
    }
}

function parseConceptIdentifier(value: string): ConceptMatcher {
    const [identifierType, identifierBody] = splitString(value, ":", 1);
    switch (identifierType) {
        case "label":
            return new TypeLabelMatcher(identifierBody);
        case "key":
            return new ThingKeyMatcher(identifierBody);
        case "attr":
            return new AttributeValueMatcher(identifierBody);
        case "value":
            return new ValueMatcher(identifierBody);
        default:
            throw new Error(`Failed to parse concept identifier: ${value}`);
    }
}

type AnswerIdentifier = { [key: string]: string };

async function answerConceptsMatch(answerIdentifier: AnswerIdentifier, answer: ConceptMap): Promise<boolean> {
    for (const [var0, conceptIdentifier] of Object.entries(answerIdentifier)) {
        const matcher = parseConceptIdentifier(conceptIdentifier);
        if (!(await matcher.matches(answer.get(var0)))) return false;
    }
    return true;
}

Then("uniquely identify answer concepts", async (answerIdentifiersTable: DataTable) => {
    const answerIdentifiers: AnswerIdentifier[] = answerIdentifiersTable.hashes();
    assert.strictEqual(answers.length, answerIdentifiers.length,
        `The number of answers [${answers.length}] should match the number of answer identifiers [${answerIdentifiers.length}`);
    const resultSet: [AnswerIdentifier, ConceptMap[]][] = answerIdentifiers.map(ai => [ai, []]);
    for (const answer of answers) {
        for (const [answerIdentifier, matchedAnswers] of resultSet) {
            if (await answerConceptsMatch(answerIdentifier, answer)) {
                matchedAnswers.push(answer);
            }
        }
    }

    for (const [answerIdentifier, answers] of resultSet) {
        assert.strictEqual(answers.length, 1, `Each answer identifier should match precisely 1 answer, but [${answers.length}] matched the identifier [${JSON.stringify(answerIdentifier)}].`);
    }
});

Then("order of answer concepts is", async (answerIdentifiersTable: DataTable) => {
    const answerIdentifiers: AnswerIdentifier[] = answerIdentifiersTable.hashes();
    assert.strictEqual(answers.length, answerIdentifiers.length,
        `The number of answers [${answers.length}] should match the number of answer identifiers [${answerIdentifiers.length}`);
    for (let i = 0; i < answers.length; i++) {
        const [answer, answerIdentifier] = [answers[i], answerIdentifiers[i]];
        assert(await answerConceptsMatch(answerIdentifier, answer),
            `The answer at index [${i}] does not match the identifier [${JSON.stringify(answerIdentifier)}].`);
    }
});

function getNumberFromValue(value: Value | null) {
    if (value) {
        if (value.isLong() || value.isDouble()) {
            return value.value;
        } else throw new Error(`Expected numerical Value, but got: ${value}`);
    } else return NaN;
}

function assertValue(value: Value | null, expectedAnswer: number, reason?: string) {
    if (value) {
        assert(Math.abs(expectedAnswer - (value.value as number)) < 0.001, reason);
    } else {
        fail();
    }
}

Then("aggregate value is: {float}", async (expectedAnswer: number) => {
    assert(valueAnswer != null, "The last query executed was not an aggregate query.");
    assertValue(valueAnswer, expectedAnswer);
});

Then("aggregate answer is not a number", async () => {
    assert(!valueAnswer);
});

class AnswerIdentifierGroup {
    public static GROUP_COLUMN_NAME = "owner";

    private readonly _ownerIdentifier: string;
    private readonly _answerIdentifiers: AnswerIdentifier[];

    constructor(rawAnswerIdentifiers: AnswerIdentifier[]) {
        this._ownerIdentifier = rawAnswerIdentifiers[0][AnswerIdentifierGroup.GROUP_COLUMN_NAME];
        this._answerIdentifiers = rawAnswerIdentifiers.map(rawAnswerIdentifier => {
            const answerIdentifier = Object.assign({}, rawAnswerIdentifier);
            delete answerIdentifier[AnswerIdentifierGroup.GROUP_COLUMN_NAME];
            return answerIdentifier;
        });
    }

    get ownerIdentifier(): string {
        return this._ownerIdentifier;
    }

    get answerIdentifiers(): AnswerIdentifier[] {
        return this._answerIdentifiers;
    }
}

Then("answer groups are", async (answerIdentifiersTable: DataTable) => {
    const rawAnswerIdentifiers: AnswerIdentifier[] = answerIdentifiersTable.hashes();
    const groupedAnswerIdentifiers: { [key: string]: AnswerIdentifier[] } = {};
    for (const rawAnswerIdentifier of rawAnswerIdentifiers) {
        const owner = rawAnswerIdentifier[AnswerIdentifierGroup.GROUP_COLUMN_NAME];
        if (groupedAnswerIdentifiers[owner]) groupedAnswerIdentifiers[owner].push(rawAnswerIdentifier);
        else groupedAnswerIdentifiers[owner] = [rawAnswerIdentifier];
    }
    const answerIdentifierGroups = Object.values(groupedAnswerIdentifiers).map(rais => new AnswerIdentifierGroup(rais));

    assert.strictEqual(answerGroups.length, answerIdentifierGroups.length,
        `Expected [${answerIdentifierGroups.length}] answer groups, but found [${answerGroups.length}].`);

    for (const answerIdentifierGroup of answerIdentifierGroups) {
        const identifier = parseConceptIdentifier(answerIdentifierGroup.ownerIdentifier);
        let answerGroup: ConceptMapGroup;
        for (const group of answerGroups) {
            if (await identifier.matches(group.owner)) {
                answerGroup = group;
                break;
            }
        }
        assert(answerGroup, `The group identifier [${JSON.stringify(answerIdentifierGroup.ownerIdentifier)}] does not match any of the answer group owners.`);

        const resultSet: [AnswerIdentifier, ConceptMap[]][] = answerIdentifierGroup.answerIdentifiers.map(ai => [ai, []]);
        for (const answer0 of answerGroup.conceptMaps) {
            for (const [answerIdentifier, matchedAnswers] of resultSet) {
                if (await answerConceptsMatch(answerIdentifier, answer0)) {
                    matchedAnswers.push(answer0);
                }
            }
        }

        for (const [answerIdentifier, answers] of resultSet) {
            assert.strictEqual(answers.length, 1, `Each answer identifier should match precisely 1 answer, but [${answers.length}] matched the identifier [${JSON.stringify(answerIdentifier)}].`);
        }
    }
});

Then("group aggregate values are", async (answerIdentifiersTable: DataTable) => {
    const rawAnswerIdentifiers: AnswerIdentifier[] = answerIdentifiersTable.hashes();
    const expectations: { [key: string]: number } = {};
    for (const rawAnswerIdentifier of rawAnswerIdentifiers) {
        const owner = rawAnswerIdentifier[AnswerIdentifierGroup.GROUP_COLUMN_NAME];
        expectations[owner] = parseFloat(rawAnswerIdentifier.value);
    }
    assert.strictEqual(valueAnswerGroups.length, Object.keys(expectations).length,
        `Expected [${Object.keys(expectations).length}], but found [${valueAnswerGroups.length}].`);

    for (const [ownerIdentifier, expectedAnswer] of Object.entries(expectations)) {
        const identifier = parseConceptIdentifier(ownerIdentifier);
        let valueGroup;
        for (const group of valueAnswerGroups) {
            if (await identifier.matches(group.owner)) {
                valueGroup = group;
                break;
            }
        }
        assert(valueGroup, `The group identifier [${JSON.stringify(ownerIdentifier)}] does not match any of the answer group owners.`);

        const actualAnswer = getNumberFromValue(valueGroup.value);
        assertValue(valueGroup.numeric, expectedAnswer,
            `Expected answer [${expectedAnswer}] for group [${JSON.stringify(ownerIdentifier)}], but got [${actualAnswer}]`);
    }
});

function variableFromTemplatePlaceholder(placeholder: string): string {
    if (placeholder.endsWith(".iid")) return placeholder.replace(".iid", "").replace("answer.", "");
    else throw new Error("Cannot replace template not based on IID.");
}

function applyQueryTemplate(template: string, answer: ConceptMap): string {
    let query = "";
    const pattern = /<(.+?)>/g;
    let i = 0;
    let match: RegExpExecArray;
    while ((match = pattern.exec(template))) {
        const requiredVariable = variableFromTemplatePlaceholder(match[1]);
        query += template.substring(i, match.index);
        if (Array.from(answer.variables()).includes(requiredVariable)) {
            const concept = answer.get(requiredVariable);
            if (!concept.isThing()) throw new TypeError("Cannot apply IID templating to Types");
            query += concept.asThing().iid;
        } else {
            throw new Error(`No IID available for template placeholder: [${match[0]}]`);
        }
        i = match.index + match[0].length;
    }
    query += template.substring(i);
    return query;
}

Then("each answer satisfies", async (template: string) => {
    for (const answer of answers) {
        const query = applyQueryTemplate(template, answer);
        assert.strictEqual((await tx().query.get(query).collect()).length, 1);
    }
});

Then("templated typeql get; throws exception", async (template: string) => {
    for (const answer of answers) {
        const query = applyQueryTemplate(template, answer);
        await assertThrows(async () => await tx().query.get(query).collect());
    }
});

Then("fetch answers are", async (answers: string) => {
    let jsonAnswers = JSON.parse(answers);
    assert.ok(JSONArrayEquals(jsonAnswers as JSONArray, fetchAnswers as JSONArray));
})
