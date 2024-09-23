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

import com.vaticle.typedb.driver.api.database.Database;
import com.vaticle.typedb.driver.api.database.DatabaseManager;
import com.vaticle.typedb.driver.common.NativeIterator;
import com.vaticle.typedb.driver.common.exception.TypeDBDriverException;

import java.util.List;

import static com.vaticle.typedb.driver.common.exception.ErrorMessage.Driver.MISSING_DB_NAME;
import static com.vaticle.typedb.driver.jni.typedb_driver.databases_all;
import static com.vaticle.typedb.driver.jni.typedb_driver.databases_contains;
import static com.vaticle.typedb.driver.jni.typedb_driver.databases_create;
import static com.vaticle.typedb.driver.jni.typedb_driver.databases_get;
import static java.util.stream.Collectors.toList;

public class TypeDBDatabaseManagerImpl implements DatabaseManager {
    com.vaticle.typedb.driver.jni.TypeDBDriver nativeDriver;

    public TypeDBDatabaseManagerImpl(com.vaticle.typedb.driver.jni.TypeDBDriver driver) {
        nativeDriver = driver;
    }

    @Override
    public Database get(String name) throws Error {
        if (name == null || name.isEmpty()) throw new TypeDBDriverException(MISSING_DB_NAME);
        try {
            return new TypeDBDatabaseImpl(databases_get(nativeDriver, name));
        } catch (com.vaticle.typedb.driver.jni.Error e) {
            throw new TypeDBDriverException(e);
        }
    }

    @Override
    public boolean contains(String name) throws Error {
        if (name == null || name.isEmpty()) throw new TypeDBDriverException(MISSING_DB_NAME);
        try {
            return databases_contains(nativeDriver, name);
        } catch (com.vaticle.typedb.driver.jni.Error e) {
            throw new TypeDBDriverException(e);
        }
    }

    @Override
    public void create(String name) throws Error {
        if (name == null || name.isEmpty()) throw new TypeDBDriverException(MISSING_DB_NAME);
        try {
            databases_create(nativeDriver, name);
        } catch (com.vaticle.typedb.driver.jni.Error e) {
            throw new TypeDBDriverException(e);
        }
    }

    @Override
    public List<Database> all() {
        try {
            return new NativeIterator<>(databases_all(nativeDriver)).stream().map(TypeDBDatabaseImpl::new).collect(toList());
        } catch (com.vaticle.typedb.driver.jni.Error e) {
            throw new TypeDBDriverException(e);
        }
    }
}
