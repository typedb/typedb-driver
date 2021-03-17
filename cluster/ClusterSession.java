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

package grakn.client.cluster;

import grakn.client.api.GraknOptions;
import grakn.client.api.GraknSession;
import grakn.client.api.GraknTransaction;
import grakn.client.api.database.Database;
import grakn.client.core.CoreClient;
import grakn.client.core.CoreSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClusterSession implements GraknSession {
    private static final Logger LOG = LoggerFactory.getLogger(GraknSession.class);
    private final ClusterClient clusterClient;
    private final GraknOptions.Cluster options;
    private CoreClient coreClient;
    private CoreSession coreSession;

    public ClusterSession(ClusterClient clusterClient, String serverAddress, String database, Type type, GraknOptions.Cluster options) {
        this.clusterClient = clusterClient;
        this.coreClient = clusterClient.coreClient(serverAddress);
        LOG.debug("Opening a session to '{}'", serverAddress);
        this.coreSession = coreClient.session(database, type, options);
        this.options = options;
    }

    @Override
    public GraknTransaction transaction(GraknTransaction.Type type) {
        return transaction(type, GraknOptions.cluster());
    }

    @Override
    public GraknTransaction transaction(GraknTransaction.Type type, GraknOptions options) {
        GraknOptions.Cluster clusterOpt = options.asCluster();
        if (clusterOpt.readAnyReplica().isPresent() && clusterOpt.readAnyReplica().get()) {
            return transactionAnyReplica(type, clusterOpt);
        } else {
            return transactionPrimaryReplica(type, options);
        }
    }

    private GraknTransaction transactionPrimaryReplica(GraknTransaction.Type type, GraknOptions options) {
        return transactionFailsafeTask(type, options).runPrimaryReplica();
    }

    private GraknTransaction transactionAnyReplica(GraknTransaction.Type type, GraknOptions.Cluster options) {
        return transactionFailsafeTask(type, options).runAnyReplica();
    }

    private FailsafeTask<GraknTransaction> transactionFailsafeTask(GraknTransaction.Type type, GraknOptions options) {
        return new FailsafeTask<GraknTransaction>(clusterClient, database().name()) {

            @Override
            GraknTransaction run(ClusterDatabase.Replica replica) {
                return coreSession.transaction(type, options);
            }

            @Override
            GraknTransaction rerun(ClusterDatabase.Replica replica) {
                if (coreSession != null) coreSession.close();
                coreClient = clusterClient.coreClient(replica.address());
                coreSession = coreClient.session(database().name(), ClusterSession.this.type(), ClusterSession.this.options());
                return coreSession.transaction(type, options);
            }
        };
    }

    @Override
    public GraknSession.Type type() {
        return coreSession.type();
    }

    @Override
    public GraknOptions.Cluster options() {
        return options;
    }

    @Override
    public boolean isOpen() {
        return coreSession.isOpen();
    }

    @Override
    public void close() {
        coreSession.close();
    }

    @Override
    public Database database() {
        return coreSession.database();
    }
}
