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

#include <typedb_driver.h>

const char* TYPEDB_CORE_ADDRESS = "127.0.0.1:1729";


bool check_error_may_print(const char* filename, int lineno) {
    if (check_error()) {
        Error* error = get_last_error();
        char* errcode = error_code(error);
        char* errmsg = error_message(error);
        fprintf(stderr, "Error!\nCheck called at %s:%d\n%s: %s\n", filename, lineno, errcode, errmsg);
        string_free(errmsg);
        string_free(errcode);
        error_drop(error);
        return true;
    } else return false;
}

#define FAILED() check_error_may_print(__FILE__, __LINE__)

int main() {
    const char databaseName[] = "test_assembly_clib";

    Connection* connection = NULL;
    DatabaseManager* databaseManager = NULL;

    bool success = false;

    connection = connection_open_core(TYPEDB_CORE_ADDRESS);
    if (FAILED()) goto cleanup;

    databaseManager = database_manager_new(connection);
    if (FAILED()) goto cleanup;

    if (databases_contains(databaseManager, databaseName)) {
        database_delete(databases_get(databaseManager, databaseName));
    }
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
    check_error_may_print(__FILE__, __LINE__);
    database_manager_drop(databaseManager);
    connection_close(connection);

    printf("Success: %s\n", success ? "true" : "false");
    return 0;
}
