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

#define NOT_NULL(RES, FN_CALL) if(!((RES) = (FN_CALL)) ){return 1;}
#define OK(OP) {int errno=(OP); if(errno) return errno;}

#define ASSERT(COND, MSG)


#ifdef TEST_TYPEDB_ENTERPRISE
    #define RUN_TEST(A) run_test_enterprise(#A, A)
#else
    #define RUN_TEST(A) run_test_core(#A, A)
#endif



void print_error();

int run_test_core(const char* test_name, const char* test_fn);

int run_test_enterprise(const char* test_name, const char* test_fn);