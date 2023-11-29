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

using namespace cucumber::messages;

namespace TypeDB::BDD {

void forEachSessionTransaction_serial(Context& context, std::function<void(TypeDB::Transaction*)> fn) {
    for (auto& sess : context.sessions) {
        foreach_serial(context.sessionTransactions[&sess], fn);
    }
}

void forEachSessionTransaction_parallel(Context& context, std::function<void(TypeDB::Transaction*)> fn) {
    for (auto& sess : context.sessions) {
        foreach_parallel(context.sessionTransactions[&sess], fn);
    }
}

cucumber_bdd::StepCollection<Context> transactionSteps = {

    BDD_STEP("session opens transaction of type: (read|write)", {
        context.setTransaction(context.session().transaction(parseTransactionType(matches[1].str()), context.transactionOptions));
    }),
    BDD_UNIMPLEMENTED("session open transaction of type; throws exception: (read|write)"),
    BDD_UNIMPLEMENTED("for each session, open transactions of type; throws exception"),
    BDD_STEP("session transaction is null: (true|false)", {
        ASSERT_EQ(parseBoolean(matches[1]), !context.transaction().isOpen());
    }),
    BDD_STEP("session transaction is open: (true|false)", {
        ASSERT_EQ(parseBoolean(matches[1]), context.transaction().isOpen());
    }),
    BDD_STEP("(?:session )?transaction commits", {
        context.transaction().commit();
    }),
    BDD_STEP("session transaction closes", {
        context.transaction().close();
    }),
    BDD_STEP("(?:session )?transaction commits; throws exception", {
        DRIVER_THROWS("", { context.transaction().commit(); });
    }),
    BDD_STEP("transaction commits; throws exception containing \"(.+)\"", {
        DRIVER_THROWS(matches[1].str(), { context.transaction().commit(); });
    }),
    BDD_STEP("session transaction has type: (read|write)", {
        ASSERT_EQ(parseTransactionType(matches[1].str()), context.transaction().type());
    }),
    BDD_STEP("set transaction option ([A-Za-z_\\-]+) to: ([A-Za-z0-9]+)", {
        assert(matches[1] == "transaction-timeout-millis");
        context.transactionOptions.transactionTimeoutMillis(atoi(matches[2].str().c_str()));
    }),

    BDD_STEP("for each session, open transaction of type: (read|write)", {
        std::function<void(TypeDB::Session*)> fn = [&](TypeDB::Session* session) {
            context.sessionTransactions[session].push_back(session->transaction(parseTransactionType(matches[1].str()), context.transactionOptions));
        };
        foreach_serial(context.sessions, fn);
    }),
    BDD_UNIMPLEMENTED("for each session, open transaction of type:"),
    BDD_STEP("for each session, open transactions of type:", {
        std::function<void(TypeDB::Session*)> fn = [&](TypeDB::Session* session) {
            for (auto& row : step.argument->data_table->rows) {
                context.sessionTransactions[session].push_back(session->transaction(parseTransactionType(row.cells[0].value), context.transactionOptions));
            }
        };
        foreach_serial(context.sessions, fn);
    }),
    BDD_STEP("for each session, transaction[s]? (?:are|is) null: (true|false)", {  // overload
        std::function<void(TypeDB::Transaction*)> fn = [&](TypeDB::Transaction* tx) { ASSERT_EQ(parseBoolean(matches[1]), !tx->isOpen()); };
        forEachSessionTransaction_serial(context, fn);
    }),
    BDD_STEP("for each session, transaction[s]? (?:are|is) open: (true|false)", {  // overload
        std::function<void(TypeDB::Transaction*)> fn = [&](TypeDB::Transaction* tx) -> void { ASSERT_EQ(parseBoolean(matches[1]), tx->isOpen()); };
        forEachSessionTransaction_serial(context, fn);
    }),
    BDD_STEP("for each session, transaction[s]? commit[s]?", {  // overload
        std::function<void(TypeDB::Transaction*)> fn = [&](TypeDB::Transaction* transaction) { transaction->commit(); };
        forEachSessionTransaction_serial(context, fn);
    }),
    BDD_STEP("for each session, transaction commit[s]?; throws exception", {  // overload
        std::function<void(TypeDB::Transaction*)> fn = [&](TypeDB::Transaction* transaction) { DRIVER_THROWS("", transaction->commit()); };
        forEachSessionTransaction_serial(context, fn);
    }),
    BDD_STEP("for each session, transaction closes", {
        std::function<void(TypeDB::Transaction*)> fn = [&](TypeDB::Transaction* transaction) { transaction->close(); };
        forEachSessionTransaction_serial(context, fn);
    }),
    BDD_STEP("for each session, transaction has type", {
        std::vector<TypeDB::Transaction*> transactions;
        for (auto& session : context.sessions) {
            for (auto& txn : context.sessionTransactions[&session]) {
                transactions.push_back(&txn);
            }
        }
        ASSERT_EQ(step.argument->data_table->rows.size(), transactions.size());
        for (int i = 0; i < transactions.size(); i++) {
            ASSERT_EQ(parseTransactionType(step.argument->data_table->rows[i].cells[0].value), transactions[i]->type());
        }
    }),
    BDD_STEP("for each session, transactions have type:", {
        std::vector<TypeDB::Transaction*> transactions;
        for (auto& session : context.sessions) {
            ASSERT_EQ(step.argument->data_table->rows.size(), context.sessionTransactions[&session].size());
            for (int i = 0; i < step.argument->data_table->rows.size(); i++) {
                ASSERT_EQ(parseTransactionType(step.argument->data_table->rows[i].cells[0].value), context.sessionTransactions[&session][i].type());
            }
        }
    }),
    BDD_STEP("for each session, transaction has type: (read|write)", {
        std::function<void(TypeDB::Transaction*)> fn = [&](TypeDB::Transaction* transaction) { ASSERT_EQ(parseTransactionType(matches[1].str()), transaction->type()); };
        forEachSessionTransaction_serial(context, fn);
    }),

    BDD_STEP("for each session, open transactions in parallel of type:", {
        std::vector<int> nums;
        for (int i = 0; i < step.argument->data_table->rows.size(); i++)
            nums.push_back(i);

        for (TypeDB::Session& session : context.sessions) {
            std::vector<std::optional<TypeDB::Transaction>> txn(nums.size());
            std::function<void(int*)> fn = [&](int* i) {
                txn[*i] = session.transaction(parseTransactionType(step.argument->data_table->rows[*i].cells[0].value), context.transactionOptions);
            };

            foreach_parallel(nums, fn);

            for (auto& tx : txn) {
                context.sessionTransactions[&session].push_back(std::move(tx.value()));
            }
        }
    }),
    BDD_STEP("for each session, transactions in parallel are null: (true|false)", {
        std::function<void(TypeDB::Transaction*)> fn = [&](TypeDB::Transaction* tx) { ASSERT_EQ(parseBoolean(matches[1]), !tx->isOpen()); };
        forEachSessionTransaction_parallel(context, fn);
    }),
    BDD_STEP("for each session, transactions in parallel are open: (true|false)", {
        std::function<void(TypeDB::Transaction*)> fn = [&](TypeDB::Transaction* tx) { ASSERT_EQ(parseBoolean(matches[1]), tx->isOpen()); };
        forEachSessionTransaction_parallel(context, fn);
    }),
    BDD_STEP("for each session, transactions in parallel have type:", {
        std::function<void(zipped<Transaction>*)> fn = [&](zipped<Transaction>* zipped) {
            ASSERT_EQ(parseTransactionType(zipped->row->cells[0].value), zipped->obj->type());
        };
        for (auto& session : context.sessions) {
            auto z = zip(step.argument->data_table->rows, context.sessionTransactions[&session]);
            foreach_parallel(z, fn);
        }
    }),

    // I don't see them in the BDD
    BDD_UNIMPLEMENTED("for each transaction, define query; throws exception containing \"(.+)\""),
    BDD_UNIMPLEMENTED("for each session in parallel, transactions in parallel are null: (true|false)"),
    BDD_UNIMPLEMENTED("for each session in parallel, transactions in parallel are open: (true|false)"),

};

}  // namespace TypeDB::BDD
