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

package com.vaticle.typedb.client.cluster;

import com.vaticle.typedb.client.api.TypeDBOptions;
import com.vaticle.typedb.client.api.TypeDBSession;
import com.vaticle.typedb.client.api.TypeDBTransaction;
import com.vaticle.typedb.client.api.database.Database;
import com.vaticle.typedb.client.core.CoreSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClusterSession implements TypeDBSession {

    private static final Logger LOG = LoggerFactory.getLogger(TypeDBSession.class);
    private final ClusterClient clusterClient;
    private final TypeDBOptions.Cluster options;
    private ClusterServerClient clusterServerClient;
    private CoreSession coreSession;

    public ClusterSession(ClusterClient clusterClient, String serverAddress, String database, Type type, TypeDBOptions.Cluster options) {
        this.clusterClient = clusterClient;
        this.clusterServerClient = clusterClient.clusterServerClient(serverAddress);
        LOG.debug("Opening a session to '{}'", serverAddress);
        this.coreSession = clusterServerClient.session(database, type, options);
        this.options = options;
    }

    @Override
    public TypeDBTransaction transaction(TypeDBTransaction.Type type) {
        return transaction(type, TypeDBOptions.cluster());
    }

    @Override
    public TypeDBTransaction transaction(TypeDBTransaction.Type type, TypeDBOptions options) {
        TypeDBOptions.Cluster clusterOpt = options.asCluster();
        if (clusterOpt.readAnyReplica().isPresent() && clusterOpt.readAnyReplica().get()) {
            return transactionAnyReplica(type, clusterOpt);
        } else {
            return transactionPrimaryReplica(type, options);
        }
    }

    private TypeDBTransaction transactionPrimaryReplica(TypeDBTransaction.Type type, TypeDBOptions options) {
        return transactionFailsafeTask(type, options).runPrimaryReplica();
    }

    private TypeDBTransaction transactionAnyReplica(TypeDBTransaction.Type type, TypeDBOptions.Cluster options) {
        return transactionFailsafeTask(type, options).runAnyReplica();
    }

    private FailsafeTask<TypeDBTransaction> transactionFailsafeTask(TypeDBTransaction.Type type, TypeDBOptions options) {
        return new FailsafeTask<TypeDBTransaction>(clusterClient, database().name()) {

            @Override
            TypeDBTransaction run(ClusterDatabase.Replica replica) {
                return coreSession.transaction(type, options);
            }

            @Override
            TypeDBTransaction rerun(ClusterDatabase.Replica replica) {
                if (coreSession != null) coreSession.close();
                clusterServerClient = clusterClient.clusterServerClient(replica.address());
                coreSession = clusterServerClient.session(database().name(), ClusterSession.this.type(), ClusterSession.this.options());
                return coreSession.transaction(type, options);
            }
        };
    }

    @Override
    public TypeDBSession.Type type() {
        return coreSession.type();
    }

    @Override
    public TypeDBOptions.Cluster options() {
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
