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

import {defineParameterType} from "@cucumber/cucumber";
import DataTable from "@cucumber/cucumber/lib/models/data_table";
import {Concept, ThingType, TransactionType} from "../../../dist";
import Annotation = ThingType.Annotation;

export function parseBool(value: string): boolean {
    return value === "true";
}

defineParameterType({
    name: "bool",
    regexp: /true|false/,
    transformer: parseBool,
});

defineParameterType({
    name: "number",
    regexp: /[0-9]+/,
    transformer: s => parseInt(s),
});

defineParameterType({
    name: "datetime",
    regexp: /\d\d\d\d-\d\d-\d\d \d\d:\d\d:\d\d/,
    transformer: s =>
     {
       const date = new Date(s);
       const userTimezoneOffset = date.getTimezoneOffset() * 60000;
       return new Date(date.getTime() - userTimezoneOffset);
     }
});

defineParameterType({
    name: "Timezone",
    regexp: /[A-Za-z]+\/[A-Za-z]+/,
    transformer: s => s
});

defineParameterType({
    name: "words",
    regexp: /[\w-_]+/,
    transformer: s => s
})

defineParameterType({
    name: "root_label",
    regexp: /entity|attribute|relation|thing/,
    transformer: s => {
        switch (s) {
            case "entity":
                return RootLabel.ENTITY;
            case "attribute":
                return RootLabel.ATTRIBUTE;
            case "relation":
                return RootLabel.RELATION;
            case "thing":
                return RootLabel.THING;
            default:
                throw `Root label "${s}" was unrecognised.`
        }
    }
});

export class ScopedLabel {
    private readonly _scope: string;
    private readonly _role: string;

    constructor(scope: string, role: string) {
        this._scope = scope;
        this._role = role;
    }

    static parse(value: string): ScopedLabel {
        const split = value.split(":");
        return new ScopedLabel(split[0], split[1]);
    }

    get scope(): string {
        return this._scope;
    }

    get role(): string {
        return this._role;
    }

    toString(): string {
        return this._scope + ":" + this._role;
    }
}

defineParameterType({
    name: "scoped_label",
    regexp: /[a-zA-Z0-9-_]+:[a-zA-Z0-9-_]+/,
    transformer: ScopedLabel.parse,
});

defineParameterType({
    name: "value_type",
    regexp: /long|double|string|boolean|datetime/,
    transformer: s => {
        switch (s) {
            case "long":
                return Concept.ValueType.LONG
            case "double":
                return Concept.ValueType.DOUBLE
            case "string":
                return Concept.ValueType.STRING
            case "boolean":
                return Concept.ValueType.BOOLEAN
            case "datetime":
                return Concept.ValueType.DATETIME
            default:
                throw "Unrecognised value type in step definition"
        }
    },
});

defineParameterType({
    name: "var",
    regexp: /\$([a-zA-Z0-9]+)/,
    transformer: s => s
});

export function parseVar(value: string): string {
    return value.slice(1);
}

defineParameterType({
    name: "type_label",
    regexp: /[a-zA-Z0-9-_]+/,
    transformer: s => s
});

defineParameterType({
    name: "annotations",
    regexp: new RegExp("(\\s*([\\w\\-_]+,\\s*)*[\\w\\-_]*\\s*)"),
    transformer: s => {
        return s.split(',').map(a => Annotation.parse(a.trim()));
    }
});

defineParameterType({
    name: "transaction_type",
    regexp: /read|write/,
    transformer: s => s === "read" ? TransactionType.READ : TransactionType.WRITE
});

export enum RootLabel {
    ATTRIBUTE,
    ENTITY,
    RELATION,
    THING,
}

export function parseList(dataTable: DataTable): string[]
export function parseList<T>(dataTable: DataTable, parseFn: (value: string) => T): T[]
export function parseList<T>(dataTable: DataTable, parseFn: (value: string) => string | T = val => val): (string | T)[] {
    return dataTable.raw().map(row => parseFn(row[0]));
}
