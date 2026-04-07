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

package com.typedb.driver.api;

import com.typedb.driver.api.database.DatabaseManager;
import com.typedb.driver.api.server.Server;
import com.typedb.driver.api.server.ServerVersion;
import com.typedb.driver.api.user.UserManager;
import com.typedb.driver.common.exception.TypeDBDriverException;

import javax.annotation.CheckReturnValue;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface Driver extends AutoCloseable {
    String LANGUAGE = "java";

    /**
     * Checks whether this connection is presently open.
     *
     * <h3>Examples</h3>
     * <pre>
     * driver.isOpen();
     * </pre>
     */
    @CheckReturnValue
    boolean isOpen();

    /**
     * Retrieves the server's version, using default automatic routing.
     * See {@link #serverVersion(ServerRouting)} for more details and options.
     *
     * <h3>Examples</h3>
     * <pre>
     * driver.serverVersion();
     * </pre>
     */
    @CheckReturnValue
    default ServerVersion serverVersion() {
        return serverVersion(null);
    }

    /**
     * Retrieves the server's version.
     *
     * <h3>Examples</h3>
     * <pre>
     * driver.serverVersion();
     * </pre>
     *
     * @param serverRouting The server routing to use for the operation
     */
    @CheckReturnValue
    ServerVersion serverVersion(ServerRouting serverRouting);

    /**
     * The <code>DatabaseManager</code> for this connection, providing access to database management methods.
     *
     * <h3>Examples</h3>
     * <pre>
     * driver.databases();
     * </pre>
     */
    @CheckReturnValue
    DatabaseManager databases();

    /**
     * The <code>UserManager</code> for this connection, providing access to user management methods.
     *
     * <h3>Examples</h3>
     * <pre>
     * driver.users();
     * </pre>
     */
    @CheckReturnValue
    UserManager users();

    /**
     * Opens a communication tunnel (transaction) to the given database on the running TypeDB server.
     *
     * <h3>Examples</h3>
     * <pre>
     * driver.transaction(database, sessionType);
     * </pre>
     *
     * @param database The name of the database with which the transaction connects
     * @param type     The type of transaction to be created (READ, WRITE, or SCHEMA)
     */
    @CheckReturnValue
    Transaction transaction(String database, Transaction.Type type) throws TypeDBDriverException;

    /**
     * Opens a communication tunnel (transaction) to the given database on the running TypeDB server.
     *
     * <h3>Examples</h3>
     * <pre>
     * driver.transaction(database, sessionType);
     * </pre>
     *
     * @param database The name of the database with which the transaction connects
     * @param type     The type of transaction to be created (READ, WRITE, or SCHEMA)
     * @param options  <code>TransactionOptions</code> to configure the opened transaction
     */
    @CheckReturnValue
    Transaction transaction(String database, Transaction.Type type, TransactionOptions options);

    /**
     * Set of servers for this driver connection, using default automatic routing.
     * See {@link #servers(ServerRouting)} for more details and options.
     *
     * <h3>Examples</h3>
     * <pre>
     * driver.servers();
     * </pre>
     */
    @CheckReturnValue
    default Set<? extends Server> servers() {
        return servers(null);
    }

    /**
     * Set of servers for this driver connection.
     *
     * <h3>Examples</h3>
     * <pre>
     * driver.servers(new ServerRouting.Auto());
     * </pre>
     *
     * @param serverRouting The server routing to use for the operation
     */
    @CheckReturnValue
    Set<? extends Server> servers(ServerRouting serverRouting);

    /**
     * Returns the primary server for this driver connection, using default automatic routing.
     * See {@link #primaryServer(ServerRouting)} for more details and options.
     *
     * <h3>Examples</h3>
     * <pre>
     * driver.primaryServer();
     * </pre>
     */
    @CheckReturnValue
    default Optional<? extends Server> primaryServer() {
        return primaryServer(null);
    }

    /**
     * Returns the primary server for this driver connection.
     *
     * <h3>Examples</h3>
     * <pre>
     * driver.primaryServer(new ServerRouting.Auto());
     * </pre>
     *
     * @param serverRouting The server routing to use for the operation
     */
    @CheckReturnValue
    Optional<? extends Server> primaryServer(ServerRouting serverRouting);

    /**
     * Registers a new server in the cluster the driver is currently connected to. The registered
     * server will become available eventually, depending on the behavior of the whole cluster.
     * To register a server, its clustering address should be passed, not the connection address.
     *
     * <h3>Examples</h3>
     * <pre>
     * driver.registerServer(2, "127.0.0.1:11729");
     * </pre>
     *
     * @param serverID The numeric identifier of the new server
     * @param address  The address(es) of the TypeDB server as a string
     */
    void registerServer(long serverID, String address);

    /**
     * Deregisters a server from the cluster the driver is currently connected to. This server
     * will no longer play a raft role in this cluster.
     *
     * <h3>Examples</h3>
     * <pre>
     * driver.deregisterServer(2);
     * </pre>
     *
     * @param serverID The numeric identifier of the deregistered server
     */
    void deregisterServer(long serverID);

    /**
     * Closes the driver. Before instantiating a new driver, the driver that's currently open should first be closed.
     *
     * <h3>Examples</h3>
     * <pre>
     * driver.close();
     * </pre>
     */
    void close();
}
