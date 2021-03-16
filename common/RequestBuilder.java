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

package grakn.client.common;

import com.google.protobuf.ByteString;
import grabl.tracing.client.GrablTracingThreadStatic;
import grakn.protocol.ConceptProto;
import grakn.protocol.LogicProto;
import grakn.protocol.OptionsProto;
import grakn.protocol.QueryProto;
import grakn.protocol.SessionProto;
import grakn.protocol.TransactionProto;
import graql.lang.query.GraqlDefine;
import graql.lang.query.GraqlDelete;
import graql.lang.query.GraqlInsert;
import graql.lang.query.GraqlMatch;
import graql.lang.query.GraqlUndefine;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static grabl.tracing.client.GrablTracingThreadStatic.currentThreadTrace;
import static grabl.tracing.client.GrablTracingThreadStatic.isTracingEnabled;
import static grakn.client.common.RequestBuilder.Thing.byteString;
import static grakn.common.collection.Bytes.hexStringToBytes;
import static java.util.Collections.emptyMap;

public class RequestBuilder {

    public static Map<String, String> tracingData() {
        if (isTracingEnabled()) {
            GrablTracingThreadStatic.ThreadTrace threadTrace = currentThreadTrace();
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

    public static class Database {

        public static grakn.protocol.DatabaseProto.Database.All.Req allReq() {
            return grakn.protocol.DatabaseProto.Database.All.Req.getDefaultInstance();
        }

        public static grakn.protocol.DatabaseProto.Database.Contains.Req containsReq(String name) {
            return grakn.protocol.DatabaseProto.Database.Contains.Req.newBuilder().setName(name).build();
        }

        public static grakn.protocol.DatabaseProto.Database.Create.Req createReq(String name) {
            return grakn.protocol.DatabaseProto.Database.Create.Req.newBuilder().setName(name).build();
        }

        public static grakn.protocol.DatabaseProto.Database.Delete.Req deleteReq(String name) {
            return grakn.protocol.DatabaseProto.Database.Delete.Req.newBuilder().setName(name).build();
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
            return TransactionProto.Transaction.Req.newBuilder().setReqId(reqID.toString()).setStreamReq(
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

        public static TransactionProto.Transaction.Req.Builder defineReq(GraqlDefine query, OptionsProto.Options options) {
            return queryManagerReq(QueryProto.QueryManager.Req.newBuilder().setDefineReq(
                    QueryProto.QueryManager.Define.Req.newBuilder().setQuery(query.toString())
            ), options);
        }

        public static TransactionProto.Transaction.Req.Builder undefineReq(GraqlUndefine query, OptionsProto.Options options) {
            return queryManagerReq(QueryProto.QueryManager.Req.newBuilder().setUndefineReq(
                    QueryProto.QueryManager.Undefine.Req.newBuilder().setQuery(query.toString())
            ), options);
        }

        public static TransactionProto.Transaction.Req.Builder matchReq(GraqlMatch query, OptionsProto.Options options) {
            return queryManagerReq(QueryProto.QueryManager.Req.newBuilder().setMatchReq(
                    QueryProto.QueryManager.Match.Req.newBuilder().setQuery(query.toString())
            ), options);
        }

        public static TransactionProto.Transaction.Req.Builder matchAggregateReq(
                GraqlMatch.Aggregate query, OptionsProto.Options options) {
            return queryManagerReq(QueryProto.QueryManager.Req.newBuilder().setMatchAggregateReq(
                    QueryProto.QueryManager.MatchAggregate.Req.newBuilder().setQuery(query.toString())
            ), options);
        }

        public static TransactionProto.Transaction.Req.Builder matchGroupReq(
                GraqlMatch.Group query, OptionsProto.Options options) {
            return queryManagerReq(QueryProto.QueryManager.Req.newBuilder().setMatchGroupReq(
                    QueryProto.QueryManager.MatchGroup.Req.newBuilder().setQuery(query.toString())
            ), options);
        }

        public static TransactionProto.Transaction.Req.Builder matchGroupAggregateReq(
                GraqlMatch.Group.Aggregate query, OptionsProto.Options options) {
            return queryManagerReq(QueryProto.QueryManager.Req.newBuilder().setMatchGroupAggregateReq(
                    QueryProto.QueryManager.MatchGroupAggregate.Req.newBuilder().setQuery(query.toString())
            ), options);
        }

        public static TransactionProto.Transaction.Req.Builder insertReq(GraqlInsert query, OptionsProto.Options options) {
            return queryManagerReq(QueryProto.QueryManager.Req.newBuilder().setInsertReq(
                    QueryProto.QueryManager.Insert.Req.newBuilder().setQuery(query.toString())
            ), options);
        }

        public static TransactionProto.Transaction.Req.Builder deleteReq(GraqlDelete query, OptionsProto.Options options) {
            return queryManagerReq(QueryProto.QueryManager.Req.newBuilder().setDeleteReq(
                    QueryProto.QueryManager.Delete.Req.newBuilder().setQuery(query.toString())
            ), options);
        }

        public static TransactionProto.Transaction.Req.Builder updateReq(String query, OptionsProto.Options options) {
            return queryManagerReq(QueryProto.QueryManager.Req.newBuilder().setUpdateReq(
                    QueryProto.QueryManager.Update.Req.newBuilder().setQuery(query)
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

        public static TransactionProto.Transaction.Req.Builder isAbstractReq(String label) {
            return typeReq(ConceptProto.Type.Req.newBuilder().setLabel(label).setTypeIsAbstractReq(
                    ConceptProto.Type.IsAbstract.Req.getDefaultInstance()
            ));
        }

        public static TransactionProto.Transaction.Req.Builder setLabelReq(String label, String newLabel) {
            return typeReq(ConceptProto.Type.Req.newBuilder().setLabel(label).setTypeSetLabelReq(
                    ConceptProto.Type.SetLabel.Req.newBuilder().setLabel(newLabel)
            ));
        }

        public static TransactionProto.Transaction.Req.Builder getSupertypesReq(String label) {
            return typeReq(ConceptProto.Type.Req.newBuilder().setLabel(label).setTypeGetSupertypesReq(
                    ConceptProto.Type.GetSupertypes.Req.getDefaultInstance()
            ));
        }

        public static TransactionProto.Transaction.Req.Builder getSubtypesReq(String label) {
            return typeReq(ConceptProto.Type.Req.newBuilder().setLabel(label).setTypeGetSubtypesReq(
                    ConceptProto.Type.GetSubtypes.Req.getDefaultInstance()
            ));
        }

        public static TransactionProto.Transaction.Req.Builder getSupertypeReq(String label) {
            return typeReq(ConceptProto.Type.Req.newBuilder().setLabel(label).setTypeGetSupertypeReq(
                    ConceptProto.Type.GetSupertype.Req.getDefaultInstance()
            ));
        }

        public static TransactionProto.Transaction.Req.Builder deleteReq(String label) {
            return typeReq(ConceptProto.Type.Req.newBuilder().setLabel(label).setTypeDeleteReq(
                    ConceptProto.Type.Delete.Req.getDefaultInstance()
            ));
        }

        public static class RoleType {

            public static ConceptProto.Type protoRoleType(String scope, String label, ConceptProto.Type.Encoding encoding) {
                return ConceptProto.Type.newBuilder().setScope(scope).setLabel(label).setEncoding(encoding).build();
            }

            public static TransactionProto.Transaction.Req.Builder getRelationTypeReq(String scope, String label) {
                return typeReq(ConceptProto.Type.Req.newBuilder().setScope(scope).setLabel(label).setRoleTypeGetRelationTypeReq(
                        ConceptProto.RoleType.GetRelationType.Req.getDefaultInstance()
                ));
            }

            public static TransactionProto.Transaction.Req.Builder getRelationTypesReq(String scope, String label) {
                return typeReq(ConceptProto.Type.Req.newBuilder().setLabel(label).setScope(scope).setRoleTypeGetRelationTypesReq(
                        ConceptProto.RoleType.GetRelationTypes.Req.getDefaultInstance()
                ));
            }

            public static TransactionProto.Transaction.Req.Builder getPlayersReq(String scope, String label) {
                return typeReq(ConceptProto.Type.Req.newBuilder().setLabel(label).setScope(scope).setRoleTypeGetPlayersReq(
                        ConceptProto.RoleType.GetPlayers.Req.getDefaultInstance()
                ));
            }
        }

        public static class ThingType {

            public static ConceptProto.Type protoThingType(String label, ConceptProto.Type.Encoding encoding) {
                return ConceptProto.Type.newBuilder().setLabel(label).setEncoding(encoding).build();
            }

            public static TransactionProto.Transaction.Req.Builder setAbstractReq(String label) {
                return typeReq(ConceptProto.Type.Req.newBuilder().setLabel(label).setThingTypeSetAbstractReq(
                        ConceptProto.ThingType.SetAbstract.Req.getDefaultInstance()
                ));
            }

            public static TransactionProto.Transaction.Req.Builder unsetAbstractReq(String label) {
                return typeReq(ConceptProto.Type.Req.newBuilder().setLabel(label).setThingTypeUnsetAbstractReq(
                        ConceptProto.ThingType.UnsetAbstract.Req.getDefaultInstance()
                ));
            }

            public static TransactionProto.Transaction.Req.Builder setSupertypeReq(String label, ConceptProto.Type supertype) {
                return typeReq(ConceptProto.Type.Req.newBuilder().setLabel(label).setTypeSetSupertypeReq(
                        ConceptProto.Type.SetSupertype.Req.newBuilder().setType(supertype)
                ));
            }

            public static TransactionProto.Transaction.Req.Builder getPlaysReq(String label) {
                return typeReq(ConceptProto.Type.Req.newBuilder().setLabel(label).setThingTypeGetPlaysReq(
                        ConceptProto.ThingType.GetPlays.Req.getDefaultInstance()
                ));
            }

            public static TransactionProto.Transaction.Req.Builder setPlaysReq(String label, ConceptProto.Type roleType) {
                return typeReq(ConceptProto.Type.Req.newBuilder().setLabel(label).setThingTypeSetPlaysReq(
                        ConceptProto.ThingType.SetPlays.Req.newBuilder().setRole(roleType)
                ));
            }

            public static TransactionProto.Transaction.Req.Builder setPlaysReq(
                    String label, ConceptProto.Type roleType, ConceptProto.Type overriddenRoleType) {
                return typeReq(ConceptProto.Type.Req.newBuilder().setLabel(label).setThingTypeSetPlaysReq(
                        ConceptProto.ThingType.SetPlays.Req.newBuilder().setRole(roleType)
                                .setOverriddenRole(overriddenRoleType)
                ));
            }

            public static TransactionProto.Transaction.Req.Builder unsetPlaysReq(String label, ConceptProto.Type roleType) {
                return typeReq(ConceptProto.Type.Req.newBuilder().setLabel(label).setThingTypeUnsetPlaysReq(
                        ConceptProto.ThingType.UnsetPlays.Req.newBuilder().setRole(roleType)
                ));
            }

            public static TransactionProto.Transaction.Req.Builder getOwnsReq(String label, boolean keysOnly) {
                return typeReq(ConceptProto.Type.Req.newBuilder().setLabel(label).setThingTypeGetOwnsReq(
                        ConceptProto.ThingType.GetOwns.Req.newBuilder().setKeysOnly(keysOnly)
                ));
            }

            public static TransactionProto.Transaction.Req.Builder getOwnsReq(
                    String label, ConceptProto.AttributeType.ValueType valueType, boolean keysOnly) {
                return typeReq(ConceptProto.Type.Req.newBuilder().setLabel(label).setThingTypeGetOwnsReq(
                        ConceptProto.ThingType.GetOwns.Req.newBuilder().setKeysOnly(keysOnly)
                                .setValueType(valueType)
                ));
            }

            public static TransactionProto.Transaction.Req.Builder setOwnsReq(
                    String label, ConceptProto.Type attributeType, boolean isKey) {
                return typeReq(ConceptProto.Type.Req.newBuilder().setLabel(label).setThingTypeSetOwnsReq(
                        ConceptProto.ThingType.SetOwns.Req.newBuilder()
                                .setAttributeType(attributeType)
                                .setIsKey(isKey)
                ));
            }

            public static TransactionProto.Transaction.Req.Builder setOwnsReq(
                    String label, ConceptProto.Type attributeType, ConceptProto.Type overriddenType, boolean isKey) {
                return typeReq(ConceptProto.Type.Req.newBuilder().setLabel(label).setThingTypeSetOwnsReq(
                        ConceptProto.ThingType.SetOwns.Req.newBuilder()
                                .setAttributeType(attributeType)
                                .setOverriddenType(overriddenType)
                                .setIsKey(isKey)
                ));
            }

            public static TransactionProto.Transaction.Req.Builder unsetOwnsReq(
                    String label, ConceptProto.Type attributeType) {
                return typeReq(ConceptProto.Type.Req.newBuilder().setLabel(label).setThingTypeUnsetOwnsReq(
                        ConceptProto.ThingType.UnsetOwns.Req.newBuilder().setAttributeType(attributeType)
                ));
            }

            public static TransactionProto.Transaction.Req.Builder getInstancesReq(String label) {
                return typeReq(ConceptProto.Type.Req.newBuilder().setLabel(label).setThingTypeGetInstancesReq(
                        ConceptProto.ThingType.GetInstances.Req.getDefaultInstance()
                ));
            }
        }

        public static class EntityType {

            public static TransactionProto.Transaction.Req.Builder createReq(String label) {
                return typeReq(ConceptProto.Type.Req.newBuilder().setLabel(label).setEntityTypeCreateReq(
                        ConceptProto.EntityType.Create.Req.getDefaultInstance()
                ));
            }
        }

        public static class RelationType {

            public static TransactionProto.Transaction.Req.Builder createReq(String label) {
                return typeReq(ConceptProto.Type.Req.newBuilder().setLabel(label).setRelationTypeCreateReq(
                        ConceptProto.RelationType.Create.Req.getDefaultInstance()
                ));
            }

            public static TransactionProto.Transaction.Req.Builder getRelatesReq(String label) {
                return typeReq(ConceptProto.Type.Req.newBuilder().setLabel(label).setRelationTypeGetRelatesReq(
                        ConceptProto.RelationType.GetRelates.Req.getDefaultInstance()
                ));
            }

            public static TransactionProto.Transaction.Req.Builder getRelatesReq(String label, String roleLabel) {
                return typeReq(ConceptProto.Type.Req.newBuilder().setLabel(label).setRelationTypeGetRelatesForRoleLabelReq(
                        ConceptProto.RelationType.GetRelatesForRoleLabel.Req.newBuilder().setLabel(roleLabel)
                ));
            }

            public static TransactionProto.Transaction.Req.Builder setRelatesReq(String label, String roleLabel) {
                return typeReq(ConceptProto.Type.Req.newBuilder().setLabel(label).setRelationTypeSetRelatesReq(
                        ConceptProto.RelationType.SetRelates.Req.newBuilder().setLabel(roleLabel)
                ));
            }

            public static TransactionProto.Transaction.Req.Builder setRelatesReq(
                    String label, String roleLabel, String overriddenLabel) {
                return typeReq(ConceptProto.Type.Req.newBuilder().setLabel(label).setRelationTypeSetRelatesReq(
                        ConceptProto.RelationType.SetRelates.Req.newBuilder().setLabel(roleLabel)
                                .setOverriddenLabel(overriddenLabel)
                ));
            }

            public static TransactionProto.Transaction.Req.Builder unsetRelatesReq(String label, String roleLabel) {
                return typeReq(ConceptProto.Type.Req.newBuilder().setLabel(label).setRelationTypeUnsetRelatesReq(
                        ConceptProto.RelationType.UnsetRelates.Req.newBuilder().setLabel(roleLabel)
                ));
            }
        }

        public static class AttributeType {

            public static TransactionProto.Transaction.Req.Builder getOwnersReq(String label, boolean onlyKey) {
                return typeReq(ConceptProto.Type.Req.newBuilder().setLabel(label).setAttributeTypeGetOwnersReq(
                        ConceptProto.AttributeType.GetOwners.Req.newBuilder().setOnlyKey(onlyKey)
                ));
            }

            public static TransactionProto.Transaction.Req.Builder putReq(String label, ConceptProto.Attribute.Value value) {
                return typeReq(ConceptProto.Type.Req.newBuilder().setLabel(label).setAttributeTypePutReq(
                        ConceptProto.AttributeType.Put.Req.newBuilder().setValue(value)
                ));
            }

            public static TransactionProto.Transaction.Req.Builder getReq(String label, ConceptProto.Attribute.Value value) {
                return typeReq(ConceptProto.Type.Req.newBuilder().setLabel(label).setAttributeTypeGetReq(
                        ConceptProto.AttributeType.Get.Req.newBuilder().setValue(value)
                ));
            }

            public static TransactionProto.Transaction.Req.Builder getRegexReq(String label) {
                return typeReq(ConceptProto.Type.Req.newBuilder().setLabel(label).setAttributeTypeGetRegexReq(
                        ConceptProto.AttributeType.GetRegex.Req.getDefaultInstance()
                ));
            }

            public static TransactionProto.Transaction.Req.Builder setRegexReq(String label, String regex) {
                return typeReq(ConceptProto.Type.Req.newBuilder().setLabel(label).setAttributeTypeSetRegexReq(
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

        public static TransactionProto.Transaction.Req.Builder isInferredReq(String iid) {
            return thingReq(ConceptProto.Thing.Req.newBuilder().setIid(byteString(iid)).setThingIsInferredReq(
                    ConceptProto.Thing.IsInferred.Req.getDefaultInstance())
            );
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

        public static TransactionProto.Transaction.Req.Builder getPlaysReq(String iid) {
            return thingReq(ConceptProto.Thing.Req.newBuilder().setIid(byteString(iid)).setThingGetPlaysReq(
                    ConceptProto.Thing.GetPlays.Req.getDefaultInstance()
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

            public static TransactionProto.Transaction.Req.Builder getPlayersByRoleTypeReq(String iid) {
                return thingReq(ConceptProto.Thing.Req.newBuilder().setIid(byteString(iid)).setRelationGetPlayersByRoleTypeReq(
                        ConceptProto.Relation.GetPlayersByRoleType.Req.getDefaultInstance()
                ));
            }

            public static TransactionProto.Transaction.Req.Builder getPlayersReq(
                    String iid, List<ConceptProto.Type> roleTypes) {
                return thingReq(ConceptProto.Thing.Req.newBuilder().setIid(byteString(iid)).setRelationGetPlayersReq(
                        ConceptProto.Relation.GetPlayers.Req.newBuilder().addAllRoleTypes(roleTypes)
                ));
            }

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

            public static ConceptProto.Attribute.Value attributeValueBooleanReq(boolean value) {
                return ConceptProto.Attribute.Value.newBuilder().setBoolean(value).build();
            }

            public static ConceptProto.Attribute.Value attributeValueLongReq(long value) {
                return ConceptProto.Attribute.Value.newBuilder().setLong(value).build();
            }

            public static ConceptProto.Attribute.Value attributeValueDoubleReq(double value) {
                return ConceptProto.Attribute.Value.newBuilder().setDouble(value).build();
            }

            public static ConceptProto.Attribute.Value attributeValueStringReq(String value) {
                return ConceptProto.Attribute.Value.newBuilder().setString(value).build();
            }

            public static ConceptProto.Attribute.Value attributeValueDateTimeReq(LocalDateTime value) {
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
