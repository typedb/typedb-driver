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

package com.vaticle.typedb.client;

import com.vaticle.typedb.client.api.TypeDBClient;
import com.vaticle.typedb.client.api.TypeDBCredential;
import com.vaticle.typedb.client.rpc.cluster.ClusterClient;
import com.vaticle.typedb.client.rpc.core.CoreClient;

import java.util.Set;

import static com.vaticle.typedb.common.collection.Collections.set;

public class TypeDB {

    public static final String DEFAULT_ADDRESS = "localhost:1729";

    public static TypeDBClient coreClient(String address) {
        return CoreClient.create(address);
    }

    public static TypeDBClient coreClient(String address, int parallelisation) {
        return CoreClient.create(address, parallelisation);
    }

    public static TypeDBClient.Cluster clusterClient(String address, TypeDBCredential credential) {
        return ClusterClient.create(set(address), credential);
    }

    public static TypeDBClient.Cluster clusterClient(String address, TypeDBCredential credential, int parallelisation) {
        return ClusterClient.create(set(address), credential, parallelisation);
    }

    public static TypeDBClient.Cluster clusterClient(Set<String> addresses, TypeDBCredential credential) {
        return ClusterClient.create(addresses, credential);
    }

    public static TypeDBClient.Cluster clusterClient(Set<String> addresses, TypeDBCredential credential, int parallelisation) {
        return ClusterClient.create(addresses, credential, parallelisation);
    }

}
