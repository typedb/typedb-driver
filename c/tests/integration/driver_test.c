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

// Avoid helpers so the tests serve as documentation for the C API.

bool test_database_management() {
    const char dbName[] = "test_database_management";

    Connection* conn = 0;
    DatabaseManager* dbMgr = 0;

    bool completed = false;

    conn = connection_open_core(TYPEDB_CORE_ADDRESS);
    if (check_error()) goto cleanup;

    dbMgr = database_manager_new(conn);
    if (check_error()) goto cleanup;

    delete_database_if_exists(dbMgr, dbName);
    if (check_error()) goto cleanup;

    databases_create(dbMgr, dbName);
    if (check_error()) goto cleanup;

    if (!databases_contains(dbMgr, dbName)) {
        fprintf(stderr, "databases_contains(\'%s\') failed\n", dbName);
        goto cleanup;
    }

    bool foundDB = false;
    DatabaseIterator* it = databases_all(dbMgr);
    Database* database = 0;
    while (0 != (database = database_iterator_next(it))) {
        char* name = database_get_name(database);
        foundDB = foundDB || (0 == strcmp(dbName, name));
        free(name);
        database_drop(database);
    }
    database_iterator_drop(it);

    if (!foundDB) {
        fprintf(stderr, "Did not find database \'%s\' in list of databases\n", dbName);
        goto cleanup;
    }

    completed = true;
cleanup:
    if (check_error()) print_error(__FILE__, __LINE__);
    delete_database_if_exists(dbMgr, dbName);
    if (check_error()) print_error(__FILE__, __LINE__);
    database_manager_drop(dbMgr);
    connection_close(conn);
    return completed;
}

bool test_query_schema() {
    const char dbName[] = "test_query_schema";

    Connection* conn = 0;
    DatabaseManager* dbMgr = 0;
    Session* session = 0;
    Transaction* transaction = 0;
    Options* opts = 0;

    bool completed = false;

    // Set up connection & database
    conn = connection_open_core(TYPEDB_CORE_ADDRESS);
    if (check_error()) goto cleanup;

    dbMgr = database_manager_new(conn);
    if (check_error()) goto cleanup;

    delete_database_if_exists(dbMgr, dbName);
    if (check_error()) goto cleanup;

    databases_create(dbMgr, dbName);
    if (check_error()) goto cleanup;

    opts = options_new();
    if (check_error()) goto cleanup;

    // test schema queries
    {
        session = session_new(dbMgr, dbName, Schema, opts);
        if (check_error()) goto cleanup;

        transaction = transaction_new(session, Write, opts);
        if (check_error()) goto cleanup;

        query_define(transaction, "define name sub attribute, value string;", opts);
        if (check_error()) goto cleanup;

        ConceptMapIterator* it = query_match(transaction, "match $t sub thing;", opts);
        ConceptMap* conceptMap;
        bool foundName = false;
        while (0 != (conceptMap = concept_map_iterator_next(it))) {
            Concept* concept = concept_map_get(conceptMap, "t");
            char* label = thing_type_get_label(concept);
            foundName = foundName || (0 == strcmp(label, "name"));
            free(label);
            concept_drop(concept);
            concept_map_drop(conceptMap);
        }
        concept_map_iterator_drop(it);

        transaction_commit(transaction);
        transaction = 0;

        if (!foundName) {
            fprintf(stderr, "Did not find type \'name\' in query result.\n");
            goto cleanup;
        }
    }
    completed = true;

cleanup:
    if (check_error()) print_error(__FILE__, __LINE__);
    transaction_drop(transaction);
    session_drop(session);
    options_drop(opts);

    delete_database_if_exists(dbMgr, dbName);
    if (check_error()) print_error(__FILE__, __LINE__);
    database_manager_drop(dbMgr);
    connection_close(conn);
    return completed;
}

bool test_query_data() {
    const char dbName[] = "test_query_data";

    Connection* conn = 0;
    DatabaseManager* dbMgr = 0;
    Session* session = 0;
    Transaction* transaction = 0;
    Options* opts = 0;

    bool completed = false;

    // Set up connection & database
    conn = connection_open_core(TYPEDB_CORE_ADDRESS);
    if (check_error()) goto cleanup;

    dbMgr = database_manager_new(conn);
    if (check_error()) goto cleanup;

    delete_database_if_exists(dbMgr, dbName);
    if (check_error()) goto cleanup;

    databases_create(dbMgr, dbName);
    if (check_error()) goto cleanup;

    opts = options_new();
    if (check_error()) goto cleanup;

    // Set up schema
    {
        session = session_new(dbMgr, dbName, Schema, opts);
        if (check_error()) goto cleanup;

        transaction = transaction_new(session, Write, opts);
        if (check_error()) goto cleanup;

        query_define(transaction, "define name sub attribute, value string;", opts);
        if (check_error()) goto cleanup;

        transaction_commit(transaction);
        transaction = 0;

        session_drop(session);
        session = 0;
    }

    {
        session = session_new(dbMgr, dbName, Data, opts);
        if (check_error()) goto cleanup;

        transaction = transaction_new(session, Write, opts);
        if (check_error()) goto cleanup;

        ConceptMapIterator* insertResult = query_insert(transaction, "insert $n \"John\" isa name;", opts);
        if (check_error()) goto cleanup;
        else concept_map_iterator_drop(insertResult);

        ConceptMapIterator* it = query_match(transaction, "match $n isa name;", opts);
        ConceptMap* conceptMap;
        bool foundJohn = false;
        while (0 != (conceptMap = concept_map_iterator_next(it)) && !check_error()) {
            Concept* concept = concept_map_get(conceptMap, "n");
            Concept* asValue = attribute_get_value(concept);
            char* attr = value_get_string(asValue);
            foundJohn = foundJohn || (0 == strcmp(attr, "John"));
            free(attr);
            concept_drop(asValue);
            concept_drop(concept);
            concept_map_drop(conceptMap);
        }
        concept_map_iterator_drop(it);

        transaction_commit(transaction);
        transaction = 0;

        if (!foundJohn) {
            fprintf(stderr, "Did not find inserted name \'John\' in query result.\n");
            goto cleanup;
        }
    }

    completed = true;

cleanup:
    if (check_error()) print_error(__FILE__, __LINE__);
    transaction_drop(transaction);
    session_drop(session);
    options_drop(opts);

    delete_database_if_exists(dbMgr, dbName);
    if (check_error()) print_error(__FILE__, __LINE__);
    database_manager_drop(dbMgr);
    connection_close(conn);
    return completed;
}

bool test_concept_api_schema() {
    const char dbName[] = "test_concept_api";

    Connection* conn = 0;
    DatabaseManager* dbMgr = 0;
    Session* session = 0;
    Transaction* transaction = 0;
    Options* opts = 0;

    bool completed = false;

    conn = connection_open_core(TYPEDB_CORE_ADDRESS);
    if (check_error()) goto cleanup;

    dbMgr = database_manager_new(conn);
    if (check_error()) goto cleanup;

    delete_database_if_exists(dbMgr, dbName);
    if (check_error()) goto cleanup;

    databases_create(dbMgr, dbName);
    if (check_error()) goto cleanup;

    opts = options_new();
    if (check_error()) goto cleanup;

    // test schema api
    {
        session = session_new(dbMgr, dbName, Schema, opts);
        if (check_error()) goto cleanup;

        transaction = transaction_new(session, Write, opts);
        if (check_error()) goto cleanup;
        {
            Concept* definedNameType = concepts_put_attribute_type(transaction, "name", String);
            if (check_error()) goto cleanup;
            else concept_drop(definedNameType);
        }

        {
            ConceptIterator* it = 0;
            Concept* nameType = 0;
            Concept* rootAttributeType = 0;
            bool foundName = false;
            if (
                0 != (nameType = concepts_get_attribute_type(transaction, "name")) &&
                0 != (rootAttributeType = concepts_get_attribute_type(transaction, "attribute")) &&
                0 != (it = attribute_type_get_subtypes(transaction, rootAttributeType, Transitive))) {
                Concept* concept;
                while (0 != (concept = concept_iterator_next(it))) {
                    char* label = thing_type_get_label(concept);
                    foundName = foundName || (0 == strcmp(label, "name"));
                    free(label);
                    concept_drop(concept);
                }
            }
            concept_iterator_drop(it);
            concept_drop(rootAttributeType);
            concept_drop(nameType);

            transaction_commit(transaction);
            transaction = 0;
            if (!foundName) {
                fprintf(stderr, "Did not find type \'name\' in subtypes of attribute.\n");
                goto cleanup;
            }
        }
    }

    completed = true;

cleanup:
    if (check_error()) print_error(__FILE__, __LINE__);

    transaction_drop(transaction);
    session_drop(session);
    options_drop(opts);

    delete_database_if_exists(dbMgr, dbName);
    if (check_error()) print_error(__FILE__, __LINE__);
    database_manager_drop(dbMgr);
    connection_close(conn);
    return completed;
}

bool test_concept_api_data() {
    const char dbName[] = "test_concept_api";

    Connection* conn = 0;
    DatabaseManager* dbMgr = 0;
    Session* session = 0;
    Transaction* transaction = 0;
    Options* opts = 0;

    Concept* nameType = 0;

    bool completed = false;

    conn = connection_open_core(TYPEDB_CORE_ADDRESS);
    if (check_error()) goto cleanup;

    dbMgr = database_manager_new(conn);
    if (check_error()) goto cleanup;

    delete_database_if_exists(dbMgr, dbName);
    if (check_error()) goto cleanup;

    databases_create(dbMgr, dbName);
    if (check_error()) goto cleanup;

    opts = options_new();
    if (check_error()) goto cleanup;

    // Set up schema
    {
        session = session_new(dbMgr, dbName, Schema, opts);
        if (check_error()) goto cleanup;

        transaction = transaction_new(session, Write, opts);
        if (check_error()) goto cleanup;

        {
            Concept* definedNameType = concepts_put_attribute_type(transaction, "name", String);
            if (check_error()) goto cleanup;
            else concept_drop(definedNameType);
        }

        transaction_commit(transaction);
        transaction = 0;

        session_drop(session);
        session = 0;
    }

    // Test data API
    {
        session = session_new(dbMgr, dbName, Data, opts);
        if (check_error()) goto cleanup;

        transaction = transaction_new(session, Write, opts);
        if (check_error()) goto cleanup;
        if (0 == (nameType = concepts_get_attribute_type(transaction, "name"))) goto cleanup;
        {
            Concept* valueOfJohn = 0;
            Concept* insertedJohn = 0;
            bool success = 0 != (valueOfJohn = value_new_string("John")) &&
                           0 != (insertedJohn = attribute_type_put(transaction, nameType, valueOfJohn));
            concept_drop(insertedJohn);
            concept_drop(valueOfJohn);
            if (!success) goto cleanup;
        }

        bool foundJohn = false;
        {
            ConceptIterator* it = attribute_type_get_instances(transaction, nameType, Transitive);
            if (check_error()) goto cleanup;

            Concept* concept;
            while (0 != (concept = concept_iterator_next(it))) {
                Concept* asValue = attribute_get_value(concept);
                char* attr = value_get_string(asValue);
                foundJohn = foundJohn || (0 == strcmp(attr, "John"));
                free(attr);
                concept_drop(asValue);
                concept_drop(concept);
            }
            concept_iterator_drop(it);
        }

        transaction_commit(transaction);
        transaction = 0;

        if (!foundJohn) {
            fprintf(stderr, "Did not find inserted name \'John\' in query result.\n");
            goto cleanup;
        }
    }

    completed = true;

cleanup:
    if (check_error()) print_error(__FILE__, __LINE__);
    concept_drop(nameType);

    transaction_drop(transaction);
    session_drop(session);
    options_drop(opts);

    delete_database_if_exists(dbMgr, dbName);
    if (check_error()) print_error(__FILE__, __LINE__);
    database_manager_drop(dbMgr);
    connection_close(conn);
    return completed;
}
