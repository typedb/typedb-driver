#
# Copyright (C) 2022 Vaticle
#
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.
#

load("//csharp:build_opts.bzl", "target_frameworks", "targeting_packs")
load("//csharp:rules.bzl", "csharp_behaviour_test")


csharp_behaviour_test(
    name = "simple-test",
    steps = ["DatabaseTest.cs"],
    features = ["@vaticle_typedb_behaviour//connection:database.feature"],
    deps = [],
    target_frameworks = target_frameworks,
    targeting_packs = targeting_packs,
)

csharp_behaviour_test(
    name = "complex-test",
    steps = ["DatabaseSteps.cs"],
    features = ["@vaticle_typedb_behaviour//connection:database.feature"],
    deps = [
        "//csharp/test/behaviour/connection:steps-base",
        "//csharp/test/behaviour/connection:steps-core",
        "//csharp/api",
        "//csharp/common",
    ],
    target_frameworks = target_frameworks,
    targeting_packs = targeting_packs,
)
