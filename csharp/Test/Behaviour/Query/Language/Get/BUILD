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

package(default_visibility = ["//csharp/Test/Behaviour:__subpackages__"])

load("//csharp:build_opts.bzl", "nullable_context", "target_frameworks", "targeting_packs")
load("//csharp/Test:build_opts.bzl", "behaviour_tests_deps")
load("//csharp/Test:rules.bzl", "csharp_behaviour_test")
load("@typedb_dependencies//tool/checkstyle:rules.bzl", "checkstyle_test")

csharp_behaviour_test(
    name = "test-core",
    test_files = ["GetTest.cs"],
    steps_files = [
        "//csharp/Test/Behaviour/Util:steps",
        "//csharp/Test/Behaviour/Query:steps",
        "//csharp/Test/Behaviour/Connection/Transaction:steps",
        "//csharp/Test/Behaviour/Connection/Session:steps",
        "//csharp/Test/Behaviour/Connection/Database:steps",
        "//csharp/Test/Behaviour/Connection:steps-core",
    ],
    features = ["@typedb_behaviour//query/language:get.feature"],
    deps = [
        "//csharp/Api:api",
        "//csharp/Common:common",
        "//csharp:driver-csharp",
        "//csharp/Test/Behaviour/Util:util",
        "@paket.csharp_deps//newtonsoft.json",
    ],
    target_frameworks = target_frameworks,
    targeting_packs = targeting_packs,
)

csharp_behaviour_test(
    name = "test-cloud",
    test_files = ["GetTest.cs"],
    steps_files = [
        "//csharp/Test/Behaviour/Util:steps",
        "//csharp/Test/Behaviour/Query:steps",
        "//csharp/Test/Behaviour/Connection/Transaction:steps",
        "//csharp/Test/Behaviour/Connection/Session:steps",
        "//csharp/Test/Behaviour/Connection/Database:steps",
        "//csharp/Test/Behaviour/Connection:steps-cloud",
    ],
    features = ["@typedb_behaviour//query/language:get.feature"],
    deps = [
        "//csharp/Api:api",
        "//csharp/Common:common",
        "//csharp:driver-csharp",
        "//csharp/Test/Behaviour/Util:util",
        "@paket.csharp_deps//newtonsoft.json",
    ],
    add_certificates = True,
    target_frameworks = target_frameworks,
    targeting_packs = targeting_packs,
)

checkstyle_test(
    name = "checkstyle",
    include = glob(["*"]),
    license_type = "apache-header",
)
