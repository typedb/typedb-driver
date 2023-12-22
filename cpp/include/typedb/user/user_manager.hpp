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
#include "typedb/user/user.hpp"

namespace TypeDB {

class Driver;  // forward declaration for friendship

using UserIterator = Iterator<_native::UserIterator, _native::User, User>;
using UserIterable = Iterable<_native::UserIterator, _native::User, User>;

/**
 * \brief Provides access to all user management methods.
 */
class UserManager {
public:
    ~UserManager() = default;

    /**
     * Checks if a user with the given name exists.
     *
     * <h3>Examples</h3>
     * <pre>
     * driver.users.contains(username);
     * </pre>
     *
     * @param username The user name to be checked
     */
    bool contains(const std::string& username) const;

    /**
     * Creates a user with the given name &amp; password.
     *
     * <h3>Examples</h3>
     * <pre>
     * driver.users.create(username, password);
     * </pre>
     *
     * @param username The name of the user to be created
     * @param password The password of the user to be created
     */
    void create(const std::string& username, const std::string& password) const;

    /**
     * Deletes a user with the given name.
     *
     * <h3>Examples</h3>
     * <pre>
     * driver.users.deleteUser(username);
     * </pre>
     *
     * @param username The name of the user to be deleted
     */
    void deleteUser(const std::string& username) const;

    /**
     * Retrieves all users which exist on the TypeDB server.
     *
     * <h3>Examples</h3>
     * <pre>
     * driver.users.all();
     * </pre>
     */
    UserIterable all() const;

    /**
     * Retrieves a user with the given name.
     *
     * <h3>Examples</h3>
     * <pre>
     * driver.users.get(username);
     * </pre>
     *
     * @param username The name of the user to retrieve
     */
    std::unique_ptr<User> get(const std::string& username) const;

    /**
     * Sets a new password for a user. This operation can only be performed by administrators.
     *
     * <h3>Examples</h3>
     * <pre>
     * driver.users.passwordSet(username, password);
     * </pre>
     *
     * @param username The name of the user to set the password of
     * @param password The new password
     */
    void passwordSet(const std::string& username, const std::string& password) const;

private:
    NativePointer<_native::UserManager> userManagerNative;

    UserManager(const _native::Connection*);
    UserManager(UserManager&&) = default;
    UserManager& operator=(UserManager&&) = default;
    UserManager(const UserManager&) = delete;
    UserManager& operator=(const UserManager&) = delete;

    _native::UserManager* getNative() const;
    User getCurrentUser() const;

    friend class TypeDB::Driver;
    friend class User;
};

}  // namespace TypeDB
