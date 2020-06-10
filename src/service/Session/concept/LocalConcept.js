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

module.exports = {} // Fix circular dependency

const Constant = require('./BaseTypeConstants');
const BaseType = Constant.baseType;
const ConceptFactory = require('./ConceptFactory');
const ProtoValueType = require("../../../../grpc/nodejs/protocol/session/Concept_pb").AttributeType.VALUE_TYPE;

function convertValueType(valueTypeRes) {
    if (valueTypeRes.hasNull()) return null;
    switch (valueTypeRes.getValuetype()) {
        case ProtoValueType.STRING: return "String";
        case ProtoValueType.BOOLEAN: return "Boolean";
        case ProtoValueType.INTEGER: return "Integer";
        case ProtoValueType.LONG: return "Long";
        case ProtoValueType.FLOAT: return "Float";
        case ProtoValueType.DOUBLE: return "Double";
        case ProtoValueType.DATETIME: return "Datetime";
    }
}

function convertValue(attrValue) {
    if (attrValue.hasString())
        return attrValue.getString();
    if (attrValue.hasBoolean())
        return attrValue.getBoolean();
    if (attrValue.hasInteger())
        return attrValue.getInteger();
    if (attrValue.hasLong())
        return attrValue.getLong();
    if (attrValue.hasFloat())
        return attrValue.getFloat();
    if (attrValue.hasDouble())
        return attrValue.getDouble();
    if (attrValue.hasDatetime())
        return new Date(attrValue.getDatetime());
}

class Concept {
    constructor(grpcConcept) {
        this.id = grpcConcept.getId();
        this.baseType = Constant.fromGrpcConcept(grpcConcept);
    }

    asRemote(tx) {
        return ConceptFactory.createRemoteConcept(this.id, this.baseType, tx.txService);
    }

    isSchemaConcept() { return Constant.set.SCHEMA_CONCEPTS.has(this.baseType); }
    isType()          { return Constant.set.TYPES.has(this.baseType); }
    isThing()         { return Constant.set.THINGS.has(this.baseType); }
    isAttributeType() { return this.baseType === BaseType.ATTRIBUTE_TYPE; }
    isEntityType()    { return this.baseType === BaseType.ENTITY_TYPE; }
    isRelationType()  { return this.baseType === BaseType.RELATION_TYPE; }
    isRole()          { return this.baseType === BaseType.ROLE; }
    isRule()          { return this.baseType === BaseType.RULE; }
    isAttribute()     { return this.baseType === BaseType.ATTRIBUTE; }
    isEntity()        { return this.baseType === BaseType.ENTITY; }
    isRelation()      { return this.baseType === BaseType.RELATION; }
}

class SchemaConcept extends Concept {
    constructor(grpcConcept) {
        super(grpcConcept)
        this._label = grpcConcept.getLabelRes().getLabel();
    }

    label() { return this._label; }
}

class Thing extends Concept {
    constructor(grpcConcept) {
        super(grpcConcept)
        this._inferred = grpcConcept.getInferredRes().getInferred();
        this._type = ConceptFactory.createLocalConcept(grpcConcept.getTypeRes().getType());
    }

    isInferred() { return this._inferred; }
    type() { return this._type; }
}

class Type extends SchemaConcept {
}

class Attribute extends Thing {
    constructor(grpcConcept) {
        super(grpcConcept);
        this._value = convertValue(grpcConcept.getValueRes().getValue());
        this._valueType = convertValueType(grpcConcept.getValuetypeRes());
    }

    valueType() { return this._valueType; }
    value() { return this._value; }
}

class AttributeType extends Type {
}

class Entity extends Thing {
}

class EntityType extends Type {
}

class Relation extends Thing {
}

class RelationType extends Type {
}

class Role extends SchemaConcept {
}

class Rule extends SchemaConcept {
}

Object.assign(module.exports, {
    Concept,
    SchemaConcept,
    Thing,
    Type,
    Attribute,
    AttributeType,
    Entity,
    EntityType,
    Relation,
    RelationType,
    Role,
    Rule
})
