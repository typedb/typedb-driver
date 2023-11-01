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
    Attribute as AttributeProto,
    AttributeGetOwnersReq,
    AttributeType as AttributeTypeProto,
    AttributeTypeGetInstancesReq,
    AttributeTypeGetOwnersReq,
    AttributeTypeGetRegexReq,
    AttributeTypeGetReq,
    AttributeTypeGetSubtypesReq,
    AttributeTypeGetSupertypeReq,
    AttributeTypeGetSupertypesReq,
    AttributeTypePutReq,
    AttributeTypeSetRegexReq,
    AttributeTypeSetSupertypeReq,
    ConceptManagerGetAttributeReq,
    ConceptManagerGetAttributeTypeReq,
    ConceptManagerGetEntityReq,
    ConceptManagerGetEntityTypeReq,
    ConceptManagerGetRelationReq,
    ConceptManagerGetRelationTypeReq,
    ConceptManagerGetSchemaExceptionsReq,
    ConceptManagerPutAttributeTypeReq,
    ConceptManagerPutEntityTypeReq,
    ConceptManagerPutRelationTypeReq,
    ConceptManagerReq,
    Entity as EntityProto,
    EntityType as EntityTypeProto,
    EntityTypeCreateReq,
    EntityTypeGetInstancesReq,
    EntityTypeGetSubtypesReq,
    EntityTypeGetSupertypeReq,
    EntityTypeGetSupertypesReq,
    EntityTypeSetSupertypeReq,
    Relation as RelationProto,
    RelationAddRolePlayerReq,
    RelationGetPlayersByRoleTypeReq,
    RelationGetRelatingReq,
    RelationGetRolePlayersReq,
    RelationRemoveRolePlayerReq,
    RelationRolePlayer,
    RelationType as RelationTypeProto,
    RelationTypeCreateReq,
    RelationTypeGetInstancesReq,
    RelationTypeGetRelatesForRoleLabelReq,
    RelationTypeGetRelatesOverriddenReq,
    RelationTypeGetRelatesReq,
    RelationTypeGetSubtypesReq,
    RelationTypeGetSupertypeReq,
    RelationTypeGetSupertypesReq,
    RelationTypeSetRelatesReq,
    RelationTypeSetSupertypeReq,
    RelationTypeUnsetRelatesReq,
    RoleType as RoleTypeProto,
    RoleType,
    RoleTypeDeleteReq,
    RoleTypeGetPlayerInstancesReq,
    RoleTypeGetPlayerTypesReq,
    RoleTypeGetRelationInstancesReq,
    RoleTypeGetRelationTypesReq,
    RoleTypeGetSubtypesReq,
    RoleTypeGetSupertypeReq,
    RoleTypeGetSupertypesReq,
    RoleTypeReq,
    RoleTypeSetLabelReq,
    Thing as ThingProto,
    ThingDeleteReq,
    ThingGetHasReq,
    ThingGetPlayingReq,
    ThingGetRelationsReq,
    ThingReq,
    ThingSetHasReq,
    ThingType as ThingTypeProto,
    ThingTypeDeleteReq,
    ThingTypeGetOwnsOverriddenReq,
    ThingTypeGetOwnsReq,
    ThingTypeGetPlaysOverriddenReq,
    ThingTypeGetPlaysReq,
    ThingTypeGetSyntaxReq,
    ThingTypeReq,
    ThingTypeSetAbstractReq,
    ThingTypeSetLabelReq,
    ThingTypeSetOwnsReq,
    ThingTypeSetPlaysReq,
    ThingTypeUnsetAbstractReq,
    ThingTypeUnsetOwnsReq,
    ThingTypeUnsetPlaysReq,
    ThingUnsetHasReq,
    TypeAnnotation,
    TypeAnnotationKey,
    TypeAnnotationUnique,
    TypeReq,
    TypeTransitivity,
    Value as ValueProto,
    ValueType,
} from "typedb-protocol/proto/concept";
import {
    LogicManagerGetRuleReq,
    LogicManagerGetRulesReq,
    LogicManagerPutRuleReq,
    LogicManagerReq,
    RuleDeleteReq,
    RuleReq,
    RuleSetLabelReq
} from "typedb-protocol/proto/logic";
import {Options} from "typedb-protocol/proto/options";
import {
    QueryManagerDefineReq,
    QueryManagerDeleteReq,
    QueryManagerExplainReq,
    QueryManagerInsertReq,
    QueryManagerGetAggregateReq,
    QueryManagerGetGroupAggregateReq,
    QueryManagerGetGroupReq,
    QueryManagerGetReq,
    QueryManagerFetchReq,
    QueryManagerReq,
    QueryManagerUndefineReq,
    QueryManagerUpdateReq
} from "typedb-protocol/proto/query";
import {Version} from "typedb-protocol/proto/version";
import * as uuid from "uuid";
import {Label} from "../Label";
import {Bytes} from "../util/Bytes";
import {
    UserManagerAllReq,
    UserManagerContainsReq,
    UserManagerCreateReq,
    UserManagerDeleteReq,
    UserManagerGetReq,
    UserManagerPasswordSetReq,
    UserPasswordUpdateReq,
    UserTokenReq
} from "typedb-protocol/proto/user";
import {
    DatabaseDeleteReq,
    DatabaseManagerAllReq,
    DatabaseManagerContainsReq,
    DatabaseManagerCreateReq,
    DatabaseManagerGetReq,
    DatabaseRuleSchemaReq,
    DatabaseSchemaReq,
    DatabaseTypeSchemaReq
} from "typedb-protocol/proto/database";
import {
    TransactionClient,
    TransactionCommitReq,
    TransactionOpenReq,
    TransactionReq,
    TransactionRollbackReq,
    TransactionStreamReq,
    TransactionType
} from "typedb-protocol/proto/transaction";
import {ServerManagerAllReq} from "typedb-protocol/proto/server";
import {ConnectionOpenReq} from "typedb-protocol/proto/connection";
import {SessionCloseReq, SessionOpenReq, SessionPulseReq, SessionType} from "typedb-protocol/proto/session";
import {ErrorMessage} from "../errors/ErrorMessage";
import {TypeDBDriverError} from "../errors/TypeDBDriverError";
import BAD_VALUE_TYPE = ErrorMessage.Concept.BAD_VALUE_TYPE;


/* eslint no-inner-declarations: "off" */
export namespace RequestBuilder {
    export namespace DatabaseManager {
        export function getReq(name: string) {
            return new DatabaseManagerGetReq({name: name});
        }

        export function createReq(name: string) {
            return new DatabaseManagerCreateReq({name: name});
        }

        export function containsReq(name: string) {
            return new DatabaseManagerContainsReq({name: name});
        }

        export function allReq() {
            return new DatabaseManagerAllReq();
        }
    }

    export namespace Database {
        export function schemaReq(name: string) {
            return new DatabaseSchemaReq({name: name});
        }

        export function typeSchemaReq(name: string) {
            return new DatabaseTypeSchemaReq({name: name});
        }

        export function ruleSchemaReq(name: string) {
            return new DatabaseRuleSchemaReq({name: name});
        }

        export function deleteReq(name: string) {
            return new DatabaseDeleteReq({name: name});
        }
    }

    export namespace ServerManager {
        export function allReq() {
            return new ServerManagerAllReq();
        }
    }

    export namespace UserManager {
        export function containsReq(name: string): UserManagerContainsReq {
            return new UserManagerContainsReq({username: name});
        }

        export function createReq(name: string, password: string): UserManagerCreateReq {
            return new UserManagerCreateReq({username: name, password: password});
        }

        export function deleteReq(name: string): UserManagerDeleteReq {
            return new UserManagerDeleteReq({username: name});
        }

        export function allReq(): UserManagerAllReq {
            return new UserManagerAllReq();
        }

        export function passwordSetReq(name: string, password: string): UserManagerPasswordSetReq {
            return new UserManagerPasswordSetReq({username: name, password: password});
        }

        export function getReq(name: string): UserManagerGetReq {
            return new UserManagerGetReq({username: name});
        }
    }

    export namespace User {
        export function passwordUpdateReq(name: string, passwordOld: string, passwordNew: string): UserPasswordUpdateReq {
            return new UserPasswordUpdateReq({username: name, password_old: passwordOld, password_new: passwordNew});
        }

        export function tokenReq(username: string) {
            return new UserTokenReq({username: username});
        }
    }

    export namespace Connection {
        export function openReq() {
            return new ConnectionOpenReq({version: Version.VERSION})
        }
    }

    export namespace Session {
        export function openReq(database: string, type: SessionType, options: Options) {
            return new SessionOpenReq({database: database, type: type, options: options});
        }

        export function closeReq(id: string) {
            return new SessionCloseReq({session_id: Bytes.hexStringToBytes(id)});
        }

        export function pulseReq(id: string) {
            return new SessionPulseReq({session_id: Bytes.hexStringToBytes(id)});
        }
    }

    export namespace Transaction {
        export function clientReq(reqs: TransactionReq[]) {
            return new TransactionClient({reqs: reqs});
        }

        export function openReq(sessionId: string, type: TransactionType, options: Options, latencyMillis: number) {
            return new TransactionReq({
                open_req:
                    new TransactionOpenReq({
                        session_id: Bytes.hexStringToBytes(sessionId),
                        type: type,
                        options: options,
                        network_latency_millis: latencyMillis
                    })
            });
        }

        export function commitReq() {
            return new TransactionReq({commit_req: new TransactionCommitReq()});
        }

        export function rollbackReq() {
            return new TransactionReq({rollback_req: new TransactionRollbackReq()});
        }

        export function streamReq(requestId: string) {
            return new TransactionReq({
                req_id: uuid.parse(requestId) as Uint8Array,
                stream_req: new TransactionStreamReq()
            });
        }
    }

    export namespace LogicManager {
        export function logicManagerReq(logicReq: LogicManagerReq) {
            return new TransactionReq({logic_manager_req: logicReq});
        }

        export function putRuleReq(label: string, when: string, then: string) {
            return logicManagerReq(new LogicManagerReq({
                put_rule_req: new LogicManagerPutRuleReq({
                    label: label,
                    when: when,
                    then: then
                })
            }));
        }

        export function getRuleReq(label: string) {
            return logicManagerReq(new LogicManagerReq({
                get_rule_req: new LogicManagerGetRuleReq({label: label})
            }));
        }

        export function getRulesReq() {
            return logicManagerReq(new LogicManagerReq({get_rules_req: new LogicManagerGetRulesReq()}));
        }
    }

    export namespace Rule {
        export function ruleReq(request: RuleReq) {
            return new TransactionReq({rule_req: request});
        }

        export function setLabelReq(currentLabel: string, newLabel: string) {
            return ruleReq(new RuleReq({
                label: currentLabel,
                rule_set_label_req: new RuleSetLabelReq({label: newLabel})
            }));
        }

        export function deleteReq(label: string) {
            return ruleReq(new RuleReq({label: label, rule_delete_req: new RuleDeleteReq()}));
        }
    }

    export namespace QueryManager {
        function queryManagerReq(queryReq: QueryManagerReq, options: Options) {
            queryReq.options = options;
            return new TransactionReq({query_manager_req: queryReq});
        }

        export function defineReq(query: string, options: Options) {
            return queryManagerReq(new QueryManagerReq({
                define_req: new QueryManagerDefineReq({query: query})
            }), options);
        }

        export function undefineReq(query: string, options: Options) {
            return queryManagerReq(new QueryManagerReq({
                undefine_req: new QueryManagerUndefineReq({query: query})
            }), options);
        }

        export function getReq(query: string, options: Options) {
            return queryManagerReq(new QueryManagerReq({
                get_req: new QueryManagerGetReq({query: query})
            }), options);
        }

        export function getAggregateReq(query: string, options: Options) {
            return queryManagerReq(new QueryManagerReq({
                get_aggregate_req: new QueryManagerGetAggregateReq({query: query})
            }), options);
        }

        export function getGroupReq(query: string, options: Options) {
            return queryManagerReq(new QueryManagerReq({
                get_group_req: new QueryManagerGetGroupReq({query: query})
            }), options);
        }

        export function getGroupAggregateReq(query: string, options: Options) {
            return queryManagerReq(new QueryManagerReq({
                get_group_aggregate_req: new QueryManagerGetGroupAggregateReq({query: query})
            }), options);
        }

        export function fetchReq(query: string, options: Options) {
            return queryManagerReq(new QueryManagerReq({
                fetch_req: new QueryManagerFetchReq({query: query})
            }), options);
        }

        export function insertReq(query: string, options: Options) {
            return queryManagerReq(new QueryManagerReq({
                insert_req: new QueryManagerInsertReq({query: query})
            }), options);
        }

        export function deleteReq(query: string, options: Options) {
            return queryManagerReq(new QueryManagerReq({
                delete_req: new QueryManagerDeleteReq({query: query})
            }), options);
        }

        export function updateReq(query: string, options: Options) {
            return queryManagerReq(new QueryManagerReq({
                update_req: new QueryManagerUpdateReq({query: query})
            }), options);
        }

        export function explainReq(id: number, options: Options) {
            return queryManagerReq(new QueryManagerReq({
                explain_req: new QueryManagerExplainReq({explainable_id: id})
            }), options);
        }
    }

    export namespace ConceptManager {
        function conceptManagerReq(req: ConceptManagerReq): TransactionReq {
            return new TransactionReq({concept_manager_req: req});
        }

        export function putEntityTypeReq(label: string) {
            return conceptManagerReq(new ConceptManagerReq({
                put_entity_type_req: new ConceptManagerPutEntityTypeReq({label: label})
            }));
        }

        export function putRelationTypeReq(label: string) {
            return conceptManagerReq(new ConceptManagerReq({
                put_relation_type_req: new ConceptManagerPutRelationTypeReq({label: label})
            }));
        }

        export function putAttributeTypeReq(label: string, valueType: ValueType) {
            return conceptManagerReq(new ConceptManagerReq({
                put_attribute_type_req: new ConceptManagerPutAttributeTypeReq({label: label, value_type: valueType})
            }));
        }

        export function getEntityTypeReq(label: string) {
            return conceptManagerReq(new ConceptManagerReq({
                get_entity_type_req: new ConceptManagerGetEntityTypeReq({label: label})
            }));
        }

        export function getRelationTypeReq(label: string) {
            return conceptManagerReq(new ConceptManagerReq({
                get_relation_type_req: new ConceptManagerGetRelationTypeReq({label: label})
            }));
        }

        export function getAttributeTypeReq(label: string) {
            return conceptManagerReq(new ConceptManagerReq({
                get_attribute_type_req: new ConceptManagerGetAttributeTypeReq({label: label})
            }));
        }

        export function getEntityReq(iid: string) {
            return conceptManagerReq(new ConceptManagerReq({
                get_entity_req: new ConceptManagerGetEntityReq({iid: Bytes.hexStringToBytes(iid)})
            }));
        }

        export function getRelationReq(iid: string) {
            return conceptManagerReq(new ConceptManagerReq({
                get_relation_req: new ConceptManagerGetRelationReq({iid: Bytes.hexStringToBytes(iid)})
            }));
        }

        export function getAttributeReq(iid: string) {
            return conceptManagerReq(new ConceptManagerReq({
                get_attribute_req: new ConceptManagerGetAttributeReq({iid: Bytes.hexStringToBytes(iid)})
            }));
        }

        export function getSchemaExceptions() {
            return conceptManagerReq(new ConceptManagerReq({
                get_schema_exceptions_req: new ConceptManagerGetSchemaExceptionsReq()
            }));
        }
    }

    export namespace Type {
        export namespace RoleType {
            function roleTypeReq(req: RoleTypeReq): TransactionReq {
                return new TransactionReq({type_req: new TypeReq({role_type_req: req})});
            }

            function newReqBuilder(label: Label) {
                return {label: label.name, scope: label.scope};
            }

            export function protoRoleType(label: Label) {
                return new RoleTypeProto({scope: label.scope, label: label.name});
            }

            export function deleteReq(label: Label) {
                return roleTypeReq(new RoleTypeReq({
                    ...newReqBuilder(label),
                    role_type_delete_req: new RoleTypeDeleteReq(),
                }));
            }

            export function setLabelReq(label: Label, newLabel: string) {
                return roleTypeReq(new RoleTypeReq({
                    ...newReqBuilder(label),
                    role_type_set_label_req: new RoleTypeSetLabelReq({label: newLabel}),
                }));
            }

            export function getSupertypeReq(label: Label) {
                return roleTypeReq(new RoleTypeReq({
                    ...newReqBuilder(label),
                    role_type_get_supertype_req: new RoleTypeGetSupertypeReq(),
                }));
            }

            export function getSupertypesReq(label: Label) {
                return roleTypeReq(new RoleTypeReq({
                    ...newReqBuilder(label),
                    role_type_get_supertypes_req: new RoleTypeGetSupertypesReq(),
                }));
            }

            export function getSubtypesReq(label: Label, transitivity: TypeTransitivity) {
                return roleTypeReq(new RoleTypeReq({
                    ...newReqBuilder(label),
                    role_type_get_subtypes_req: new RoleTypeGetSubtypesReq({transitivity: transitivity}),
                }));
            }

            export function getRelationTypesReq(label: Label) {
                return roleTypeReq(new RoleTypeReq({
                    ...newReqBuilder(label),
                    role_type_get_relation_types_req: new RoleTypeGetRelationTypesReq(),
                }));
            }

            export function getPlayerTypesReq(label: Label, transitivity: TypeTransitivity) {
                return roleTypeReq(new RoleTypeReq({
                    ...newReqBuilder(label),
                    role_type_get_player_types_req: new RoleTypeGetPlayerTypesReq({transitivity: transitivity}),
                }));
            }

            export function getRelationInstancesReq(label: Label, transitivity: TypeTransitivity) {
                return roleTypeReq(new RoleTypeReq({
                    ...newReqBuilder(label),
                    role_type_get_relation_instances_req: new RoleTypeGetRelationInstancesReq({transitivity: transitivity}),
                }));
            }

            export function getPlayerInstancesReq(label: Label, transitivity: TypeTransitivity) {
                return roleTypeReq(new RoleTypeReq({
                    ...newReqBuilder(label),
                    role_type_get_player_instances_req: new RoleTypeGetPlayerInstancesReq({transitivity: transitivity}),
                }));
            }
        }

        function thingTypeReq(req: ThingTypeReq): TransactionReq {
            return new TransactionReq({type_req: new TypeReq({thing_type_req: req})});
        }

        export namespace ThingType {
            export function protoThingTypeEntityType(label: Label): ThingTypeProto {
                return new ThingTypeProto({entity_type: EntityType.protoEntityType(label)});
            }

            export function protoThingTypeRelationType(label: Label): ThingTypeProto {
                return new ThingTypeProto({relation_type: RelationType.protoRelationType(label)});
            }

            export function protoThingTypeAttributeType(label: Label): ThingTypeProto {
                return new ThingTypeProto({attribute_type: AttributeType.protoAttributeType(label)});
            }

            export function deleteReq(label: Label) {
                return thingTypeReq(new ThingTypeReq({
                    label: label.name,
                    thing_type_delete_req: new ThingTypeDeleteReq(),
                }));
            }

            export function setLabelReq(label: Label, newLabel: string) {
                return thingTypeReq(new ThingTypeReq({
                    label: label.name,
                    thing_type_set_label_req: new ThingTypeSetLabelReq({label: newLabel}),
                }));
            }

            export function setAbstractReq(label: Label) {
                return thingTypeReq(new ThingTypeReq({
                    label: label.name,
                    thing_type_set_abstract_req: new ThingTypeSetAbstractReq(),
                }));
            }

            export function unsetAbstractReq(label: Label) {
                return thingTypeReq(new ThingTypeReq({
                    label: label.name,
                    thing_type_unset_abstract_req: new ThingTypeUnsetAbstractReq(),
                }));
            }

            export function getOwnsReq(label: Label, value_type: ValueType, annotations: TypeAnnotation[], transitivity: TypeTransitivity) {
                return thingTypeReq(new ThingTypeReq({
                    label: label.name,
                    thing_type_get_owns_req: new ThingTypeGetOwnsReq({
                        value_type: value_type,
                        annotations: annotations,
                        transitivity: transitivity,
                    }),
                }));
            }

            export function getOwnsOverriddenReq(label: Label, attributeType: AttributeTypeProto) {
                return thingTypeReq(new ThingTypeReq({
                    label: label.name,
                    thing_type_get_owns_overridden_req: new ThingTypeGetOwnsOverriddenReq({attribute_type: attributeType}),
                }));
            }

            export function setOwnsReq(
                label: Label,
                attributeType: AttributeTypeProto,
                overriddenType: AttributeTypeProto | null,
                annotations: TypeAnnotation[],
            ) {
                return thingTypeReq(new ThingTypeReq({
                    label: label.name,
                    thing_type_set_owns_req: new ThingTypeSetOwnsReq({
                        attribute_type: attributeType,
                        overridden_type: overriddenType,
                        annotations: annotations,
                    }),
                }));
            }

            export function unsetOwnsReq(label: Label, attributeType: AttributeTypeProto) {
                return thingTypeReq(new ThingTypeReq({
                    label: label.name,
                    thing_type_unset_owns_req: new ThingTypeUnsetOwnsReq({attribute_type: attributeType}),
                }));
            }

            export function getPlaysReq(label: Label, transitivity: TypeTransitivity) {
                return thingTypeReq(new ThingTypeReq({
                    label: label.name,
                    thing_type_get_plays_req: new ThingTypeGetPlaysReq({transitivity: transitivity}),
                }));
            }

            export function getPlaysOverriddenReq(label: Label, roleType: RoleTypeProto) {
                return thingTypeReq(new ThingTypeReq({
                    label: label.name,
                    thing_type_get_plays_overridden_req: new ThingTypeGetPlaysOverriddenReq({role_type: roleType}),
                }));
            }

            export function setPlaysReq(label: Label, roleType: RoleTypeProto, overriddenType?: RoleTypeProto) {
                return thingTypeReq(new ThingTypeReq({
                    label: label.name,
                    thing_type_set_plays_req: new ThingTypeSetPlaysReq({
                        role_type: roleType,
                        overridden_role_type: overriddenType,
                    }),
                }));
            }

            export function unsetPlaysReq(label: Label, roleType: RoleTypeProto) {
                return thingTypeReq(new ThingTypeReq({
                    label: label.name,
                    thing_type_unset_plays_req: new ThingTypeUnsetPlaysReq({role_type: roleType}),
                }));
            }

            export function getSyntaxReq(label: Label) {
                return thingTypeReq(new ThingTypeReq({
                    label: label.name,
                    thing_type_get_syntax_req: new ThingTypeGetSyntaxReq(),
                }));
            }
        }

        export namespace EntityType {
            export function protoEntityType(label: Label): EntityTypeProto {
                return new EntityTypeProto({label: label.name});
            }

            export function createReq(label: Label) {
                return thingTypeReq(new ThingTypeReq({
                    label: label.name,
                    entity_type_create_req: new EntityTypeCreateReq(),
                }));
            }

            export function getSupertypeReq(label: Label) {
                return thingTypeReq(new ThingTypeReq({
                    label: label.name,
                    entity_type_get_supertype_req: new EntityTypeGetSupertypeReq(),
                }));
            }

            export function setSupertypeReq(label: Label, supertype: EntityTypeProto) {
                return thingTypeReq(new ThingTypeReq({
                    label: label.name,
                    entity_type_set_supertype_req: new EntityTypeSetSupertypeReq({entity_type: supertype}),
                }));
            }

            export function getSupertypesReq(label: Label) {
                return thingTypeReq(new ThingTypeReq({
                    label: label.name,
                    entity_type_get_supertypes_req: new EntityTypeGetSupertypesReq(),
                }));
            }

            export function getSubtypesReq(label: Label, transitivity: TypeTransitivity) {
                return thingTypeReq(new ThingTypeReq({
                    label: label.name,
                    entity_type_get_subtypes_req: new EntityTypeGetSubtypesReq({transitivity: transitivity}),
                }));
            }

            export function getInstancesReq(label: Label, transitivity: TypeTransitivity) {
                return thingTypeReq(new ThingTypeReq({
                    label: label.name,
                    entity_type_get_instances_req: new EntityTypeGetInstancesReq({transitivity: transitivity}),
                }));
            }
        }

        export namespace RelationType {
            export function protoRelationType(label: Label): RelationTypeProto {
                return new RelationTypeProto({label: label.name});
            }

            export function createReq(label: Label) {
                return thingTypeReq(new ThingTypeReq({
                    label: label.name,
                    relation_type_create_req: new RelationTypeCreateReq(),
                }));
            }

            export function getSupertypeReq(label: Label) {
                return thingTypeReq(new ThingTypeReq({
                    label: label.name,
                    relation_type_get_supertype_req: new RelationTypeGetSupertypeReq(),
                }));
            }

            export function setSupertypeReq(label: Label, supertype: RelationTypeProto) {
                return thingTypeReq(new ThingTypeReq({
                    label: label.name,
                    relation_type_set_supertype_req: new RelationTypeSetSupertypeReq({relation_type: supertype}),
                }));
            }

            export function getSupertypesReq(label: Label) {
                return thingTypeReq(new ThingTypeReq({
                    label: label.name,
                    relation_type_get_supertypes_req: new RelationTypeGetSupertypesReq(),
                }));
            }

            export function getSubtypesReq(label: Label, transitivity: TypeTransitivity) {
                return thingTypeReq(new ThingTypeReq({
                    label: label.name,
                    relation_type_get_subtypes_req: new RelationTypeGetSubtypesReq({transitivity: transitivity}),
                }));
            }

            export function getInstancesReq(label: Label, transitivity: TypeTransitivity) {
                return thingTypeReq(new ThingTypeReq({
                    label: label.name,
                    relation_type_get_instances_req: new RelationTypeGetInstancesReq({transitivity: transitivity}),
                }));
            }

            export function getRelatesReq(label: Label, transitivity: TypeTransitivity) {
                return thingTypeReq(new ThingTypeReq({
                    label: label.name,
                    relation_type_get_relates_req: new RelationTypeGetRelatesReq({transitivity: transitivity}),
                }));
            }

            export function getRelatesForRoleLabel(label: Label, roleLabel: string) {
                return thingTypeReq(new ThingTypeReq({
                    label: label.name,
                    relation_type_get_relates_for_role_label_req: new RelationTypeGetRelatesForRoleLabelReq({label: roleLabel}),
                }));
            }

            export function getRelatesOverriddenReq(label: Label, roleLabel: string) {
                return thingTypeReq(new ThingTypeReq({
                    label: label.name,
                    relation_type_get_relates_overridden_req: new RelationTypeGetRelatesOverriddenReq({label: roleLabel}),
                }));
            }

            export function setRelatesReq(label: Label, roleLabel: string, overriddenLabel?: string) {
                return thingTypeReq(new ThingTypeReq({
                    label: label.name,
                    relation_type_set_relates_req: new RelationTypeSetRelatesReq({
                        label: roleLabel,
                        overridden_label: overriddenLabel,
                    })
                }));
            }

            export function unsetRelatesReq(label: Label, roleLabel: string) {
                return thingTypeReq(new ThingTypeReq({
                    label: label.name,
                    relation_type_unset_relates_req: new RelationTypeUnsetRelatesReq({label: roleLabel})
                }));
            }
        }

        export namespace AttributeType {
            export function protoAttributeType(label: Label): AttributeTypeProto {
                return new AttributeTypeProto({label: label.name});
            }

            export function putReq(label: Label, value: ValueProto) {
                return thingTypeReq(new ThingTypeReq({
                    label: label.name,
                    attribute_type_put_req: new AttributeTypePutReq({value: value}),
                }));
            }

            export function getReq(label: Label, value: ValueProto) {
                return thingTypeReq(new ThingTypeReq({
                    label: label.name,
                    attribute_type_get_req: new AttributeTypeGetReq({value: value}),
                }));
            }

            export function getSupertypeReq(label: Label) {
                return thingTypeReq(new ThingTypeReq({
                    label: label.name,
                    attribute_type_get_supertype_req: new AttributeTypeGetSupertypeReq(),
                }));
            }

            export function setSupertypeReq(label: Label, supertype: AttributeTypeProto) {
                return thingTypeReq(new ThingTypeReq({
                    label: label.name,
                    attribute_type_set_supertype_req: new AttributeTypeSetSupertypeReq({attribute_type: supertype}),
                }));
            }

            export function getSupertypesReq(label: Label) {
                return thingTypeReq(new ThingTypeReq({
                    label: label.name,
                    attribute_type_get_supertypes_req: new AttributeTypeGetSupertypesReq(),
                }));
            }

            export function getSubtypesReq(label: Label, transitivity: TypeTransitivity, valueType?: ValueType) {
                return thingTypeReq(new ThingTypeReq({
                    label: label.name,
                    attribute_type_get_subtypes_req: new AttributeTypeGetSubtypesReq({
                        transitivity: transitivity,
                        value_type: valueType,
                    }),
                }));
            }

            export function getInstancesReq(label: Label, transitivity: TypeTransitivity) {
                return thingTypeReq(new ThingTypeReq({
                    label: label.name,
                    attribute_type_get_instances_req: new AttributeTypeGetInstancesReq({transitivity: transitivity}),
                }));
            }

            export function getRegexReq(label: Label) {
                return thingTypeReq(new ThingTypeReq({
                    label: label.name,
                    attribute_type_get_regex_req: new AttributeTypeGetRegexReq(),
                }));
            }

            export function setRegexReq(label: Label, regex: string) {
                return thingTypeReq(new ThingTypeReq({
                    label: label.name,
                    attribute_type_set_regex_req: new AttributeTypeSetRegexReq({regex: regex}),
                }));
            }

            export function getOwnersReq(label: Label, transitivity: TypeTransitivity, annotations: TypeAnnotation[]) {
                return thingTypeReq(new ThingTypeReq({
                    label: label.name,
                    attribute_type_get_owners_req: new AttributeTypeGetOwnersReq({
                        transitivity: transitivity,
                        annotations: annotations,
                    }),
                }));
            }
        }

        export namespace Annotation {
            export function annotationKey(): TypeAnnotation {
                return new TypeAnnotation({key: new TypeAnnotationKey()});
            }

            export function annotationUnique() {
                return new TypeAnnotation({unique: new TypeAnnotationUnique()});
            }
        }
    }

    export namespace Thing {
        export function protoThingEntity(iid: string): ThingProto {
            return new ThingProto({entity: Entity.protoEntity(iid)});
        }

        export function protoThingRelation(iid: string): ThingProto {
            return new ThingProto({relation: Relation.protoRelation(iid)});
        }

        export function protoThingAttribute(iid: string): ThingProto {
            return new ThingProto({attribute: Attribute.protoAttribute(iid)});
        }

        function thingReq(req: ThingReq) {
            return new TransactionReq({thing_req: req});
        }

        export function getHasReq(iid: string, attributeTypes: AttributeTypeProto[], annotations: TypeAnnotation[]) {
            return thingReq(new ThingReq({
                iid: Bytes.hexStringToBytes(iid),
                thing_get_has_req: new ThingGetHasReq({annotations: annotations, attribute_types: attributeTypes})
            }));
        }

        export function setHasReq(iid: string, attribute: AttributeProto) {
            return thingReq(new ThingReq({
                iid: Bytes.hexStringToBytes(iid),
                thing_set_has_req: new ThingSetHasReq({attribute: attribute}),
            }));
        }

        export function unsetHasReq(iid: string, attribute: AttributeProto) {
            return thingReq(new ThingReq({
                iid: Bytes.hexStringToBytes(iid),
                thing_unset_has_req: new ThingUnsetHasReq({attribute: attribute}),
            }));
        }

        export function getPlayingReq(iid: string) {
            return thingReq(new ThingReq({
                iid: Bytes.hexStringToBytes(iid),
                thing_get_playing_req: new ThingGetPlayingReq(),
            }));
        }

        export function getRelationsReq(iid: string, roleTypes: RoleType[]) {
            return thingReq(new ThingReq({
                iid: Bytes.hexStringToBytes(iid),
                thing_get_relations_req: new ThingGetRelationsReq({role_types: roleTypes}),
            }));
        }

        export function deleteReq(iid: string) {
            return thingReq(new ThingReq({
                iid: Bytes.hexStringToBytes(iid),
                thing_delete_req: new ThingDeleteReq(),
            }));
        }

        export namespace Entity {
            export function protoEntity(iid: string): EntityProto {
                return new EntityProto({iid: Bytes.hexStringToBytes(iid)});
            }
        }

        export namespace Relation {
            export function protoRelation(iid: string): RelationProto {
                return new RelationProto({iid: Bytes.hexStringToBytes(iid)});
            }

            export function protoRolePlayer(roleType: RoleType, player: ThingProto): RelationRolePlayer {
                return new RelationRolePlayer({role_type: roleType, player: player});
            }

            export function addRolePlayerReq(iid: string, roleType: RoleType, player: ThingProto) {
                return thingReq(new ThingReq({
                    iid: Bytes.hexStringToBytes(iid),
                    relation_add_role_player_req: new RelationAddRolePlayerReq({role_player: protoRolePlayer(roleType, player)})
                }));
            }

            export function removeRolePlayerReq(iid: string, roleType: RoleType, player: ThingProto) {
                return thingReq(new ThingReq({
                    iid: Bytes.hexStringToBytes(iid),
                    relation_remove_role_player_req: new RelationRemoveRolePlayerReq({role_player: protoRolePlayer(roleType, player)})
                }));
            }

            export function getPlayersByRoleTypeReq(iid: string, roleTypes: RoleType[]) {
                return thingReq(new ThingReq({
                    iid: Bytes.hexStringToBytes(iid),
                    relation_get_players_by_role_type_req: new RelationGetPlayersByRoleTypeReq({role_types: roleTypes})
                }));
            }

            export function getRolePlayersReq(iid: string) {
                return thingReq(new ThingReq({
                    iid: Bytes.hexStringToBytes(iid),
                    relation_get_role_players_req: new RelationGetRolePlayersReq(),
                }));
            }

            export function getRelatingReq(iid: string) {
                return thingReq(new ThingReq({
                    iid: Bytes.hexStringToBytes(iid),
                    relation_get_relating_req: new RelationGetRelatingReq(),
                }));
            }
        }

        export namespace Attribute {
            export function protoAttribute(iid: string): AttributeProto {
                return new AttributeProto({iid: Bytes.hexStringToBytes(iid)});
            }

            export function getOwnersReq(iid: string, ownerType?: ThingTypeProto) {
                return thingReq(new ThingReq({
                    iid: Bytes.hexStringToBytes(iid),
                    attribute_get_owners_req: new AttributeGetOwnersReq({thing_type: ownerType}),
                }));
            }
        }
    }

    export namespace Value {
        export function protoValue(valueType: ValueType, value: boolean | string | number | Date): ValueProto {
            switch (valueType) {
                case ValueType.BOOLEAN: return new ValueProto({boolean: value as boolean});
                case ValueType.LONG: return new ValueProto({long: value as number});
                case ValueType.DOUBLE: return new ValueProto({double: value as number});
                case ValueType.STRING: return new ValueProto({string: value as string});
                case ValueType.DATETIME: return new ValueProto({date_time: (value as Date).getTime()});
                default: throw new TypeDBDriverError(BAD_VALUE_TYPE.message());
            }
        }
    }
}
