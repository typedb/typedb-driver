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

package grakn.client.api;

import grakn.client.api.concept.ConceptManager;
import grakn.client.api.logic.LogicManager;
import grakn.client.api.query.QueryFuture;
import grakn.client.api.query.QueryManager;
import grakn.protocol.TransactionProto;

import java.util.stream.Stream;

public interface Transaction extends AutoCloseable {

    boolean isOpen();

    Type type();

    GraknOptions options();

    ConceptManager concepts();

    LogicManager logic();

    QueryManager query();

    void commit();

    void rollback();

    void close();

    enum Type {
        READ(0),
        WRITE(1);

        private final int id;
        private final boolean isWrite;

        Type(int id) {
            this.id = id;
            this.isWrite = id == 1;
        }

        public static Type of(int value) {
            for (Type t : values()) {
                if (t.id == value) return t;
            }
            return null;
        }

        public int id() {
            return id;
        }

        public boolean isRead() { return !isWrite; }

        public boolean isWrite() { return isWrite; }

        public TransactionProto.Transaction.Type proto() {
            return TransactionProto.Transaction.Type.forNumber(id);
        }
    }

    interface Extended extends Transaction {

        TransactionProto.Transaction.Res execute(TransactionProto.Transaction.Req.Builder request);

        QueryFuture<TransactionProto.Transaction.Res> query(TransactionProto.Transaction.Req.Builder request);

        Stream<TransactionProto.Transaction.ResPart> stream(TransactionProto.Transaction.Req.Builder request);
    }
}
