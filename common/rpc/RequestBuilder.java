/*
 * Copyright (C) 2021 Vaticle
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

package com.vaticle.typedb.client.common.rpc;

import com.google.protobuf.ByteString;
import com.vaticle.factory.tracing.client.FactoryTracingThreadStatic;
import com.vaticle.typedb.client.common.Label;
import com.vaticle.typedb.protocol.ClusterDatabaseProto;
import com.vaticle.typedb.protocol.ClusterServerProto;
import com.vaticle.typedb.protocol.ClusterUserProto;
import com.vaticle.typedb.protocol.ConceptProto;
import com.vaticle.typedb.protocol.CoreDatabaseProto;
import com.vaticle.typedb.protocol.LogicProto;
import com.vaticle.typedb.protocol.OptionsProto;
import com.vaticle.typedb.protocol.QueryProto;
import com.vaticle.typedb.protocol.SessionProto;
import com.vaticle.typedb.protocol.TransactionProto;
import com.vaticle.typeql.lang.query.TypeQLDefine;
import com.vaticle.typeql.lang.query.TypeQLDelete;
import com.vaticle.typeql.lang.query.TypeQLInsert;
import com.vaticle.typeql.lang.query.TypeQLMatch;
import com.vaticle.typeql.lang.query.TypeQLUndefine;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.google.protobuf.ByteString.copyFrom;
import static com.vaticle.factory.tracing.client.FactoryTracingThreadStatic.currentThreadTrace;
import static com.vaticle.factory.tracing.client.FactoryTracingThreadStatic.isTracingEnabled;
import static com.vaticle.typedb.client.common.collection.Bytes.uuidToBytes;
import static com.vaticle.typedb.client.common.rpc.RequestBuilder.Thing.byteString;
import static com.vaticle.typedb.common.collection.Bytes.hexStringToBytes;
import static java.util.Collections.emptyMap;

public class RequestBuilder {

    public static Map<String, String> tracingData() {
        if (isTracingEnabled()) {
            FactoryTracingThreadStatic.ThreadTrace threadTrace = currentThreadTrace();
            if (threadTrace == null) return emptyMap();
            if (threadTrace.getId() == null || threadTrace.getRootId() == null) return emptyMap();

            Map<String, String> metadata = new HashMap<>(2);
            metadata.put("traceParentId", threadTrace.getId().toString());
            metadata.put("traceRootId", threadTrace.getRootId().toString());
            return metadata;
        } else {
            return emptyMap();
        }
    }

    public static ByteString UUIDAsByteString(UUID uuid) {
        return copyFrom(uuidToBytes(uuid));
    }

    public static class Core {

        public static class DatabaseManager {

            public static CoreDatabaseProto.CoreDatabaseManager.Create.Req createReq(String name) {
                return CoreDatabaseProto.CoreDatabaseManager.Create.Req.newBuilder().setName(name).build();
            }

            public static CoreDatabaseProto.CoreDatabaseManager.Contains.Req containsReq(String name) {
                return CoreDatabaseProto.CoreDatabaseManager.Contains.Req.newBuilder().setName(name).build();
            }

            public static CoreDatabaseProto.CoreDatabaseManager.All.Req allReq() {
                return CoreDatabaseProto.CoreDatabaseManager.All.Req.getDefaultInstance();
            }
        }

        public static class Database {

            public static CoreDatabaseProto.CoreDatabase.Schema.Req schemaReq(String name) {
                return CoreDatabaseProto.CoreDatabase.Schema.Req.newBuilder().setName(name).build();
            }

            public static CoreDatabaseProto.CoreDatabase.Delete.Req deleteReq(String name) {
                return CoreDatabaseProto.CoreDatabase.Delete.Req.newBuilder().setName(name).build();
            }
        }
    }

    public static class Cluster {

        public static class ServerManager {

            public static ClusterServerProto.ServerManager.All.Req allReq() {
                return ClusterServerProto.ServerManager.All.Req.newBuilder().build();
            }
        }

        public static class UserManager {
            public static ClusterUserProto.ClusterUserManager.Contains.Req containsReq(String name) {
                return ClusterUserProto.ClusterUserManager.Contains.Req.newBuilder().setName(name).build();
            }

            public static ClusterUserProto.ClusterUserManager.Create.Req createReq(String name, String password) {
                return ClusterUserProto.ClusterUserManager.Create.Req.newBuilder().setName(name).setPassword(password).build();
            }

            public static ClusterUserProto.ClusterUserManager.All.Req allReq() {
                return ClusterUserProto.ClusterUserManager.All.Req.newBuilder().build();
            }
        }

        public static class User {
            public static ClusterUserProto.ClusterUser.Delete.Req deleteReq(String name) {
                return ClusterUserProto.ClusterUser.Delete.Req.newBuilder().setName(name).build();
            }
        }

        public static class DatabaseManager {

            public static ClusterDatabaseProto.ClusterDatabaseManager.Get.Req getReq(String name) {
                return ClusterDatabaseProto.ClusterDatabaseManager.Get.Req.newBuilder().setName(name).build();
            }

            public static ClusterDatabaseProto.ClusterDatabaseManager.All.Req allReq() {
                return ClusterDatabaseProto.ClusterDatabaseManager.All.Req.getDefaultInstance();
            }
        }

        public static class Database {

        }
    }

    public static class Session {

        public static SessionProto.Session.Open.Req openReq(
                String database, SessionProto.Session.Type type, OptionsProto.Options options) {
            return SessionProto.Session.Open.Req.newBuilder().setDatabase(database)
                    .setType(type).setOptions(options).build();
        }

        public static SessionProto.Session.Pulse.Req pulseReq(ByteString sessionID) {
            return SessionProto.Session.Pulse.Req.newBuilder().setSessionId(sessionID).build();
        }

        public static SessionProto.Session.Close.Req closeReq(ByteString sessionID) {
            return SessionProto.Session.Close.Req.newBuilder().setSessionId(sessionID).build();
        }
    }

    public static class Transaction {

        public static TransactionProto.Transaction.Client clientMsg(List<TransactionProto.Transaction.Req> reqs) {
            return TransactionProto.Transaction.Client.newBuilder().addAllReqs(reqs).build();
        }

        public static TransactionProto.Transaction.Req streamReq(UUID reqID) {
            return TransactionProto.Transaction.Req.newBuilder().setReqId(UUIDAsByteString(reqID)).setStreamReq(
                    TransactionProto.Transaction.Stream.Req.getDefaultInstance()
            ).build();
        }

        public static TransactionProto.Transaction.Req.Builder openReq(
                ByteString sessionID, TransactionProto.Transaction.Type type, OptionsProto.Options options, int networkLatencyMillis) {
            return TransactionProto.Transaction.Req.newBuilder().setOpenReq(
                    TransactionProto.Transaction.Open.Req.newBuilder().setSessionId(sessionID)
                            .setType(type).setOptions(options).setNetworkLatencyMillis(networkLatencyMillis)
            );
        }

        public static TransactionProto.Transaction.Req.Builder commitReq() {
            return TransactionProto.Transaction.Req.newBuilder().putAllMetadata(tracingData())
                    .setCommitReq(TransactionProto.Transaction.Commit.Req.getDefaultInstance());
        }

        public static TransactionProto.Transaction.Req.Builder rollbackReq() {
            return TransactionProto.Transaction.Req.newBuilder().putAllMetadata(tracingData())
                    .setRollbackReq(TransactionProto.Transaction.Rollback.Req.getDefaultInstance());
        }
    }

    public static class QueryManager {

        private static TransactionProto.Transaction.Req.Builder queryManagerReq(
                QueryProto.QueryManager.Req.Builder queryReq, OptionsProto.Options options) {
            return TransactionProto.Transaction.Req.newBuilder().setQueryManagerReq(queryReq.setOptions(options));
        }

        public static TransactionProto.Transaction.Req.Builder defineReq(TypeQLDefine query, OptionsProto.Options options) {
            return queryManagerReq(QueryProto.QueryManager.Req.newBuilder().setDefineReq(
                    QueryProto.QueryManager.Define.Req.newBuilder().setQuery(query.toString())
            ), options);
        }

        public static TransactionProto.Transaction.Req.Builder undefineReq(TypeQLUndefine query, OptionsProto.Options options) {
            return queryManagerReq(QueryProto.QueryManager.Req.newBuilder().setUndefineReq(
                    QueryProto.QueryManager.Undefine.Req.newBuilder().setQuery(query.toString())
            ), options);
        }

        public static TransactionProto.Transaction.Req.Builder matchReq(TypeQLMatch query, OptionsProto.Options options) {
            return queryManagerReq(QueryProto.QueryManager.Req.newBuilder().setMatchReq(
                    QueryProto.QueryManager.Match.Req.newBuilder().setQuery(query.toString())
            ), options);
        }

        public static TransactionProto.Transaction.Req.Builder matchAggregateReq(
                TypeQLMatch.Aggregate query, OptionsProto.Options options) {
            return queryManagerReq(QueryProto.QueryManager.Req.newBuilder().setMatchAggregateReq(
                    QueryProto.QueryManager.MatchAggregate.Req.newBuilder().setQuery(query.toString())
            ), options);
        }

        public static TransactionProto.Transaction.Req.Builder matchGroupReq(
                TypeQLMatch.Group query, OptionsProto.Options options) {
            return queryManagerReq(QueryProto.QueryManager.Req.newBuilder().setMatchGroupReq(
                    QueryProto.QueryManager.MatchGroup.Req.newBuilder().setQuery(query.toString())
            ), options);
        }

        public static TransactionProto.Transaction.Req.Builder matchGroupAggregateReq(
                TypeQLMatch.Group.Aggregate query, OptionsProto.Options options) {
            return queryManagerReq(QueryProto.QueryManager.Req.newBuilder().setMatchGroupAggregateReq(
                    QueryProto.QueryManager.MatchGroupAggregate.Req.newBuilder().setQuery(query.toString())
            ), options);
        }

        public static TransactionProto.Transaction.Req.Builder insertReq(TypeQLInsert query, OptionsProto.Options options) {
            return queryManagerReq(QueryProto.QueryManager.Req.newBuilder().setInsertReq(
                    QueryProto.QueryManager.Insert.Req.newBuilder().setQuery(query.toString())
            ), options);
        }

        public static TransactionProto.Transaction.Req.Builder deleteReq(TypeQLDelete query, OptionsProto.Options options) {
            return queryManagerReq(QueryProto.QueryManager.Req.newBuilder().setDeleteReq(
                    QueryProto.QueryManager.Delete.Req.newBuilder().setQuery(query.toString())
            ), options);
        }

        public static TransactionProto.Transaction.Req.Builder updateReq(String query, OptionsProto.Options options) {
            return queryManagerReq(QueryProto.QueryManager.Req.newBuilder().setUpdateReq(
                    QueryProto.QueryManager.Update.Req.newBuilder().setQuery(query)
            ), options);
        }

        public static TransactionProto.Transaction.Req.Builder explainReq(long id, OptionsProto.Options options) {
            return queryManagerReq(QueryProto.QueryManager.Req.newBuilder().setExplainReq(
                    QueryProto.QueryManager.Explain.Req.newBuilder().setExplainableId(id)
            ), options);
        }
    }

    public static class ConceptManager {

        public static TransactionProto.Transaction.Req.Builder conceptManagerReq(
                ConceptProto.ConceptManager.Req.Builder req) {
            return TransactionProto.Transaction.Req.newBuilder().putAllMetadata(tracingData()).setConceptManagerReq(req);
        }

        public static TransactionProto.Transaction.Req.Builder putEntityTypeReq(String label) {
            return conceptManagerReq(ConceptProto.ConceptManager.Req.newBuilder().setPutEntityTypeReq(
                    ConceptProto.ConceptManager.PutEntityType.Req.newBuilder().setLabel(label))
            );
        }

        public static TransactionProto.Transaction.Req.Builder putRelationTypeReq(String label) {
            return conceptManagerReq(ConceptProto.ConceptManager.Req.newBuilder().setPutRelationTypeReq(
                    ConceptProto.ConceptManager.PutRelationType.Req.newBuilder().setLabel(label))
            );
        }

        public static TransactionProto.Transaction.Req.Builder putAttributeTypeReq(
                String label, ConceptProto.AttributeType.ValueType valueType) {
            return conceptManagerReq(ConceptProto.ConceptManager.Req.newBuilder().setPutAttributeTypeReq(
                    ConceptProto.ConceptManager.PutAttributeType.Req.newBuilder().setLabel(label).setValueType(valueType)
            ));
        }

        public static TransactionProto.Transaction.Req.Builder getThingTypeReq(String label) {
            return conceptManagerReq(ConceptProto.ConceptManager.Req.newBuilder().setGetThingTypeReq(
                    ConceptProto.ConceptManager.GetThingType.Req.newBuilder().setLabel(label)
            ));
        }

        public static TransactionProto.Transaction.Req.Builder getThingReq(String iid) {
            return conceptManagerReq(ConceptProto.ConceptManager.Req.newBuilder().setGetThingReq(
                    ConceptProto.ConceptManager.GetThing.Req.newBuilder().setIid(byteString(iid))
            ));
        }
    }

    public static class LogicManager {

        private static TransactionProto.Transaction.Req.Builder logicManagerReq(
                LogicProto.LogicManager.Req.Builder logicReq) {
            return TransactionProto.Transaction.Req.newBuilder()
                    .putAllMetadata(tracingData()).setLogicManagerReq(logicReq);
        }

        public static TransactionProto.Transaction.Req.Builder putRuleReq(String label, String whenStr, String thenStr) {
            return logicManagerReq(LogicProto.LogicManager.Req.newBuilder().setPutRuleReq(
                    LogicProto.LogicManager.PutRule.Req.newBuilder()
                            .setLabel(label).setWhen(whenStr).setThen(thenStr)
            ));
        }

        public static TransactionProto.Transaction.Req.Builder getRuleReq(String label) {
            return logicManagerReq(LogicProto.LogicManager.Req.newBuilder().setGetRuleReq(
                    LogicProto.LogicManager.GetRule.Req.newBuilder().setLabel(label)
            ));
        }

        public static TransactionProto.Transaction.Req.Builder getRulesReq() {
            return logicManagerReq(LogicProto.LogicManager.Req.newBuilder().setGetRulesReq(
                    LogicProto.LogicManager.GetRules.Req.getDefaultInstance()
            ));
        }
    }

    public static class Type {

        private static TransactionProto.Transaction.Req.Builder typeReq(ConceptProto.Type.Req.Builder req) {
            return TransactionProto.Transaction.Req.newBuilder().setTypeReq(req);
        }

        private static ConceptProto.Type.Req.Builder newReqBuilder(Label label) {
            ConceptProto.Type.Req.Builder builder = ConceptProto.Type.Req.newBuilder().setLabel(label.name());
            if (label.scope().isPresent()) builder.setScope(label.scope().get());
            return builder;
        }

        public static TransactionProto.Transaction.Req.Builder isAbstractReq(Label label) {
            return typeReq(newReqBuilder(label).setTypeIsAbstractReq(
                    ConceptProto.Type.IsAbstract.Req.getDefaultInstance()
            ));
        }

        public static TransactionProto.Transaction.Req.Builder setLabelReq(Label label, String newLabel) {
            return typeReq(newReqBuilder(label).setTypeSetLabelReq(
                    ConceptProto.Type.SetLabel.Req.newBuilder().setLabel(newLabel)
            ));
        }

        public static TransactionProto.Transaction.Req.Builder getSupertypesReq(Label label) {
            return typeReq(newReqBuilder(label).setTypeGetSupertypesReq(
                    ConceptProto.Type.GetSupertypes.Req.getDefaultInstance()
            ));
        }

        public static TransactionProto.Transaction.Req.Builder getSubtypesReq(Label label) {
            return typeReq(newReqBuilder(label).setTypeGetSubtypesReq(
                    ConceptProto.Type.GetSubtypes.Req.getDefaultInstance()
            ));
        }

        public static TransactionProto.Transaction.Req.Builder getSupertypeReq(Label label) {
            return typeReq(newReqBuilder(label).setTypeGetSupertypeReq(
                    ConceptProto.Type.GetSupertype.Req.getDefaultInstance()
            ));
        }

        public static TransactionProto.Transaction.Req.Builder deleteReq(Label label) {
            return typeReq(newReqBuilder(label).setTypeDeleteReq(
                    ConceptProto.Type.Delete.Req.getDefaultInstance()
            ));
        }

        public static class RoleType {

            public static ConceptProto.Type protoRoleType(Label label, ConceptProto.Type.Encoding encoding) {
                assert label.scope().isPresent();
                return ConceptProto.Type.newBuilder().setScope(label.scope().get())
                        .setLabel(label.name()).setEncoding(encoding).build();
            }

            public static TransactionProto.Transaction.Req.Builder getRelationTypesReq(Label label) {
                return typeReq(newReqBuilder(label).setRoleTypeGetRelationTypesReq(
                        ConceptProto.RoleType.GetRelationTypes.Req.getDefaultInstance()
                ));
            }

            public static TransactionProto.Transaction.Req.Builder getPlayersReq(Label label) {
                return typeReq(newReqBuilder(label).setRoleTypeGetPlayersReq(
                        ConceptProto.RoleType.GetPlayers.Req.getDefaultInstance()
                ));
            }
        }

        public static class ThingType {

            public static ConceptProto.Type protoThingType(Label label, ConceptProto.Type.Encoding encoding) {
                return ConceptProto.Type.newBuilder().setLabel(label.name()).setEncoding(encoding).build();
            }

            public static TransactionProto.Transaction.Req.Builder setAbstractReq(Label label) {
                return typeReq(newReqBuilder(label).setThingTypeSetAbstractReq(
                        ConceptProto.ThingType.SetAbstract.Req.getDefaultInstance()
                ));
            }

            public static TransactionProto.Transaction.Req.Builder unsetAbstractReq(Label label) {
                return typeReq(newReqBuilder(label).setThingTypeUnsetAbstractReq(
                        ConceptProto.ThingType.UnsetAbstract.Req.getDefaultInstance()
                ));
            }

            public static TransactionProto.Transaction.Req.Builder setSupertypeReq(Label label, ConceptProto.Type supertype) {
                return typeReq(newReqBuilder(label).setTypeSetSupertypeReq(
                        ConceptProto.Type.SetSupertype.Req.newBuilder().setType(supertype)
                ));
            }

            public static TransactionProto.Transaction.Req.Builder getPlaysReq(Label label) {
                return typeReq(newReqBuilder(label).setThingTypeGetPlaysReq(
                        ConceptProto.ThingType.GetPlays.Req.getDefaultInstance()
                ));
            }

            public static TransactionProto.Transaction.Req.Builder setPlaysReq(Label label, ConceptProto.Type roleType) {
                return typeReq(newReqBuilder(label).setThingTypeSetPlaysReq(
                        ConceptProto.ThingType.SetPlays.Req.newBuilder().setRole(roleType)
                ));
            }

            public static TransactionProto.Transaction.Req.Builder setPlaysReq(
                    Label label, ConceptProto.Type roleType, ConceptProto.Type overriddenRoleType) {
                return typeReq(newReqBuilder(label).setThingTypeSetPlaysReq(
                        ConceptProto.ThingType.SetPlays.Req.newBuilder().setRole(roleType)
                                .setOverriddenRole(overriddenRoleType)
                ));
            }

            public static TransactionProto.Transaction.Req.Builder unsetPlaysReq(Label label, ConceptProto.Type roleType) {
                return typeReq(newReqBuilder(label).setThingTypeUnsetPlaysReq(
                        ConceptProto.ThingType.UnsetPlays.Req.newBuilder().setRole(roleType)
                ));
            }

            public static TransactionProto.Transaction.Req.Builder getOwnsReq(Label label, boolean keysOnly) {
                return typeReq(newReqBuilder(label).setThingTypeGetOwnsReq(
                        ConceptProto.ThingType.GetOwns.Req.newBuilder().setKeysOnly(keysOnly)
                ));
            }

            public static TransactionProto.Transaction.Req.Builder getOwnsReq(
                    Label label, ConceptProto.AttributeType.ValueType valueType, boolean keysOnly) {
                return typeReq(newReqBuilder(label).setThingTypeGetOwnsReq(
                        ConceptProto.ThingType.GetOwns.Req.newBuilder().setKeysOnly(keysOnly)
                                .setValueType(valueType)
                ));
            }

            public static TransactionProto.Transaction.Req.Builder setOwnsReq(
                    Label label, ConceptProto.Type attributeType, boolean isKey) {
                return typeReq(newReqBuilder(label).setThingTypeSetOwnsReq(
                        ConceptProto.ThingType.SetOwns.Req.newBuilder()
                                .setAttributeType(attributeType)
                                .setIsKey(isKey)
                ));
            }

            public static TransactionProto.Transaction.Req.Builder setOwnsReq(
                    Label label, ConceptProto.Type attributeType, ConceptProto.Type overriddenType, boolean isKey) {
                return typeReq(newReqBuilder(label).setThingTypeSetOwnsReq(
                        ConceptProto.ThingType.SetOwns.Req.newBuilder()
                                .setAttributeType(attributeType)
                                .setOverriddenType(overriddenType)
                                .setIsKey(isKey)
                ));
            }

            public static TransactionProto.Transaction.Req.Builder unsetOwnsReq(
                    Label label, ConceptProto.Type attributeType) {
                return typeReq(newReqBuilder(label).setThingTypeUnsetOwnsReq(
                        ConceptProto.ThingType.UnsetOwns.Req.newBuilder().setAttributeType(attributeType)
                ));
            }

            public static TransactionProto.Transaction.Req.Builder getInstancesReq(Label label) {
                return typeReq(newReqBuilder(label).setThingTypeGetInstancesReq(
                        ConceptProto.ThingType.GetInstances.Req.getDefaultInstance()
                ));
            }
        }

        public static class EntityType {

            public static TransactionProto.Transaction.Req.Builder createReq(Label label) {
                return typeReq(newReqBuilder(label).setEntityTypeCreateReq(
                        ConceptProto.EntityType.Create.Req.getDefaultInstance()
                ));
            }
        }

        public static class RelationType {

            public static TransactionProto.Transaction.Req.Builder createReq(Label label) {
                return typeReq(newReqBuilder(label).setRelationTypeCreateReq(
                        ConceptProto.RelationType.Create.Req.getDefaultInstance()
                ));
            }

            public static TransactionProto.Transaction.Req.Builder getRelatesReq(Label label) {
                return typeReq(newReqBuilder(label).setRelationTypeGetRelatesReq(
                        ConceptProto.RelationType.GetRelates.Req.getDefaultInstance()
                ));
            }

            public static TransactionProto.Transaction.Req.Builder getRelatesReq(Label label, String roleLabel) {
                return typeReq(newReqBuilder(label).setRelationTypeGetRelatesForRoleLabelReq(
                        ConceptProto.RelationType.GetRelatesForRoleLabel.Req.newBuilder().setLabel(roleLabel)
                ));
            }

            public static TransactionProto.Transaction.Req.Builder setRelatesReq(Label label, String roleLabel) {
                return typeReq(newReqBuilder(label).setRelationTypeSetRelatesReq(
                        ConceptProto.RelationType.SetRelates.Req.newBuilder().setLabel(roleLabel)
                ));
            }

            public static TransactionProto.Transaction.Req.Builder setRelatesReq(
                    Label label, String roleLabel, String overriddenLabel) {
                return typeReq(newReqBuilder(label).setRelationTypeSetRelatesReq(
                        ConceptProto.RelationType.SetRelates.Req.newBuilder().setLabel(roleLabel)
                                .setOverriddenLabel(overriddenLabel)
                ));
            }

            public static TransactionProto.Transaction.Req.Builder unsetRelatesReq(Label label, String roleLabel) {
                return typeReq(newReqBuilder(label).setRelationTypeUnsetRelatesReq(
                        ConceptProto.RelationType.UnsetRelates.Req.newBuilder().setLabel(roleLabel)
                ));
            }
        }

        public static class AttributeType {

            public static TransactionProto.Transaction.Req.Builder getOwnersReq(Label label, boolean onlyKey) {
                return typeReq(newReqBuilder(label).setAttributeTypeGetOwnersReq(
                        ConceptProto.AttributeType.GetOwners.Req.newBuilder().setOnlyKey(onlyKey)
                ));
            }

            public static TransactionProto.Transaction.Req.Builder putReq(Label label, ConceptProto.Attribute.Value value) {
                return typeReq(newReqBuilder(label).setAttributeTypePutReq(
                        ConceptProto.AttributeType.Put.Req.newBuilder().setValue(value)
                ));
            }

            public static TransactionProto.Transaction.Req.Builder getReq(Label label, ConceptProto.Attribute.Value value) {
                return typeReq(newReqBuilder(label).setAttributeTypeGetReq(
                        ConceptProto.AttributeType.Get.Req.newBuilder().setValue(value)
                ));
            }

            public static TransactionProto.Transaction.Req.Builder getRegexReq(Label label) {
                return typeReq(newReqBuilder(label).setAttributeTypeGetRegexReq(
                        ConceptProto.AttributeType.GetRegex.Req.getDefaultInstance()
                ));
            }

            public static TransactionProto.Transaction.Req.Builder setRegexReq(Label label, String regex) {
                return typeReq(newReqBuilder(label).setAttributeTypeSetRegexReq(
                        ConceptProto.AttributeType.SetRegex.Req.newBuilder().setRegex(regex)
                ));
            }
        }
    }

    public static class Thing {

        static ByteString byteString(String iid) {
            return ByteString.copyFrom(hexStringToBytes(iid));
        }

        public static ConceptProto.Thing protoThing(String iid) {
            return ConceptProto.Thing.newBuilder().setIid(byteString(iid)).build();
        }

        private static TransactionProto.Transaction.Req.Builder thingReq(ConceptProto.Thing.Req.Builder req) {
            return TransactionProto.Transaction.Req.newBuilder().setThingReq(req);
        }

        public static TransactionProto.Transaction.Req.Builder getHasReq(
                String iid, List<ConceptProto.Type> attributeTypes) {
            return thingReq(ConceptProto.Thing.Req.newBuilder().setIid(byteString(iid)).setThingGetHasReq(
                    ConceptProto.Thing.GetHas.Req.newBuilder().addAllAttributeTypes(attributeTypes)
            ));
        }

        public static TransactionProto.Transaction.Req.Builder getHasReq(String iid, boolean onlyKey) {
            return thingReq(ConceptProto.Thing.Req.newBuilder().setIid(byteString(iid)).setThingGetHasReq(
                    ConceptProto.Thing.GetHas.Req.newBuilder().setKeysOnly(onlyKey)
            ));
        }

        public static TransactionProto.Transaction.Req.Builder setHasReq(String iid, ConceptProto.Thing attribute) {
            return thingReq(ConceptProto.Thing.Req.newBuilder().setIid(byteString(iid)).setThingSetHasReq(
                    ConceptProto.Thing.SetHas.Req.newBuilder().setAttribute(attribute)
            ));
        }

        public static TransactionProto.Transaction.Req.Builder unsetHasReq(String iid, ConceptProto.Thing attribute) {
            return thingReq(ConceptProto.Thing.Req.newBuilder().setIid(byteString(iid)).setThingUnsetHasReq(
                    ConceptProto.Thing.UnsetHas.Req.newBuilder().setAttribute(attribute)
            ));
        }

        public static TransactionProto.Transaction.Req.Builder getPlayingReq(String iid) {
            return thingReq(ConceptProto.Thing.Req.newBuilder().setIid(byteString(iid)).setThingGetPlayingReq(
                    ConceptProto.Thing.GetPlaying.Req.getDefaultInstance()
            ));
        }

        public static TransactionProto.Transaction.Req.Builder getRelationsReq(
                String iid, List<ConceptProto.Type> roleTypes) {
            return thingReq(ConceptProto.Thing.Req.newBuilder().setIid(byteString(iid)).setThingGetRelationsReq(
                    ConceptProto.Thing.GetRelations.Req.newBuilder().addAllRoleTypes(roleTypes)
            ));
        }

        public static TransactionProto.Transaction.Req.Builder deleteReq(String iid) {
            return thingReq(ConceptProto.Thing.Req.newBuilder().setIid(byteString(iid)).setThingDeleteReq(
                    ConceptProto.Thing.Delete.Req.getDefaultInstance()
            ));
        }

        public static class Relation {

            public static TransactionProto.Transaction.Req.Builder addPlayerReq(
                    String iid, ConceptProto.Type roleType, ConceptProto.Thing player) {
                return thingReq(ConceptProto.Thing.Req.newBuilder().setIid(byteString(iid)).setRelationAddPlayerReq(
                        ConceptProto.Relation.AddPlayer.Req.newBuilder().setRoleType(roleType).setPlayer(player)
                ));
            }

            public static TransactionProto.Transaction.Req.Builder removePlayerReq(
                    String iid, ConceptProto.Type roleType, ConceptProto.Thing player) {
                return thingReq(ConceptProto.Thing.Req.newBuilder().setIid(byteString(iid)).setRelationRemovePlayerReq(
                        ConceptProto.Relation.RemovePlayer.Req.newBuilder().setRoleType(roleType).setPlayer(player)
                ));
            }

            public static TransactionProto.Transaction.Req.Builder getPlayersReq(
                    String iid, List<ConceptProto.Type> roleTypes) {
                return thingReq(ConceptProto.Thing.Req.newBuilder().setIid(byteString(iid)).setRelationGetPlayersReq(
                        ConceptProto.Relation.GetPlayers.Req.newBuilder().addAllRoleTypes(roleTypes)
                ));
            }

            public static TransactionProto.Transaction.Req.Builder getPlayersByRoleTypeReq(String iid) {
                return thingReq(ConceptProto.Thing.Req.newBuilder().setIid(byteString(iid)).setRelationGetPlayersByRoleTypeReq(
                        ConceptProto.Relation.GetPlayersByRoleType.Req.getDefaultInstance()
                ));
            }

            public static TransactionProto.Transaction.Req.Builder getRelatingReq(String iid) {
                return thingReq(ConceptProto.Thing.Req.newBuilder().setIid(byteString(iid)).setRelationGetRelatingReq(
                        ConceptProto.Relation.GetRelating.Req.getDefaultInstance()
                ));
            }
        }

        public static class Attribute {

            public static TransactionProto.Transaction.Req.Builder getOwnersReq(String iid) {
                return thingReq(ConceptProto.Thing.Req.newBuilder().setIid(byteString(iid)).setAttributeGetOwnersReq(
                        ConceptProto.Attribute.GetOwners.Req.getDefaultInstance()
                ));
            }

            public static TransactionProto.Transaction.Req.Builder getOwnersReq(String iid, ConceptProto.Type ownerType) {
                return thingReq(ConceptProto.Thing.Req.newBuilder().setIid(byteString(iid)).setAttributeGetOwnersReq(
                        ConceptProto.Attribute.GetOwners.Req.newBuilder().setThingType(ownerType)
                ));
            }

            public static ConceptProto.Attribute.Value protoBooleanAttributeValue(boolean value) {
                return ConceptProto.Attribute.Value.newBuilder().setBoolean(value).build();
            }

            public static ConceptProto.Attribute.Value protoLongAttributeValue(long value) {
                return ConceptProto.Attribute.Value.newBuilder().setLong(value).build();
            }

            public static ConceptProto.Attribute.Value protoDoubleAttributeValue(double value) {
                return ConceptProto.Attribute.Value.newBuilder().setDouble(value).build();
            }

            public static ConceptProto.Attribute.Value protoStringAttributeValue(String value) {
                return ConceptProto.Attribute.Value.newBuilder().setString(value).build();
            }

            public static ConceptProto.Attribute.Value protoDateTimeAttributeValue(LocalDateTime value) {
                long epochMillis = value.atZone(ZoneId.of("Z")).toInstant().toEpochMilli();
                return ConceptProto.Attribute.Value.newBuilder().setDateTime(epochMillis).build();
            }
        }
    }

    public static class Rule {

        private static TransactionProto.Transaction.Req.Builder ruleReq(LogicProto.Rule.Req.Builder req) {
            return TransactionProto.Transaction.Req.newBuilder().setRuleReq(req);
        }

        public static TransactionProto.Transaction.Req.Builder setLabelReq(String currentLabel, String newLabel) {
            return ruleReq(LogicProto.Rule.Req.newBuilder().setLabel(currentLabel).setRuleSetLabelReq(
                    LogicProto.Rule.SetLabel.Req.newBuilder().setLabel(newLabel)
            ));
        }

        public static TransactionProto.Transaction.Req.Builder deleteReq(String label) {
            return ruleReq(LogicProto.Rule.Req.newBuilder().setLabel(label).setRuleDeleteReq(
                    LogicProto.Rule.Delete.Req.getDefaultInstance()
            ));
        }
    }
}
