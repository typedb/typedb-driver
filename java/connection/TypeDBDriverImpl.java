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

package com.vaticle.typedb.driver.connection;

import com.vaticle.typedb.driver.api.TypeDBDriver;
import com.vaticle.typedb.driver.api.TypeDBTransaction;
import com.vaticle.typedb.driver.api.database.DatabaseManager;
import com.vaticle.typedb.driver.common.NativeObject;
import com.vaticle.typedb.driver.common.exception.TypeDBDriverException;

import static com.vaticle.typedb.driver.jni.typedb_driver.connection_force_close;
import static com.vaticle.typedb.driver.jni.typedb_driver.connection_is_open;
import static com.vaticle.typedb.driver.jni.typedb_driver.connection_open_core;

public class TypeDBDriverImpl extends NativeObject<com.vaticle.typedb.driver.jni.TypeDBDriver> implements TypeDBDriver {

    public TypeDBDriverImpl(String address) throws TypeDBDriverException {
        this(openCore(address));
    }

//    public TypeDBDriverImpl(Set<String> initAddresses, TypeDBCredential credential) throws TypeDBDriverException {
//        this(openCloud(initAddresses, credential));
//    }
//
//    public TypeDBDriverImpl(Map<String, String> addressTranslation, TypeDBCredential credential) throws TypeDBDriverException {
//        this(openCloud(addressTranslation, credential));
//    }

    private TypeDBDriverImpl(com.vaticle.typedb.driver.jni.TypeDBDriver connection) {
        super(connection);
    }

    private static com.vaticle.typedb.driver.jni.TypeDBDriver openCore(String address) {
        try {
            return connection_open_core(address);
        } catch (com.vaticle.typedb.driver.jni.Error e) {
            throw new TypeDBDriverException(e);
        }
    }

//    private static com.vaticle.typedb.driver.jni.TypeDBDriver openCloud(Set<String> initAddresses, TypeDBCredential credential) {
//        try {
//            return connection_open_cloud(initAddresses.toArray(new String[0]), credential.nativeObject);
//        } catch (com.vaticle.typedb.driver.jni.Error e) {
//            throw new TypeDBDriverException(e);
//        }
//    }

//    private static com.vaticle.typedb.driver.jni.TypeDBDriver openCloud(Map<String, String> addressTranslation, TypeDBCredential credential) {
//        try {
//            List<String> publicAddresses = new ArrayList();
//            List<String> privateAddresses = new ArrayList();
//            for (Map.Entry<String, String> entry: addressTranslation.entrySet()) {
//                publicAddresses.add(entry.getKey());
//                privateAddresses.add(entry.getValue());
//            }
//            return connection_open_cloud_translated(
//                publicAddresses.toArray(new String[0]),
//                privateAddresses.toArray(new String[0]),
//                credential.nativeObject
//            );
//        } catch (com.vaticle.typedb.driver.jni.Error e) {
//            throw new TypeDBDriverException(e);
//        }
//    }

    @Override
    public boolean isOpen() {
        return connection_is_open(nativeObject);
    }

//    @Override
//    public User user() {
//        return users().getCurrentUser();
//    }
//
//    @Override
//    public UserManager users() {
//        return new TypeDBUserManagerImpl(nativeObject);
//    }

    @Override
    public DatabaseManager databases() {
        return new TypeDBDatabaseManagerImpl(nativeObject);
    }

    @Override
    public TypeDBTransaction transaction(String database, TypeDBTransaction.Type type) {
        return new TypeDBTransactionImpl(this, database, type/*, options*/);
    }

    @Override
    public void close() {
        if (!isOpen()) return;
        try {
            connection_force_close(nativeObject);
        } catch (com.vaticle.typedb.driver.jni.Error error) {
            throw new TypeDBDriverException(error);
        }
    }
}
