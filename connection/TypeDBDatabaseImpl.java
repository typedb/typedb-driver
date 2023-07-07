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

import static com.vaticle.typedb.client.jni.typedb_client.database_delete;
import static com.vaticle.typedb.client.jni.typedb_client.database_get_name;
import static com.vaticle.typedb.client.jni.typedb_client.database_rule_schema;
import static com.vaticle.typedb.client.jni.typedb_client.database_schema;
import static com.vaticle.typedb.client.jni.typedb_client.database_type_schema;

public class TypeDBDatabaseImpl implements Database {

    private final String name;
    final com.vaticle.typedb.client.jni.Database database;

    public TypeDBDatabaseImpl(com.vaticle.typedb.client.jni.Database database) {
        this.database = database;
        this.name = database_get_name(database);
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String schema() {
        return database_schema(database);
    }

    @Override
    public String typeSchema() {
        return database_type_schema(database);
    }

    @Override
    public String ruleSchema() {
        return database_rule_schema(database);
    }

    @Override
    public void delete() {
        database_delete(database);
    }

    @Override
    public String toString() {
        return name;
    }
}
