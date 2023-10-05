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

bool print_error(char* filename, int lineno) {
    fprintf(stdout, "Checking errors at : %s: %d\n", filename, lineno);
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

int run_test_core(const char* test_name, int (*test_fn)(const Connection*) ) {
    fprintf(stdout, "Running test %s on core\n", test_name);
    int errno = -1;
    {
        Connection* conn = connection_open_core("127.0.0.1:1729");
        errno = (*test_fn)(conn);
        if (errno) PRINT_ERR(); // TODO: Can I move this after connection close?
        connection_close(conn);
    }
    return errno;
}

int run_test_enterprise(const char* test_name, int (*test_fn)(const Connection*)) {
    fprintf(stdout, "Running test %s on enterprise\n", test_name);
    int errno = -1;
    {
        Connection* conn = connection_open_core("127.0.0.1:1729");
        errno = (*test_fn)(conn);
        if (errno) PRINT_ERR(); // TODO: Can I move this after connection close?
        connection_close(conn);
    }
    return errno;
}
