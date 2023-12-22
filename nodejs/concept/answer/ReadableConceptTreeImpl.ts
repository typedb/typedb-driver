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

import {
    ReadableConceptTree as ReadableConceptTreeProto,
    ReadableConceptTreeNode as NodeProto,
    ReadableConceptTreeNodeMap as MapProto,
    ReadableConceptTreeNodeList as ListProto,
    ReadableConceptTreeNodeReadableConcept as ReadableConceptProto,
} from "typedb-protocol/proto/answer";

import {Type} from "../../api/concept/type/Type";
import {Attribute} from "../../api/concept/thing/Attribute";
import {Value} from "../../api/concept/value/Value";
import {Concept} from "../../api/concept/Concept";
import {AttributeType} from "../../api/concept/type/AttributeType";
import {RelationType} from "../../api/concept/type/RelationType";
import {EntityType} from "../../api/concept/type/EntityType";
import {RoleType} from "../../api/concept/type/RoleType";
import {ThingType} from "../../api/concept/type/ThingType";
import {JSON, JSONArray, JSONObject} from "../../api/answer/JSON";
import {EntityTypeImpl} from "../type/EntityTypeImpl";
import {RelationTypeImpl} from "../type/RelationTypeImpl";
import {AttributeTypeImpl} from "../type/AttributeTypeImpl";
import {RoleTypeImpl} from "../type/RoleTypeImpl";
import {AttributeImpl} from "../thing/AttributeImpl";
import {ValueImpl} from "../value/ValueImpl";
import {ThingTypeImpl} from "../type/ThingTypeImpl";
import {TypeDBDriverError} from "../../common/errors/TypeDBDriverError";
import {ErrorMessage} from "../../common/errors/ErrorMessage";
import ILLEGAL_STATE = ErrorMessage.Internal.ILLEGAL_STATE;
import {Readable} from "stream";

export class ReadableConceptTreeImpl {
    private readonly _root: ReadableConceptTreeImpl.Node.NodeMap;

    constructor(root: ReadableConceptTreeImpl.Node.NodeMap) {
        this._root = root;
    }

    asJSON(): JSONObject {
        return this._root.asObject();
    }
}

/* eslint no-inner-declarations: "off" */
export namespace ReadableConceptTreeImpl {

    export function of(proto: ReadableConceptTreeProto): ReadableConceptTreeImpl {
        const root = Node.NodeMap.of(proto.root);
        return new ReadableConceptTreeImpl(root);
    }

    export type Node = Node.NodeMap | Node.NodeList | Node.Leaf;

    export namespace Node {

        export function of(proto: NodeProto): Node {
            if (proto.has_map) return NodeMap.of(proto.map);
            else if (proto.has_list) return NodeList.of(proto.list);
            else if (proto.has_readable_concept) {
                return Leaf.of(proto.readable_concept);
            } else {
                throw new TypeDBDriverError(ILLEGAL_STATE.message());
            }
        }

        export class NodeMap {
            private readonly map: Map<string, ReadableConceptTreeImpl.Node>;

            constructor(map: Map<string, Node>) {
                this.map = map;
            }

            asObject(): JSONObject {
                const asObject: JSONObject = {};
                this.map.forEach((value, key) => {
                    if (value instanceof NodeMap) {
                        asObject[key] = value.asObject();
                    } else if (value instanceof NodeList) {
                        asObject[key] = value.asArray();
                    } else if (value instanceof Leaf) {
                        asObject[key] = value.asJSON();
                    } else {
                        throw new TypeDBDriverError(ILLEGAL_STATE.message());
                    }
                })
                return asObject;
            }
        }

        export namespace NodeMap {

            export function of(proto: MapProto): NodeMap {
                const nodeMap = new Map<string, Node>();
                proto.map.forEach((proto: NodeProto, label: string) => nodeMap.set(label, Node.of(proto)));
                return new NodeMap(nodeMap);
            }
        }

        export class NodeList {
            private readonly list: ReadableConceptTreeImpl.Node[];

            constructor(list: Node[]) {
                this.list = list;
            }

            asArray(): JSONArray {
                return this.list.map((node) => {
                    if (node instanceof NodeMap) {
                        return node.asObject();
                    } else if (node instanceof NodeList) {
                        return node.asArray();
                    } else if (node instanceof Leaf) {
                        return node.asJSON();
                    } else {
                        throw new TypeDBDriverError(ILLEGAL_STATE.message());
                    }
                });
            }
        }

        export namespace NodeList {
            export function of(proto: ListProto): NodeList {
                return new NodeList(proto.list.map((nodeProto: NodeProto) => Node.of(nodeProto)));
            }
        }

        export class Leaf {
            private readonly readableConcept: ReadableConcept | null;

            constructor(readableConcept: ReadableConcept | null) {
                this.readableConcept = readableConcept;
            }

            asJSON(): JSON {
                if (this.readableConcept == null) return null;
                else return this.readableConcept.asObject();
            }
        }

        export namespace Leaf {
            export function of(proto: ReadableConceptProto): Leaf {
                return new Leaf(ReadableConcept.of(proto));
            }
        }

        export class ReadableConcept {
            private readonly KEY_TYPE: string = "type";
            private readonly KEY_VALUE: string = "value";
            private readonly KEY_VALUE_TYPE: string = "value_type";
            private readonly KEY_TYPE_LABEL: string = "label";
            private readonly KEY_TYPE_ROOT: string = "root";

            private readonly concept: Type | Attribute | Value;

            constructor(concept: Type | Attribute | Value) {
                this.concept = concept;
            }

            asObject(): JSONObject {
                let asObject: JSONObject = {}
                const concept = this.concept as Concept;
                if (concept.isType()) {
                    asObject = this.typeAsObject(concept.asType());
                } else if (concept.isAttribute()) {
                    asObject[this.KEY_TYPE] = this.typeAsObject(concept.asThing().type);
                    const value = concept.asAttribute().value;
                    if (value instanceof Date) {
                        asObject[this.KEY_VALUE] = value.toISOString().slice(0, -1);
                    } else asObject[this.KEY_VALUE] = value;
                } else if (concept.isValue()) {
                    const value = concept.asValue();
                    if (value.value instanceof Date) {
                        asObject[this.KEY_VALUE] = value.value.toISOString().slice(0, -1);
                    } else asObject[this.KEY_VALUE] = value.value;
                    asObject[this.KEY_VALUE_TYPE] = value.valueType.name();
                } else {
                    throw new TypeDBDriverError(ILLEGAL_STATE.message());
                }
                return asObject;
            }

            typeAsObject(type: Type): JSONObject {
                const asObject: JSONObject = {};
                asObject[this.KEY_TYPE_LABEL] = type.label.scopedName;
                let rootType;
                if (type.isEntityType()) {
                    rootType = EntityType.NAME;
                } else if (type.isRelationType()) {
                    rootType = RelationType.NAME;
                } else if (type.isAttributeType()) {
                    rootType = AttributeType.NAME;
                    asObject[this.KEY_VALUE_TYPE] = type.asAttributeType().valueType.name();
                } else if (type.isRoleType()) {
                    rootType = RoleType.NAME;
                } else if (type.isThingType() && type.root) {
                    rootType = ThingType.NAME;
                } else {
                    throw new TypeDBDriverError(ILLEGAL_STATE.message());
                }
                asObject[this.KEY_TYPE_ROOT] = rootType;
                return asObject;
            }
        }

        export namespace ReadableConcept {

            export function of(proto: ReadableConceptProto): ReadableConcept | null {
                let concept: Type | Attribute | Value;
                if (proto.has_entity_type) {
                    concept = EntityTypeImpl.ofEntityTypeProto(proto.entity_type);
                } else if (proto.has_relation_type) {
                    concept = RelationTypeImpl.ofRelationTypeProto(proto.relation_type);
                } else if (proto.has_attribute_type) {
                    concept = AttributeTypeImpl.ofAttributeTypeProto(proto.attribute_type);
                } else if (proto.has_role_type) {
                    concept = RoleTypeImpl.ofRoleTypeProto(proto.role_type);
                } else if (proto.has_attribute) {
                    concept = AttributeImpl.ofAttributeProto(proto.attribute);
                } else if (proto.has_value) {
                    concept = ValueImpl.ofValueProto(proto.value);
                } else if (proto.has_thing_type_root) {
                    concept = ThingTypeImpl.Root.ofThingTypeRootProto(proto.thing_type_root);
                } else {
                    return null;
                }
                return new ReadableConcept(concept);
            }
        }
    }
}
