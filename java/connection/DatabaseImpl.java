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

package com.typedb.driver.connection;

import com.typedb.driver.api.database.Database;
import com.typedb.driver.common.NativeObject;
import com.typedb.driver.common.Validator;
import com.typedb.driver.common.exception.TypeDBDriverException;

import static com.typedb.driver.common.exception.ErrorMessage.Driver.DATABASE_DELETED;
import static com.typedb.driver.jni.typedb_driver.database_delete;
import static com.typedb.driver.jni.typedb_driver.database_export_file;
import static com.typedb.driver.jni.typedb_driver.database_get_name;
import static com.typedb.driver.jni.typedb_driver.database_schema;
import static com.typedb.driver.jni.typedb_driver.database_type_schema;

public class DatabaseImpl extends NativeObject<com.typedb.driver.jni.Database> implements Database {
    public DatabaseImpl(com.typedb.driver.jni.Database database) {
        super(database);
    }

    @Override
    public String name() {
        if (!nativeObject.isOwned()) throw new TypeDBDriverException(DATABASE_DELETED);
        return database_get_name(nativeObject);
    }

    @Override
    public String schema() throws TypeDBDriverException {
        if (!nativeObject.isOwned()) throw new TypeDBDriverException(DATABASE_DELETED);
        try {
            return database_schema(nativeObject);
        } catch (com.typedb.driver.jni.Error e) {
            throw new TypeDBDriverException(e);
        }
    }

    @Override
    public String typeSchema() throws TypeDBDriverException {
        if (!nativeObject.isOwned()) throw new TypeDBDriverException(DATABASE_DELETED);
        try {
            return database_type_schema(nativeObject);
        } catch (com.typedb.driver.jni.Error e) {
            throw new TypeDBDriverException(e);
        }
    }

    @Override
    public void exportFile(String schemaFilePath, String dataFilePath) throws TypeDBDriverException {
        Validator.requireNonNull(schemaFilePath, "schemaFilePath");
        Validator.requireNonNull(dataFilePath, "dataFilePath");
        try {
            database_export_file(nativeObject, schemaFilePath, dataFilePath);
        } catch (com.typedb.driver.jni.Error e) {
            throw new TypeDBDriverException(e);
        }
    }

    @Override
    public void delete() throws TypeDBDriverException {
        if (!nativeObject.isOwned()) throw new TypeDBDriverException(DATABASE_DELETED);
        try {
            // NOTE: .released() relinquishes ownership of the native object to the Rust side
            database_delete(nativeObject.released());
        } catch (com.typedb.driver.jni.Error e) {
            throw new TypeDBDriverException(e);
        }
    }

    @Override
    public String toString() {
        return name();
    }

//    @Override
//    public Set<? extends Database.Replica> replicas() {
//        if (!nativeObject.isOwned()) throw new TypeDBDriverException(DATABASE_DELETED);
//        return new NativeIterator<>(database_get_replicas_info(nativeObject)).stream().map(Replica::new).collect(Collectors.toSet());
//    }
//
//    @Override
//    public Optional<? extends Database.Replica> primaryReplica() {
//        if (!nativeObject.isOwned()) throw new TypeDBDriverException(DATABASE_DELETED);
//        com.typedb.driver.jni.ReplicaInfo res = database_get_primary_replica_info(nativeObject);
//        if (res != null) return Optional.of(new Replica(res));
//        else return Optional.empty();
//    }
//
//    @Override
//    public Optional<? extends Database.Replica> preferredReplica() {
//        if (!nativeObject.isOwned()) throw new TypeDBDriverException(DATABASE_DELETED);
//        com.typedb.driver.jni.ReplicaInfo res = database_get_preferred_replica_info(nativeObject);
//        if (res != null) return Optional.of(new Replica(res));
//        else return Optional.empty();
//    }
//
//    public static class Replica extends NativeObject<com.typedb.driver.jni.ReplicaInfo> implements Database.Replica {
//        Replica(com.typedb.driver.jni.ReplicaInfo replicaInfo) {
//            super(replicaInfo);
//        }
//
//        @Override
//        public String server() {
//            return replica_info_get_server(nativeObject);
//        }
//
//        @Override
//        public boolean isPrimary() {
//            return replica_info_is_primary(nativeObject);
//        }
//
//        @Override
//        public boolean isPreferred(){
//            return replica_info_is_preferred(nativeObject);
//        }
//
//        @Override
//        public long term() {
//            return replica_info_get_term(nativeObject);
//        }
//    }
}
