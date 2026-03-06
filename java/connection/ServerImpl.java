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

import com.typedb.driver.api.server.ReplicationRole;
import com.typedb.driver.api.server.Server;
import com.typedb.driver.common.NativeObject;

import java.util.Optional;

import static com.typedb.driver.jni.typedb_driver.server_get_address;
import static com.typedb.driver.jni.typedb_driver.server_get_id;
import static com.typedb.driver.jni.typedb_driver.server_is_primary;
import static com.typedb.driver.jni.typedb_driver.server_has_term;
import static com.typedb.driver.jni.typedb_driver.server_has_role;
import static com.typedb.driver.jni.typedb_driver.server_get_term;
import static com.typedb.driver.jni.typedb_driver.server_get_role;

public class ServerImpl extends NativeObject<com.typedb.driver.jni.Server> implements Server {
    public ServerImpl(com.typedb.driver.jni.Server server) {
        super(server);
    }

    @Override
    public long getID() {
        return server_get_id(nativeObject);
    }

    @Override
    public String getAddress() {
        return server_get_address(nativeObject);
    }

    @Override
    public Optional<ReplicationRole> getRole() {
        if (server_has_role(nativeObject)) {
            return Optional.of(ReplicationRole.of(server_get_role(nativeObject)));
        }
        return Optional.empty();
    }

    @Override
    public Boolean isPrimary() {
        return server_is_primary(nativeObject);
    }

    @Override
    public Optional<Long> getTerm() {
        if (server_has_term(nativeObject)) {
            return Optional.of(server_get_term(nativeObject));
        }
        return Optional.empty();
    }

    @Override
    public String toString() {
        return getAddress();
    }
}
