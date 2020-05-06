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

module.exports = {}

const BaseTypeConstants = require("./BaseTypeConstants");
const BaseType = BaseTypeConstants.baseType;

/**
 * This factory creates Concepts as Javascipt objects from GrpcConcept provided
 * @param {Object} txService Object implementing all the functionalities of gRPC Transaction as defined in Session.proto
 */
class ConceptFactory {
  constructor(txService) {
    this.txService = txService;
  }

  createConcept(grpcConcept) {
    return ConceptFactory.createRemoteConcept(grpcConcept.getId(), BaseTypeConstants.fromGrpcConcept(grpcConcept), this.txService);
  }
}

module.exports = ConceptFactory; // Circular dependency fix

const RemoteConcept = require("./RemoteConcept");
const LocalConcept = require("./LocalConcept");


ConceptFactory.createRemoteConcept = function(conceptId, baseType, txService) {
  switch (baseType) {
    case BaseType.ENTITY: return new RemoteConcept.Entity(conceptId, BaseType.ENTITY, txService);
    case BaseType.RELATION: return new RemoteConcept.Relation(conceptId, BaseType.RELATION, txService);
    case BaseType.ATTRIBUTE: return new RemoteConcept.Attribute(conceptId, BaseType.ATTRIBUTE, txService);
    case BaseType.ENTITY_TYPE: return new RemoteConcept.EntityType(conceptId, BaseType.ENTITY_TYPE, txService);
    case BaseType.RELATION_TYPE: return new RemoteConcept.RelationType(conceptId, BaseType.RELATION_TYPE, txService);
    case BaseType.ATTRIBUTE_TYPE: return new RemoteConcept.AttributeType(conceptId, BaseType.ATTRIBUTE_TYPE, txService);
    case BaseType.ROLE: return new RemoteConcept.Role(conceptId, BaseType.ROLE, txService);
    case BaseType.RULE: return new RemoteConcept.Rule(conceptId, BaseType.RULE, txService);
    case BaseType.META_TYPE: return new RemoteConcept.SchemaConcept(conceptId, BaseType.META_TYPE, txService);
    default:
      throw "BaseType not recognised.";
  }
}

ConceptFactory.createLocalConcept = function(grpcConcept) {
  const baseType = BaseTypeConstants.fromGrpcConcept(grpcConcept);
  switch (baseType) {
    case BaseType.ENTITY: return new LocalConcept.Entity(grpcConcept);
    case BaseType.RELATION: return new LocalConcept.Relation(grpcConcept);
    case BaseType.ATTRIBUTE: return new LocalConcept.Attribute(grpcConcept);
    case BaseType.ENTITY_TYPE: return new LocalConcept.EntityType(grpcConcept);
    case BaseType.RELATION_TYPE: return new LocalConcept.RelationType(grpcConcept);
    case BaseType.ATTRIBUTE_TYPE: return new LocalConcept.AttributeType(grpcConcept);
    case BaseType.ROLE: return new LocalConcept.Role(grpcConcept);
    case BaseType.RULE: return new LocalConcept.Rule(grpcConcept);
    case BaseType.META_TYPE: return new LocalConcept.SchemaConcept(grpcConcept);
    default:
      throw "BaseType not recognised.";
  }
}
