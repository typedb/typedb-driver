/*
 * Copyright (C) 2021 Vaticle
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
import {tx} from "../connection/ConnectionStepsBase";
import {assertThrows, assertThrowsWithMessage, splitString} from "../util/Util";
import {ConceptMap} from "../../../dist/api/answer/ConceptMap";
import {Numeric} from "../../../dist/api/answer/Numeric";
import {ConceptMapGroup} from "../../../dist/api/answer/ConceptMapGroup";
import {NumericGroup} from "../../../dist/api/answer/NumericGroup";
import {Concept} from "../../../dist/api/concept/Concept";
import {RoleType} from "../../../dist/api/concept/type/RoleType";
import {Type} from "../../../dist/api/concept/type/Type";
import {AttributeType} from "../../../dist/api/concept/type/AttributeType";
import {Attribute} from "../../../dist/api/concept/thing/Attribute";
import {Thing} from "../../../dist/api/concept/thing/Thing";
import {parseBool} from "../config/Parameters";
import DataTable from "@cucumber/cucumber/lib/models/data_table";
import {fail} from "assert";
import assert = require("assert");
import ValueClass = AttributeType.ValueClass;

let answers: ConceptMap[] = [];
let numericAnswer: Numeric;
let answerGroups: ConceptMapGroup[] = []
let numericAnswerGroups: NumericGroup[] = []

function clearAnswers() {
    answers.length = 0;
    numericAnswer = null;
    answerGroups.length = 0;
    numericAnswerGroups.length = 0;
}

Then("the integrity is validated", async () => {
    // TODO
});

When("typeql define", async (query: string) => {
    await tx().query().define(query);
});

Then("typeql define; throws exception containing {string}", async (exceptionString: string, query: string) => {
    await assertThrowsWithMessage(async () => await tx().query().define(query), exceptionString);
});

Then("typeql define; throws exception", async (query: string) => {
    await assertThrows(async () => await tx().query().define(query));
});

When("typeql undefine", async (query: string) => {
    await tx().query().undefine(query);
});

Then("typeql undefine; throws exception containing {string}", async (exceptionString: string, query: string) => {
    await assertThrowsWithMessage(async () => await tx().query().undefine(query), exceptionString);
});

Then("typeql undefine; throws exception", async (query: string) => {
    await assertThrows(async () => await tx().query().undefine(query));
});

When("typeql insert", async (query: string) => {
    await tx().query().insert(query).collect();
});

Then("typeql insert; throws exception containing {string}", async (exceptionString: string, query: string) => {
    await assertThrowsWithMessage(async () => await tx().query().insert(query).first(), exceptionString);
});

Then("typeql insert; throws exception", async (query: string) => {
    await assertThrows(async () => await tx().query().insert(query).first());
});

When("typeql delete", async (query: string) => {
    await tx().query().delete(query);
});

Then("typeql delete; throws exception containing {string}", async (exceptionString: string, query: string) => {
    await assertThrowsWithMessage(async () => await tx().query().delete(query), exceptionString);
});

Then("typeql delete; throws exception", async (query: string) => {
    await assertThrows(async () => await tx().query().delete(query));
});

When("typeql update", async (query: string) => {
    await tx().query().update(query).collect();
});

Then("typeql update; throws exception containing {string}", async (exceptionString: string, query: string) => {
    await assertThrowsWithMessage(async () => await tx().query().update(query).first(), exceptionString);
});

Then("typeql update; throws exception", async (query: string) => {
    await assertThrows(async () => await tx().query().update(query).first());
});

When("get answers of typeql insert", async (query: string) => {
    clearAnswers();
    answers = await tx().query().insert(query).collect();
});

When("get answers of typeql update", async (query: string) => {
    clearAnswers();
    answers = await tx().query().update(query).collect();
});

When("get answers of typeql match", async (query: string) => {
    clearAnswers();
    answers = await tx().query().match(query).collect();
});

Then("typeql match; throws exception", async (query: string) => {
    await assertThrows(async () => await tx().query().match(query).first());
});

When("get answer of typeql match aggregate", async (query: string) => {
    clearAnswers();
    numericAnswer = await tx().query().matchAggregate(query);
});

When("get answers of typeql match group", async (query: string) => {
    clearAnswers();
    answerGroups = await tx().query().matchGroup(query).collect();
});

When("typeql match group; throws exception", async (query: string) => {
    await assertThrows(async () => await tx().query().matchGroup(query).first());
})

When("get answers of typeql match group aggregate", async (query: string) => {
    clearAnswers();
    numericAnswerGroups = await tx().query().matchGroupAggregate(query).collect();
});

When("typeql match aggregate; throws exception", async (query: string) => {
    await assertThrows(async () => await tx().query().matchAggregate(query));

})

Then("answer size is: {number}", async (expectedAnswers: number) => {
    assert.strictEqual(answers.length, expectedAnswers, `Expected [${expectedAnswers}], but got [${answers.length}]`);
});

Then("rules contain: {type_label}", async (ruleLabel: string) => {
    for await (const rule of (await tx().logic().getRules())) {
        if (rule.getLabel() === ruleLabel) return;
    }
    assert.fail();
});

Then("rules do not contain: {type_label}", async (ruleLabel: string) => {
    for await (const rule of (await tx().logic().getRules())) {
        if (rule.getLabel() === ruleLabel) assert.fail();
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
        if (concept.isRoleType()) return this.label == (concept as RoleType).getLabel().scopedName();
        else if (concept.isType()) return this.label == (concept as Type).getLabel().scopedName();
        else throw new TypeError("A Concept was matched by label, but it is not a Type.");
    }
}

abstract class AttributeMatcher implements ConceptMatcher {
    private readonly _typeLabel: string;
    private readonly _value: string;

    constructor(typeAndValue: string) {
        const s = typeAndValue.split(":");
        assert.strictEqual(s.length, 2, `[${typeAndValue}] is not a valid attribute identifier. It should have format "typeLabel:value".`);
        [this._typeLabel, this._value] = s;
    }

    protected get typeLabel(): string {
        return this._typeLabel;
    }

    protected get value(): string {
        return this._value;
    }

    check(attribute: Attribute<ValueClass>) {
        if (attribute.isBoolean()) return attribute.getValue() === parseBool(this.value);
        else if (attribute.isLong()) {
            return attribute.getValue() === parseInt(this.value);
        }
        else if (attribute.isDouble()) return attribute.getValue() === parseFloat(this.value);
        else if (attribute.isString()) return attribute.getValue() === this.value;
        else if (attribute.isDateTime()) return (attribute.getValue() as Date).getTime() === new Date(this.value).getTime();
        else throw new Error(`Unrecognised value type ${attribute.constructor.name}`);
    }

    abstract matches(concept: Concept): Promise<boolean>;
}

class AttributeValueMatcher extends AttributeMatcher {

    async matches(concept: Concept): Promise<boolean> {
        if (!concept.isAttribute()) return false;

        const attribute = concept as Attribute<ValueClass>;

        if (this.typeLabel !== attribute.getType().getLabel().scopedName()) return false;

        return this.check(attribute);
    }
}

class ThingKeyMatcher extends AttributeMatcher {

    async matches(concept: Concept): Promise<boolean> {
        if (!concept.isThing()) return false;

        const keys = await (concept as Thing).asRemote(tx()).getHas(true).collect();

        for (const key of keys) {
            if (key.getType().getLabel().scopedName() === this.typeLabel) {
                return this.check(key);
            }
        }

        return false;
    }
}

function parseConceptIdentifier(value: string): ConceptMatcher {
    const [identifierType, identifierBody] = splitString(value, ":", 1);
    switch (identifierType) {
        case "label": return new TypeLabelMatcher(identifierBody);
        case "key": return new ThingKeyMatcher(identifierBody);
        case "value": return new AttributeValueMatcher(identifierBody);
        default: throw new Error(`Failed to parse concept identifier: ${value}`);
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

function getNumericValue(numeric: Numeric) {
    if (numeric.isNumber()) return numeric.asNumber();
    else if (numeric.isNaN()) return NaN;
    else throw new Error(`Unexpected Numeric value: ${numeric}`);
}

function assertNumericValue(numeric: Numeric, expectedAnswer: number, reason?: string) {
    if (numeric.isNumber()) {
        assert(Math.abs(expectedAnswer - numeric.asNumber()) < 0.001, reason);
    } else {
        fail();
    }
}

Then("aggregate value is: {float}", async (expectedAnswer: number) => {
    assert(numericAnswer != null, "The last query executed was not an aggregate query.");
    assertNumericValue(numericAnswer, expectedAnswer);
});

Then("aggregate answer is not a number", async () => {
    assert(numericAnswer.isNaN());
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
            if (await identifier.matches(group.owner())) {
                answerGroup = group;
                break;
            }
        }
        assert(answerGroup, `The group identifier [${JSON.stringify(answerIdentifierGroup.ownerIdentifier)}] does not match any of the answer group owners.`);

        const resultSet: [AnswerIdentifier, ConceptMap[]][] = answerIdentifierGroup.answerIdentifiers.map(ai => [ai, []]);
        for (const answer0 of answerGroup.conceptMaps()) {
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
    assert.strictEqual(numericAnswerGroups.length, Object.keys(expectations).length,
        `Expected [${Object.keys(expectations).length}], but found [${numericAnswerGroups.length}].`);

    for (const [ownerIdentifier, expectedAnswer] of Object.entries(expectations)) {
        const identifier = parseConceptIdentifier(ownerIdentifier);
        let numericGroup;
        for (const group of numericAnswerGroups) {
            if (await identifier.matches(group.owner())) {
                numericGroup = group;
                break;
            }
        }
        assert(numericGroup, `The group identifier [${JSON.stringify(ownerIdentifier)}] does not match any of the answer group owners.`);

        const actualAnswer = getNumericValue(numericGroup.numeric());
        assertNumericValue(numericGroup.numeric(), expectedAnswer,
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
        if (answer.map().has(requiredVariable)) {
            const concept = answer.get(requiredVariable);
            if (!concept.isThing()) throw new TypeError("Cannot apply IID templating to Types");
            query += (concept as Thing).getIID();
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
        assert.strictEqual((await tx().query().match(query).collect()).length, 1);
    }
});

Then("each answer does not satisfy", async (template: string) => {
    for (const answer of answers) {
        const query = applyQueryTemplate(template, answer);
        assert.strictEqual((await tx().query().match(query).collect()).length, 0);
    }
});
