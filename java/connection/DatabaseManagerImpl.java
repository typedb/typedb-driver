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
import com.typedb.driver.api.database.DatabaseManager;
import com.typedb.driver.common.NativeIterator;
import com.typedb.driver.common.exception.TypeDBDriverException;

import java.util.List;

import static com.typedb.driver.jni.typedb_driver.databases_all;
import static com.typedb.driver.jni.typedb_driver.databases_contains;
import static com.typedb.driver.jni.typedb_driver.databases_create;
import static com.typedb.driver.jni.typedb_driver.databases_get;
import static java.util.stream.Collectors.toList;

public class DatabaseManagerImpl implements DatabaseManager {
    com.typedb.driver.jni.TypeDBDriver nativeDriver;

    public DatabaseManagerImpl(com.typedb.driver.jni.TypeDBDriver driver) {
        nativeDriver = driver;
    }

    @Override
    public Database get(String name) throws Error {
        try {
            return new DatabaseImpl(databases_get(nativeDriver, name));
        } catch (com.typedb.driver.jni.Error e) {
            throw new TypeDBDriverException(e);
        }
    }

    @Override
    public boolean contains(String name) throws Error {
        try {
            return databases_contains(nativeDriver, name);
        } catch (com.typedb.driver.jni.Error e) {
            throw new TypeDBDriverException(e);
        }
    }

    @Override
    public void create(String name) throws Error {
        try {
            databases_create(nativeDriver, name);
        } catch (com.typedb.driver.jni.Error e) {
            throw new TypeDBDriverException(e);
        }
    }

    @Override
    public List<Database> all() {
        try {
            return new NativeIterator<>(databases_all(nativeDriver)).stream().map(DatabaseImpl::new).collect(toList());
        } catch (com.typedb.driver.jni.Error e) {
            throw new TypeDBDriverException(e);
        }
    }
}
