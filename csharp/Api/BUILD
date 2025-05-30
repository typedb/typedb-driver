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

package(default_visibility = ["//visibility:public"])

load("//csharp:build_opts.bzl", "nullable_context", "target_frameworks", "targeting_packs")
load("@rules_dotnet//dotnet:defs.bzl", "csharp_library")
load("@typedb_dependencies//tool/checkstyle:rules.bzl", "checkstyle_test")

csharp_library(
    name = "api",
    srcs = glob(["*.cs", "*/*.cs", "*/*/*.cs"]),
    deps = [
        "//csharp/Common:common",
        "//csharp:typedb_driver_pinvoke",
        "@paket.csharp_deps//newtonsoft.json",
    ],
    out = "TypeDB.Driver.Api",
    nullable = nullable_context,
    target_frameworks = target_frameworks,
    targeting_packs = targeting_packs,
)

filegroup(
    name = "docs_source_files",
    srcs = glob(["*.cs", "*/*.cs", "*/*/*.cs"]),
    visibility = ["//csharp:__pkg__"],
)

checkstyle_test(
    name = "checkstyle",
    include = glob(["*", "*/*", "*/*/*"]),
    license_type = "apache-header",
    size = "small",
)
