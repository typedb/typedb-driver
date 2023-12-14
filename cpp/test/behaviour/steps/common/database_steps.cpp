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

#include "common.hpp"
#include "steps.hpp"
#include "utils.hpp"

using namespace cucumber::messages;

namespace TypeDB::BDD {

cucumber_bdd::StepCollection<Context> databaseSteps = {
    // basic
    BDD_STEP("connection create database: (\\w+)", {
        context.driver->databases.create(matches[1]);
    }),

    BDD_STEP("connection has database: (\\w+)", {
        ASSERT_TRUE(context.driver->databases.contains(matches[1]));
    }),

    BDD_STEP("connection does not have database: (\\w+)", {
        ASSERT_FALSE(context.driver->databases.contains(matches[1]));
    }),

    BDD_STEP("connection delete database: (\\w+)", {
        context.driver->databases.get(matches[1]).deleteDatabase();
    }),

    BDD_STEP("connection delete database; throws exception: (\\w+)", {
        DRIVER_THROWS(matches[1].str(), { context.driver->databases.get(matches[1]).deleteDatabase(); });
    }),

    // multi
    BDD_STEP("connection create databases:", {
        std::function<void(pickle_table_row*)> fn = [&](pickle_table_row* row) { context.driver->databases.create(row->cells[0].value); };
        foreach_serial(step.argument->data_table->rows, fn);
    }),

    BDD_STEP("connection has databases:", {
        std::function<std::string(const TypeDB::Database&)> getDbName = [](const TypeDB::Database& db) { return db.name(); };
        ASSERT_TRUE(checkEqual(step.argument->data_table.value(), transform(context.driver->databases.all(), getDbName)));
    }),

    BDD_STEP("connection does not have databases:", {
        std::function<void(pickle_table_row*)> fn = [&](pickle_table_row* row) { ASSERT_FALSE(context.driver->databases.contains(row->cells[0].value)); };
        foreach_serial(step.argument->data_table->rows, fn);
    }),

    BDD_STEP("connection delete databases:", {
        std::function<void(pickle_table_row*)> fn = [&](pickle_table_row* row) { context.driver->databases.get(row->cells[0].value).deleteDatabase(); };
        foreach_serial(step.argument->data_table->rows, fn);
    }),

    // parallel
    BDD_STEP("connection create databases in parallel:", {
        std::function<void(pickle_table_row*)> fn = [&](pickle_table_row* row) { context.driver->databases.create(row->cells[0].value); };
        foreach_parallel(step.argument->data_table->rows, fn);
    }),

    BDD_STEP("connection delete databases in parallel:", {
        std::function<void(pickle_table_row*)> fn = [&](pickle_table_row* row) { context.driver->databases.get(row->cells[0].value).deleteDatabase(); };
        foreach_parallel(step.argument->data_table->rows, fn);
    }),
};

}  // namespace TypeDB::BDD
