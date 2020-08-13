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

import com.google.common.collect.ImmutableList;
import grakn.client.Grakn.DatabaseManager;
import grakn.client.exception.GraknClientException;
import grakn.client.rpc.RequestBuilder;
import grakn.protocol.GraknGrpc;
import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;

import java.util.List;

class GraknDatabaseManager implements DatabaseManager {
    private final GraknGrpc.GraknBlockingStub blockingStub;

    GraknDatabaseManager(ManagedChannel channel) {
        blockingStub = GraknGrpc.newBlockingStub(channel);
    }

    @Override
    public boolean contains(String name) {
        try {
            return blockingStub.databaseContains(RequestBuilder.DatabaseMessage.contains(name)).getContains();
        } catch (StatusRuntimeException e) {
            throw GraknClientException.create(e.getMessage(), e);
        }
    }

    @Override
    public void create(String name) {
        try {
            blockingStub.databaseCreate(RequestBuilder.DatabaseMessage.create(name));
        } catch (StatusRuntimeException e) {
            throw GraknClientException.create(e.getMessage(), e);
        }
    }

    @Override
    public void delete(String name) {
        try {
            blockingStub.databaseDelete(RequestBuilder.DatabaseMessage.delete(name));
        } catch (StatusRuntimeException e) {
            throw GraknClientException.create(e.getMessage(), e);
        }
    }

    @Override
    public List<String> all() {
        try {
            return ImmutableList.copyOf(blockingStub.databaseAll(RequestBuilder.DatabaseMessage.all()).getNamesList().iterator());
        } catch (StatusRuntimeException e) {
            throw GraknClientException.create(e.getMessage(), e);
        }
    }
}
