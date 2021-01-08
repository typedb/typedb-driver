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

exports_files([
    "node_modules",
    "package.json",
    "package-lock.json",
    "RELEASE_TEMPLATE.md",
    "VERSION",
])

load("@build_bazel_rules_nodejs//:index.bzl", "pkg_npm", "nodejs_binary")
load("@graknlabs_bazel_distribution//npm:rules.bzl", "assemble_npm", "deploy_npm")
load("@graknlabs_bazel_distribution//github:rules.bzl", "deploy_github")
load("@graknlabs_bazel_distribution//artifact:rules.bzl", "artifact_extractor")
load("@graknlabs_dependencies//tool/checkstyle:rules.bzl", "checkstyle_test")
load("@graknlabs_dependencies//tool/release:rules.bzl", "release_validate_nodejs_deps")
load("@graknlabs_dependencies//distribution:deployment.bzl", "deployment")
load("//:deployment.bzl", github_deployment = "deployment")

load("@npm//@bazel/typescript:index.bzl", "ts_library")

genrule(
    name = "client-nodejs-compiled",
    outs = ["client-nodejs.tar.gz"],
    cmd = "npx tsc; tar -cf $(@D)/client-nodejs.tar.gz dist;",
    tools = [
        "//:client-nodejs-ts",
        "//:package.json",
        "//:package-lock.json",
    ],
    visibility = ["//visibility:public"],
)

filegroup(
    name = "client-nodejs-ts",
    srcs = glob([
        "*.ts",
        "common/**/*.ts",
        "concept/**/*.ts",
        "query/**/*.ts",
        "rpc/**/*.ts",
        "tsconfig.json",
        "node_modules/**"
    ]),
)

filegroup(
    name = "behavioural-steps",
    srcs = [
        "//test/behaviour/config:Parameters.ts",
        "//test/behaviour/connection:ConnectionSteps.ts",
        "//test/behaviour/connection/database:DatabaseSteps.ts",
        "//test/behaviour/connection/session:SessionSteps.ts",
        "//test/behaviour/connection/transaction:TransactionSteps.ts",
        "//test/behaviour/graql/language/define:DefineSteps.ts",
        "//test/behaviour/util:Util.ts",
        "//:tsconfig-test.json"
    ] + glob(["node_modules/**"]),
    visibility = ["//test/behaviour:__pkg__"],
)

ts_library(
    name = "_client_nodejs",
    srcs = glob([
        "*.ts",
        "common/**/*.ts",
        "concept/**/*.ts",
        "query/**/*.ts",
        "rpc/**/*.ts",
    ]),
    module_name = "grakn-client",
    module_root = ".",
    tsconfig = "tsconfig.json",
    deps = [
        "@npm//@grpc/grpc-js",
        "@npm//grakn-protocol",
        "@npm//@types/node",
    ],
)

pkg_npm(
    name = "client-nodejs",
    package_name = "grakn-client",
    srcs = glob([
       "package.json",
       "README.md",
       ".npmignore",
    ]),
    deps = [
        "@npm//grakn-protocol",
        "@npm//@grpc/grpc-js",
        "@npm//google-protobuf",
        ":_client_nodejs",
    ],
    visibility = ["//visibility:public"],
    vendor_external = [],
)

assemble_npm(
    name = "assemble-npm",
    target = ":client-nodejs",
)

deploy_npm(
    name = "deploy-npm",
    target = ":assemble-npm",
    snapshot = deployment["npm.snapshot"],
    release = deployment["npm.release"],
)

deploy_github(
    name = "deploy-github",
    release_description = "//:RELEASE_TEMPLATE.md",
    title = "Grakn Client Node.js",
    title_append_version = True,
    organisation = github_deployment["github.organisation"],
    repository = github_deployment["github.repository"],
    draft = False
)

NODEJS_TEST_DEPENDENCIES = [
    ":client-nodejs",
    "@npm//fs-extra",
    "@npm//google-protobuf",
    "@npm//grpc",
    "@npm//tmp",
    "@npm//properties-reader",
]

genrule(
    name = "grakn-artifact-path",
    srcs = ["@graknlabs_grakn_core_artifact_mac//file"],
    outs = ["grakn-artifact-path.txt"],
    cmd = "echo $(location @graknlabs_grakn_core_artifact_mac//file) > \"$@\"",
)

artifact_extractor(
    name = "grakn-extractor",
    artifact = "@graknlabs_grakn_core_artifact_linux//file",
)

release_validate_nodejs_deps(
    name = "release-validate-nodejs-deps",
    package_json = "//:package.json",
    tagged_deps = ["grakn-protocol"]
)

checkstyle_test(
    name = "checkstyle",
    include = glob([
        "*",
        "common/**/*.ts",
        "concept/**/*.ts",
        "query/**/*.ts",
        "rpc/**/*.ts",
        ".grabl/automation.yml",
    ]),
    exclude = glob([
        "dist/**/*.*",
        "**/*.json",
        ".eslintrc",
    ]),
    license_type = "apache",
)

# CI targets that are not declared in any BUILD file, but are called externally
filegroup(
    name = "ci",
    data = [
        "@graknlabs_dependencies//tool/bazelrun:rbe",
        "@graknlabs_dependencies//distribution/artifact:create-netrc",
        "@graknlabs_dependencies//tool/sync:dependencies",
        "@graknlabs_dependencies//tool/release:approval",
        "@graknlabs_dependencies//tool/release:create-notes",
    ],
)
