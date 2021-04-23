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

package typedb.client;

import typedb.client.api.TypeDBClient;
import typedb.client.cluster.ClusterClient;
import typedb.client.core.CoreClient;

import java.util.Set;

import static grakn.common.collection.Collections.set;

public class TypeDB {

    public static final String DEFAULT_ADDRESS = "localhost:1729";

    public static TypeDBClient coreClient(String address) {
        return new CoreClient(address);
    }

    public static TypeDBClient coreClient(String address, int parallelisation) {
        return new CoreClient(address, parallelisation);
    }

    public static TypeDBClient.Cluster clusterClient(String address) {
        return new ClusterClient(set(address));
    }

    public static TypeDBClient.Cluster clusterClient(String address, int parallelisation) {
        return new ClusterClient(set(address), parallelisation);
    }

    public static TypeDBClient.Cluster clusterClient(Set<String> addresses) {
        return new ClusterClient(addresses);
    }

    public static TypeDBClient.Cluster clusterClient(Set<String> addresses, int parallelisation) {
        return new ClusterClient(addresses, parallelisation);
    }
}
