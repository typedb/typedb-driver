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

package com.vaticle.typedb.client.connection.core;

import com.vaticle.typedb.client.common.rpc.TypeDBStub;
import com.vaticle.typedb.client.connection.TypeDBConnectionFactory;
import io.grpc.ManagedChannel;

public class CoreConnectionFactory extends TypeDBConnectionFactory {

    @Override
    public ManagedChannel newManagedChannel(String address) {
        return plainTextChannel(address);
    }

    @Override
    public TypeDBStub newTypeDBStub(ManagedChannel channel) {
        return CoreStub.create(channel);
    }

}
