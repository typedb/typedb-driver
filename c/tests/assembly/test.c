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

#include <typedb_driver.h>

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

#define FAILED() check_error_may_print(__FILE__, __LINE__)
TypeDBDriver* driver_open_for_tests(const char* address, const char* username, const char* password) {
    DriverOptions* options = NULL;
    Credentials* creds = credentials_new(username, password);
    if (check_error_may_print(__FILE__, __LINE__)) goto cleanup;
    options = driver_options_new(false, NULL);;
    if (check_error_may_print(__FILE__, __LINE__)) goto cleanup;
    TypeDBDriver* driver = driver_open_with_description(address, creds, options, DRIVER_LANG);
cleanup:
    driver_options_drop(options);
    credentials_drop(creds);

    return driver;
}

int main() {
    const char databaseName[] = "test_assembly_clib";

   TypeDBDriver* driver = NULL;

    bool success = false;

    driver = driver_open_for_tests(TYPEDB_CORE_ADDRESS, TYPEDB_CORE_USERNAME, TYPEDB_CORE_PASSWORD);
    if (FAILED()) goto cleanup;

    databases_create(driver, databaseName);
    if (FAILED()) goto cleanup;

    if (!databases_contains(driver, databaseName)) {
        fprintf(stderr, "databases_contains(\'%s\') failed\n", databaseName);
        goto cleanup;
    }

    success = true;
cleanup:
    check_error_may_print(__FILE__, __LINE__);
    driver_close(driver);

    printf("Success: %s\n", success ? "true" : "false");
    return success ? 0 : 1;
}
