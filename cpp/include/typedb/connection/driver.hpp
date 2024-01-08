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
#pragma once

#include "typedb/common/native.hpp"
#include "typedb/connection/database_manager.hpp"
#include "typedb/connection/session.hpp"
#include "typedb/user/user_manager.hpp"

#include <string>

// The namespace comment is needed to document enums.
/**
 * \brief Namespace encapsulating everything in TypeDB
 */
namespace TypeDB {

class Credential;

/**
 * \brief A connection to a TypeDB server which serves as the starting point for all interaction.
 */
class Driver {
private:
    NativePointer<_native::Connection> connectionNative;  // Remains on top for construction order

public:
    /**
     * The <code>DatabaseManager</code> for this connection, providing access to database management methods.
     */
    DatabaseManager databases;

    /**
     * The <code>UserManager</code> instance for this connection, providing access to user management methods.
     * Only for TypeDB Cloud.
     */
    UserManager users;

    /**
     * Enables logging in the TypeDB driver.
     *
     * <h3>Examples</h3>
     * <pre>
     * Driver::initLogging();
     * </pre>
     */
    static void initLogging();

    /**
     * Open a TypeDB Driver to a TypeDB Core server available at the provided address.
     *
     * <h3>Examples</h3>
     * <pre>
     * Driver::coreDriver(address);
     * </pre>
     *
     * @param address The address of the TypeDB server
     */
    static Driver coreDriver(const std::string& coreAddress);

    /**
     * Open a TypeDB Driver to TypeDB Cloud server(s) available at the provided addresses, using
     * the provided credential.
     *
     * <h3>Examples</h3>
     * <pre>
     * Driver::cloudDriver(addresses, credential);
     * </pre>
     *
     * @param addresses The address(es) of the TypeDB server(s)
     * @param credential The Credential to connect with
     */
    static Driver cloudDriver(const std::vector<std::string>& cloudAddresses, const Credential& credential);

    Driver(const Driver&) = delete;
    Driver(Driver&& from) = default;
    ~Driver() = default;

    Driver& operator=(const Driver& from) = delete;
    Driver& operator=(Driver&& from) = default;

    /**
     * Checks whether this connection is presently open.
     *
     * <h3>Examples</h3>
     * <pre>
     * driver.isOpen();
     * </pre>
     */
    bool isOpen();

    /**
     * Closes the driver. Before instantiating a new driver, the driver that’s currently open should first be closed.
     *
     * <h3>Examples</h3>
     * <pre>
     * driver.close()
     * </pre>
     */
    void close();

    /**
     * Opens a communication tunnel (session) to the given database on the running TypeDB server.
     * For more information on the methods, available with sessions, see the <code>TypeDBSession</code> section.
     *
     * <h3>Examples</h3>
     * <pre>
     * driver.session(database, sessionType, options);
     * </pre>
     *
     * @param database The name of the database with which the session connects
     * @param type The type of session to be created (DATA or SCHEMA)
     * @param options <code>TypeDBOptions</code> for the session
     */
    Session session(const std::string& database, SessionType sessionType, const Options& options);

    /**
     * Returns the logged-in user for the connection. Only for TypeDB Cloud.
     *
     * <h3>Examples</h3>
     * <pre>
     * driver.user();
     * </pre>
     */
    User user();

private:
    Driver(TypeDB::_native::Connection* conn) noexcept;
};

/**
 * \brief User credentials and TLS encryption settings for connecting to TypeDB Cloud.
 *
 * <h3>Examples</h3>
 * <pre>
 * // Creates a credential as above, but the connection will be made over TLS.
 * Credential credential(username, password, true);
 *
 * // Creates a credential as above, but TLS will use the specified CA to authenticate server certificates.
 * Credential credential(username, password, "path/to/ca-certificate.pem");
 * </pre>
 */
class Credential {
public:
    /**
     *
     * @param username The name of the user to connect as
     * @param password The password for the user
     * @param withTLS Specify whether the connection to TypeDB Cloud must be done over TLS
     * @param customRootCAPath Optional, Path to a custom CA certificate to use for authenticating server certificates.
     */
    Credential(const std::string& username, const std::string& password, bool withTLS, const std::string& customRootCAPath = "");
    Credential(const Credential&) = delete;
    Credential& operator=(const Credential&) = delete;
    Credential(Credential&&) = default;
    Credential& operator=(Credential&&) = default;

private:
    NativePointer<_native::Credential> credentialNative;

    _native::Credential* getNative() const;

    friend class Driver;
};

}  // namespace TypeDB
