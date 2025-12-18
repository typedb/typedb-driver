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

#include "c/typedb_driver.h"
#include "common.h"

const char* TYPEDB_CORE_ADDRESS = "127.0.0.1:1729";
const char* TYPEDB_CORE_USERNAME = "admin";
const char* TYPEDB_CORE_PASSWORD = "password";
const char* DRIVER_LANG = "c";

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

void delete_database_if_exists(TypeDBDriver* driver, const char* name) {
    if (driver == NULL) return;
    bool contains = databases_contains(driver, name, NULL);
    if (check_error_may_print(__FILE__, __LINE__)) return;
    if (contains) {
        const Database* db = databases_get(driver, name, NULL);
        if (check_error_may_print(__FILE__, __LINE__)) return;
        database_delete(db, NULL);
    }
}

TypeDBDriver* driver_new_for_tests(const char* address, const char* username, const char* password) {
    DriverOptions* options = NULL;
    Credentials* creds = credentials_new(username, password);
    if (check_error_may_print(__FILE__, __LINE__)) goto cleanup;
    options = driver_options_new();
    driver_options_set_tls_enabled(options, false);
    if (check_error_may_print(__FILE__, __LINE__)) goto cleanup;
    TypeDBDriver* driver = driver_new_with_description(address, creds, options, DRIVER_LANG);
cleanup:
    driver_options_drop(options);
    credentials_drop(creds);

    return driver;
}
