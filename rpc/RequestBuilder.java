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

import com.google.protobuf.ByteString;
import grakn.client.Grakn;
import grakn.client.concept.ConceptIID;
import grakn.client.concept.rpc.ConceptMessage;
import grakn.client.concept.type.AttributeType.ValueType;
import grakn.protocol.DatabaseProto;
import grakn.protocol.OptionsProto;
import grakn.protocol.SessionProto;
import grakn.protocol.TransactionProto;
import graql.lang.pattern.Pattern;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static grabl.tracing.client.GrablTracingThreadStatic.ThreadTrace;
import static grabl.tracing.client.GrablTracingThreadStatic.currentThreadTrace;
import static grabl.tracing.client.GrablTracingThreadStatic.isTracingEnabled;

/**
 * A utility class to build RPC Requests from a provided set of Grakn concepts.
 */
public class RequestBuilder {

    public static class Session {

        public static SessionProto.Session.Open.Req open(String database) {
            return SessionProto.Session.Open.Req.newBuilder().setDatabase(database).build();
        }

        public static SessionProto.Session.Close.Req close(ByteString sessionId) {
            return SessionProto.Session.Close.Req.newBuilder().setSessionID(sessionId).build();
        }
    }

    /**
     * An RPC Request Builder class for Transaction Service
     */
    public static class Transaction {

        public static TransactionProto.Transaction.Req open(ByteString sessionID, Grakn.Transaction.Type txType) {
            TransactionProto.Transaction.Open.Req openRequest = TransactionProto.Transaction.Open.Req.newBuilder()
                    .setSessionID(sessionID)
                    .setType(TransactionProto.Transaction.Type.forNumber(txType.iid()))
                    .build();

            return TransactionProto.Transaction.Req.newBuilder().putAllMetadata(getTracingData()).setOpenReq(openRequest).build();
        }

        public static TransactionProto.Transaction.Req commit() {
            return TransactionProto.Transaction.Req.newBuilder()
                    .putAllMetadata(getTracingData())
                    .setCommitReq(TransactionProto.Transaction.Commit.Req.getDefaultInstance())
                    .build();
        }

        public static TransactionProto.Transaction.Iter.Req query(String queryString, Grakn.Transaction.QueryOptions options) {
            OptionsProto.Options.Builder builder = OptionsProto.Options.newBuilder();
            options
                    .whenSet(Grakn.Transaction.BooleanOption.INFER, builder::setInfer)
                    .whenSet(Grakn.Transaction.BooleanOption.EXPLAIN, builder::setExplain);

            TransactionProto.Transaction.Iter.Req.Builder req = TransactionProto.Transaction.Iter.Req.newBuilder()
                    .setQueryIterReq(TransactionProto.Transaction.Query.Iter.Req.newBuilder()
                            .setQuery(queryString)
                            .setOptions(builder));

            options.whenSet(Grakn.Transaction.BatchOption.BATCH_SIZE, req::setOptions);

            return req.build();
        }

        public static TransactionProto.Transaction.Req getType(String label) {
            return TransactionProto.Transaction.Req.newBuilder()
                    .putAllMetadata(getTracingData())
                    .setGetTypeReq(TransactionProto.Transaction.GetType.Req.newBuilder().setLabel(label))
                    .build();
        }

        public static TransactionProto.Transaction.Req getThing(ConceptIID iid) {
            return TransactionProto.Transaction.Req.newBuilder()
                    .putAllMetadata(getTracingData())
                    .setGetThingReq(TransactionProto.Transaction.GetThing.Req.newBuilder().setIid(iid.getValue()))
                    .build();
        }

        public static TransactionProto.Transaction.Req putEntityType(String label) {
            return TransactionProto.Transaction.Req.newBuilder()
                    .putAllMetadata(getTracingData())
                    .setPutEntityTypeReq(TransactionProto.Transaction.PutEntityType.Req.newBuilder().setLabel(label))
                    .build();
        }

        public static TransactionProto.Transaction.Req putAttributeType(String label, ValueType valueType) {
            TransactionProto.Transaction.PutAttributeType.Req request = TransactionProto.Transaction.PutAttributeType.Req.newBuilder()
                    .setLabel(label)
                    .setValueType(ConceptMessage.valueType(valueType))
                    .build();

            return TransactionProto.Transaction.Req.newBuilder().putAllMetadata(getTracingData()).setPutAttributeTypeReq(request).build();
        }

        public static TransactionProto.Transaction.Req putRelationType(String label) {
            TransactionProto.Transaction.PutRelationType.Req request = TransactionProto.Transaction.PutRelationType.Req.newBuilder()
                    .setLabel(label)
                    .build();
            return TransactionProto.Transaction.Req.newBuilder().putAllMetadata(getTracingData()).setPutRelationTypeReq(request).build();
        }

        public static TransactionProto.Transaction.Req putRule(String label, Pattern when, Pattern then) {
            throw new UnsupportedOperationException();
//            TransactionProto.Transaction.PutRule.Req request = TransactionProto.Transaction.PutRule.Req.newBuilder()
//                    .setLabel(label.getValue())
//                    .setWhen(when.toString())
//                    .setThen(then.toString())
//                    .build();
//            return TransactionProto.Transaction.Req.newBuilder().putAllMetadata(getTracingData()).setPutRuleReq(request).build();
        }
    }

    /**
     * An RPC Request Builder class for Database Service
     */
    public static class DatabaseMessage {

        public static DatabaseProto.Database.Contains.Req contains(String name) {
            return DatabaseProto.Database.Contains.Req.newBuilder().setName(name).build();
        }

        public static DatabaseProto.Database.Create.Req create(String name) {
            return DatabaseProto.Database.Create.Req.newBuilder().setName(name).build();
        }

        public static DatabaseProto.Database.Delete.Req delete(String name) {
            return DatabaseProto.Database.Delete.Req.newBuilder().setName(name).build();
        }

        public static DatabaseProto.Database.All.Req all() {
            return DatabaseProto.Database.All.Req.getDefaultInstance();
        }
    }

    public static Map<String, String> getTracingData() {
        if (isTracingEnabled()) {
            ThreadTrace threadTrace = currentThreadTrace();
            if (threadTrace == null) {
                return Collections.emptyMap();
            }

            if (threadTrace.getId() == null || threadTrace.getRootId() == null) {
                return Collections.emptyMap();
            }

            Map<String, String> metadata = new HashMap<>(2);
            metadata.put("traceParentId", threadTrace.getId().toString());
            metadata.put("traceRootId", threadTrace.getRootId().toString());
            return metadata;
        } else {
            return Collections.emptyMap();
        }
    }
}
