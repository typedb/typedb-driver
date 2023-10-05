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

void print_error() {
    if (check_error()) {
        Error* error = get_last_error();
        char* errcode = error_code(error);
        char* errmsg = error_message(error);
        fprintf(stderr, "Error!\n%s: %s", errcode, errmsg);
        free(errmsg);
        free(errcode);
        error_drop(error);
    }
}

int run_test_core(const char* test_name, const char* test_fn) {
    fprintf(stdout, "Running test %s on core", test_name);
    int errno;
    {
        Connection* conn = connection_open_core("127.0.0.1:1729");
        run_test(conn);
        if (errno) print_error(); // TODO: Can I move this after connection close?
        connection_close(conn);
    }
    return errno;
}

int run_test_enterprise(const char* test_name, const char* test_fn) {
    fprintf(stdout, "Running test %s on enterprise", test_name);
    int errno;
    {
        Connection* conn = connection_open_core("127.0.0.1:1729");
        run_test(conn);
        if (errno) print_error(); // TODO: Can I move this after connection close?
        connection_close(conn);
    }
    return errno;
}
