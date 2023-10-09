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

char TYPEDB_CORE_ADDRESS[] = "127.0.0.1:1729";

int run_test_core(const char* test_name, int (*test_fn)(const Connection*) ) {
    fprintf(stdout, "Running test %s on core\n", test_name);
    int errno = -1;
    {
        Connection* conn = connection_open_core(TYPEDB_CORE_ADDRESS);
        if (0 == conn) {
            PRINT_ERR();
            return 1;
        }
        errno = (*test_fn)(conn);
        if (errno) PRINT_ERR();
        connection_close(conn);
    }
    return errno;
}

bool print_error(char* filename, int lineno) {
    fflush(stdout);
    if (check_error()) {
        Error* error = get_last_error();
        char* errcode = error_code(error);
        char* errmsg = error_message(error);
        fprintf(stderr, "Error!\nCheck called at %s:%d\n%s: %s\n", filename, lineno, errcode, errmsg);
        fflush(stderr);
        free(errmsg);
        free(errcode);
        error_drop(error);
        return true;
    } else return false;
}

void delete_database_if_exists(DatabaseManager* dbMgr, char* name) {
    if (databases_contains(dbMgr, name)) {
        Database* database = databases_get(dbMgr, name);
        database_delete(database);
    }
}
