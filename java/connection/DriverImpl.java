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

import com.typedb.driver.api.DriverOptions;
import com.typedb.driver.api.Credentials;
import com.typedb.driver.api.Driver;
import com.typedb.driver.api.Transaction;
import com.typedb.driver.api.database.DatabaseManager;
import com.typedb.driver.api.user.UserManager;
import com.typedb.driver.common.NativeObject;
import com.typedb.driver.common.exception.TypeDBDriverException;
import com.typedb.driver.user.UserManagerImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.typedb.driver.jni.typedb_driver.driver_force_close;
import static com.typedb.driver.jni.typedb_driver.driver_is_open;
import static com.typedb.driver.jni.typedb_driver.driver_open_cluster;
import static com.typedb.driver.jni.typedb_driver.driver_open_cluster_translated;
import static com.typedb.driver.jni.typedb_driver.driver_open_core;

public class DriverImpl extends NativeObject<com.typedb.driver.jni.TypeDBDriver> implements Driver {

    public DriverImpl(String address, Credentials credentials, DriverOptions driverOptions) throws TypeDBDriverException {
        this(openCore(address, credentials, driverOptions));
    }

    public DriverImpl(Set<String> initAddresses, Credentials credentials, DriverOptions driverOptions) throws TypeDBDriverException {
        this(openCloud(initAddresses, credentials, driverOptions));
    }

    public DriverImpl(Map<String, String> addressTranslation, Credentials credentials, DriverOptions driverOptions) throws TypeDBDriverException {
        this(openCloud(addressTranslation, credentials, driverOptions));
    }

    private DriverImpl(com.typedb.driver.jni.TypeDBDriver connection) {
        super(connection);
    }

    private static com.typedb.driver.jni.TypeDBDriver openCore(String address, Credentials credentials, DriverOptions driverOptions) {
        try {
            return driver_open_core(address, credentials.nativeObject, driverOptions.nativeObject, LANGUAGE);
        } catch (com.typedb.driver.jni.Error e) {
            throw new TypeDBDriverException(e);
        }
    }

    private static com.typedb.driver.jni.TypeDBDriver openCloud(Set<String> initAddresses, Credentials credentials, DriverOptions driverOptions) {
        try {
            return driver_open_cluster(initAddresses.toArray(new String[0]), credentials.nativeObject, driverOptions.nativeObject, LANGUAGE);
        } catch (com.typedb.driver.jni.Error e) {
            throw new TypeDBDriverException(e);
        }
    }

    private static com.typedb.driver.jni.TypeDBDriver openCloud(Map<String, String> addressTranslation, Credentials credentials, DriverOptions driverOptions) {
        try {
            List<String> publicAddresses = new ArrayList();
            List<String> privateAddresses = new ArrayList();
            for (Map.Entry<String, String> entry : addressTranslation.entrySet()) {
                publicAddresses.add(entry.getKey());
                privateAddresses.add(entry.getValue());
            }
            return driver_open_cluster_translated(
                    publicAddresses.toArray(new String[0]),
                    privateAddresses.toArray(new String[0]),
                    credentials.nativeObject,
                    driverOptions.nativeObject,
                    LANGUAGE
            );
        } catch (com.typedb.driver.jni.Error e) {
            throw new TypeDBDriverException(e);
        }
    }

    @Override
    public boolean isOpen() {
        return driver_is_open(nativeObject);
    }

    @Override
    public UserManager users() {
        return new UserManagerImpl(nativeObject);
    }

    @Override
    public DatabaseManager databases() {
        return new DatabaseManagerImpl(nativeObject);
    }

    @Override
    public Transaction transaction(String database, Transaction.Type type) throws TypeDBDriverException {
        return new TransactionImpl(this, database, type/*, options*/);
    }

    @Override
    public void close() {
        try {
            driver_force_close(nativeObject);
        } catch (com.typedb.driver.jni.Error error) {
            throw new TypeDBDriverException(error);
        }
    }
}
