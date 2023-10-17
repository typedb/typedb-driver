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

#include "c/typedb_driver.h"
#include "common.h"

const char* TYPEDB_CORE_ADDRESS = "127.0.0.1:1729";

bool check_error_may_print(const char* filename, int lineno) {
    if (check_error()) {
        Error* error = get_last_error();
        char* errcode = error_code(error);
        char* errmsg = error_message(error);
        fprintf(stderr, "Error!\nCheck called at %s:%d\n%s: %s\n", filename, lineno, errcode, errmsg);
        free(errmsg);
        free(errcode);
        error_drop(error);
        return true;
    } else return false;
}

void delete_database_if_exists(DatabaseManager* dbMgr, const char* name) {
    if (NULL != dbMgr && databases_contains(dbMgr, name)) {
        Database* database = databases_get(dbMgr, name);
        database_delete(database);
    }
}
