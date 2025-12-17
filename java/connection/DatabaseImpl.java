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
import com.typedb.driver.api.database.Database;
import com.typedb.driver.common.NativeObject;
import com.typedb.driver.common.Validator;
import com.typedb.driver.common.exception.TypeDBDriverException;

import static com.typedb.driver.common.exception.ErrorMessage.Driver.DATABASE_DELETED;
import static com.typedb.driver.jni.typedb_driver.database_delete;
import static com.typedb.driver.jni.typedb_driver.database_export_to_file;
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
    public String schema(ConsistencyLevel consistencyLevel) throws TypeDBDriverException {
        if (!nativeObject.isOwned()) throw new TypeDBDriverException(DATABASE_DELETED);
        try {
            return database_schema(nativeObject, ConsistencyLevel.nativeValue(consistencyLevel));
        } catch (com.typedb.driver.jni.Error e) {
            throw new TypeDBDriverException(e);
        }
    }

    @Override
    public String typeSchema(ConsistencyLevel consistencyLevel) throws TypeDBDriverException {
        if (!nativeObject.isOwned()) throw new TypeDBDriverException(DATABASE_DELETED);
        try {
            return database_type_schema(nativeObject, ConsistencyLevel.nativeValue(consistencyLevel));
        } catch (com.typedb.driver.jni.Error e) {
            throw new TypeDBDriverException(e);
        }
    }

    @Override
    public void exportToFile(String schemaFilePath, String dataFilePath, ConsistencyLevel consistencyLevel) throws TypeDBDriverException {
        Validator.requireNonNull(schemaFilePath, "schemaFilePath");
        Validator.requireNonNull(dataFilePath, "dataFilePath");
        try {
            database_export_to_file(nativeObject, schemaFilePath, dataFilePath, ConsistencyLevel.nativeValue(consistencyLevel));
        } catch (com.typedb.driver.jni.Error e) {
            throw new TypeDBDriverException(e);
        }
    }

    @Override
    public void delete(ConsistencyLevel consistencyLevel) throws TypeDBDriverException {
        if (!nativeObject.isOwned()) throw new TypeDBDriverException(DATABASE_DELETED);
        try {
            // NOTE: .released() relinquishes ownership of the native object to the Rust side
            database_delete(nativeObject.released(), ConsistencyLevel.nativeValue(consistencyLevel));
        } catch (com.typedb.driver.jni.Error e) {
            throw new TypeDBDriverException(e);
        }
    }

    @Override
    public String toString() {
        return name();
    }
}
