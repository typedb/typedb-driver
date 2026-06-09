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

import com.typedb.driver.api.*;
import com.typedb.driver.api.concept.Concept;
import com.typedb.driver.api.database.DatabaseManager;
import com.typedb.driver.api.server.Server;
import com.typedb.driver.api.server.ServerVersion;
import com.typedb.driver.api.user.UserManager;
import com.typedb.driver.common.NativeIterator;
import com.typedb.driver.common.NativeObject;
import com.typedb.driver.common.Validator;
import com.typedb.driver.common.exception.TypeDBDriverException;
import com.typedb.driver.concept.ConceptImpl;
import com.typedb.driver.user.UserManagerImpl;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.typedb.driver.jni.typedb_driver.driver_force_close;
import static com.typedb.driver.jni.typedb_driver.driver_is_open;
import static com.typedb.driver.jni.typedb_driver.driver_new;
import static com.typedb.driver.jni.typedb_driver.driver_new_with_addresses;
import static com.typedb.driver.jni.typedb_driver.driver_new_with_address_translation;
import static com.typedb.driver.jni.typedb_driver.driver_primary_server;
import static com.typedb.driver.jni.typedb_driver.driver_servers;
import static com.typedb.driver.jni.typedb_driver.driver_server_version;
import static com.typedb.driver.jni.typedb_driver.given_rows_builder_new;
import static com.typedb.driver.jni.typedb_driver.given_rows_builder_finish;
import static com.typedb.driver.jni.typedb_driver.given_rows_builder_commit_row;
import static com.typedb.driver.jni.typedb_driver.given_rows_builder_start_new_row;
import static com.typedb.driver.jni.typedb_driver.given_rows_builder_set_index_to_concept;
import static com.typedb.driver.jni.typedb_driver.given_rows_builder_set_index_to_empty;
import static com.typedb.driver.jni.typedb_driver.given_rows_builder_set_variable_to_concept;
import static com.typedb.driver.jni.typedb_driver.given_rows_builder_set_variable_to_empty;
import static com.typedb.driver.jni.typedb_driver.given_rows_header_builder_new;
import static com.typedb.driver.jni.typedb_driver.given_rows_header_builder_push;
import static com.typedb.driver.jni.typedb_driver.given_rows_header_builder_finish;

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
            return driver_new(address, credentials.nativeObject, driverOptions.nativeObject, LANGUAGE);
        } catch (com.typedb.driver.jni.Error e) {
            throw new TypeDBDriverException(e);
        }
    }

    private static com.typedb.driver.jni.TypeDBDriver open(Set<String> addresses, Credentials credentials, DriverOptions driverOptions) {
        Validator.requireNonNull(addresses, "addresses");
        Validator.requireNonNull(credentials, "credentials");
        Validator.requireNonNull(driverOptions, "driverOptions");
        try {
            return driver_new_with_addresses(addresses.toArray(new String[0]), credentials.nativeObject, driverOptions.nativeObject, LANGUAGE);
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
            return driver_new_with_address_translation(addresses.getKey(), addresses.getValue(), credentials.nativeObject, driverOptions.nativeObject, LANGUAGE);
        } catch (com.typedb.driver.jni.Error e) {
            throw new TypeDBDriverException(e);
        }
    }

    @Override
    public GivenRows buildGivenRowsFrom(List<? extends Map<String, Optional<? extends Concept>>> givenRows) {
        try{
            Set<String> variables= new HashSet<>();
            givenRows.forEach(row -> variables.addAll(row.keySet()));
            com.typedb.driver.jni.GivenRowsHeaderBuilder headerBuilder = given_rows_header_builder_new(variables.size());
            for (String variable : variables) {
                given_rows_header_builder_push(headerBuilder, variable);
            }
            com.typedb.driver.jni.GivenRowsHeader header = given_rows_header_builder_finish(headerBuilder.released());
            com.typedb.driver.jni.GivenRowsBuilder rowsBuilder = given_rows_builder_new(header, givenRows.size());
            for (Map<String, Optional<? extends Concept>> row : givenRows) {
                given_rows_builder_start_new_row(rowsBuilder);
                for (Map.Entry<String, Optional<? extends Concept>> variableAndEntry : row.entrySet()) {
                    if (variableAndEntry.getValue().isEmpty()) {
                        given_rows_builder_set_variable_to_empty(rowsBuilder, variableAndEntry.getKey());
                    } else {
                        Concept concept = variableAndEntry.getValue().get();
                        given_rows_builder_set_variable_to_concept(rowsBuilder, variableAndEntry.getKey(), ((ConceptImpl) concept).nativeObject);
                    }
                }
                given_rows_builder_commit_row(rowsBuilder);
            }
            return new GivenRowsImpl(given_rows_builder_finish(rowsBuilder.released()));
        } catch (com.typedb.driver.jni.Error e) {
            throw new TypeDBDriverException(e);
        }
    }

    @Override
    public GivenRows buildGivenRowsFrom(List<String> variables, List<? extends List<Optional<? extends Concept>>> rows) throws TypeDBDriverException {
        try{
            com.typedb.driver.jni.GivenRowsHeaderBuilder headerBuilder = given_rows_header_builder_new(variables.size());
            for (String variable : variables) {
                given_rows_header_builder_push(headerBuilder, variable);
            }
            com.typedb.driver.jni.GivenRowsHeader header = given_rows_header_builder_finish(headerBuilder.released());

            com.typedb.driver.jni.GivenRowsBuilder rowsBuilder = given_rows_builder_new(header, rows.size());
            for (List<Optional<? extends Concept>> row : rows) {
                given_rows_builder_start_new_row(rowsBuilder);
                int colIndex = 0;
                for (Optional<? extends Concept> entry : row) {
                    if (entry.isEmpty()) {
                        given_rows_builder_set_index_to_empty(rowsBuilder, colIndex);
                    } else {
                        Concept concept = entry.get();
                        given_rows_builder_set_index_to_concept(rowsBuilder, colIndex, ((ConceptImpl) concept).nativeObject);
                    }
                    colIndex += 1;
                }
                given_rows_builder_commit_row(rowsBuilder);
            }
            return new GivenRowsImpl(given_rows_builder_finish(rowsBuilder.released()));
        } catch (com.typedb.driver.jni.Error e) {
            throw new TypeDBDriverException(e);
        }
    }

    @Override
    public boolean isOpen() {
        return driver_is_open(nativeObject);
    }

    @Override
    public ServerVersion serverVersion(ServerRouting serverRouting) {
        try {
            return new ServerVersion(driver_server_version(nativeObject, ServerRouting.nativeValue(serverRouting)));
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
    public Set<? extends Server> servers(ServerRouting serverRouting) {
        try {

            return new NativeIterator<>(driver_servers(nativeObject, ServerRouting.nativeValue(serverRouting)))
                    .stream().map(ServerImpl::new).collect(toSet());
        } catch (com.typedb.driver.jni.Error error) {
            throw new TypeDBDriverException(error);
        }
    }

    @Override
    public Optional<? extends Server> primaryServer(ServerRouting serverRouting) {
        try {
            com.typedb.driver.jni.Server nativeServer = driver_primary_server(nativeObject, ServerRouting.nativeValue(serverRouting));
            if (nativeServer != null) {
                return Optional.of(new ServerImpl(nativeServer));
            }
            return Optional.empty();
        } catch (com.typedb.driver.jni.Error error) {
            throw new TypeDBDriverException(error);
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
