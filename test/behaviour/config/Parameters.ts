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

import { defineParameterType } from "@cucumber/cucumber";
import { AttributeType } from "../../../dist/concept/type/AttributeType";
import { Grakn } from "../../../dist/Grakn";
import TransactionType = Grakn.TransactionType;

defineParameterType({
    name: "bool",
    regexp: /true|false/,
    transformer: s => s === "true",
});

defineParameterType({
    name: "number",
    regexp: /[0-9]+/,
    transformer: s => parseInt(s),
});

defineParameterType({
    name: "datetime",
    regexp: /\d\d\d\d-\d\d-\d\d \d\d:\d\d:\d\d/,
    transformer: s => Date.parse(s),
});

defineParameterType({
    name: "root_label",
    regexp: /entity|attribute|relation/,
    transformer: s => s,
});

defineParameterType({
    name: "scoped_label",
    regexp: /[a-zA-Z0-9-_]+:[a-zA-Z0-9-_]+/,
    transformer: s => s,
});

defineParameterType({
    name: "value_type",
    regexp: /long|double|string|boolean|datetime/,
    transformer: s => {
        switch(s) {
            case "long":
                return AttributeType.ValueType.LONG
            case "double":
                return AttributeType.ValueType.DOUBLE
            case "string":
                return AttributeType.ValueType.STRING
            case "boolean":
                return AttributeType.ValueType.BOOLEAN
            case "datetime":
                return AttributeType.ValueType.DATETIME
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

defineParameterType({
    name: "transaction_type",
    regexp: /read|write/,
    transformer: s => s === "read" ? TransactionType.READ : TransactionType.WRITE
});

//TODO: scoped labelS (plural form), transaction typeS, possibly investigate if root label and scoped label are gonna mess with me
