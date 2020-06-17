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

const RequestBuilder = require("./util/RequestBuilder");
const GrpcCommunicator = require("./util/GrpcCommunicator");
const ConceptFactory = require("./concept/ConceptFactory");
const ResponseConverter = require("./util/ResponseConverter");
const GrpcIteratorFactory = require("./util/GrpcIteratorFactory");

/**
 * TransactionService provides implementation of methods that belong to 
 * the transaction rpc method defined in Session.proto
 * 
 * It implements every method using 3 collaborators:
 *  - a gRPC request builder (static)
 *  - a communicator which handles gRPC requests/responses over a duplex Stream
 *  - a converter to convert gRPC responses to valid JS types
 * 
 * @param {Duplex Stream} txStream
 */
function TransactionService(txStream) {
    this.communicator = new GrpcCommunicator(txStream);
    const conceptFactory = new ConceptFactory(this);
    this.respConverter = new ResponseConverter(conceptFactory);
    this.iteratorFactory = new GrpcIteratorFactory(conceptFactory, this.respConverter, this.communicator, this);
}

// Closes txStream
TransactionService.prototype.close = function () {
    return this.communicator.end();
}

TransactionService.prototype.isOpen = function () {
    return this.communicator.stream.writable;
};

// Concept
TransactionService.prototype.deleteConcept = function (id) {
    const txRequest = RequestBuilder.deleteConcept(id);
    return this.communicator.send(txRequest);
};

// Schema concept
TransactionService.prototype.getLabel = async function (id) {
    const txRequest = RequestBuilder.getLabel(id);
    const resp = await this.communicator.send(txRequest);
    return this.respConverter.getLabel(resp);
};
TransactionService.prototype.setLabel = function (id, label) {
    const txRequest = RequestBuilder.setLabel(id, label);
    return this.communicator.send(txRequest);
};
TransactionService.prototype.subs = function (id) {
    const iterRequest = RequestBuilder.subs(id);
    return this.iteratorFactory.createIterator(iterRequest,
        (resp) => this.respConverter.subs(resp));
};
TransactionService.prototype.sups = function (id) {
    const iterRequest = RequestBuilder.sups(id);
    return this.iteratorFactory.createIterator(iterRequest,
        (resp) => this.respConverter.sups(resp));
};
TransactionService.prototype.getSup = async function (id) {
    const txRequest = RequestBuilder.getSup(id);
    const resp = await this.communicator.send(txRequest);
    return this.respConverter.getSup(resp);
};
TransactionService.prototype.setSup = function (id, superConcept) {
    const txRequest = RequestBuilder.setSup(id, superConcept);
    return this.communicator.send(txRequest);
};

// Rule 
TransactionService.prototype.getWhen = async function (id) {
    const txRequest = RequestBuilder.getWhen(id);
    const resp = await this.communicator.send(txRequest);
    return this.respConverter.getWhen(resp);
};
TransactionService.prototype.getThen = async function (id) {
    const txRequest = RequestBuilder.getThen(id);
    const resp = await this.communicator.send(txRequest);
    return this.respConverter.getThen(resp);
};

// Role
TransactionService.prototype.getRelationTypesThatRelateRole = function (id) {
    const iterRequest = RequestBuilder.getRelationTypesThatRelateRole(id);
    return this.iteratorFactory.createIterator(iterRequest,
        (resp) => this.respConverter.getRelationTypesThatRelateRole(resp));
}
TransactionService.prototype.getTypesThatPlayRole = function (id) {
    const iterRequest = RequestBuilder.getTypesThatPlayRole(id);
    return this.iteratorFactory.createIterator(iterRequest,
        (resp) => this.respConverter.getTypesThatPlayRole(resp));
}

// Type
TransactionService.prototype.instances = function (id) {
    const iterRequest = RequestBuilder.instances(id);
    return this.iteratorFactory.createIterator(iterRequest,
        (resp) => this.respConverter.instances(resp));
};
TransactionService.prototype.getAttributeTypes = function (id) {
    const iterRequest = RequestBuilder.getAttributeTypes(id);
    return this.iteratorFactory.createIterator(iterRequest,
        (resp) => this.respConverter.getAttributeTypes(resp));
};
TransactionService.prototype.setAttributeType = function (id, type) {
    const txRequest = RequestBuilder.setAttributeType(id, type);
    return this.communicator.send(txRequest);
};
TransactionService.prototype.unsetAttributeType = function (id, type) {
    const txRequest = RequestBuilder.unsetAttributeType(id, type);
    return this.communicator.send(txRequest);
};
TransactionService.prototype.getKeyTypes = function (id) {
    const iterRequest = RequestBuilder.getKeyTypes(id);
    return this.iteratorFactory.createIterator(iterRequest,
        (resp) => this.respConverter.getKeyTypes(resp));
};
TransactionService.prototype.setKeyType = function (id, keyType) {
    const txRequest = RequestBuilder.setKeyType(id, keyType);
    return this.communicator.send(txRequest);
};
TransactionService.prototype.unsetKeyType = function (id, keyType) {
    const txRequest = RequestBuilder.unsetKeyType(id, keyType);
    return this.communicator.send(txRequest);
};
TransactionService.prototype.isAbstract = async function (id) {
    const txRequest = RequestBuilder.isAbstract(id);
    const resp = await this.communicator.send(txRequest);
    return this.respConverter.isAbstract(resp);
};
TransactionService.prototype.setAbstract = function (id, bool) {
    const txRequest = RequestBuilder.setAbstract(id, bool);
    return this.communicator.send(txRequest);
};
TransactionService.prototype.getRolesPlayedByType = function (id) {
    const iterRequest = RequestBuilder.getRolesPlayedByType(id);
    return this.iteratorFactory.createIterator(iterRequest,
        (resp) => this.respConverter.getRolesPlayedByType(resp));
};
TransactionService.prototype.setRolePlayedByType = function (id, role) {
    const txRequest = RequestBuilder.setRolePlayedByType(id, role);
    return this.communicator.send(txRequest);
};
TransactionService.prototype.unsetRolePlayedByType = function (id, role) {
    const txRequest = RequestBuilder.unsetRolePlayedByType(id, role);
    return this.communicator.send(txRequest);
};

// Entity type
TransactionService.prototype.addEntity = async function (id) {
    const txRequest = RequestBuilder.addEntity(id);
    const resp = await this.communicator.send(txRequest);
    return this.respConverter.addEntity(resp);
};

// Relation Type
TransactionService.prototype.addRelation = async function (id) {
    const txRequest = RequestBuilder.addRelation(id);
    const resp = await this.communicator.send(txRequest);
    return this.respConverter.addRelation(resp);
};
TransactionService.prototype.getRelatedRoles = function (id) {
    const iterRequest = RequestBuilder.getRelatedRoles(id);
    return this.iteratorFactory.createIterator(iterRequest,
        (resp) => this.respConverter.getRelatedRoles(resp));
};
TransactionService.prototype.setRelatedRole = function (id, role) {
    const txRequest = RequestBuilder.setRelatedRole(id, role);
    return this.communicator.send(txRequest);
};
TransactionService.prototype.unsetRelatedRole = function (id, role) {
    const txRequest = RequestBuilder.unsetRelatedRole(id, role);
    return this.communicator.send(txRequest);
};

// Attribute type
TransactionService.prototype.putAttribute = async function (id, value) {
    const valueTypeTxRequest = RequestBuilder.getValueTypeOfType(id);
    const resp = await this.communicator.send(valueTypeTxRequest);
    const valueType = resp.getConceptmethodRes().getResponse().getAttributetypeValuetypeRes().getValuetype();
    const txRequest = RequestBuilder.putAttribute(id, valueType, value);
    return this.communicator.send(txRequest)
        .then(resp => this.respConverter.putAttribute(resp));
};
TransactionService.prototype.getAttribute = async function (id, value) {
    const valueTypeTxRequest = RequestBuilder.getValueTypeOfType(id);
    const resp = await this.communicator.send(valueTypeTxRequest);
    const valueType = resp.getConceptmethodRes().getResponse().getAttributetypeValuetypeRes().getValuetype();
    const txRequest = RequestBuilder.getAttribute(id, valueType, value);
    return this.communicator.send(txRequest)
        .then(resp => this.respConverter.getAttribute(resp));
};
TransactionService.prototype.getValueTypeOfType = async function (id) {
    const txRequest = RequestBuilder.getValueTypeOfType(id);
    const resp = await this.communicator.send(txRequest);
    return this.respConverter.getValueTypeOfType(resp);
};
TransactionService.prototype.getRegex = async function (id) {
    const txRequest = RequestBuilder.getRegex(id);
    const resp = await this.communicator.send(txRequest);
    return this.respConverter.getRegex(resp);
};
TransactionService.prototype.setRegex = function (id, regex) {
    const txRequest = RequestBuilder.setRegex(id, regex);
    return this.communicator.send(txRequest);
};

//Thing
TransactionService.prototype.isInferred = async function (id) {
    const txRequest = RequestBuilder.isInferred(id);
    const resp = await this.communicator.send(txRequest);
    return this.respConverter.isInferred(resp);
};
TransactionService.prototype.getDirectType = async function (id) {
    const txRequest = RequestBuilder.getDirectType(id);
    const response = await this.communicator.send(txRequest);
    return this.respConverter.getDirectType(response);
};
TransactionService.prototype.getRelationsByRoles = function (id, roles) {
    const iterRequest = RequestBuilder.getRelationsByRoles(id, roles);
    return this.iteratorFactory.createIterator(iterRequest,
        (response) => this.respConverter.getRelationsByRoles(response));
};
TransactionService.prototype.getRolesPlayedByThing = function (id) {
    const iterRequest = RequestBuilder.getRolesPlayedByThing(id);
    return this.iteratorFactory.createIterator(iterRequest,
        (response) => this.respConverter.getRolesPlayedByThing(response));
};
TransactionService.prototype.getAttributesByTypes = function (id, types) {
    const iterRequest = RequestBuilder.getAttributesByTypes(id, types);
    return this.iteratorFactory.createIterator(iterRequest,
        (response) => this.respConverter.getAttributesByTypes(response));
};
TransactionService.prototype.getKeysByTypes = function (id, types) {
    const iterRequest = RequestBuilder.getKeysByTypes(id, types);
    return this.iteratorFactory.createIterator(iterRequest,
        (response) => this.respConverter.getKeysByTypes(response));
};
TransactionService.prototype.setAttribute = function (id, attribute) {
    const txRequest = RequestBuilder.setAttribute(id, attribute);
    return this.communicator.send(txRequest);
};
TransactionService.prototype.unsetAttribute = function (id, attribute) {
    const txRequest = RequestBuilder.unsetAttribute(id, attribute);
    return this.communicator.send(txRequest);
};

// Relation
TransactionService.prototype.rolePlayersMap = async function (id) {
    const iterRequest = RequestBuilder.rolePlayersMap(id);
    const iterator = await this.iteratorFactory.createIterator(iterRequest,
        (response) => this.respConverter.rolePlayersMap(response));
    return await this.respConverter.collectRolePlayersMap(iterator);
};
TransactionService.prototype.rolePlayers = function (id, roles) {
    const iterRequest = RequestBuilder.rolePlayers(id, roles);
    return this.iteratorFactory.createIterator(iterRequest,
        (response) => this.respConverter.rolePlayers(response));
};
TransactionService.prototype.setRolePlayer = function (id, role, thing) {
    const txRequest = RequestBuilder.setRolePlayer(id, role, thing);
    return this.communicator.send(txRequest);
};
TransactionService.prototype.unsetRolePlayer = function (id, role, thing) {
    const txRequest = RequestBuilder.unsetRolePlayer(id, role, thing);
    return this.communicator.send(txRequest);
};
// Attribute
TransactionService.prototype.getValue = async function (id) {
    const txRequest = RequestBuilder.getValue(id);
    const response = await this.communicator.send(txRequest);
    return this.respConverter.getValue(response);
};
TransactionService.prototype.getOwners = function (id) {
    const iterRequest = RequestBuilder.getOwners(id);
    return this.iteratorFactory.createIterator(iterRequest,
        (response) => this.respConverter.getOwners(response));
};

// ======================= Grakn transaction methods ========================= //

TransactionService.prototype.getConcept = async function (conceptId) {
    const txRequest = RequestBuilder.getConcept(conceptId);
    const response = await this.communicator.send(txRequest);
    return this.respConverter.getConcept(response);
}

TransactionService.prototype.getSchemaConcept = async function (label) {
    const txRequest = RequestBuilder.getSchemaConcept(label);
    const response = await this.communicator.send(txRequest);
    return this.respConverter.getSchemaConcept(response);
}

TransactionService.prototype.putEntityType = async function (label) {
    const txRequest = RequestBuilder.putEntityType(label);
    const response = await this.communicator.send(txRequest);
    return this.respConverter.putEntityType(response);
}

TransactionService.prototype.putRelationType = async function (label) {
    const txRequest = RequestBuilder.putRelationType(label);
    const response = await this.communicator.send(txRequest);
    return this.respConverter.putRelationType(response);
}

TransactionService.prototype.putRole = async function (label) {
    const txRequest = RequestBuilder.putRole(label);
    const response = await this.communicator.send(txRequest);
    return this.respConverter.putRole(response);
}

TransactionService.prototype.putRule = async function (label, when, then) {
    const txRequest = RequestBuilder.putRule(label, when, then);
    const response = await this.communicator.send(txRequest);
    return this.respConverter.putRule(response);
}

TransactionService.prototype.putAttributeType = async function (label, valueType) {
    const txRequest = RequestBuilder.putAttributeType(label, valueType);
    const response = await this.communicator.send(txRequest);
    return this.respConverter.putAttributeType(response);
}

TransactionService.prototype.getAttributesByValue = function (value, valueType) {
    const iterRequest = RequestBuilder.startGetAttributesByValueIter(value, valueType);
    return this.iteratorFactory.createIterator(iterRequest,
        (response) => this.respConverter.getAttributesByValue(response));
}

TransactionService.prototype.openTx = function (sessionId, txType) {
    const txRequest = RequestBuilder.openTx(sessionId, txType);
    return this.communicator.send(txRequest);
};

TransactionService.prototype.commit = function () {
    const txRequest = RequestBuilder.commit();
    return this.communicator.send(txRequest);
}

TransactionService.prototype.query = function (query, options) {
    const queryIter = RequestBuilder.startQueryIter(query, options, RequestBuilder.iterOptions(options ? options.batchSize : undefined));
    return this.iteratorFactory.createQueryIterator(queryIter);
};

TransactionService.prototype.explanation = function (grpcConceptMap) {
    const txRequest = RequestBuilder.explanation(grpcConceptMap);
    return this.communicator.send(txRequest);
};

module.exports = TransactionService;