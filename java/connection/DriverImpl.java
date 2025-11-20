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

import com.typedb.driver.api.ConsistencyLevel;
import com.typedb.driver.api.Credentials;
import com.typedb.driver.api.Driver;
import com.typedb.driver.api.DriverOptions;
import com.typedb.driver.api.Transaction;
import com.typedb.driver.api.TransactionOptions;
import com.typedb.driver.api.database.DatabaseManager;
import com.typedb.driver.api.server.ServerReplica;
import com.typedb.driver.api.server.ServerVersion;
import com.typedb.driver.api.user.UserManager;
import com.typedb.driver.common.NativeIterator;
import com.typedb.driver.common.NativeObject;
import com.typedb.driver.common.Validator;
import com.typedb.driver.common.exception.TypeDBDriverException;
import com.typedb.driver.user.UserManagerImpl;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.typedb.driver.jni.typedb_driver.driver_deregister_replica;
import static com.typedb.driver.jni.typedb_driver.driver_force_close;
import static com.typedb.driver.jni.typedb_driver.driver_is_open;
import static com.typedb.driver.jni.typedb_driver.driver_new_with_address_translation_with_description;
import static com.typedb.driver.jni.typedb_driver.driver_new_with_addresses_with_description;
import static com.typedb.driver.jni.typedb_driver.driver_new_with_description;
import static com.typedb.driver.jni.typedb_driver.driver_primary_replica;
import static com.typedb.driver.jni.typedb_driver.driver_register_replica;
import static com.typedb.driver.jni.typedb_driver.driver_replicas;
import static com.typedb.driver.jni.typedb_driver.driver_server_version;
import static com.typedb.driver.jni.typedb_driver.driver_update_address_translation;
import static java.util.stream.Collectors.toSet;

public class DriverImpl extends NativeObject<com.typedb.driver.jni.TypeDBDriver> implements Driver {

    public DriverImpl(String address, Credentials credentials, DriverOptions driverOptions) throws TypeDBDriverException {
        this(open(address, credentials, driverOptions));
    }

    public DriverImpl(Set<String> addresses, Credentials credentials, DriverOptions driverOptions) throws TypeDBDriverException {
        this(open(addresses, credentials, driverOptions));
    }

    public DriverImpl(Map<String, String> addressTranslation, Credentials credentials, DriverOptions driverOptions) throws TypeDBDriverException {
        this(open(addressTranslation, credentials, driverOptions));
    }

    private DriverImpl(com.typedb.driver.jni.TypeDBDriver connection) {
        super(connection);
    }

    private static com.typedb.driver.jni.TypeDBDriver open(String address, Credentials credentials, DriverOptions driverOptions) {
        Validator.requireNonNull(address, "address");
        Validator.requireNonNull(credentials, "credentials");
        Validator.requireNonNull(driverOptions, "driverOptions");
        try {
            return driver_new_with_description(address, credentials.nativeObject, driverOptions.nativeObject, LANGUAGE);
        } catch (com.typedb.driver.jni.Error e) {
            throw new TypeDBDriverException(e);
        }
    }

    private static com.typedb.driver.jni.TypeDBDriver open(Set<String> addresses, Credentials credentials, DriverOptions driverOptions) {
        Validator.requireNonNull(addresses, "addresses");
        Validator.requireNonNull(credentials, "credentials");
        Validator.requireNonNull(driverOptions, "driverOptions");
        try {
            return driver_new_with_addresses_with_description(addresses.toArray(new String[0]), credentials.nativeObject, driverOptions.nativeObject, LANGUAGE);
        } catch (com.typedb.driver.jni.Error e) {
            throw new TypeDBDriverException(e);
        }
    }

    private static com.typedb.driver.jni.TypeDBDriver open(Map<String, String> addressTranslation, Credentials credentials, DriverOptions driverOptions) {
        Validator.requireNonNull(addressTranslation, "addressTranslation");
        Validator.requireNonNull(credentials, "credentials");
        Validator.requireNonNull(driverOptions, "driverOptions");
        try {
            Map.Entry<String[], String[]> addresses = getTranslatedAddresses(addressTranslation);
            return driver_new_with_address_translation_with_description(addresses.getKey(), addresses.getValue(), credentials.nativeObject, driverOptions.nativeObject, LANGUAGE);
        } catch (com.typedb.driver.jni.Error e) {
            throw new TypeDBDriverException(e);
        }
    }

    @Override
    public boolean isOpen() {
        return driver_is_open(nativeObject);
    }

    @Override
    public ServerVersion serverVersion(ConsistencyLevel consistencyLevel) {
        try {
            return new ServerVersion(driver_server_version(nativeObject, ConsistencyLevel.nativeValue(consistencyLevel)));
        } catch (com.typedb.driver.jni.Error e) {
            throw new TypeDBDriverException(e);
        }
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
        return transaction(database, type, new TransactionOptions());
    }

    @Override
    public Transaction transaction(String database, Transaction.Type type, TransactionOptions options) throws TypeDBDriverException {
        Validator.requireNonNull(database, "database");
        Validator.requireNonNull(type, "type");
        return new TransactionImpl(this, database, type, options);
    }

    @Override
    public Set<? extends ServerReplica> replicas() {
        try {
            return new NativeIterator<>(driver_replicas(nativeObject)).stream().map(ServerReplicaImpl::new).collect(toSet());
        } catch (com.typedb.driver.jni.Error error) {
            throw new TypeDBDriverException(error);
        }
    }

    @Override
    public Optional<? extends ServerReplica> primaryReplica() {
        com.typedb.driver.jni.ServerReplica nativeReplica = driver_primary_replica(nativeObject);
        if (nativeReplica != null) {
            return Optional.of(new ServerReplicaImpl(nativeReplica));
        }
        return Optional.empty();
    }

    @Override
    public void registerReplica(long replicaID, String address) {
        try {
            driver_register_replica(nativeObject, replicaID, address);
        } catch (com.typedb.driver.jni.Error error) {
            throw new TypeDBDriverException(error);
        }
    }

    @Override
    public void deregisterReplica(long replicaID) {
        try {
            driver_deregister_replica(nativeObject, replicaID);
        } catch (com.typedb.driver.jni.Error error) {
            throw new TypeDBDriverException(error);
        }
    }

    @Override
    public void updateAddressTranslation(Map<String, String> addressTranslation) {
        Validator.requireNonNull(addressTranslation, "addressTranslation");
        try {
            Map.Entry<String[], String[]> addresses = getTranslatedAddresses(addressTranslation);
            driver_update_address_translation(nativeObject, addresses.getKey(), addresses.getValue());
        } catch (com.typedb.driver.jni.Error e) {
            throw new TypeDBDriverException(e);
        }
    }

    @Override
    public void close() {
        try {
            driver_force_close(nativeObject);
        } catch (com.typedb.driver.jni.Error error) {
            throw new TypeDBDriverException(error);
        }
    }

    public static Map.Entry<String[], String[]> getTranslatedAddresses(Map<String, String> addressTranslation) {
        List<String> publicAddresses = new ArrayList<>();
        List<String> privateAddresses = new ArrayList<>();

        for (Map.Entry<String, String> entry : addressTranslation.entrySet()) {
            publicAddresses.add(entry.getKey());
            privateAddresses.add(entry.getValue());
        }

        return new AbstractMap.SimpleEntry<>(publicAddresses.toArray(new String[0]), privateAddresses.toArray(new String[0]));
    }
}
