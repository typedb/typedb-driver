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
import com.typedb.driver.api.server.ServerReplica;
import com.typedb.driver.api.server.ServerVersion;
import com.typedb.driver.api.user.UserManager;
import com.typedb.driver.common.exception.TypeDBDriverException;

import javax.annotation.CheckReturnValue;
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
     * Retrieves the server's version.
     *
     * <h3>Examples</h3>
     * <pre>
     * driver.serverVersion();
     * </pre>
     */
    @CheckReturnValue
    ServerVersion serverVersion();

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
     * Closes the driver. Before instantiating a new driver, the driver that’s currently open should first be closed.
     *
     * <h3>Examples</h3>
     * <pre>
     * driver.close();
     * </pre>
     */
    void close();

    /**
     * Set of <code>Replica</code> instances for this driver connection.
     *
     * <h3>Examples</h3>
     * <pre>
     * driver.replicas();
     * </pre>
     */
    @CheckReturnValue
    Set<? extends ServerReplica> replicas();

    /**
     * Returns the primary replica for this driver connection.
     *
     * <h3>Examples</h3>
     * <pre>
     * driver.primaryReplica();
     * </pre>
     */
    @CheckReturnValue
    Optional<? extends ServerReplica> primaryReplica();

    /**
     * Registers a new replica in the cluster the driver is currently connected to. The registered
     * replica will become available eventually, depending on the behavior of the whole cluster.
     *
     * <h3>Examples</h3>
     * <pre>
     * driver.registerReplica(2, "127.0.0.1:11729");
     * </pre>
     *
     * @param replicaID The numeric identifier of the new replica
     * @param address   The address(es) of the TypeDB replica as a string
     */
    void registerReplica(long replicaID, String address);

    /**
     * Deregisters a replica from the cluster the driver is currently connected to. This replica
     * will no longer play a raft role in this cluster.
     *
     * <h3>Examples</h3>
     * <pre>
     * driver.deregisterReplica(2);
     * </pre>
     *
     * @param replicaID The numeric identifier of the deregistered replica
     */
    void deregisterReplica(long replicaID);
}
