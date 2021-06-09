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

import {tx} from "../../../connection/ConnectionStepsBase";
import {Then, When} from "@cucumber/cucumber";
import assert from "assert";
import {AttributeType} from "../../../../../dist/api/concept/type/AttributeType";
import {TypeDBClientError} from "../../../../../dist/common/errors/TypeDBClientError";
import {ErrorMessage} from "../../../../../dist/common/errors/ErrorMessage";
import DataTable from "@cucumber/cucumber/lib/models/data_table";
import {parseList} from "../../../config/Parameters";
import ValueType = AttributeType.ValueType;

When("put attribute type: {type_label}, with value type: {value_type}", async (typeLabel: string, valueType: ValueType) => {
    await tx().concepts().putAttributeType(typeLabel, valueType);
});

Then("attribute\\({type_label}) get value type: {value_type}", async (typeLabel: string, valueType: ValueType) => {
    assert.strictEqual((await tx().concepts().getAttributeType(typeLabel)).getValueType(), valueType);
});

Then("attribute\\({type_label}) get supertype value type: {value_type}", async (typeLabel: string, valueType: ValueType) => {
    const supertype = await (await tx().concepts().getAttributeType(typeLabel)).asRemote(tx()).getSupertype();
    assert.strictEqual((supertype as AttributeType).getValueType(), valueType);
});

async function attributeTypeAsValueType(typeLabel: string, valueType: ValueType) {
    const attributeType = await tx().concepts().getAttributeType(typeLabel);
    switch (valueType) {
        case ValueType.OBJECT:
            return attributeType;
        case ValueType.BOOLEAN:
            return attributeType.asBoolean();
        case ValueType.LONG:
            return attributeType.asLong();
        case ValueType.DOUBLE:
            return attributeType.asDouble();
        case ValueType.STRING:
            return attributeType.asString();
        case ValueType.DATETIME:
            return attributeType.asDateTime();
        default:
            throw new TypeDBClientError(ErrorMessage.Concept.BAD_VALUE_TYPE.message(valueType));
    }
}

Then("attribute\\({type_label}) as\\({value_type}) get subtypes contain:", async (typeLabel: string, valueType: ValueType, subLabelsTable: DataTable) => {
    const subLabels = parseList(subLabelsTable);
    const attributeType = await attributeTypeAsValueType(typeLabel, valueType);
    const actuals = await attributeType.asRemote(tx()).getSubtypes().map(tt => tt.getLabel().scopedName()).collect();
    await subLabels.every(sl => assert(actuals.includes(sl)));
});

Then("attribute\\({type_label}) as\\({value_type}) get subtypes do not contain:", async (typeLabel: string, valueType: ValueType, subLabelsTable: DataTable) => {
    const subLabels = parseList(subLabelsTable);
    const attributeType = await attributeTypeAsValueType(typeLabel, valueType);
    const actuals = await attributeType.asRemote(tx()).getSubtypes().map(tt => tt.getLabel().scopedName()).collect();
    await subLabels.every(sl => assert(!actuals.includes(sl)));
});

Then("attribute\\({type_label}) as\\({value_type}) set regex: {}", async (typeLabel: string, valueType: ValueType, regex: string) => {
    assert(valueType == ValueType.STRING);
    const attributeType = await attributeTypeAsValueType(typeLabel, valueType);
    await attributeType.asString().asRemote(tx()).setRegex(regex);
});

Then("attribute\\({type_label}) as\\({value_type}) unset regex", async (typeLabel: string, valueType: ValueType) => {
    assert(valueType == ValueType.STRING);
    const attributeType = await attributeTypeAsValueType(typeLabel, valueType);
    await attributeType.asString().asRemote(tx()).setRegex(null);
});

Then("attribute\\({type_label}) as\\({value_type}) get regex: {}", async (typeLabel: string, valueType: ValueType, regex: string) => {
    assert(valueType == ValueType.STRING);
    const attributeType = await attributeTypeAsValueType(typeLabel, valueType);
    assert.strictEqual(await attributeType.asString().asRemote(tx()).getRegex(), regex);
});

Then("attribute\\({type_label}) as\\({value_type}) does not have any regex", async (typeLabel: string, valueType: ValueType) => {
    assert(valueType == ValueType.STRING);
    const attributeType = await attributeTypeAsValueType(typeLabel, valueType);
    assert(!(await attributeType.asString().asRemote(tx()).getRegex()));
});

Then("attribute\\({type_label}) get key owners contain:", async (typeLabel: string, ownerLabelsTable: DataTable) => {
    const ownerLabels = parseList(ownerLabelsTable);
    const attributeType = await tx().concepts().getAttributeType(typeLabel);
    const actuals = await attributeType.asRemote(tx()).getOwners(true).map(tt => tt.getLabel().scopedName()).collect();
    await ownerLabels.every(ol => assert(actuals.includes(ol)));
});

Then("attribute\\({type_label}) get key owners do not contain:", async (typeLabel: string, ownerLabelsTable: DataTable) => {
    const ownerLabels = parseList(ownerLabelsTable);
    const attributeType = await tx().concepts().getAttributeType(typeLabel);
    const actuals = await attributeType.asRemote(tx()).getOwners(true).map(tt => tt.getLabel().scopedName()).collect();
    await ownerLabels.every(ol => assert(!actuals.includes(ol)));
});

Then("attribute\\({type_label}) get attribute owners contain:", async (typeLabel: string, ownerLabelsTable: DataTable) => {
    const ownerLabels = parseList(ownerLabelsTable);
    const attributeType = await tx().concepts().getAttributeType(typeLabel);
    const actuals = await attributeType.asRemote(tx()).getOwners(false).map(tt => tt.getLabel().scopedName()).collect();
    await ownerLabels.every(ol => assert(actuals.includes(ol)));
});

Then("attribute\\({type_label}) get attribute owners do not contain:", async (typeLabel: string, ownerLabelsTable: DataTable) => {
    const ownerLabels = parseList(ownerLabelsTable);
    const attributeType = await tx().concepts().getAttributeType(typeLabel);
    const actuals = await attributeType.asRemote(tx()).getOwners(false).map(tt => tt.getLabel().scopedName()).collect();
    await ownerLabels.every(ol => assert(!actuals.includes(ol)));
});
