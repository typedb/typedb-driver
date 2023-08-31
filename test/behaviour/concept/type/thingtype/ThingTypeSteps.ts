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
import assert from "assert";
import {Concept, ThingType} from "../../../../../dist";
import {parseList, RootLabel, ScopedLabel} from "../../../config/Parameters";
import {tx} from "../../../connection/ConnectionStepsBase";
import {assertThrows} from "../../../util/Util";
import Annotation = ThingType.Annotation;
import EXPLICIT = Concept.Transitivity.EXPLICIT;

export function getThingType(rootLabel: RootLabel, typeLabel: string): Promise<ThingType> {
    switch (rootLabel) {
        case RootLabel.ENTITY:
            return tx().concepts.getEntityType(typeLabel);
        case RootLabel.ATTRIBUTE:
            return tx().concepts.getAttributeType(typeLabel);
        case RootLabel.RELATION:
            return tx().concepts.getRelationType(typeLabel);
        case RootLabel.THING:
            return tx().concepts.getRootThingType();
        default:
            throw "Unsupported type"
    }
}

When("thing type root get supertypes contain:", async (supertypesTable: DataTable) => {
    const supertypes = parseList(supertypesTable);
    const actuals = await (await tx().concepts.getRootThingType()).getSupertypes(tx()).map(tt => tt.label.scopedName).collect();
    supertypes.every(st => assert(actuals.includes(st)));
});

When("thing type root get supertypes do not contain:", async (supertypesTable: DataTable) => {
    const supertypes = parseList(supertypesTable);
    const actuals = await (await tx().concepts.getRootThingType()).getSupertypes(tx()).map(tt => tt.label.scopedName).collect();
    supertypes.every(st => assert(!actuals.includes(st)));
});

When("thing type root get subtypes contain:", async (subtypesTable: DataTable) => {
    const subtypes = parseList(subtypesTable);
    const actuals = await (await tx().concepts.getRootThingType()).getSubtypes(tx()).map(tt => tt.label.scopedName).collect();
    subtypes.every(st => assert(actuals.includes(st)));
});

When("thing type root get subtypes do not contain:", async (subtypesTable: DataTable) => {
    const subtypes = parseList(subtypesTable);
    const actuals = await (await tx().concepts.getRootThingType()).getSubtypes(tx()).map(tt => tt.label.scopedName).collect();
    subtypes.every(st => assert(!actuals.includes(st)));
});

When("put {root_label} type: {type_label}", async (rootLabel: RootLabel, typeLabel: string) => {
    switch (rootLabel) {
        case RootLabel.ENTITY:
            await tx().concepts.putEntityType(typeLabel);
            break;
        case RootLabel.RELATION:
            await tx().concepts.putRelationType(typeLabel);
            break;
        default:
            throw `Could not put ${typeLabel} of type Attribute`;
    }
});

When("delete {root_label} type: {type_label}", async (rootLabel: RootLabel, typeLabel: string) => {
    await (await getThingType(rootLabel, typeLabel)).delete(tx());
});

When("delete {root_label} type: {type_label}; throws exception", async (rootLabel: RootLabel, typeLabel: string) => {
    await assertThrows(async () => await (await getThingType(rootLabel, typeLabel)).delete(tx()));
});

When("{root_label}\\({type_label}) is null: {bool}", async (rootLabel: RootLabel, typeLabel: string, isNull: boolean) => {
    assert.strictEqual(null === await getThingType(rootLabel, typeLabel), isNull);
});

When("{root_label}\\({type_label}) set label: {type_label}", async (rootLabel: RootLabel, typeLabel: string, label: string) => {
    await (await getThingType(rootLabel, typeLabel)).setLabel(tx(), label);
});

When("{root_label}\\({type_label}) get label: {type_label}", async (rootLabel: RootLabel, typeLabel: string, label: string) => {
    await assert.strictEqual((await getThingType(rootLabel, typeLabel)).label.scopedName, label);
});

When("{root_label}\\({type_label}) set abstract: {bool}", async (rootLabel: RootLabel, typeLabel: string, isAbstract: boolean) => {
    if (isAbstract) {
        await (await getThingType(rootLabel, typeLabel)).setAbstract(tx());
    } else {
        await (await getThingType(rootLabel, typeLabel)).unsetAbstract(tx());
    }
});

When("{root_label}\\({type_label}) set abstract: {bool}; throws exception", async (rootLabel: RootLabel, typeLabel: string, isAbstract: boolean) => {
    if (isAbstract) {
        await assertThrows(async () => await (await getThingType(rootLabel, typeLabel)).setAbstract(tx()));
    } else {
        await assertThrows(async () => await (await getThingType(rootLabel, typeLabel)).unsetAbstract(tx()));
    }
});

When("{root_label}\\({type_label}) is abstract: {bool}", async (rootLabel: RootLabel, typeLabel: string, isAbstract: boolean) => {
    assert.strictEqual((await getThingType(rootLabel, typeLabel)).abstract, isAbstract);
});

When("{root_label}\\({type_label}) set supertype: {type_label}", async (rootLabel: RootLabel, typeLabel: string, superLabel: string) => {
    switch (rootLabel) {
        case RootLabel.ENTITY: {
            const entitySuperType = await tx().concepts.getEntityType(superLabel);
            return await (await tx().concepts.getEntityType(typeLabel)).setSupertype(tx(), entitySuperType);
        }
        case RootLabel.ATTRIBUTE: {
            const attributeSuperType = await tx().concepts.getAttributeType(superLabel);
            return await (await tx().concepts.getAttributeType(typeLabel)).setSupertype(tx(), attributeSuperType);
        }
        case RootLabel.RELATION: {
            const relationSuperType = await tx().concepts.getRelationType(superLabel);
            return await (await tx().concepts.getRelationType(typeLabel)).setSupertype(tx(), relationSuperType);
        }
        default:
            throw "Unsupported type"
    }
});

When("{root_label}\\({type_label}) set supertype: {type_label}; throws exception", async (rootLabel: RootLabel, typeLabel: string, superLabel: string) => {
    switch (rootLabel) {
        case RootLabel.ENTITY: {
            const entitySuperType = await tx().concepts.getEntityType(superLabel);
            return await assertThrows(async () => await (await tx().concepts.getEntityType(typeLabel)).setSupertype(tx(), entitySuperType));
        }
        case RootLabel.ATTRIBUTE: {
            const attributeSuperType = await tx().concepts.getAttributeType(superLabel);
            return await assertThrows(async () => await (await tx().concepts.getAttributeType(typeLabel)).setSupertype(tx(), attributeSuperType));
        }
        case RootLabel.RELATION: {
            const relationSuperType = await tx().concepts.getRelationType(superLabel);
            return await assertThrows(async () => await (await tx().concepts.getRelationType(typeLabel)).setSupertype(tx(), relationSuperType));
        }
        default:
            throw "Unsupported type"
    }
});

Then("{root_label}\\({type_label}) get supertype: {type_label}", async (rootLabel: RootLabel, typeLabel: string, superLabel: string) => {
    const supertype = await getThingType(rootLabel, superLabel);
    assert((await (await getThingType(rootLabel, typeLabel)).getSupertype(tx())).equals(supertype));
});

Then("{root_label}\\({type_label}) get supertypes contain:", async (rootLabel: RootLabel, typeLabel: string, superLabelsTable: DataTable) => {
    const superLabels = parseList(superLabelsTable);
    const thingType = await getThingType(rootLabel, typeLabel);
    const actuals = await thingType.getSupertypes(tx()).map(tt => tt.label.scopedName).collect();
    superLabels.every(sl => assert(actuals.includes(sl)));
});

Then("{root_label}\\({type_label}) get supertypes do not contain:", async (rootLabel: RootLabel, typeLabel: string, superLabelsTable: DataTable) => {
    const superLabels = parseList(superLabelsTable);
    const thingType = await getThingType(rootLabel, typeLabel);
    const actuals = await thingType.getSupertypes(tx()).map(tt => tt.label.scopedName).collect();
    superLabels.every(sl => assert(!actuals.includes(sl)));
});

Then("{root_label}\\({type_label}) get subtypes contain:", async (rootLabel: RootLabel, typeLabel: string, subLabelsTable: DataTable) => {
    const subLabels = parseList(subLabelsTable);
    const thingType = await getThingType(rootLabel, typeLabel);
    const actuals = await thingType.getSubtypes(tx()).map(tt => tt.label.scopedName).collect();
    subLabels.every(sl => assert(actuals.includes(sl)));
});

Then("{root_label}\\({type_label}) get subtypes do not contain:", async (rootLabel: RootLabel, typeLabel: string, subLabelsTable: DataTable) => {
    const subLabels = parseList(subLabelsTable);
    const thingType = await getThingType(rootLabel, typeLabel);
    const actuals = await thingType.getSubtypes(tx()).map(tt => tt.label.scopedName).collect();
    subLabels.every(sl => assert(!actuals.includes(sl)));
});

Then("{root_label}\\({type_label}) get owns attribute types contain:", async (rootLabel: RootLabel, typeLabel: string, attributeLabelsTable: DataTable) => {
    const attributeLabels = parseList(attributeLabelsTable);
    const actuals = await (await getThingType(rootLabel, typeLabel)).getOwns(tx()).map(tt => tt.label.scopedName).collect();
    attributeLabels.every(al => assert(actuals.includes(al)));
});

Then(
    "{root_label}\\({type_label}) get owns attribute types, with annotations: {annotations}; contain:",
    async (rootLabel: RootLabel, typeLabel: string, annotations: Annotation[], attributeLabelsTable: DataTable) => {
        const attributeLabels = parseList(attributeLabelsTable);
        const actuals = await (await getThingType(rootLabel, typeLabel)).getOwns(tx(), annotations).map(tt => tt.label.scopedName).collect();
        attributeLabels.every(al => assert(actuals.includes(al)));
    }
);

Then("{root_label}\\({type_label}) get owns attribute types do not contain:", async (rootLabel: RootLabel, typeLabel: string, attributeLabelsTable: DataTable) => {
    const attributeLabels = parseList(attributeLabelsTable);
    const actuals = await (await getThingType(rootLabel, typeLabel)).getOwns(tx()).map(tt => tt.label.scopedName).collect();
    attributeLabels.every(al => assert(!actuals.includes(al)));
});

Then(
    "{root_label}\\({type_label}) get owns attribute types, with annotations: {annotations}; do not contain:",
    async (rootLabel: RootLabel, typeLabel: string, annotations: Annotation[], attributeLabelsTable: DataTable) => {
        const attributeLabels = parseList(attributeLabelsTable);
        const actuals = await (await getThingType(rootLabel, typeLabel)).getOwns(tx(), annotations).map(tt => tt.label.scopedName).collect();
        attributeLabels.every(al => assert(!actuals.includes(al)));
    }
);

Then("{root_label}\\({type_label}) get owns explicit attribute types contain:", async (rootLabel: RootLabel, typeLabel: string, attributeLabelsTable: DataTable) => {
    const attributeLabels = parseList(attributeLabelsTable);
    const actuals = await (await getThingType(rootLabel, typeLabel)).getOwns(tx(), EXPLICIT).map(tt => tt.label.scopedName).collect();
    attributeLabels.every(al => assert(actuals.includes(al)));
});

Then(
    "{root_label}\\({type_label}) get owns explicit attribute types, with annotations: {annotations}; contain:",
    async (rootLabel: RootLabel, typeLabel: string, annotations: Annotation[], attributeLabelsTable: DataTable) => {
        const attributeLabels = parseList(attributeLabelsTable);
        const actuals = await (await getThingType(rootLabel, typeLabel))
            .getOwns(tx(), annotations, EXPLICIT)
            .map(tt => tt.label.scopedName).collect();
        attributeLabels.every(al => assert(actuals.includes(al)));
    }
);

Then("{root_label}\\({type_label}) get owns explicit attribute types do not contain:", async (rootLabel: RootLabel, typeLabel: string, attributeLabelsTable: DataTable) => {
    const attributeLabels = parseList(attributeLabelsTable);
    const actuals = await (await getThingType(rootLabel, typeLabel)).getOwns(tx(), EXPLICIT).map(tt => tt.label.scopedName).collect();
    attributeLabels.every(al => assert(!actuals.includes(al)));
});

Then(
    "{root_label}\\({type_label}) get owns explicit attribute types, with annotations: {annotations}; do not contain:",
    async (rootLabel: RootLabel, typeLabel: string, annotations: Annotation[], attributeLabelsTable: DataTable) => {
        const attributeLabels = parseList(attributeLabelsTable);
        const actuals = await (await getThingType(rootLabel, typeLabel)).getOwns(tx(), annotations, EXPLICIT).map(tt => tt.label.scopedName).collect();
        attributeLabels.every(al => assert(!actuals.includes(al)));
    }
);

When("{root_label}\\({type_label}) set owns attribute type: {type_label}", async (rootLabel: RootLabel, typeLabel: string, attributeLabel: string) => {
    const attributeType = await tx().concepts.getAttributeType(attributeLabel);
    await (await getThingType(rootLabel, typeLabel)).setOwns(tx(), attributeType);
});

When(
    "{root_label}\\({type_label}) set owns attribute type: {type_label}, with annotations: {annotations}",
    async (rootLabel: RootLabel, typeLabel: string, attTypeLabel: string, annotations: Annotation[]) => {
        const attributeType = await tx().concepts.getAttributeType(attTypeLabel);
        await (await getThingType(rootLabel, typeLabel)).setOwns(tx(), attributeType, annotations);
    }
);

Then("{root_label}\\({type_label}) set owns attribute type: {type_label}; throws exception", async (rootLabel: RootLabel, typeLabel: string, attributeLabel: string) => {
    const attributeType = await tx().concepts.getAttributeType(attributeLabel);
    await assertThrows(async () => await (await getThingType(rootLabel, typeLabel)).setOwns(tx(), attributeType));
});

Then(
    "{root_label}\\({type_label}) set owns attribute type: {type_label}, with annotations: {annotations}; throws exception",
    async (rootLabel: RootLabel, typeLabel: string, attributeLabel: string, annotations: Annotation[]) => {
        const attributeType = await tx().concepts.getAttributeType(attributeLabel);
        await assertThrows(async () => await (await getThingType(rootLabel, typeLabel)).setOwns(tx(), attributeType, annotations));
    }
);

When("{root_label}\\({type_label}) set owns attribute type: {type_label} as {type_label}", async (rootLabel: RootLabel, typeLabel: string, attributeLabel: string, overriddenLabel: string) => {
    const attributeType = await tx().concepts.getAttributeType(attributeLabel);
    const overriddenType = await tx().concepts.getAttributeType(overriddenLabel);
    await (await getThingType(rootLabel, typeLabel)).setOwns(tx(), attributeType, overriddenType);
});

When(
    "{root_label}\\({type_label}) set owns attribute type: {type_label} as {type_label}, with annotations: {annotations}",
    async (rootLabel: RootLabel, typeLabel: string, attTypeLabel: string, overriddenLabel: string, annotations: Annotation[]) => {
        const attributeType = await tx().concepts.getAttributeType(attTypeLabel);
        const overriddenType = await tx().concepts.getAttributeType(overriddenLabel);
        await (await getThingType(rootLabel, typeLabel))
            .setOwns(tx(), attributeType, overriddenType, annotations);
    }
);

Then("{root_label}\\({type_label}) set owns attribute type: {type_label} as {type_label}; throws exception", async (rootLabel: RootLabel, typeLabel: string, attributeLabel: string, overriddenLabel: string) => {
    const attributeType = await tx().concepts.getAttributeType(attributeLabel);
    const overriddenType = await tx().concepts.getAttributeType(overriddenLabel);
    await assertThrows(async () => await (await getThingType(rootLabel, typeLabel))
        .setOwns(tx(), attributeType, overriddenType));
});

Then(
    "{root_label}\\({type_label}) set owns attribute type: {type_label} as {type_label}, with annotations: {annotations}; throws exception",
    async (rootLabel: RootLabel, typeLabel: string, attributeLabel: string, overriddenLabel: string, annotations: Annotation[]) => {
        const attributeType = await tx().concepts.getAttributeType(attributeLabel);
        const overriddenType = await tx().concepts.getAttributeType(overriddenLabel);
        await assertThrows(async () => await (await getThingType(rootLabel, typeLabel))
            .setOwns(tx(), attributeType, overriddenType, annotations));
    }
);

When("{root_label}\\({type_label}) unset owns attribute type: {type_label}", async (rootLabel: RootLabel, typeLabel: string, attributeLabel: string) => {
    const attributeType = await tx().concepts.getAttributeType(attributeLabel);
    await (await getThingType(rootLabel, typeLabel)).unsetOwns(tx(), attributeType);
});

When("{root_label}\\({type_label}) unset owns attribute type: {type_label}; throws exception", async (rootLabel: RootLabel, typeLabel: string, attributeLabel: string) => {
    const attributeType = await tx().concepts.getAttributeType(attributeLabel);
    await assertThrows(async () => await (await getThingType(rootLabel, typeLabel)).unsetOwns(tx(), attributeType));
});

Then("{root_label}\\({type_label}) get owns overridden attribute\\({type_label}) is null: {bool}", async (rootLabel: RootLabel, typeLabel: string, attributeLabel: string, isNull: boolean) => {
    const attributeType = await tx().concepts.getAttributeType(attributeLabel);
    assert.strictEqual(null === await (await getThingType(rootLabel, typeLabel)).getOwnsOverridden(tx(), attributeType), isNull);
});

Then("{root_label}\\({type_label}) get owns overridden attribute\\({type_label}) get label: {type_label}", async (rootLabel: RootLabel, typeLabel: string, attributeLabel: string, getLabel: string) => {
    const attributeType = await tx().concepts.getAttributeType(attributeLabel);
    assert.strictEqual((await (await getThingType(rootLabel, typeLabel)).getOwnsOverridden(tx(), attributeType)).label.name, getLabel);
});

When("{root_label}\\({type_label}) set plays role: {scoped_label}", async (rootLabel: RootLabel, typeLabel: string, roleLabel: ScopedLabel) => {
    const roleType = await (await tx().concepts.getRelationType(roleLabel.scope)).getRelatesForRoleLabel(tx(), roleLabel.role);
    await (await getThingType(rootLabel, typeLabel)).setPlays(tx(), roleType);
});

When("{root_label}\\({type_label}) set plays role: {scoped_label}; throws exception", async (rootLabel: RootLabel, typeLabel: string, roleLabel: ScopedLabel) => {
    const roleType = await (await tx().concepts.getRelationType(roleLabel.scope)).getRelatesForRoleLabel(tx(), roleLabel.role);
    await assertThrows(async () => await (await getThingType(rootLabel, typeLabel)).setPlays(tx(), roleType));
});

When("{root_label}\\({type_label}) set plays role: {scoped_label} as {scoped_label}", async (rootLabel: RootLabel, typeLabel: string, roleLabel: ScopedLabel, overriddenLabel: ScopedLabel) => {
    const roleType = await (await tx().concepts.getRelationType(roleLabel.scope)).getRelatesForRoleLabel(tx(), roleLabel.role);
    const overriddenType = await (await tx().concepts.getRelationType(overriddenLabel.scope)).getRelatesForRoleLabel(tx(), overriddenLabel.role);
    await (await getThingType(rootLabel, typeLabel)).setPlays(tx(), roleType, overriddenType);
});

When("{root_label}\\({type_label}) set plays role: {scoped_label} as {scoped_label}; throws exception", async (rootLabel: RootLabel, typeLabel: string, roleLabel: ScopedLabel, overriddenLabel: ScopedLabel) => {
    const roleType = await (await tx().concepts.getRelationType(roleLabel.scope)).getRelatesForRoleLabel(tx(), roleLabel.role);
    const overriddenType = await (await tx().concepts.getRelationType(overriddenLabel.scope)).getRelatesForRoleLabel(tx(), overriddenLabel.role);
    await assertThrows(async () => await (await getThingType(rootLabel, typeLabel)).setPlays(tx(), roleType, overriddenType));
});

When("{root_label}\\({type_label}) unset plays role: {scoped_label}", async (rootLabel: RootLabel, typeLabel: string, roleLabel: ScopedLabel) => {
    const roleType = await (await tx().concepts.getRelationType(roleLabel.scope)).getRelatesForRoleLabel(tx(), roleLabel.role);
    await (await getThingType(rootLabel, typeLabel)).unsetPlays(tx(), roleType);
});

When("{root_label}\\({type_label}) unset plays role: {scoped_label}; throws exception", async (rootLabel: RootLabel, typeLabel: string, roleLabel: ScopedLabel) => {
    const roleType = await (await tx().concepts.getRelationType(roleLabel.scope)).getRelatesForRoleLabel(tx(), roleLabel.role);
    await assertThrows(async () => await (await getThingType(rootLabel, typeLabel)).unsetPlays(tx(), roleType));
});

Then("{root_label}\\({type_label}) get playing roles contain:", async (rootLabel: RootLabel, typeLabel: string, roleLabelsTable: DataTable) => {
    const roleLabels = parseList(roleLabelsTable);
    const actuals = await (await getThingType(rootLabel, typeLabel)).getPlays(tx()).map(r => r.label.scopedName).collect();
    roleLabels.every(rl => assert(actuals.includes(rl)));
});

Then("{root_label}\\({type_label}) get playing roles do not contain:", async (rootLabel: RootLabel, typeLabel: string, roleLabelsTable: DataTable) => {
    const roleLabels = parseList(roleLabelsTable);
    const actuals = await (await getThingType(rootLabel, typeLabel)).getPlays(tx()).map(r => r.label.scopedName).collect();
    roleLabels.every(rl => assert(!actuals.includes(rl)));
});

Then("{root_label}\\({type_label}) get playing roles explicit contain:", async (rootLabel: RootLabel, typeLabel: string, roleLabelsTable: DataTable) => {
    const roleLabels = parseList(roleLabelsTable);
    const actuals = await (await getThingType(rootLabel, typeLabel)).getPlays(tx(), EXPLICIT).map(r => r.label.scopedName).collect();
    roleLabels.every(rl => assert(actuals.includes(rl)));
});

Then("{root_label}\\({type_label}) get playing roles explicit do not contain:", async (rootLabel: RootLabel, typeLabel: string, roleLabelsTable: DataTable) => {
    const roleLabels = parseList(roleLabelsTable);
    const actuals = await (await getThingType(rootLabel, typeLabel)).getPlays(tx(), EXPLICIT).map(r => r.label.scopedName).collect();
    roleLabels.every(rl => assert(!actuals.includes(rl)));
});
