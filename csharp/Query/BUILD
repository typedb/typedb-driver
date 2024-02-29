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

package(default_visibility = ["//visibility:public"])

load("@rules_dotnet//dotnet:defs.bzl", "csharp_library")
load("//csharp:build_opts.bzl", "target_frameworks", "targeting_packs", "nullable_context")
load("@vaticle_dependencies//tool/checkstyle:rules.bzl", "checkstyle_test")


csharp_library(
    name = "query",
    srcs = glob(["*.cs"]),
    deps = [
        "//csharp/Api:api",
        "//csharp/Common:common",
        "//csharp/Concept:concept",
        "//csharp/Logic:logic",
        "//csharp:typedb_driver_Pinvoke",
    ],
    nullable = nullable_context,
    target_frameworks = target_frameworks,
    targeting_packs = targeting_packs,
)


checkstyle_test(
    name = "checkstyle",
    include = glob(["*"]),
    license_type = "apache-header",
)
