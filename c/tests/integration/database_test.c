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

#include <stdio.h>

#include "../typedb_driver.h"
#include "common.h"

bool delete_database_if_exists(DatabaseManager* dbMgr, char* name) {
    Database* database = databases_get(dbMgr, "test");
    if (null != database) {
        database_delete(database); // PRINT_ERR();
    } else error_drop(get_last_error()); // Clear the error
    return true;
}


int test_basic_happy(const Connection* conn) {
    DatabaseManager* dbMgr = database_manager_new(conn);
    delete_database_if_exists(dbMgr, "test");
    databases_create(dbMgr, "test");

    Options* opts = options_new();
    Session* session = session_new(databases_get(dbMgr, "test"), Schema, opts);
    Transaction* transaction = transaction_new(session, Write, opts);

    query_define(transaction, "define person sub entity;", opts);
    {
        ConceptMapIterator* it = query_match(transaction, "match $t sub thing;", opts);
        ConceptMap* conceptMap;
        printf("Results:\n");
        while ( null != (conceptMap = concept_map_iterator_next(it)) ) {
            Concept* concept = concept_map_get(conceptMap, "t");
            char* label = thing_type_get_label(concept);
            printf("- %s\n", label);
            free(label);
            concept_drop(concept);
            concept_map_drop(conceptMap);
        }
        concept_map_iterator_drop(it);
    }
    transaction_commit(transaction);

    session_drop(session);
    options_drop(opts);
    delete_database_if_exists(dbMgr, "test");
    database_manager_drop(dbMgr);
    printf("Test ran successfully\n");
    return 0;
}

int main() {
    // run with `leaks --atExit -- ./bazel-bin/path/to/this-binary`
    run_test_core("test_basic_happy", &test_basic_happy);
//    RUN_TEST(test_basic_happy);
//    malloc(123);

    return 0;
}