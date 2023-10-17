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

#include "gtest/gtest.h"

extern "C" {
#include "c/tests/integration/tests.h"
}

TEST(TestDatabaseManagement, TestDatabaseManagement) {
    EXPECT_TRUE(test_database_management());
}

TEST(TestQuery, TestSchema) {
    EXPECT_TRUE(test_query_schema());
}
TEST(TestQuery, TestData) {
    EXPECT_TRUE(test_query_data());
}

TEST(TestConceptAPI, TestSchema) {
    EXPECT_TRUE(test_concept_api_schema());
}
TEST(TestConceptAPI, TestData) {
    EXPECT_TRUE(test_concept_api_data());
}

int main(int argc, char **argv) {
    ::testing::InitGoogleTest(&argc, argv);
    return RUN_ALL_TESTS();
}
