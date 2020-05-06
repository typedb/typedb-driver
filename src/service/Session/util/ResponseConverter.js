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

const ProtoDataType = require("../../../../grpc/nodejs/protocol/session/Concept_pb").AttributeType.DATA_TYPE;

/**
 * This is used to parse gRPC responses and build type of Concepts or Iterators
 */
class ResponseConverter {
    constructor(conceptFactory) {
        this.conceptFactory = conceptFactory;
    }
    // SchemaConcept
    getLabel(resp) {
        return resp.getConceptmethodRes().getResponse().getSchemaconceptGetlabelRes().getLabel();
    }
    isImplicit(resp) {
        return resp.getConceptmethodRes().getResponse().getSchemaconceptIsimplicitRes().getImplicit();
    }
    subs(resp) {
        const grpcConcept = resp.getConceptmethodIterRes().getResponse().getSchemaconceptSubsIterRes().getSchemaconcept();
        return this.conceptFactory.createConcept(grpcConcept);
    }
    sups(resp) {
        const grpcConcept = resp.getConceptmethodIterRes().getResponse().getSchemaconceptSupsIterRes().getSchemaconcept();
        return this.conceptFactory.createConcept(grpcConcept);
    }
    getSup(resp) {
        const grpcRes = resp.getConceptmethodRes().getResponse().getSchemaconceptGetsupRes();
        if (grpcRes.hasNull())
            return null;
        return this.conceptFactory.createConcept(grpcRes.getSchemaconcept());
    }
    // Type
    instances(resp) {
        const grpcConcept = resp.getConceptmethodIterRes().getResponse().getTypeInstancesIterRes().getThing();
        return this.conceptFactory.createConcept(grpcConcept);
    }
    getAttributeTypes(resp) {
        const grpcConcept = resp.getConceptmethodIterRes().getResponse().getTypeAttributesIterRes().getAttributetype();
        return this.conceptFactory.createConcept(grpcConcept);
    }
    getKeyTypes(resp) {
        const grpcConcept = resp.getConceptmethodIterRes().getResponse().getTypeKeysIterRes().getAttributetype();
        return this.conceptFactory.createConcept(grpcConcept);
    }
    isAbstract(resp) {
        return resp.getConceptmethodRes().getResponse().getTypeIsabstractRes().getAbstract();
    }
    getRolesPlayedByType(resp) {
        const grpcConcept = resp.getConceptmethodIterRes().getResponse().getTypePlayingIterRes().getRole();
        return this.conceptFactory.createConcept(grpcConcept);
    }
    // Entity type
    addEntity(resp) {
        const grpcConcept = resp.getConceptmethodRes().getResponse().getEntitytypeCreateRes().getEntity();
        return this.conceptFactory.createConcept(grpcConcept);
    }
    // Attribute type
    getAttribute(resp) {
        const grpcRes = resp.getConceptmethodRes().getResponse().getAttributetypeAttributeRes();
        return (grpcRes.hasNull()) ? null : this.conceptFactory.createConcept(grpcRes.getAttribute());
    }
    putAttribute(resp) {
        const grpcConcept = resp.getConceptmethodRes().getResponse().getAttributetypeCreateRes().getAttribute();
        return this.conceptFactory.createConcept(grpcConcept);
    }
    getDataTypeOfType(resp) {
        const dataType = resp.getConceptmethodRes().getResponse().getAttributetypeDatatypeRes().getDatatype();
        switch (dataType) {
            case ProtoDataType.STRING: return "String";
            case ProtoDataType.BOOLEAN: return "Boolean";
            case ProtoDataType.INTEGER: return "Integer";
            case ProtoDataType.LONG: return "Long";
            case ProtoDataType.FLOAT: return "Float";
            case ProtoDataType.DOUBLE: return "Double";
            case ProtoDataType.DATE: return "Date";
        }
    }
    getRegex(resp) {
        return resp.getConceptmethodRes().getResponse().getAttributetypeGetregexRes().getRegex();
    }
    // Relation type
    addRelation(resp) {
        const grpcConcept = resp.getConceptmethodRes().getResponse().getRelationtypeCreateRes().getRelation();
        return this.conceptFactory.createConcept(grpcConcept);
    }
    getRelatedRoles(resp) {
        const grpcConcept = resp.getConceptmethodIterRes().getResponse().getRelationtypeRolesIterRes().getRole();
        return this.conceptFactory.createConcept(grpcConcept);
    }
    // Thing
    isInferred(resp) {
        return resp.getConceptmethodRes().getResponse().getThingIsinferredRes().getInferred();
    }
    getDirectType(resp) {
        const grpcConcept = resp.getConceptmethodRes().getResponse().getThingTypeRes().getType();
        return this.conceptFactory.createConcept(grpcConcept);
    }
    getRelationsByRoles(resp) {
        const grpcConcept = resp.getConceptmethodIterRes().getResponse().getThingRelationsIterRes().getRelation();
        return this.conceptFactory.createConcept(grpcConcept);
    }
    getRolesPlayedByThing(resp) {
        const grpcConcept = resp.getConceptmethodIterRes().getResponse().getThingRolesIterRes().getRole();
        return this.conceptFactory.createConcept(grpcConcept);
    }
    getAttributesByTypes(resp) {
        const grpcConcept = resp.getConceptmethodIterRes().getResponse().getThingAttributesIterRes().getAttribute();
        return this.conceptFactory.createConcept(grpcConcept);
    }
    getKeysByTypes(resp) {
        const grpcConcept = resp.getConceptmethodIterRes().getResponse().getThingKeysIterRes().getAttribute();
        return this.conceptFactory.createConcept(grpcConcept);
    }
    // Attribute
    getValue(resp) {
        const attrValue = resp.getConceptmethodRes().getResponse().getAttributeValueRes().getValue();
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
        if (attrValue.hasDate())
            return new Date(attrValue.getDate());
    }
    getOwners(resp) {
        const grpcConcept = resp.getConceptmethodIterRes().getResponse().getAttributeOwnersIterRes().getThing();
        return this.conceptFactory.createConcept(grpcConcept);
    }
    // Relation
    rolePlayersMap(resp) {
        const resContent = resp.getConceptmethodIterRes().getResponse().getRelationRoleplayersmapIterRes();
        return {
            role: this.conceptFactory.createConcept(resContent.getRole()),
            player: this.conceptFactory.createConcept(resContent.getPlayer())
        };
    }
    async collectRolePlayersMap(iterator) {
        const rolePlayers = await iterator.collect();
        // Temp map to store String id to Role object
        const tempMap = new Map(rolePlayers.map(entry => [entry.role.id, entry.role]));
        const map = new Map();
        // Create map using string as key and set as value
        rolePlayers.forEach(rp => {
            const key = rp.role.id;
            if (map.has(key))
                map.get(key).push(rp.player);
            else
                map.set(key, [rp.player]);
        });
        const resultMap = new Map();
        // Convert map to use Role object as key
        map.forEach((value, key) => {
            resultMap.set(tempMap.get(key), value);
        });
        return resultMap;
    }
    rolePlayers(resp) {
        const grpcConcept = resp.getConceptmethodIterRes().getResponse().getRelationRoleplayersIterRes().getThing();
        return this.conceptFactory.createConcept(grpcConcept);
    }
    // Rule
    getWhen(resp) {
        const methodRes = resp.getConceptmethodRes().getResponse().getRuleWhenRes();
        return (methodRes.hasNull()) ? null : methodRes.getPattern();
    }
    getThen(resp) {
        const methodRes = resp.getConceptmethodRes().getResponse().getRuleThenRes();
        return (methodRes.hasNull()) ? null : methodRes.getPattern();
    }
    // Role
    getRelationTypesThatRelateRole(resp) {
        const grpcConcept = resp.getConceptmethodIterRes().getResponse().getRoleRelationsIterRes().getRelationtype();
        return this.conceptFactory.createConcept(grpcConcept);
    }
    getTypesThatPlayRole(resp) {
        const grpcConcept = resp.getConceptmethodIterRes().getResponse().getRolePlayersIterRes().getType();
        return this.conceptFactory.createConcept(grpcConcept);
    }
    // ======================= Grakn transaction methods ========================= //
    getSchemaConcept(resp) {
        const grpcRes = resp.getGetschemaconceptRes();
        return (grpcRes.hasNull()) ? null : this.conceptFactory.createConcept(grpcRes.getSchemaconcept());
    }
    getConcept(resp) {
        const grpcRes = resp.getGetconceptRes();
        return (grpcRes.hasNull()) ? null : this.conceptFactory.createConcept(grpcRes.getConcept());
    }
    putEntityType(resp) {
        const concept = resp.getPutentitytypeRes().getEntitytype();
        return this.conceptFactory.createConcept(concept);
    }
    putRelationType(resp) {
        const concept = resp.getPutrelationtypeRes().getRelationtype();
        return this.conceptFactory.createConcept(concept);
    }
    putAttributeType(resp) {
        const concept = resp.getPutattributetypeRes().getAttributetype();
        return this.conceptFactory.createConcept(concept);
    }
    putRole(resp) {
        const concept = resp.getPutroleRes().getRole();
        return this.conceptFactory.createConcept(concept);
    }
    putRule(resp) {
        const concept = resp.getPutruleRes().getRule();
        return this.conceptFactory.createConcept(concept);
    }
    getAttributesByValue(resp) {
        const concept = resp.getGetattributesIterRes().getAttribute();
        return this.conceptFactory.createConcept(concept);
    }
}

module.exports = ResponseConverter;