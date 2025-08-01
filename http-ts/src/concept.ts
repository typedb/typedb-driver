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

export type TypeKind = "entityType" | "relationType" | "attributeType" | "roleType";

export type ThingKind = "entity" | "relation" | "attribute";

export type ValueKind = "value";

export type ValueType = "boolean" | "integer" | "double" | "decimal" | "date" | "datetime" | "datetime-tz" | "duration" | "string" | "struct";

export type EdgeKind = "isa" | "has" | "links" | "sub" | "owns" | "relates" | "plays" | "isaExact" | "subExact" | "assigned" | "argument";

export interface EntityType {
    kind: "entityType";
    label: string;
}

export interface RelationType {
    kind: "relationType";
    label: string;
}

export interface RoleType {
    kind: "roleType";
    label: string;
}

export type AttributeType = {
    label: string;
    kind: "attributeType";
    valueType: ValueType;
}

export type Type = InstantiableType | RoleType;
export type InstantiableType = EntityType | RelationType | AttributeType;

export interface Entity {
    kind: "entity";
    iid: string;
    type: EntityType;
}

export interface Relation {
    kind: "relation";
    iid: string;
    type: RelationType;
}

export interface Attribute {
    kind: "attribute";
    iid: string;
    value: any;
    valueType: ValueType;
    type: AttributeType;
}

export interface Value {
    kind: ValueKind;
    value: any;
    valueType: ValueType;
}

export type Concept = Type | Entity | Relation | Attribute | Value;
