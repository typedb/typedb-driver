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
#include <string.h>

#include "c/typedb_driver.h"
#include "common.h"

#define FAILED() check_error_may_print(__FILE__, __LINE__)

bool test_database_management() {
    const char databaseName[] = "test_database_management";

    Connection* connection = NULL;
    DatabaseManager* databaseManager = NULL;

    bool success = false;

    connection = connection_open_core(TYPEDB_CORE_ADDRESS);
    if (FAILED()) goto cleanup;

    databaseManager = database_manager_new(connection);
    if (FAILED()) goto cleanup;

    delete_database_if_exists(databaseManager, databaseName);
    if (FAILED()) goto cleanup;

    databases_create(databaseManager, databaseName);
    if (FAILED()) goto cleanup;

    if (!databases_contains(databaseManager, databaseName)) {
        fprintf(stderr, "databases_contains(\'%s\') failed\n", databaseName);
        goto cleanup;
    }

    bool foundDB = false;
    DatabaseIterator* it = databases_all(databaseManager);
    Database* database = NULL;
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
    delete_database_if_exists(databaseManager, databaseName);
    check_error_may_print(__FILE__, __LINE__);
    database_manager_drop(databaseManager);
    connection_close(connection);
    return success;
}

bool test_query_schema() {
    const char databaseName[] = "test_query_schema";

    Connection* connection = NULL;
    DatabaseManager* databaseManager = NULL;
    Session* session = NULL;
    Transaction* transaction = NULL;
    Options* opts = NULL;

    bool success = false;

    // Set up connection & database
    connection = connection_open_core(TYPEDB_CORE_ADDRESS);
    if (FAILED()) goto cleanup;

    databaseManager = database_manager_new(connection);
    if (FAILED()) goto cleanup;

    delete_database_if_exists(databaseManager, databaseName);
    if (FAILED()) goto cleanup;

    databases_create(databaseManager, databaseName);
    if (FAILED()) goto cleanup;

    opts = options_new();
    if (FAILED()) goto cleanup;

    // test schema queries
    {
        session = session_new(databaseManager, databaseName, Schema, opts);
        if (FAILED()) goto cleanup;

        transaction = transaction_new(session, Write, opts);
        if (FAILED()) goto cleanup;

        void_promise_resolve(query_define(transaction, "define name sub attribute, value string;", opts));
        if (FAILED()) goto cleanup;

        ConceptMapIterator* it = query_get(transaction, "match $t sub thing; get;", opts);
        ConceptMap* conceptMap;
        bool foundName = false;
        while (NULL != (conceptMap = concept_map_iterator_next(it))) {
            Concept* concept = concept_map_get(conceptMap, "t");
            char* label = thing_type_get_label(concept);
            foundName = foundName || (0 == strcmp(label, "name"));
            string_free(label);
            concept_drop(concept);
            concept_map_drop(conceptMap);
        }
        concept_map_iterator_drop(it);

        void_promise_resolve(transaction_commit(transaction));
        transaction = NULL;

        if (!foundName) {
            fprintf(stderr, "Did not find type \'name\' in query result.\n");
            goto cleanup;
        }
    }
    success = true;

cleanup:
    transaction_close(transaction);
    session_close(session);
    options_drop(opts);

    delete_database_if_exists(databaseManager, databaseName);
    check_error_may_print(__FILE__, __LINE__);
    database_manager_drop(databaseManager);
    connection_close(connection);
    return success;
}

bool test_query_data() {
    const char databaseName[] = "test_query_data";

    Connection* connection = NULL;
    DatabaseManager* databaseManager = NULL;
    Session* session = NULL;
    Transaction* transaction = NULL;
    Options* opts = NULL;

    bool success = false;

    // Set up connection & database
    connection = connection_open_core(TYPEDB_CORE_ADDRESS);
    if (FAILED()) goto cleanup;

    databaseManager = database_manager_new(connection);
    if (FAILED()) goto cleanup;

    delete_database_if_exists(databaseManager, databaseName);
    if (FAILED()) goto cleanup;

    databases_create(databaseManager, databaseName);
    if (FAILED()) goto cleanup;

    opts = options_new();
    if (FAILED()) goto cleanup;

    // Set up schema
    {
        session = session_new(databaseManager, databaseName, Schema, opts);
        if (FAILED()) goto cleanup;

        transaction = transaction_new(session, Write, opts);
        if (FAILED()) goto cleanup;

        void_promise_resolve(query_define(transaction, "define name sub attribute, value string;", opts));
        if (FAILED()) goto cleanup;

        void_promise_resolve(transaction_commit(transaction));
        transaction = NULL;

        session_close(session);
        session = NULL;
    }

    {
        session = session_new(databaseManager, databaseName, Data, opts);
        if (FAILED()) goto cleanup;

        transaction = transaction_new(session, Write, opts);
        if (FAILED()) goto cleanup;

        ConceptMapIterator* insertResult = query_insert(transaction, "insert $n \"John\" isa name;", opts);
        if (FAILED()) goto cleanup;
        else concept_map_iterator_drop(insertResult);

        ConceptMapIterator* it = query_get(transaction, "match $n isa name; get;", opts);
        ConceptMap* conceptMap;
        bool foundJohn = false;
        while (NULL != (conceptMap = concept_map_iterator_next(it))) {
            Concept* concept = concept_map_get(conceptMap, "n");
            Concept* asValue = attribute_get_value(concept);
            char* attr = value_get_string(asValue);
            foundJohn = foundJohn || (0 == strcmp(attr, "John"));
            string_free(attr);
            concept_drop(asValue);
            concept_drop(concept);
            concept_map_drop(conceptMap);
        }
        concept_map_iterator_drop(it);

        void_promise_resolve(transaction_commit(transaction));
        transaction = NULL;

        if (!foundJohn) {
            fprintf(stderr, "Did not find inserted name \'John\' in query result.\n");
            goto cleanup;
        }
    }

    success = true;

cleanup:
    transaction_close(transaction);
    session_close(session);
    options_drop(opts);

    delete_database_if_exists(databaseManager, databaseName);
    check_error_may_print(__FILE__, __LINE__);
    database_manager_drop(databaseManager);
    connection_close(connection);
    return success;
}

bool test_concept_api_schema() {
    const char databaseName[] = "test_concept_api";

    Connection* connection = NULL;
    DatabaseManager* databaseManager = NULL;
    Session* session = NULL;
    Transaction* transaction = NULL;
    Options* opts = NULL;

    bool success = false;

    connection = connection_open_core(TYPEDB_CORE_ADDRESS);
    if (FAILED()) goto cleanup;

    databaseManager = database_manager_new(connection);
    if (FAILED()) goto cleanup;

    delete_database_if_exists(databaseManager, databaseName);
    if (FAILED()) goto cleanup;

    databases_create(databaseManager, databaseName);
    if (FAILED()) goto cleanup;

    opts = options_new();
    if (FAILED()) goto cleanup;

    // test schema api
    {
        session = session_new(databaseManager, databaseName, Schema, opts);
        if (FAILED()) goto cleanup;

        transaction = transaction_new(session, Write, opts);
        if (FAILED()) goto cleanup;
        {
            Concept* definedNameType =
                concept_promise_resolve(concepts_put_attribute_type(transaction, "name", String));
            if (FAILED()) goto cleanup;
            else concept_drop(definedNameType);
        }

        {
            ConceptIterator* it = NULL;
            Concept* nameType = NULL;
            Concept* rootAttributeType = NULL;
            bool foundName = false;
            if (NULL != (nameType = concept_promise_resolve(concepts_get_attribute_type(transaction, "name"))) &&
                NULL != (rootAttributeType = concepts_get_root_attribute_type()) &&
                NULL != (it = (attribute_type_get_subtypes(transaction, rootAttributeType, Transitive)))) {
                Concept* concept;
                while (NULL != (concept = concept_iterator_next(it))) {
                    char* label = thing_type_get_label(concept);
                    foundName = foundName || (0 == strcmp(label, "name"));
                    string_free(label);
                    concept_drop(concept);
                }
            }
            concept_iterator_drop(it);
            concept_drop(rootAttributeType);
            concept_drop(nameType);

            void_promise_resolve(transaction_commit(transaction));
            transaction = NULL;
            if (!foundName) {
                fprintf(stderr, "Did not find type \'name\' in subtypes of attribute.\n");
                goto cleanup;
            }
        }
    }

    success = true;

cleanup:
    transaction_close(transaction);
    session_close(session);
    options_drop(opts);

    delete_database_if_exists(databaseManager, databaseName);
    check_error_may_print(__FILE__, __LINE__);
    database_manager_drop(databaseManager);
    connection_close(connection);
    return success;
}

bool test_concept_api_data() {
    const char databaseName[] = "test_concept_api";

    Connection* connection = NULL;
    DatabaseManager* databaseManager = NULL;
    Session* session = NULL;
    Transaction* transaction = NULL;
    Options* opts = NULL;

    Concept* nameType = NULL;

    bool success = false;

    connection = connection_open_core(TYPEDB_CORE_ADDRESS);
    if (FAILED()) goto cleanup;

    databaseManager = database_manager_new(connection);
    if (FAILED()) goto cleanup;

    delete_database_if_exists(databaseManager, databaseName);
    if (FAILED()) goto cleanup;

    databases_create(databaseManager, databaseName);
    if (FAILED()) goto cleanup;

    opts = options_new();
    if (FAILED()) goto cleanup;

    // Set up schema
    {
        session = session_new(databaseManager, databaseName, Schema, opts);
        if (FAILED()) goto cleanup;

        transaction = transaction_new(session, Write, opts);
        if (FAILED()) goto cleanup;

        {
            Concept* definedNameType =
                concept_promise_resolve(concepts_put_attribute_type(transaction, "name", String));
            if (FAILED()) goto cleanup;
            else concept_drop(definedNameType);
        }

        void_promise_resolve(transaction_commit(transaction));
        transaction = NULL;

        session_close(session);
        session = NULL;
    }

    // Test data API
    {
        session = session_new(databaseManager, databaseName, Data, opts);
        if (FAILED()) goto cleanup;

        transaction = transaction_new(session, Write, opts);
        if (FAILED()) goto cleanup;
        if (NULL == (nameType = concept_promise_resolve(concepts_get_attribute_type(transaction, "name"))))
            goto cleanup;
        {
            Concept* valueOfJohn = NULL;
            Concept* insertedJohn = NULL;
            bool success =
                NULL != (valueOfJohn = value_new_string("John")) &&
                NULL !=
                    (insertedJohn = concept_promise_resolve(attribute_type_put(transaction, nameType, valueOfJohn)));
            concept_drop(insertedJohn);
            concept_drop(valueOfJohn);
            if (!success) goto cleanup;
        }

        bool foundJohn = false;
        {
            ConceptIterator* it = attribute_type_get_instances(transaction, nameType, Transitive);
            if (FAILED()) goto cleanup;

            Concept* concept;
            while (NULL != (concept = concept_iterator_next(it))) {
                Concept* asValue = attribute_get_value(concept);
                char* attr = value_get_string(asValue);
                foundJohn = foundJohn || (0 == strcmp(attr, "John"));
                string_free(attr);
                concept_drop(asValue);
                concept_drop(concept);
            }
            concept_iterator_drop(it);
        }

        void_promise_resolve(transaction_commit(transaction));
        transaction = NULL;

        if (!foundJohn) {
            fprintf(stderr, "Did not find inserted name \'John\' in query result.\n");
            goto cleanup;
        }
    }

    success = true;

cleanup:
    concept_drop(nameType);

    transaction_close(transaction);
    session_close(session);
    options_drop(opts);

    delete_database_if_exists(databaseManager, databaseName);
    check_error_may_print(__FILE__, __LINE__);
    database_manager_drop(databaseManager);
    connection_close(connection);
    return success;
}
