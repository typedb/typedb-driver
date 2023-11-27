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

#include <cstdlib>

#include "common.hpp"
#include "steps.hpp"
#include "utils.hpp"

#include "typedb/connection/session.hpp"

namespace TypeDB::BDD {

using namespace cucumber::messages;

cucumber_bdd::StepCollection<Context> sessionSteps = {
    BDD_STEP("connection open session for database: (\\w+)", {
        context.setSession(context.driver.session(matches[1], TypeDB::SessionType::DATA, context.sessionOptions));
    }),
    BDD_STEP("session is null: (true|false)", {
        ASSERT_EQ(parseBoolean(matches[1]), !context.session().isOpen());
    }),
    BDD_STEP("session is open: (true|false)", {
        ASSERT_EQ(parseBoolean(matches[1]), context.session().isOpen());
    }),
    BDD_STEP("session has database: (\\w+)", {
        ASSERT_EQ(matches[1], context.session().databaseName());
    }),

    BDD_STEP("connection open schema session for database: (\\w+)", {
        context.setSession(context.driver.session(matches[1], TypeDB::SessionType::SCHEMA, context.sessionOptions));
    }),
    BDD_STEP("connection open data session for database: (\\w+)", {
        context.setSession(context.driver.session(matches[1], TypeDB::SessionType::DATA, context.sessionOptions));
    }),
    BDD_STEP("connection open(?: data)? sessions for database:", {
        std::function<TypeDB::Session(pickle_table_row*)> fn = [&](pickle_table_row* row) { return context.driver.session(row->cells[0].value, TypeDB::SessionType::DATA, context.sessionOptions); };
        context.sessions = apply_serial(step.argument->data_table->rows, fn);
    }),
    BDD_STEP("connection open(?: data)? sessions in parallel for databases:", {
        std::function<TypeDB::Session(pickle_table_row*)> fn = [&](pickle_table_row* row) { return context.driver.session(row->cells[0].value, TypeDB::SessionType::DATA, context.sessionOptions); };
        context.sessions = apply_parallel(step.argument->data_table->rows, fn);
    }),
    BDD_STEP("connection close all sessions", {
        context.session().close();
        for (auto& sess : context.sessions) {
            sess.close();
        }
    }),

    BDD_STEP("sessions are null: (true|false)", {
        std::function<void(TypeDB::Session*)> fn = [&](TypeDB::Session* sess) { ASSERT_EQ(parseBoolean(matches[1]), !sess->isOpen()); };
        foreach_serial(context.sessions, fn);
    }),

    BDD_STEP("sessions are open: (true|false)", {
        std::function<void(TypeDB::Session*)> fn = [&](TypeDB::Session* sess) { ASSERT_EQ(parseBoolean(matches[1]), sess->isOpen()); };
        foreach_serial(context.sessions, fn);
    }),
    BDD_STEP("sessions in parallel are null: (true|false)", {
        std::function<void(TypeDB::Session*)> fn = [&](TypeDB::Session* sess) { ASSERT_EQ(parseBoolean(matches[1]), !sess->isOpen()); };
        foreach_parallel(context.sessions, fn);
    }),
    BDD_STEP("sessions in parallel are open: (true|false)", {
        std::function<void(TypeDB::Session*)> fn = [&](TypeDB::Session* sess) { ASSERT_EQ(parseBoolean(matches[1]), sess->isOpen()); };
        foreach_parallel(context.sessions, fn);
    }),


    BDD_STEP("sessions have databases:", {
        std::vector<zipped<TypeDB::Session>> z = zip(step.argument->data_table->rows, context.sessions);
        std::function<void(zipped<TypeDB::Session>*)> fn = [&](zipped<TypeDB::Session>* rowSession) {
            ASSERT_EQ(rowSession->row->cells[0].value, rowSession->obj->databaseName());
        };
        foreach_serial(z, fn);
    }),

    BDD_STEP("sessions in parallel have databases:", {
        std::vector<zipped<TypeDB::Session>> z = zip(step.argument->data_table->rows, context.sessions);
        std::function<void(zipped<TypeDB::Session>*)> fn = [&](zipped<TypeDB::Session>* rowSession) {
            ASSERT_EQ(rowSession->row->cells[0].value, rowSession->obj->databaseName());
        };
        foreach_parallel(z, fn);
    }),
    BDD_STEP("set session option ([A-Za-z_\\-]+) to: ([A-Za-z0-9]+)", {
        assert(matches[1] == "session-idle-timeout-millis");
        context.sessionOptions.sessionIdleTimeoutMillis(atoi(matches[2].str().c_str()));
    }),
};

}  // namespace TypeDB::BDD
