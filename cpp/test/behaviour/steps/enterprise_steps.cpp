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

#include <fstream>

#include "common.hpp"
#include "steps.hpp"
#include "utils.hpp"

namespace TypeDB::BDD {

const std::vector<std::string> DEFAULT_ENTERPRISE_ADDRESSES = {"localhost:11729", "localhost:21729", "localhost:31729"};
const std::string DEFAULT_ENTERPRISE_USER = "admin";
const std::string DEFAULT_ENTERPRISE_PASSWORD = "password";

void wipeDatabases(const TypeDB::Driver& driver) {
    DatabaseIterable dbIterable = driver.databases.all();
    for (DatabaseIterator it = dbIterable.begin(); it != dbIterable.end(); ++it) {
        (*it).drop();
    }
    UserIterable userIterable = driver.users.all();
    for (UserIterator it = userIterable.begin(); it != userIterable.end(); ++it) {
        if (it->username() != "admin") driver.users.drop(it->username());
    }
}

bool TestHooks::skipScenario(const cucumber::messages::pickle& scenario) const {
    return std::any_of(scenario.tags.begin(), scenario.tags.end(), [&](const cucumber::messages::pickle_tag& tag) {
        return tag.name == "@ignore" || tag.name == "@ignore-typedb-driver" || tag.name == "@ignore-typedb-driver-cpp";
    });
}

void TestHooks::beforeAll() const {
    wipeDatabases(CoreOrEnterpriseConnection::connectWithAuthentication("admin", "password"));
}

void TestHooks::afterScenario(Context& context, const cucumber_bdd::Scenario<Context>* scenario) const {
    TypeDBDriverException::check_and_throw();
    context.driver.close();
    wipeDatabases(CoreOrEnterpriseConnection::connectWithAuthentication("admin", "password"));
}

const TestHooks testHooks;

TypeDB::Driver CoreOrEnterpriseConnection::defaultConnection() {
    return TypeDB::Driver::enterpriseDriver(DEFAULT_ENTERPRISE_ADDRESSES, TypeDB::Credential(DEFAULT_ENTERPRISE_USER, DEFAULT_ENTERPRISE_PASSWORD, true, std::getenv("ROOT_CA")));
}

TypeDB::Driver CoreOrEnterpriseConnection::connectWithAuthentication(const std::string& username, const std::string& password) {
    return TypeDB::Driver::enterpriseDriver(DEFAULT_ENTERPRISE_ADDRESSES, TypeDB::Credential(username, password, true, std::getenv("ROOT_CA")));
}

}  // namespace TypeDB::BDD
