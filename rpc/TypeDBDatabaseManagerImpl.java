/*
 * Copyright (C) 2021 Vaticle
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

package com.vaticle.typedb.client.rpc;

import com.vaticle.typedb.client.api.database.Database;
import com.vaticle.typedb.client.api.database.DatabaseManager;
import com.vaticle.typedb.client.common.exception.TypeDBClientException;
import com.vaticle.typedb.client.common.rpc.TypeDBStub;

import java.util.List;

import static com.vaticle.typedb.client.common.exception.ErrorMessage.Client.DB_DOES_NOT_EXIST;
import static com.vaticle.typedb.client.common.exception.ErrorMessage.Client.MISSING_DB_NAME;
import static com.vaticle.typedb.client.common.rpc.RequestBuilder.Core.DatabaseManager.allReq;
import static com.vaticle.typedb.client.common.rpc.RequestBuilder.Core.DatabaseManager.containsReq;
import static com.vaticle.typedb.client.common.rpc.RequestBuilder.Core.DatabaseManager.createReq;
import static java.util.stream.Collectors.toList;

public class TypeDBDatabaseManagerImpl implements DatabaseManager {

    private final TypeDBClientAbstract client;

    public TypeDBDatabaseManagerImpl(TypeDBClientAbstract client) {
        this.client = client;
    }

    @Override
    public Database get(String name) {
        if (contains(name)) return new TypeDBDatabaseImpl(this, name);
        else throw new TypeDBClientException(DB_DOES_NOT_EXIST, name);
    }

    @Override
    public boolean contains(String name) {
        return stub().databasesContains(containsReq(nonNull(name))).getContains();
    }

    @Override
    public void create(String name) {
        stub().databasesCreate(createReq(nonNull(name)));
    }

    @Override
    public List<TypeDBDatabaseImpl> all() {
        List<String> databases = stub().databasesAll(allReq()).getNamesList();
        return databases.stream().map(name -> new TypeDBDatabaseImpl(this, name)).collect(toList());
    }

    TypeDBStub stub() {
        return client.stub();
    }

    static String nonNull(String name) {
        if (name == null) throw new TypeDBClientException(MISSING_DB_NAME);
        return name;
    }
}
