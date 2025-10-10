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

#include <stdio.h>
#include <string.h>

#include "c/typedb_driver.h"
#include "common.h"

#define FAILED() check_error_may_print(__FILE__, __LINE__)

bool test_database_management() {
    const char databaseName[] = "test_database_management";

    TypeDBDriver* driver = NULL;

    bool success = false;

    driver = driver_open_for_tests(TYPEDB_CORE_ADDRESS, TYPEDB_CORE_USERNAME, TYPEDB_CORE_PASSWORD);
    if (FAILED()) goto cleanup;

    delete_database_if_exists(driver, databaseName);
    if (FAILED()) goto cleanup;

    databases_create(driver, databaseName);
    if (FAILED()) goto cleanup;

    if (!databases_contains(driver, databaseName)) {
        fprintf(stderr, "databases_contains(\'%s\') failed\n", databaseName);
        goto cleanup;
    }

    bool foundDB = false;
    DatabaseIterator* it = databases_all(driver);
    const Database* database = NULL;
    while (NULL != (database = database_iterator_next(it))) {
        char* name = database_get_name(database);
        foundDB = foundDB || (0 == strcmp(databaseName, name));
        string_free(name);
        database_close(database);
    }
    database_iterator_drop(it);

    if (!foundDB) {
        fprintf(stderr, "Did not find database \'%s\' in list of databases\n", databaseName);
        goto cleanup;
    }

    success = true;
cleanup:
    delete_database_if_exists(driver, databaseName);
    check_error_may_print(__FILE__, __LINE__);
    driver_close(driver);
    return success;
}

bool test_query_schema() {
    const char databaseName[] = "test_query_schema";

    TypeDBDriver* driver = NULL;

    TransactionOptions* tx_opts = NULL;
    Transaction* transaction = NULL;
    QueryOptions* query_opts = NULL;

    bool success = false;

    // Set up connection & database
    driver = driver_open_for_tests(TYPEDB_CORE_ADDRESS, TYPEDB_CORE_USERNAME, TYPEDB_CORE_PASSWORD);
    if (FAILED()) goto cleanup;

    delete_database_if_exists(driver, databaseName);
    if (FAILED()) goto cleanup;

    databases_create(driver, databaseName);
    if (FAILED()) goto cleanup;

    tx_opts = transaction_options_new();
    if (FAILED()) goto cleanup;
    query_opts = query_options_new();
    if (FAILED()) goto cleanup;

    // test schema queries
    {
        transaction = transaction_new(driver, databaseName, Schema, tx_opts);
        if (FAILED()) goto cleanup;

        {
            QueryAnswerPromise* define_promise = transaction_query(transaction, "define attribute name, value string;", query_opts);
            if (FAILED()) goto cleanup;
            QueryAnswer* define_answer = query_answer_promise_resolve(define_promise);
            if (FAILED()) goto cleanup;
            success = query_answer_is_ok(define_answer);
            query_answer_drop(define_answer);
            if (!success) {
                goto cleanup;
            }
        }

        {
            QueryAnswerPromise* query_promise = transaction_query(transaction, "match $t sub $_;", query_opts);
            if (FAILED()) goto cleanup;
            QueryAnswer* query_answer = query_answer_promise_resolve(query_promise);
            if (FAILED()) goto cleanup;
            if (!query_answer_is_concept_row_stream(query_answer)) {
                success = false;
                query_answer_drop(query_answer);
                goto cleanup;
            }

            ConceptRowIterator* it = query_answer_into_rows(query_answer);
            if (FAILED()) goto cleanup;
            ConceptRow* conceptRow;
            bool foundName = false;
            while (NULL != (conceptRow = concept_row_iterator_next(it))) {
                Concept* concept = concept_row_get(conceptRow, "t");
                char* label = concept_get_label(concept);
                foundName = foundName || (0 == strcmp(label, "name"));
                string_free(label);
                concept_drop(concept);
                concept_row_drop(conceptRow);
            }
            concept_row_iterator_drop(it);

            void_promise_resolve(transaction_commit(transaction));
            transaction = NULL;

            if (!foundName) {
                fprintf(stderr, "Did not find type \'name\' in query result.\n");
                goto cleanup;
            }
        }
    }
    success = true;

cleanup:
    transaction_close(transaction);
    transaction_options_drop(tx_opts);
    query_options_drop(query_opts);

    delete_database_if_exists(driver, databaseName);
    check_error_may_print(__FILE__, __LINE__);
    driver_close(driver);
    return success;
}

bool test_query_data() {
    const char databaseName[] = "test_query_data";

    TypeDBDriver* driver = NULL;
    TransactionOptions* tx_opts = NULL;
    QueryOptions* query_opts = NULL;
    Transaction* transaction = NULL;

    bool success = false;

    // Set up connection & database
    driver = driver_open_for_tests(TYPEDB_CORE_ADDRESS, TYPEDB_CORE_USERNAME, TYPEDB_CORE_PASSWORD);
    if (FAILED()) goto cleanup;

    delete_database_if_exists(driver, databaseName);
    if (FAILED()) goto cleanup;

    databases_create(driver, databaseName);
    if (FAILED()) goto cleanup;

    tx_opts = transaction_options_new();
    if (FAILED()) goto cleanup;
    query_opts = query_options_new();
    if (FAILED()) goto cleanup;

    // Set up schema
    {

        transaction = transaction_new(driver, databaseName, Schema, tx_opts);
        if (FAILED()) goto cleanup;


        QueryAnswerPromise* define_promise = transaction_query(transaction, "define attribute name, value string;", query_opts);
        if (FAILED()) goto cleanup;
        QueryAnswer* define_answer = query_answer_promise_resolve(define_promise);
        if (FAILED()) goto cleanup;
        success = query_answer_is_ok(define_answer);
        query_answer_drop(define_answer);
        if (!success) {
            goto cleanup;
        }

        void_promise_resolve(transaction_commit(transaction));
        transaction = NULL;
        if (FAILED()) goto cleanup;
    }

    {
        transaction = transaction_new(driver, databaseName, Write, tx_opts);
        if (FAILED()) goto cleanup;
        QueryAnswerPromise* insert_promise = transaction_query(transaction, "insert $n isa name == \"John\";", query_opts);
        if (FAILED()) goto cleanup;
        QueryAnswer* insert_answer = query_answer_promise_resolve(insert_promise);
        if (FAILED()) goto cleanup;
        if (!query_answer_is_concept_row_stream(insert_answer)) {
            success = false;
            query_answer_drop(insert_answer);
            goto cleanup;
        }
        ConceptRowIterator* insert_result = query_answer_into_rows(insert_answer);
        if (FAILED()) goto cleanup;
        else concept_row_iterator_drop(insert_result);


        QueryAnswerPromise* match_promise = transaction_query(transaction, "match $n isa name;", query_opts);
        QueryAnswer* match_answer = query_answer_promise_resolve(match_promise);
        if (FAILED()) goto cleanup;
        if (!query_answer_is_concept_row_stream(match_answer)) {
            success = false;
            query_answer_drop(match_answer);
            goto cleanup;
        }
        ConceptRowIterator* it = query_answer_into_rows(match_answer);
        ConceptRow* conceptRow;
        bool foundJohn = false;
        while (NULL != (conceptRow = concept_row_iterator_next(it))) {
            Concept* concept = concept_row_get(conceptRow, "n");
            char* attr = concept_get_string(concept);
            foundJohn = foundJohn || (0 == strcmp(attr, "John"));
            string_free(attr);
            concept_drop(concept);
            concept_row_drop(conceptRow);
        }
        concept_row_iterator_drop(it);

        void_promise_resolve(transaction_commit(transaction));
        transaction = NULL;

        if (!foundJohn) {
            fprintf(stderr, "Did not find inserted name \'John\' in query result.\n");
            goto cleanup;
        }
    }

    success = true;

cleanup:
    if (NULL != transaction) transaction_close(transaction);
    transaction_options_drop(tx_opts);
    query_options_drop(query_opts);

    delete_database_if_exists(driver, databaseName);
    check_error_may_print(__FILE__, __LINE__);

    driver_close(driver);
    return success;
}
