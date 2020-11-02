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

import com.google.common.collect.ImmutableList;
import grakn.client.Grakn.DatabaseManager;
import grakn.client.common.exception.GraknClientException;
import grakn.protocol.DatabaseProto;
import grakn.protocol.DatabaseServiceGrpc;
import io.grpc.Channel;
import io.grpc.StatusRuntimeException;

import java.util.List;
import java.util.function.Supplier;

class RPCDatabaseManager implements DatabaseManager {
    private final DatabaseServiceGrpc.DatabaseServiceBlockingStub databaseService;

    RPCDatabaseManager(final Channel channel) {
        databaseService = DatabaseServiceGrpc.newBlockingStub(channel);
    }

    @Override
    public boolean contains(final String name) {
        return request(() -> databaseService.contains(DatabaseProto.Database.Contains.Req.newBuilder().setName(name).build()).getContains());
    }

    @Override
    public void create(final String name) {
        request(() -> databaseService.create(DatabaseProto.Database.Create.Req.newBuilder().setName(name).build()));
    }

    @Override
    public void delete(final String name) {
        request(() -> databaseService.delete(DatabaseProto.Database.Delete.Req.newBuilder().setName(name).build()));
    }

    @Override
    public List<String> all() {
        return request(() -> ImmutableList.copyOf(databaseService.all(DatabaseProto.Database.All.Req.getDefaultInstance()).getNamesList().iterator()));
    }

    private static <RES> RES request(final Supplier<RES> req) {
        try {
            return req.get();
        } catch (StatusRuntimeException e) {
            throw new GraknClientException(e);
        }
    }
}
