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

#include <atomic>
#include <chrono>
#include <iostream>
#include <regex>
#include <thread>

#include "gtest/gtest.h"

#include "typedb_driver.hpp"

using namespace TypeDB;

void delete_if_exists(const TypeDB::Driver& driver, const std::string& name) {
    if (driver.databases.contains(name)) {
        driver.databases.get(name).deleteDatabase();
    }
}

TEST(TestDatabaseManager, TestCreateTwice) {
    std::string dbName = "hello_from_cpp";
    TypeDB::Driver driver = TypeDB::Driver::coreDriver("127.0.0.1:1729");
    delete_if_exists(driver, dbName);
    driver.databases.create(dbName);

    try {
        driver.databases.create(dbName);
        FAIL();  // "Exception not thrown"
    } catch (DriverException& e) {
        ASSERT_TRUE(e.message().find("already exists") != std::string::npos);
    }
}


TEST(TestExplanations, TestExplainableOwnership) {
    std::string dbName = "test_explanations";
    TypeDB::Driver driver = TypeDB::Driver::coreDriver("127.0.0.1:1729");
    int attrValue = 42;
    std::string ruleLabel = "ownership-generation";
    std::string then = "$a has $a";
    std::string when = "$a isa attr;";
    std::regex whenRegex = std::regex("\\{\\s*\\$a isa attr;\\s*\\}");

    delete_if_exists(driver, dbName);

    driver.databases.create(dbName);
    TypeDB::Options options;

    {
        auto sess = driver.session(dbName, TypeDB::SessionType::SCHEMA, options);
        auto tx = sess.transaction(TypeDB::TransactionType::WRITE, options);
        std::string schema =
            " define attr sub attribute, value long, owns attr;"
            "rule " +
            ruleLabel + ": when { " + when + " } then { " + then + "; }; ";
        tx.query.define(schema, options).wait();

        tx.commit();
    }

    {
        auto sess = driver.session(dbName, TypeDB::SessionType::DATA, options);
        auto tx = sess.transaction(TypeDB::TransactionType::WRITE, options);

        auto res = tx.query.insert("insert $o " + std::to_string(attrValue) + " isa attr;", options);
        for (auto& it : res)
            ;
        tx.commit();
    }

    {
        TypeDB::Options explainOptions;
        explainOptions.infer(true).explain(true);

        auto sess = driver.session(dbName, TypeDB::SessionType::DATA, options);
        auto tx = sess.transaction(TypeDB::TransactionType::READ, explainOptions);

        auto res = tx.query.get("match $o1 has $o2; get;", options);
        for (TypeDB::ConceptMap& cm : res) {
            for (std::string& v : cm.variables()) {
                ASSERT_TRUE(v == "o1" || v == "o2");
                ASSERT_EQ(attrValue, cm.get(v)->asAttribute()->getValue()->asLong());
            }
            for (auto& ownsKey : cm.explainables().ownerships()) {
                auto explainable = cm.explainables().ownership(ownsKey.owner, ownsKey.attribute);
                for (auto& explanation : tx.query.explain(explainable, options)) {
                    ASSERT_EQ(ruleLabel, explanation.rule().label());
                    ASSERT_EQ(then, explanation.rule().then());
                    ASSERT_TRUE(std::regex_match(explanation.rule().when(), whenRegex));
                    // std::cout << explanation.rule().then() << " :- " << explanation.rule().when() <<  std::endl;

                    auto conclusion = explanation.conclusion();
                    for (std::string& v : conclusion.variables()) {
                        ASSERT_EQ(v, "a");
                        ASSERT_EQ(attrValue, conclusion.get(v)->asAttribute()->getValue()->asLong());
                    }
                    auto condition = explanation.condition();
                    for (std::string& v : condition.variables()) {
                        ASSERT_EQ(v, "a");
                        ASSERT_EQ(attrValue, conclusion.get(v)->asAttribute()->getValue()->asLong());
                    }
                }
            }
        }
    }
}

TEST(TestCallbacks, TestCallbacks) {
    std::string dbName = "test-integration-callbacks";
    TypeDB::Driver driver = TypeDB::Driver::coreDriver("127.0.0.1:1729");
    TypeDB::Options options;
    delete_if_exists(driver, dbName);
    driver.databases.create(dbName);
    bool txCalled = false;
    bool sessCalled = false;
    {
        auto sess = driver.session(dbName, TypeDB::SessionType::DATA, options);
        sess.onClose([&]() { sessCalled = true; });
        {
            auto tx = sess.transaction(TypeDB::TransactionType::READ, options);
            tx.onClose([&](const std::optional<DriverException>& e) { txCalled = true; });
            ASSERT_FALSE(txCalled);
        }
        std::this_thread::sleep_for(std::chrono::milliseconds(500));
        // ASSERT_TRUE(txCalled); // TODO: re-enable
        ASSERT_FALSE(sessCalled);
    }
    std::this_thread::sleep_for(std::chrono::milliseconds(500));
    ASSERT_TRUE(sessCalled);
}

TEST(TestConnection, TestMissingPort) {
    std::string dbName = "hello_from_cpp";

    try {
        TypeDB::Driver driver = TypeDB::Driver::coreDriver("127.0.0.1");
        FAIL();  // "Exception not thrown"
    } catch (DriverException& e) {
        ASSERT_TRUE(e.message().find("missing port") != std::string::npos);
    }
}

TEST(TestJSON, TestJSON) {
    std::string dbName = "test_json";
    TypeDB::Driver driver = TypeDB::Driver::coreDriver("127.0.0.1:1729");
    delete_if_exists(driver, dbName);
    driver.databases.create(dbName);
    TypeDB::Options options;
    std::string fetchResult = R"({"u": {"email": [{"type": {"label": "email", "root": "attribute", "value_type": "string"}, "value": "bob@vaticle.com"}], "name": [{"type": {"label": "name", "root": "attribute", "value_type": "string"}, "value": "Bob"}], "type": {"label": "user", "root": "entity"}}})";
    {
        auto sess = driver.session(dbName, TypeDB::SessionType::SCHEMA, options);
        auto tx = sess.transaction(TypeDB::TransactionType::WRITE, options);
        std::string schema = R"(
                            define
                            email sub attribute, value string;
                            name sub attribute, value string;
                            user sub entity,
                                owns email @key,
                                owns name;)";
        tx.query.define(schema, options).wait();
        tx.commit();
    }

    {
        auto sess = driver.session(dbName, TypeDB::SessionType::DATA, options);
        auto tx = sess.transaction(TypeDB::TransactionType::WRITE, options);
        std::string insertQuery = "insert $user isa user, has name 'Bob', has email 'bob@vaticle.com';";
        auto res = tx.query.insert(insertQuery, options);
        for (auto& it : res)
            ;
        tx.commit();
    }

    {
        auto sess = driver.session(dbName, TypeDB::SessionType::DATA, options);
        auto tx = sess.transaction(TypeDB::TransactionType::READ, options);
        std::string fetchQuery = "match $u isa user, has name 'Bob'; fetch $u: name, email;";
        TypeDB::JSONIterable response = tx.query.fetch(fetchQuery, options);
        std::string result;
        for (TypeDB::JSON json : response) {
            result.append(json.toString());
        }
        ASSERT_EQ(fetchResult, result);
    }

    {
        ASSERT_EQ(fetchResult, JSON::parse(fetchResult).toString());
    }

}

int main(int argc, char** argv) {
    ::testing::InitGoogleTest(&argc, argv);
    return RUN_ALL_TESTS();
}
