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

const ConceptGrpcMessages = require("../../../../grpc/nodejs/protocol/session/Concept_pb");

const META_TYPE = "META_TYPE";
const ATTRIBUTE_TYPE = "ATTRIBUTE_TYPE";
const RELATION_TYPE = "RELATION_TYPE";
const ENTITY_TYPE = "ENTITY_TYPE";
const ENTITY = "ENTITY";
const ATTRIBUTE = "ATTRIBUTE";
const RELATION = "RELATION";
const ROLE = "ROLE";
const RULE = "RULE";

const SCHEMA_CONCEPTS = new Set([RULE, ROLE, ATTRIBUTE_TYPE, RELATION_TYPE, ENTITY_TYPE]);
const TYPES = new Set([ATTRIBUTE_TYPE, RELATION_TYPE, ENTITY_TYPE]);
const THINGS = new Set([ATTRIBUTE, RELATION, ATTRIBUTE, ENTITY]);

function fromGrpcConcept(grpcConcept) {
    switch (grpcConcept.getBasetype()) {
        case ConceptGrpcMessages.Concept.BASE_TYPE.ENTITY: return ENTITY;
        case ConceptGrpcMessages.Concept.BASE_TYPE.RELATION: return RELATION;
        case ConceptGrpcMessages.Concept.BASE_TYPE.ATTRIBUTE: return ATTRIBUTE;
        case ConceptGrpcMessages.Concept.BASE_TYPE.ENTITY_TYPE: return ENTITY_TYPE;
        case ConceptGrpcMessages.Concept.BASE_TYPE.RELATION_TYPE: return RELATION_TYPE;
        case ConceptGrpcMessages.Concept.BASE_TYPE.ATTRIBUTE_TYPE: return ATTRIBUTE_TYPE;
        case ConceptGrpcMessages.Concept.BASE_TYPE.ROLE: return ROLE;
        case ConceptGrpcMessages.Concept.BASE_TYPE.RULE: return RULE;
        case ConceptGrpcMessages.Concept.BASE_TYPE.META_TYPE: return META_TYPE;
        default: throw "BaseType not recognised.";
    }
}

module.exports = {
    baseType: {
        META_TYPE,
        ATTRIBUTE,
        ATTRIBUTE_TYPE,
        ROLE,
        RULE,
        RELATION,
        RELATION_TYPE,
        ENTITY,
        ENTITY_TYPE
    },
    set: {
        SCHEMA_CONCEPTS, TYPES, THINGS
    },
    fromGrpcConcept
};
