/*
 * Copyright (C) 2022 Vaticle
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

package com.vaticle.typedb.client.connection;

import com.vaticle.typedb.client.api.database.Database;
import com.vaticle.typedb.client.common.NativeObject;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.vaticle.typedb.client.jni.typedb_client.database_delete;
import static com.vaticle.typedb.client.jni.typedb_client.database_get_name;
import static com.vaticle.typedb.client.jni.typedb_client.*;
import static com.vaticle.typedb.client.jni.typedb_client.database_rule_schema;
import static com.vaticle.typedb.client.jni.typedb_client.database_schema;
import static com.vaticle.typedb.client.jni.typedb_client.database_type_schema;

public class TypeDBDatabaseImpl extends NativeObject<com.vaticle.typedb.client.jni.Database> implements Database {
    public TypeDBDatabaseImpl(com.vaticle.typedb.client.jni.Database database) {
        super(database);
    }

    @Override
    public String name() {
        return database_get_name(nativeObject);
    }

    @Override
    public String schema() {
        return database_schema(nativeObject);
    }

    @Override
    public String typeSchema() {
        return database_type_schema(nativeObject);
    }

    @Override
    public String ruleSchema() {
        return database_rule_schema(nativeObject);
    }

    @Override
    public void delete() {
        database_delete(nativeObject);
    }

    @Override
    public String toString() {
        return name();
    }

    @Override
    public Set<? extends Database.Replica> replicas() {
        return database_get_replicas_info(nativeObject).stream().map(Replica::new).collect(Collectors.toSet());
    }

    @Override
    public Optional<? extends Database.Replica> primaryReplica() {
        com.vaticle.typedb.client.jni.ReplicaInfo res = database_get_primary_replica_info(nativeObject);
        if (res != null) return Optional.of(new Replica(res));
        else return Optional.empty();
    }

    @Override
    public Optional<? extends Database.Replica> preferredReplica() {
        com.vaticle.typedb.client.jni.ReplicaInfo res = database_get_preferred_replica_info(nativeObject);
        if (res != null) return Optional.of(new Replica(res));
        else return Optional.empty();
    }

    public static class Replica extends NativeObject<com.vaticle.typedb.client.jni.ReplicaInfo> implements Database.Replica {
        Replica(com.vaticle.typedb.client.jni.ReplicaInfo replicaInfo) {
            super(replicaInfo);
        }

        @Override
        public String address() {
            return replica_info_get_address(nativeObject);
        }

        @Override
        public boolean isPrimary() {
            return replica_info_is_primary(nativeObject);
        }

        @Override
        public boolean isPreferred(){
            return replica_info_is_preferred(nativeObject);
        }

        @Override
        public long term() {
            return replica_info_get_term(nativeObject);
        }
    }
}
