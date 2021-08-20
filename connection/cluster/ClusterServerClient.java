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

package com.vaticle.typedb.client.connection.cluster;

import com.vaticle.typedb.client.api.connection.TypeDBCredential;
import com.vaticle.typedb.client.connection.TypeDBClientImpl;

class ClusterServerClient extends TypeDBClientImpl {

    private final ClusterServerStub clusterServerStub;

    private ClusterServerClient(String address, TypeDBCredential credential, int parallelisation) {
        super(address, new ClusterServerConnectionFactory(credential), parallelisation);
        clusterServerStub = ClusterServerStub.create(credential.username(), credential.password(), channel());
    }

    static ClusterServerClient create(String address, TypeDBCredential credential, int parallelisation) {
        return new ClusterServerClient(address, credential, parallelisation);
    }

    ClusterServerStub clusterServerStub() {
        return clusterServerStub;
    }
}
