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

package grakn.client.connection;

import com.google.protobuf.ByteString;
import grakn.client.Grakn.Database;
import grakn.client.Grakn.Session;
import grakn.client.Grakn.Transaction;
import grakn.client.rpc.RequestBuilder;
import grakn.protocol.GraknGrpc;
import grakn.protocol.SessionProto;
import io.grpc.ManagedChannel;

public class GraknSession implements Session {

    protected ManagedChannel channel;
    private String username; // TODO: Do we need to save this? It's not used.
    private String password; // TODO: Do we need to save this? It's not used.
    protected String databaseName;
    protected GraknGrpc.GraknBlockingStub sessionStub;
    protected ByteString sessionId;
    protected boolean isOpen;

    GraknSession(ManagedChannel channel, String username, String password, String databaseName, Session.Type type) {
        this.username = username;
        this.password = password;
        this.databaseName = databaseName;
        this.channel = channel;
        this.sessionStub = GraknGrpc.newBlockingStub(channel);

        SessionProto.Session.Open.Req.Builder openReq = RequestBuilder.Session.open(databaseName).newBuilderForType();
        openReq.setDatabase(databaseName);

        switch (type) {
            case DATA:
                openReq.setType(SessionProto.Session.Type.DATA);
                break;
            case SCHEMA:
                openReq.setType(SessionProto.Session.Type.SCHEMA);
                break;
            default:
                openReq.setType(SessionProto.Session.Type.UNRECOGNIZED);
        }

        SessionProto.Session.Open.Res response = sessionStub.sessionOpen(openReq.build());
        sessionId = response.getSessionID();
        isOpen = true;
    }

    @Override
    public Transaction.Builder transaction() {
        return new GraknTransaction.Builder(channel, this, sessionId);
    }

    @Override
    public Transaction transaction(Transaction.Type type) {
        return new GraknTransaction(channel, this, sessionId, type);
    }

    @Override
    public boolean isOpen() {
        return isOpen;
    }

    public void close() {
        if (!isOpen) return;
        sessionStub.sessionClose(RequestBuilder.Session.close(sessionId));
        isOpen = false;
    }

    @Override
    public Database database() {
        return Database.of(databaseName);
    }
}
