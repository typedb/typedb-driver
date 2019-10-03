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

package grakn.client.rpc;

import grakn.client.GraknClient;
import grakn.client.concept.AttributeTypeImpl;
import grakn.client.concept.api.Concept;
import grakn.client.concept.api.AttributeType;
import grakn.client.concept.api.ConceptId;
import grakn.client.concept.api.Label;
import grakn.client.exception.GraknClientException;
import grakn.protocol.keyspace.KeyspaceProto;
import grakn.protocol.session.ConceptProto;
import grakn.protocol.session.SessionProto;
import graql.lang.pattern.Pattern;
import graql.lang.query.GraqlQuery;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collection;

import static java.util.stream.Collectors.toList;

/**
 * A utility class to build RPC Requests from a provided set of Grakn concepts.
 */
public class RequestBuilder {

    public static class Session {

        public static SessionProto.Session.Open.Req open(String keyspace) {
            return SessionProto.Session.Open.Req.newBuilder().setKeyspace(keyspace).build();
        }

        public static SessionProto.Session.Close.Req close(String sessionId) {
            return SessionProto.Session.Close.Req.newBuilder().setSessionId(sessionId).build();
        }
    }

    /**
     * An RPC Request Builder class for Transaction Service
     */
    public static class Transaction {

        public static SessionProto.Transaction.Req open(String sessionId, GraknClient.Transaction.Type txType) {
            SessionProto.Transaction.Open.Req openRequest = SessionProto.Transaction.Open.Req.newBuilder()
                    .setSessionId(sessionId)
                    .setType(SessionProto.Transaction.Type.valueOf(txType.id()))
                    .build();

            return SessionProto.Transaction.Req.newBuilder().setOpenReq(openRequest).build();
        }

        public static SessionProto.Transaction.Req commit() {
            return SessionProto.Transaction.Req.newBuilder()
                    .setCommitReq(SessionProto.Transaction.Commit.Req.getDefaultInstance())
                    .build();
        }

        public static SessionProto.Transaction.Req query(GraqlQuery query) {
            return query(query.toString(), true);
        }

        public static SessionProto.Transaction.Req query(GraqlQuery query, boolean infer) {
            return query(query.toString(), infer);
        }

        public static SessionProto.Transaction.Req query(String queryString) {
            return query(queryString, true);
        }

        public static SessionProto.Transaction.Req query(String queryString, boolean infer) {
            SessionProto.Transaction.Query.Req request = SessionProto.Transaction.Query.Req.newBuilder()
                    .setQuery(queryString)
                    .setInfer(infer ? SessionProto.Transaction.Query.INFER.TRUE : SessionProto.Transaction.Query.INFER.FALSE)
                    .build();
            return SessionProto.Transaction.Req.newBuilder().setQueryReq(request).build();
        }

        public static SessionProto.Transaction.Req getSchemaConcept(Label label) {
            return SessionProto.Transaction.Req.newBuilder()
                    .setGetSchemaConceptReq(SessionProto.Transaction.GetSchemaConcept.Req.newBuilder().setLabel(label.getValue()))
                    .build();
        }

        public static SessionProto.Transaction.Req getConcept(ConceptId id) {
            return SessionProto.Transaction.Req.newBuilder()
                    .setGetConceptReq(SessionProto.Transaction.GetConcept.Req.newBuilder().setId(id.getValue()))
                    .build();
        }


        public static SessionProto.Transaction.Req getAttributes(Object value) {
            return SessionProto.Transaction.Req.newBuilder()
                    .setGetAttributesReq(SessionProto.Transaction.GetAttributes.Req.newBuilder()
                                                 .setValue(ConceptMessage.attributeValue(value))
                    ).build();
        }

        public static SessionProto.Transaction.Req putEntityType(Label label) {
            return SessionProto.Transaction.Req.newBuilder()
                    .setPutEntityTypeReq(SessionProto.Transaction.PutEntityType.Req.newBuilder().setLabel(label.getValue()))
                    .build();
        }

        public static SessionProto.Transaction.Req putAttributeType(Label label, AttributeTypeImpl.DataType<?> dataType) {
            SessionProto.Transaction.PutAttributeType.Req request = SessionProto.Transaction.PutAttributeType.Req.newBuilder()
                    .setLabel(label.getValue())
                    .setDataType(ConceptMessage.setDataType(dataType))
                    .build();

            return SessionProto.Transaction.Req.newBuilder().setPutAttributeTypeReq(request).build();
        }

        public static SessionProto.Transaction.Req putRelationType(Label label) {
            SessionProto.Transaction.PutRelationType.Req request = SessionProto.Transaction.PutRelationType.Req.newBuilder()
                    .setLabel(label.getValue())
                    .build();
            return SessionProto.Transaction.Req.newBuilder().setPutRelationTypeReq(request).build();
        }

        public static SessionProto.Transaction.Req putRole(Label label) {
            SessionProto.Transaction.PutRole.Req request = SessionProto.Transaction.PutRole.Req.newBuilder()
                    .setLabel(label.getValue())
                    .build();
            return SessionProto.Transaction.Req.newBuilder().setPutRoleReq(request).build();
        }

        public static SessionProto.Transaction.Req putRule(Label label, Pattern when, Pattern then) {
            SessionProto.Transaction.PutRule.Req request = SessionProto.Transaction.PutRule.Req.newBuilder()
                    .setLabel(label.getValue())
                    .setWhen(when.toString())
                    .setThen(then.toString())
                    .build();
            return SessionProto.Transaction.Req.newBuilder().setPutRuleReq(request).build();
        }

        public static SessionProto.Transaction.Req iterate(int iteratorId) {
            return SessionProto.Transaction.Req.newBuilder()
                    .setIterateReq(SessionProto.Transaction.Iter.Req.newBuilder()
                                           .setId(iteratorId)).build();
        }
    }

    /**
     * An RPC Request Builder class for Concept messages
     */
    public static class ConceptMessage {

        public static ConceptProto.Concept from(Concept concept) {
            return ConceptProto.Concept.newBuilder()
                    .setId(concept.id().getValue())
                    .setBaseType(getBaseType(concept))
                    .build();
        }

        private static ConceptProto.Concept.BASE_TYPE getBaseType(Concept concept) {
            if (concept.isEntityType()) {
                return ConceptProto.Concept.BASE_TYPE.ENTITY_TYPE;
            } else if (concept.isRelationType()) {
                return ConceptProto.Concept.BASE_TYPE.RELATION_TYPE;
            } else if (concept.isAttributeType()) {
                return ConceptProto.Concept.BASE_TYPE.ATTRIBUTE_TYPE;
            } else if (concept.isEntity()) {
                return ConceptProto.Concept.BASE_TYPE.ENTITY;
            } else if (concept.isRelation()) {
                return ConceptProto.Concept.BASE_TYPE.RELATION;
            } else if (concept.isAttribute()) {
                return ConceptProto.Concept.BASE_TYPE.ATTRIBUTE;
            } else if (concept.isRole()) {
                return ConceptProto.Concept.BASE_TYPE.ROLE;
            } else if (concept.isRule()) {
                return ConceptProto.Concept.BASE_TYPE.RULE;
            } else if (concept.isType()) {
                return ConceptProto.Concept.BASE_TYPE.META_TYPE;
            } else {
                throw GraknClientException.unreachableStatement("Unrecognised concept " + concept);
            }
        }

        public static Collection<ConceptProto.Concept> concepts(Collection<Concept> concepts) {
            return concepts.stream().map(ConceptMessage::from).collect(toList());
        }

        public static ConceptProto.ValueObject attributeValue(Object value) {
            // TODO: this conversion method should use Serialiser class, once it's moved to grakn.common

            ConceptProto.ValueObject.Builder builder = ConceptProto.ValueObject.newBuilder();
            if (value instanceof String) {
                builder.setString((String) value);
            } else if (value instanceof Boolean) {
                builder.setBoolean((boolean) value);
            } else if (value instanceof Integer) {
                builder.setInteger((int) value);
            } else if (value instanceof Long) {
                builder.setLong((long) value);
            } else if (value instanceof Float) {
                builder.setFloat((float) value);
            } else if (value instanceof Double) {
                builder.setDouble((double) value);
            } else if (value instanceof LocalDateTime) {
                builder.setDate(((LocalDateTime) value).atZone(ZoneId.of("Z")).toInstant().toEpochMilli());
            } else {
                throw GraknClientException.unreachableStatement("Unrecognised " + value);
            }

            return builder.build();
        }

        public static AttributeTypeImpl.DataType dataType(ConceptProto.AttributeType.DATA_TYPE dataType) {
            switch (dataType) {
                case STRING:
                    return AttributeTypeImpl.DataType.STRING;
                case BOOLEAN:
                    return  AttributeTypeImpl.DataType.BOOLEAN;
                case INTEGER:
                    return  AttributeTypeImpl.DataType.INTEGER;
                case LONG:
                    return AttributeTypeImpl.DataType.LONG;
                case FLOAT:
                    return  AttributeTypeImpl.DataType.FLOAT;
                case DOUBLE:
                    return  AttributeTypeImpl.DataType.DOUBLE;
                case DATE:
                    return  AttributeTypeImpl.DataType.DATE;
                default:
                case UNRECOGNIZED:
                    throw new IllegalArgumentException("Unrecognised " + dataType);
            }
        }

        static ConceptProto.AttributeType.DATA_TYPE setDataType(AttributeType.DataType dataType) {
            if (dataType.equals(AttributeType.DataType.STRING)) {
                return ConceptProto.AttributeType.DATA_TYPE.STRING;
            } else if (dataType.equals( AttributeTypeImpl.DataType.BOOLEAN)) {
                return ConceptProto.AttributeType.DATA_TYPE.BOOLEAN;
            } else if (dataType.equals( AttributeTypeImpl.DataType.INTEGER)) {
                return ConceptProto.AttributeType.DATA_TYPE.INTEGER;
            } else if (dataType.equals( AttributeTypeImpl.DataType.LONG)) {
                return ConceptProto.AttributeType.DATA_TYPE.LONG;
            } else if (dataType.equals( AttributeTypeImpl.DataType.FLOAT)) {
                return ConceptProto.AttributeType.DATA_TYPE.FLOAT;
            } else if (dataType.equals( AttributeTypeImpl.DataType.DOUBLE)) {
                return ConceptProto.AttributeType.DATA_TYPE.DOUBLE;
            } else if (dataType.equals( AttributeTypeImpl.DataType.DATE)) {
                return ConceptProto.AttributeType.DATA_TYPE.DATE;
            } else {
                throw GraknClientException.unreachableStatement("Unrecognised " + dataType);
            }
        }
    }

    /**
     * An RPC Request Builder class for Keyspace Service
     */
    public static class KeyspaceMessage {

        public static KeyspaceProto.Keyspace.Delete.Req delete(String name, String username, String password) {
            KeyspaceProto.Keyspace.Delete.Req.Builder builder = KeyspaceProto.Keyspace.Delete.Req.newBuilder();
            if (username != null) {
                builder.setUsername(username);
            }
            if (password != null) {
                builder.setPassword(password);
            }
            return builder.setName(name).build();
        }

        public static KeyspaceProto.Keyspace.Retrieve.Req retrieve(String username, String password) {
            KeyspaceProto.Keyspace.Retrieve.Req.Builder builder = KeyspaceProto.Keyspace.Retrieve.Req.newBuilder();
            if (username != null) {
                builder.setUsername(username);
            }
            if (password != null) {
                builder.setPassword(password);
            }
            return builder.build();
        }
    }
}
