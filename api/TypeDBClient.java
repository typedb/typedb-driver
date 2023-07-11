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

package com.vaticle.typedb.client.api;

import com.vaticle.typedb.client.api.database.DatabaseManager;
import com.vaticle.typedb.client.api.user.User;
import com.vaticle.typedb.client.api.user.UserManager;

import javax.annotation.CheckReturnValue;

public interface TypeDBClient extends AutoCloseable {
    @CheckReturnValue
    boolean isOpen();

    @CheckReturnValue
    DatabaseManager databases();

    @CheckReturnValue
    TypeDBSession session(String database, TypeDBSession.Type type);

    @CheckReturnValue
    TypeDBSession session(String database, TypeDBSession.Type type, TypeDBOptions options);

    void close();

    @CheckReturnValue
    User user();

    @CheckReturnValue
    UserManager users();
}
