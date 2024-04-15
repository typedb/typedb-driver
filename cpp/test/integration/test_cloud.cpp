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

#include "gtest/gtest.h"

#include "typedb_driver.hpp"

void delete_if_exists(const TypeDB::Driver& driver, const std::string& name) {
    if (driver.databases.contains(name)) {
        driver.databases.get(name).deleteDatabase();
    }
}

TEST(TestConnection, TestAddressTranslation) {
    std::string dbName = "hello_from_cpp";
    auto addressTranslation = std::unordered_map<std::string, std::string> {
        {"localhost:11729", "localhost:11729"},
        {"localhost:21729", "localhost:21729"},
        {"localhost:31729", "localhost:31729"},
    };
    auto credential = TypeDB::Credential("admin", "password", true, std::getenv("ROOT_CA"));
    TypeDB::Driver driver = TypeDB::Driver::cloudDriver(addressTranslation, credential);

    delete_if_exists(driver, dbName);
    driver.databases.create(dbName);

    auto sess = driver.session(dbName, TypeDB::SessionType::DATA);
    auto tx = sess.transaction(TypeDB::TransactionType::WRITE);

    auto root = tx.concepts.getRootEntityType();
    int subtypeCount = 0;
    for ([[maybe_unused]] auto& it : root->getSubtypes(tx)) 
        subtypeCount++;
    ASSERT_EQ(1, subtypeCount);
}

