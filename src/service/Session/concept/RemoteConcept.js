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

const Constant = require('./BaseTypeConstants');
const BaseType = Constant.baseType;

class Concept {
    constructor(id, baseType, txService) {
        this.id = id;
        this.baseType = baseType;
        this.txService = txService;
    }

    delete() { return this.txService.deleteConcept(this.id); }
    async isDeleted() {
        const concept = await this.txService.getConcept(this.id);
        return concept === null;
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
    label(label) {
        if (label) return this.txService.setLabel(this.id, label);
        else return this.txService.getLabel(this.id);
    }
    subs() { return this.txService.subs(this.id); }
    sups() { return this.txService.sups(this.id); }
    sup(type) {
        if (type) return this.txService.setSup(this.id, type);
        else return this.txService.getSup(this.id);
    }
}

class Thing extends Concept {
    isInferred() { return this.txService.isInferred(this.id); }
    type() { return this.txService.getDirectType(this.id); }
    relations(...roles) { return this.txService.getRelationsByRoles(this.id, roles); }
    roles() { return this.txService.getRolesPlayedByThing(this.id); }
    attributes(...attributes) { return this.txService.getAttributesByTypes(this.id, attributes); }
    keys(...types) { return this.txService.getKeysByTypes(this.id, types); }
    has(attribute) { return this.txService.setAttribute(this.id, attribute); }
    unhas(attribute) { return this.txService.unsetAttribute(this.id, attribute); }
}

class Type extends SchemaConcept {
    isAbstract(bool) {
        if (bool != null) return this.txService.setAbstract(this.id, bool);
        else return this.txService.isAbstract(this.id);
    }
    plays(role) { return this.txService.setRolePlayedByType(this.id, role); }
    playing() { return this.txService.getRolesPlayedByType(this.id); }
    key(attributeType) { return this.txService.setKeyType(this.id, attributeType); }
    has(attributeType) { return this.txService.setAttributeType(this.id, attributeType); }
    attributes() { return this.txService.getAttributeTypes(this.id); }
    keys() { return this.txService.getKeyTypes(this.id); }
    instances() { return this.txService.instances(this.id); }
    unplay(role) { return this.txService.unsetRolePlayedByType(this.id, role); }
    unhas(attributeType) { return this.txService.unsetAttributeType(this.id, attributeType); }
    unkey(attributeType) { return this.txService.unsetKeyType(this.id, attributeType); }
}

class Attribute extends Thing {
    valueType() { return this.txService.getValueTypeOfAttribute(this.id); }
    value() { return this.txService.getValue(this.id); }
    owners() { return this.txService.getOwners(this.id); }
}

class AttributeType extends Type {
    create(value) { return this.txService.putAttribute(this.id, value); }
    attribute(value) { return this.txService.getAttribute(this.id, value); }
    valueType() { return this.txService.getValueTypeOfType(this.id); }
    regex(regex) {
        if (regex) return this.txService.setRegex(this.id, regex);
        else return this.txService.getRegex(this.id);
    }
}

class Entity extends Thing {
}

class EntityType extends Type {
    create() { return this.txService.addEntity(this.id); }
}

class Relation extends Thing {
    async rolePlayersMap() { return this.txService.rolePlayersMap(this.id); }
    async rolePlayers(...roles) { return this.txService.rolePlayers(this.id, roles); }
    assign(role, thing) { return this.txService.setRolePlayer(this.id, role, thing); }
    unassign(role, thing) { return this.txService.unsetRolePlayer(this.id, role, thing); }
}

class RelationType extends Type {
    create() { return this.txService.addRelation(this.id); }
    relates(role) { return this.txService.setRelatedRole(this.id, role); }
    roles() { return this.txService.getRelatedRoles(this.id); }
    unrelate(role) { return this.txService.unsetRelatedRole(this.id, role); }
}

class Role extends SchemaConcept {
    relations() { return this.txService.getRelationTypesThatRelateRole(this.id); }
    players() { return this.txService.getTypesThatPlayRole(this.id); }
}

class Rule extends SchemaConcept {
    getWhen() { return this.txService.getWhen(this.id); }
    getThen() { return this.txService.getThen(this.id); }
}

module.exports = {
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
}
