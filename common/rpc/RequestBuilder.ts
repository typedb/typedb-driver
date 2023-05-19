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

import { ClusterDatabaseManager } from "typedb-protocol/cluster/cluster_database_pb";
import { ServerManager as ServerManagerProto } from "typedb-protocol/cluster/cluster_server_pb";
import { ClusterUser as ClusterUserProto, ClusterUserManager as ClusterUserManagerProto } from "typedb-protocol/cluster/cluster_user_pb";
import { Attribute as AttributeProto, AttributeType as AttributeTypeProto, ConceptManager as ConceptMgrProto, EntityType as EntityTypeProto, Relation as RelationProto, RelationType as RelationTypeProto, RoleType as RoleTypeProto, Thing as ThingProto, ThingType as ThingTypeProto, Type as TypeProto } from "typedb-protocol/common/concept_pb";
import { LogicManager as LogicProto, Rule as RuleProto } from "typedb-protocol/common/logic_pb";
import { Options } from "typedb-protocol/common/options_pb";
import { QueryManager as QueryProto } from "typedb-protocol/common/query_pb";
import { Session as SessionProto } from "typedb-protocol/common/session_pb";
import { Transaction as TransactionProto } from "typedb-protocol/common/transaction_pb";
import { CoreDatabase, CoreDatabaseManager } from "typedb-protocol/core/core_database_pb";
import * as uuid from "uuid";
import { Label } from "../Label";
import { Bytes } from "../util/Bytes";

/* eslint no-inner-declarations: "off" */
export namespace RequestBuilder {

    export namespace Core {

        export namespace DatabaseManager {

            export function createReq(name: string) {
                return new CoreDatabaseManager.Create.Req().setName(name);
            }

            export function containsReq(name: string) {
                return new CoreDatabaseManager.Contains.Req().setName(name);
            }

            export function allReq() {
                return new CoreDatabaseManager.All.Req();
            }

        }

        export namespace Database {

            export function schemaReq(name: string) {
                return new CoreDatabase.Schema.Req().setName(name);
            }


            export function deleteReq(name: string) {
                return new CoreDatabase.Delete.Req().setName(name);
            }
        }
    }

    export namespace Cluster {
        export namespace ServerManager {

            export function allReq() {
                return new ServerManagerProto.All.Req();
            }
        }

        export namespace UserManager {
            export function containsReq(name: string): ClusterUserManagerProto.Contains.Req {
                return new ClusterUserManagerProto.Contains.Req().setUsername(name);
            }

            export function createReq(name: string, password: string): ClusterUserManagerProto.Create.Req {
                return new ClusterUserManagerProto.Create.Req().setUsername(name).setPassword(password);
            }

            export function deleteReq(name: string): ClusterUserManagerProto.Delete.Req {
                return new ClusterUserManagerProto.Delete.Req().setUsername(name);
            }

            export function allReq(): ClusterUserManagerProto.All.Req {
                return new ClusterUserManagerProto.All.Req();
            }

            export function passwordSetReq(name: string, password: string): ClusterUserManagerProto.PasswordSet.Req {
                return new ClusterUserManagerProto.PasswordSet.Req().setUsername(name).setPassword(password);
            }
            
            export function getReq(name: string): ClusterUserManagerProto.Get.Req {
                return new ClusterUserManagerProto.Get.Req().setUsername(name);
            }
        }

        export namespace User {
            export function passwordUpdateReq(name: string, passwordOld: string, passwordNew: string): ClusterUserProto.PasswordUpdate.Req {
                return new ClusterUserProto.PasswordUpdate.Req().setUsername(name).setPasswordOld(passwordOld).setPasswordNew(passwordNew);
            }

            export function tokenReq(username: string) {
                return new ClusterUserProto.Token.Req().setUsername(username);
            }
        }

        export namespace DatabaseManager {
            export function getReq(name: string) {
                return new ClusterDatabaseManager.Get.Req().setName(name);
            }

            export function allReq() {
                return new ClusterDatabaseManager.All.Req();
            }
        }

        export namespace Database {

        }
    }


    export namespace Session {

        export function openReq(database: string, type: SessionProto.Type, options: Options) {
            return new SessionProto.Open.Req().setDatabase(database).setType(type).setOptions(options);
        }

        export function closeReq(id: string) {
            return new SessionProto.Close.Req().setSessionId(id);
        }

        export function pulseReq(id: string) {
            return new SessionProto.Pulse.Req().setSessionId(id);
        }

    }

    export namespace Transaction {

        export function clientReq(reqs: TransactionProto.Req[]) {
            return new TransactionProto.Client().setReqsList(reqs);
        }

        export function openReq(sessionId: string, type: TransactionProto.Type, options: Options, latencyMillis: number) {
            return new TransactionProto.Req().setOpenReq(
                new TransactionProto.Open.Req().setSessionId(sessionId).setType(type).setOptions(options).setNetworkLatencyMillis(latencyMillis)
            );
        }

        export function commitReq() {
            return new TransactionProto.Req().setCommitReq(new TransactionProto.Commit.Req());
        }

        export function rollbackReq() {
            return new TransactionProto.Req().setRollbackReq(new TransactionProto.Rollback.Req());
        }

        export function streamReq(requestId: string) {
            return new TransactionProto.Req().setReqId(uuid.parse(requestId) as Uint8Array).setStreamReq(new TransactionProto.Stream.Req());
        }

    }

    export namespace LogicManager {

        export function logicManagerReq(logicReq: LogicProto.Req) {
            return new TransactionProto.Req().setLogicManagerReq(logicReq);
        }

        export function putRuleReq(label: string, when: string, then: string) {
            return logicManagerReq(new LogicProto.Req().setPutRuleReq(
                new LogicProto.PutRule.Req().setLabel(label).setWhen(when).setThen(then)
            ));
        }

        export function getRuleReq(label: string) {
            return logicManagerReq(new LogicProto.Req().setGetRuleReq(
                new LogicProto.GetRule.Req().setLabel(label)
            ));
        }

        export function getRulesReq() {
            return logicManagerReq(new LogicProto.Req().setGetRulesReq(new LogicProto.GetRules()));
        }
    }

    export namespace Rule {

        export function ruleReq(request: RuleProto.Req) {
            return new TransactionProto.Req().setRuleReq(request);
        }

        export function setLabelReq(currentLabel: string, newLabel: string) {
            return ruleReq(new RuleProto.Req().setLabel(currentLabel).setRuleSetLabelReq(
                new RuleProto.SetLabel.Req().setLabel(newLabel)
            ));
        }

        export function deleteReq(label: string) {
            return ruleReq(new RuleProto.Req().setLabel(label).setRuleDeleteReq(
                new RuleProto.Delete.Req()
            ));
        }
    }


    export namespace QueryManager {

        function queryManagerReq(queryReq: QueryProto.Req, options: Options) {
            return new TransactionProto.Req().setQueryManagerReq(queryReq.setOptions(options));
        }

        export function defineReq(query: string, options: Options) {
            return queryManagerReq(new QueryProto.Req().setDefineReq(
                new QueryProto.Define.Req().setQuery(query)
            ), options);
        }

        export function undefineReq(query: string, options: Options) {
            return queryManagerReq(new QueryProto.Req().setUndefineReq(
                new QueryProto.Undefine.Req().setQuery(query)
            ), options);
        }

        export function matchReq(query: string, options: Options) {
            return queryManagerReq(new QueryProto.Req().setMatchReq(
                new QueryProto.Match.Req().setQuery(query)
            ), options);
        }

        export function matchAggregateReq(query: string, options: Options) {
            return queryManagerReq(new QueryProto.Req().setMatchAggregateReq(
                new QueryProto.MatchAggregate.Req().setQuery(query)
            ), options);
        }

        export function matchGroupReq(query: string, options: Options) {
            return queryManagerReq(new QueryProto.Req().setMatchGroupReq(
                new QueryProto.MatchGroup.Req().setQuery(query)
            ), options);
        }

        export function matchGroupAggregateReq(query: string, options: Options) {
            return queryManagerReq(new QueryProto.Req().setMatchGroupAggregateReq(
                new QueryProto.MatchGroupAggregate.Req().setQuery(query)
            ), options);
        }

        export function insertReq(query: string, options: Options) {
            return queryManagerReq(new QueryProto.Req().setInsertReq(
                new QueryProto.Insert.Req().setQuery(query)
            ), options);
        }

        export function deleteReq(query: string, options: Options) {
            return queryManagerReq(new QueryProto.Req().setDeleteReq(
                new QueryProto.Delete.Req().setQuery(query)
            ), options);
        }

        export function updateReq(query: string, options: Options) {
            return queryManagerReq(new QueryProto.Req().setUpdateReq(
                new QueryProto.Update.Req().setQuery(query)
            ), options);
        }

        export function explainReq(id: number, options: Options) {
            return queryManagerReq(new QueryProto.Req().setExplainReq(
                new QueryProto.Explain.Req().setExplainableId(id)
            ), options);
        }
    }


    export namespace ConceptManager {

        function conceptManagerReq(req: ConceptMgrProto.Req): TransactionProto.Req {
            return new TransactionProto.Req().setConceptManagerReq(req);
        }

        export function putEntityTypeReq(label: string) {
            return conceptManagerReq(new ConceptMgrProto.Req().setPutEntityTypeReq(
                new ConceptMgrProto.PutEntityType.Req().setLabel(label))
            );
        }

        export function putRelationTypeReq(label: string) {
            return conceptManagerReq(new ConceptMgrProto.Req().setPutRelationTypeReq(
                new ConceptMgrProto.PutRelationType.Req().setLabel(label))
            );
        }

        export function putAttributeTypeReq(label: string, valueType: AttributeTypeProto.ValueType) {
            return conceptManagerReq(new ConceptMgrProto.Req().setPutAttributeTypeReq(
                new ConceptMgrProto.PutAttributeType.Req().setLabel(label).setValueType(valueType)
            ));
        }

        export function getThingTypeReq(label: string) {
            return conceptManagerReq(new ConceptMgrProto.Req().setGetThingTypeReq(
                new ConceptMgrProto.GetThingType.Req().setLabel(label)
            ));
        }

        export function getThingReq(iid: string) {
            return conceptManagerReq(new ConceptMgrProto.Req().setGetThingReq(
                new ConceptMgrProto.GetThing.Req().setIid(Bytes.hexStringToBytes(iid))
            ));
        }
    }

    export namespace Type {

        function typeReq(req: TypeProto.Req): TransactionProto.Req {
            return new TransactionProto.Req().setTypeReq(req);
        }

        function newReqBuilder(label: Label) {
            const builder = new TypeProto.Req().setLabel(label.name);
            if (label.scope) builder.setScope(label.scope);
            return builder;
        }

        export function setLabelReq(label: Label, newLabel: string) {
            return typeReq(newReqBuilder(label).setTypeSetLabelReq(
                new TypeProto.SetLabel.Req().setLabel(newLabel)
            ));
        }

        export function getSupertypesReq(label: Label) {
            return typeReq(newReqBuilder(label).setTypeGetSupertypesReq(
                new TypeProto.GetSupertypes.Req()
            ));
        }

        export function getSubtypesReq(label: Label) {
            return typeReq(newReqBuilder(label).setTypeGetSubtypesReq(
                new TypeProto.GetSubtypes.Req()
            ));
        }

        export function getSupertypeReq(label: Label) {
            return typeReq(newReqBuilder(label).setTypeGetSupertypeReq(
                new TypeProto.GetSupertype.Req()
            ));
        }

        export function deleteReq(label: Label) {
            return typeReq(newReqBuilder(label).setTypeDeleteReq(
                new TypeProto.Delete.Req()
            ));
        }

        export namespace RoleType {

            export function protoRoleType(label: Label, encoding: TypeProto.Encoding) {
                return new TypeProto().setScope(label.scope).setLabel(label.name).setEncoding(encoding);
            }

            export function getRelationTypesReq(label: Label) {
                return typeReq(newReqBuilder(label).setRoleTypeGetRelationTypesReq(
                    new RoleTypeProto.GetRelationTypes.Req()
                ));
            }

            export function getPlayerTypesReq(label: Label) {
                return typeReq(newReqBuilder(label).setRoleTypeGetPlayerTypesReq(
                    new RoleTypeProto.GetPlayerTypes.Req()
                ));
            }

            export function getPlayerTypesExplicitReq(label: Label) {
                return typeReq(newReqBuilder(label).setRoleTypeGetPlayerTypesExplicitReq(
                    new RoleTypeProto.GetPlayerTypesExplicit.Req()
                ));
            }

            export function getRelationInstancesReq(label: Label) {
                return typeReq(newReqBuilder(label).setRoleTypeGetRelationInstancesReq(
                    new RoleTypeProto.GetRelationInstances.Req()
                ));
            }

            export function getRelationInstancesExplicitReq(label: Label) {
                return typeReq(newReqBuilder(label).setRoleTypeGetRelationInstancesExplicitReq(
                    new RoleTypeProto.GetRelationInstancesExplicit.Req()
                ));
            }

            export function getPlayerInstancesReq(label: Label) {
                return typeReq(newReqBuilder(label).setRoleTypeGetPlayerInstancesReq(
                    new RoleTypeProto.GetPlayerInstances.Req()
                ));
            }

            export function getPlayerInstancesExplicitReq(label: Label) {
                return typeReq(newReqBuilder(label).setRoleTypeGetPlayerInstancesExplicitReq(
                    new RoleTypeProto.GetPlayerInstancesExplicit.Req()
                ));
            }
        }

        export namespace ThingType {

            export function protoThingType(label: Label, encoding: TypeProto.Encoding) {
                return new TypeProto().setLabel(label.name).setEncoding(encoding);
            }

            export function setAbstractReq(label: Label) {
                return typeReq(newReqBuilder(label).setThingTypeSetAbstractReq(
                    new ThingTypeProto.SetAbstract.Req()
                ));
            }

            export function unsetAbstractReq(label: Label) {
                return typeReq(newReqBuilder(label).setThingTypeUnsetAbstractReq(
                    new ThingTypeProto.UnsetAbstract.Req()
                ));
            }

            export function setSupertypeReq(label: Label, supertype: TypeProto) {
                return typeReq(newReqBuilder(label).setTypeSetSupertypeReq(
                    new TypeProto.SetSupertype.Req().setType(supertype)
                ));
            }

            export function getPlaysReq(label: Label) {
                return typeReq(newReqBuilder(label).setThingTypeGetPlaysReq(
                    new ThingTypeProto.GetPlays.Req()
                ));
            }

            export function getPlaysExplicitReq(label: Label) {
                return typeReq(newReqBuilder(label).setThingTypeGetPlaysExplicitReq(
                    new ThingTypeProto.GetPlaysExplicit.Req()
                ));
            }

            export function getPlaysOverriddenReq(label: Label) {
                return typeReq(newReqBuilder(label).setThingTypeGetPlaysOverriddenReq(
                    new ThingTypeProto.GetPlaysOverridden.Req()
                ));
            }

            export function setPlaysReq(label: Label, roleType: TypeProto) {
                return typeReq(newReqBuilder(label).setThingTypeSetPlaysReq(
                    new ThingTypeProto.SetPlays.Req().setRoleType(roleType)
                ));
            }

            export function setPlaysOverriddenReq(label: Label, roleType: TypeProto, overriddenRoleType: TypeProto) {
                return typeReq(newReqBuilder(label).setThingTypeSetPlaysReq(
                    new ThingTypeProto.SetPlays.Req().setRoleType(roleType)
                        .setOverriddenType(overriddenRoleType)
                ));
            }

            export function unsetPlaysReq(label: Label, roleType: TypeProto) {
                return typeReq(newReqBuilder(label).setThingTypeUnsetPlaysReq(
                    new ThingTypeProto.UnsetPlays.Req().setRoleType(roleType)
                ));
            }

            export function getOwnsReq(label: Label, annotations: TypeProto.Annotation[]) {
                return typeReq(newReqBuilder(label).setThingTypeGetOwnsReq(
                    new ThingTypeProto.GetOwns.Req().setAnnotationsList(annotations)
                ));
            }

            export function getOwnsByTypeReq(label: Label, valueType: AttributeTypeProto.ValueType, annotations: TypeProto.Annotation[]) {
                return typeReq(newReqBuilder(label).setThingTypeGetOwnsReq(
                    new ThingTypeProto.GetOwns.Req().setAnnotationsList(annotations)
                        .setValueType(valueType)
                ));
            }

            export function getOwnsExplicitReq(label: Label, annotations: TypeProto.Annotation[]) {
                return typeReq(newReqBuilder(label).setThingTypeGetOwnsExplicitReq(
                    new ThingTypeProto.GetOwnsExplicit.Req().setAnnotationsList(annotations)
                ));
            }

            export function getOwnsExplicitByTypeReq(label: Label, valueType: AttributeTypeProto.ValueType, annotations: TypeProto.Annotation[]) {
                return typeReq(newReqBuilder(label).setThingTypeGetOwnsExplicitReq(
                    new ThingTypeProto.GetOwnsExplicit.Req().setAnnotationsList(annotations)
                        .setValueType(valueType)
                ));
            }

            export function setOwnsReq(label: Label, attributeType: TypeProto, annotations: TypeProto.Annotation[]) {
                return typeReq(newReqBuilder(label).setThingTypeSetOwnsReq(
                    new ThingTypeProto.SetOwns.Req()
                        .setAttributeType(attributeType)
                        .setAnnotationsList(annotations)
                ));
            }

            export function setOwnsOverriddenReq(label: Label, attributeType: TypeProto, overriddenType: TypeProto, annotations: TypeProto.Annotation[]) {
                return typeReq(newReqBuilder(label).setThingTypeSetOwnsReq(
                    new ThingTypeProto.SetOwns.Req()
                        .setAttributeType(attributeType)
                        .setOverriddenType(overriddenType)
                        .setAnnotationsList(annotations)
                ));
            }

            export function unsetOwnsReq(label: Label, attributeType: TypeProto) {
                return typeReq(newReqBuilder(label).setThingTypeUnsetOwnsReq(
                    new ThingTypeProto.UnsetOwns.Req().setAttributeType(attributeType)
                ));
            }

            export function getInstancesReq(label: Label) {
                return typeReq(newReqBuilder(label).setThingTypeGetInstancesReq(
                    new ThingTypeProto.GetInstances.Req()
                ));
            }

            export function getOwnsOverriddenReq(label: Label, attributeType: TypeProto) {
                return typeReq(newReqBuilder(label).setThingTypeGetOwnsOverriddenReq(
                    new ThingTypeProto.GetOwnsOverridden.Req().setAttributeType(attributeType)
                ));
            }

            export function getSyntaxReq(label: Label) {
                return typeReq(newReqBuilder(label).setThingTypeGetSyntaxReq(new ThingTypeProto.GetSyntax.Req()));
            }
        }

        export namespace EntityType {

            export function createReq(label: Label) {
                return typeReq(newReqBuilder(label).setEntityTypeCreateReq(
                    new EntityTypeProto.Create.Req()
                ));
            }
        }

        export namespace RelationType {

            export function createReq(label: Label) {
                return typeReq(newReqBuilder(label).setRelationTypeCreateReq(
                    new RelationTypeProto.Create.Req()
                ));
            }

            export function getRelatesReq(label: Label) {
                return typeReq(newReqBuilder(label).setRelationTypeGetRelatesReq(
                    new RelationTypeProto.GetRelates.Req()
                ));
            }

            export function getRelatesExplicitReq(label: Label) {
                return typeReq(newReqBuilder(label).setRelationTypeGetRelatesExplicitReq(
                    new RelationTypeProto.GetRelatesExplicit.Req()
                ));
            }

            export function getRelatesByRoleReq(label: Label, roleLabel: string) {
                return typeReq(newReqBuilder(label).setRelationTypeGetRelatesForRoleLabelReq(
                    new RelationTypeProto.GetRelatesForRoleLabel.Req().setLabel(roleLabel)
                ));
            }

            export function getRelatesOverridden(label: Label, roleLabel: string) {
                return typeReq(newReqBuilder(label).setRelationTypeGetRelatesOverriddenReq(
                    new RelationTypeProto.GetRelatesOverridden.Req().setLabel(roleLabel)
                ));
            }

            export function setRelatesReq(label: Label, roleLabel: string) {
                return typeReq(newReqBuilder(label).setRelationTypeSetRelatesReq(
                    new RelationTypeProto.SetRelates.Req().setLabel(roleLabel)
                ));
            }

            export function setRelatesOverriddenReq(label: Label, roleLabel: string, overriddenLabel: string) {
                return typeReq(newReqBuilder(label).setRelationTypeSetRelatesReq(
                    new RelationTypeProto.SetRelates.Req().setLabel(roleLabel)
                        .setOverriddenLabel(overriddenLabel)
                ));
            }

            export function unsetRelatesReq(label: Label, roleLabel: string) {
                return typeReq(newReqBuilder(label).setRelationTypeUnsetRelatesReq(
                    new RelationTypeProto.UnsetRelates.Req().setLabel(roleLabel)
                ));
            }
        }

        export namespace AttributeType {

            export function getOwnersReq(label: Label, annotations: TypeProto.Annotation[]) {
                return typeReq(newReqBuilder(label).setAttributeTypeGetOwnersReq(
                    new AttributeTypeProto.GetOwners.Req().setAnnotationsList(annotations)
                ));
            }

            export function getOwnersExplicitReq(label: Label, annotations: TypeProto.Annotation[]) {
                return typeReq(newReqBuilder(label).setAttributeTypeGetOwnersExplicitReq(
                    new AttributeTypeProto.GetOwnersExplicit.Req().setAnnotationsList(annotations)
                ));
            }

            export function putReq(label: Label, value: AttributeProto.Value) {
                return typeReq(newReqBuilder(label).setAttributeTypePutReq(
                    new AttributeTypeProto.Put.Req().setValue(value)
                ));
            }

            export function getReq(label: Label, value: AttributeProto.Value) {
                return typeReq(newReqBuilder(label).setAttributeTypeGetReq(
                    new AttributeTypeProto.Get.Req().setValue(value)
                ));
            }

            export function getRegexReq(label: Label) {
                return typeReq(newReqBuilder(label).setAttributeTypeGetRegexReq(
                    new AttributeTypeProto.GetRegex.Req()
                ));
            }

            export function setRegexReq(label: Label, regex: string) {
                return typeReq(newReqBuilder(label).setAttributeTypeSetRegexReq(
                    new AttributeTypeProto.SetRegex.Req().setRegex(regex)
                ));
            }
        }

        export namespace Annotation {

            export function annotationKeyProto(): TypeProto.Annotation {
                return new TypeProto.Annotation().setKey(new TypeProto.Annotation.Key());
            }

            export function annotationUniqueProto() {
                return new TypeProto.Annotation().setUnique(new TypeProto.Annotation.Unique());
            }
        }

    }

    export namespace Thing {

        function thingReq(req: ThingProto.Req) {
            return new TransactionProto.Req().setThingReq(req);
        }

        export function protoThing(iid: string): ThingProto {
            return new ThingProto().setIid(Bytes.hexStringToBytes(iid));
        }

        export function getHasReqByAnnotations(iid: string, annotations: TypeProto.Annotation[]) {
            return thingReq(new ThingProto.Req().setIid(Bytes.hexStringToBytes(iid)).setThingGetHasReq(
                new ThingProto.GetHas.Req().setAnnotationsList(annotations)
            ));
        }

        export function getHasByTypeReq(iid: string, attributeTypes: TypeProto[]) {
            return thingReq(new ThingProto.Req().setIid(Bytes.hexStringToBytes(iid)).setThingGetHasReq(
                new ThingProto.GetHas.Req().setAttributeTypesList(attributeTypes)
            ));
        }

        export function setHasReq(iid: string, attribute: ThingProto) {
            return thingReq(new ThingProto.Req().setIid(Bytes.hexStringToBytes(iid)).setThingSetHasReq(
                new ThingProto.SetHas.Req().setAttribute(attribute)
            ));
        }

        export function unsetHasReq(iid: string, attribute: ThingProto) {
            return thingReq(new ThingProto.Req().setIid(Bytes.hexStringToBytes(iid)).setThingUnsetHasReq(
                new ThingProto.UnsetHas.Req().setAttribute(attribute)
            ));
        }

        export function getPlayingReq(iid: string) {
            return thingReq(new ThingProto.Req().setIid(Bytes.hexStringToBytes(iid)).setThingGetPlayingReq(
                new ThingProto.GetPlaying.Req()
            ));
        }

        export function getRelationsReq(iid: string, roleTypes: TypeProto[]) {
            return thingReq(new ThingProto.Req().setIid(Bytes.hexStringToBytes(iid)).setThingGetRelationsReq(
                new ThingProto.GetRelations.Req().setRoleTypesList(roleTypes)
            ));
        }

        export function deleteReq(iid: string) {
            return thingReq(new ThingProto.Req().setIid(Bytes.hexStringToBytes(iid)).setThingDeleteReq(
                new ThingProto.Delete.Req()
            ));
        }

        export namespace Relation {

            export function addPlayerReq(iid: string, roleType: TypeProto, player: ThingProto) {
                return thingReq(new ThingProto.Req().setIid(Bytes.hexStringToBytes(iid)).setRelationAddPlayerReq(
                    new RelationProto.AddPlayer.Req().setRoleType(roleType).setPlayer(player)
                ));
            }

            export function removePlayerReq(iid: string, roleType: TypeProto, player: ThingProto) {
                return thingReq(new ThingProto.Req().setIid(Bytes.hexStringToBytes(iid)).setRelationRemovePlayerReq(
                    new RelationProto.RemovePlayer.Req().setRoleType(roleType).setPlayer(player)
                ));
            }

            export function getPlayersReq(iid: string, roleTypes: TypeProto[]) {
                return thingReq(new ThingProto.Req().setIid(Bytes.hexStringToBytes(iid)).setRelationGetPlayersReq(
                    new RelationProto.GetPlayers.Req().setRoleTypesList(roleTypes)
                ));
            }

            export function getPlayersByRoleTypeReq(iid: string) {
                return thingReq(new ThingProto.Req().setIid(Bytes.hexStringToBytes(iid)).setRelationGetPlayersByRoleTypeReq(
                    new RelationProto.GetPlayersByRoleType.Req()
                ));
            }

            export function getRelatingReq(iid: string) {
                return thingReq(new ThingProto.Req().setIid(Bytes.hexStringToBytes(iid)).setRelationGetRelatingReq(
                    new RelationProto.GetRelating.Req()
                ));
            }
        }

        export namespace Attribute {

            export function getOwnersReq(iid: string) {
                return thingReq(new ThingProto.Req().setIid(Bytes.hexStringToBytes(iid)).setAttributeGetOwnersReq(
                    new AttributeProto.GetOwners.Req()
                ));
            }

            export function getOwnersByTypeReq(iid: string, ownerType: TypeProto) {
                return thingReq(new ThingProto.Req().setIid(Bytes.hexStringToBytes(iid)).setAttributeGetOwnersReq(
                    new AttributeProto.GetOwners.Req().setThingType(ownerType)
                ));
            }

            export function attributeValueBooleanReq(value: boolean): AttributeProto.Value {
                return new AttributeProto.Value().setBoolean(value);
            }

            export function attributeValueLongReq(value: number): AttributeProto.Value {
                return new AttributeProto.Value().setLong(value);
            }

            export function attributeValueDoubleReq(value: number): AttributeProto.Value {
                return new AttributeProto.Value().setDouble(value);
            }

            export function attributeValueStringReq(value: string): AttributeProto.Value {
                return new AttributeProto.Value().setString(value);
            }

            export function attributeValueDateTimeReq(value: Date): AttributeProto.Value {
                return new AttributeProto.Value().setDateTime(value.getTime());
            }
        }
    }
}
