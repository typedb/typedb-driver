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

#include "common.hpp"
#include "steps.hpp"
#include "utils.hpp"

namespace TypeDB::BDD {

const std::string DEFAULT_CORE_ADDRESS = "127.0.0.1:1729";

void wipeDatabases(const TypeDB::Driver& driver) {
    DatabaseIterable dbIterable = driver.databases.all();
    for (DatabaseIterator it = dbIterable.begin(); it != dbIterable.end(); ++it) {
        (*it).deleteDatabase();
    }
}

bool TestHooks::skipScenario(const cucumber::messages::pickle& scenario) const {
    return std::any_of(scenario.tags.begin(), scenario.tags.end(), [&](const cucumber::messages::pickle_tag& tag) {
        return tag.name == "@ignore" || tag.name == "@ignore-typedb-driver" || tag.name == "@ignore-typedb-driver-cpp";
    });
}

void TestHooks::beforeAll() const {
    wipeDatabases(CoreOrCloudConnection::defaultConnection());
}

void TestHooks::afterScenario(Context& context, const cucumber_bdd::Scenario<Context>* scenario) const {
    DriverException::check_and_throw();
    context.driver->close();
    wipeDatabases(CoreOrCloudConnection::defaultConnection());
}

const TestHooks testHooks;

TypeDB::Driver CoreOrCloudConnection::defaultConnection() {
    return TypeDB::Driver::coreDriver(DEFAULT_CORE_ADDRESS);
}

TypeDB::Driver CoreOrCloudConnection::connectWithAuthentication(const std::string&, const std::string&) {
    THROW_ILLEGAL_STATE("Core does not support authentication steps");
}

}  // namespace TypeDB::BDD
