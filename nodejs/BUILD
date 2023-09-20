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

exports_files([
    "pnpm-lock.yaml",
    "package.json",
    "tsconfig.json",
    "package-lock.json",
    "RELEASE_TEMPLATE.md",
    "VERSION",
])

load("@vaticle_bazel_distribution//npm:rules.bzl", "assemble_npm", "deploy_npm")
load("@vaticle_dependencies//tool/checkstyle:rules.bzl", "checkstyle_test")
load("@vaticle_dependencies//distribution:deployment.bzl", "deployment")
load("@aspect_rules_ts//ts:defs.bzl", "ts_project")
load("@aspect_rules_js//npm:defs.bzl", "npm_link_package", "npm_package")
load("@aspect_rules_js//js:defs.bzl", "js_binary", "js_run_binary")
load("@npm//:defs.bzl", "npm_link_all_packages")
#load(":typedoc.bzl", "typedoc")

npm_link_all_packages(
    name = "node_modules",
)

npm_link_package(
    name = "typedb-protocol",
    root_package = "nodejs",
    src = "@vaticle_typedb_protocol//grpc/nodejs:typedb-protocol-package",
)

genrule(
    name = "typedb-protocol-override",
    srcs = ["@vaticle_typedb_protocol//grpc/nodejs:typedb-protocol-package"],
    outs = ["dist/node_modules/typedb-protocol"],
    cmd = "cp -r $(SRCS) $(OUTS)",
    visibility = ["//visibility:public"],
)

ts_project(
    name = "driver-nodejs",
    srcs = glob([
        "*.ts",
        "api/**/*.ts",
        "common/**/*.ts",
        "connection/**/*.ts",
        "concept/**/*.ts",
        "logic/**/*.ts",
        "query/**/*.ts",
        "stream/**/*.ts",
        "user/**/*.ts",
    ]),
    tsconfig = ":tsconfig.json",
    declaration = True,
    deps = [
        ":typedb-protocol",

        ":node_modules/google-protobuf",
        ":node_modules/@types/google-protobuf",

        ":node_modules/@grpc/grpc-js",
        ":node_modules/@types/node",
        ":node_modules/typescript",

        ":node_modules/uuid",
        ":node_modules/@types/uuid",
    ],
    visibility = ["//visibility:public"],
    out_dir = "dist",
)

npm_package(
    name = "driver-nodejs-npm-package",
    srcs = [":driver-nodejs", "package.json"],
    include_runfiles = False,
    package = "typedb-driver",
    visibility = ["//visibility:public"],
)

typedoc(
    name = "driver_doc",
    entry_points = ["index.ts"],
    data = [
        ":driver-nodejs",
        "*.ts",
        "api/**/*.ts",
        "common/**/*.ts",
        "connection/**/*.ts",
        "concept/**/*.ts",
        "logic/**/*.ts",
        "query/**/*.ts",
        "stream/**/*.ts",
        "user/**/*.ts",
    ],
    visibility = ["//visibility:public"],
    out_dir = ".",
)

ts_project(
    name = "typedoc_runner",
    srcs = glob([
        "doc/TypeDocRunner.ts",
    ]),
    tsconfig = "doc/tsconfig.json",
    declaration = True,
    deps = [
        ":client-nodejs",
        ":node_modules/typedoc",
    ],
    visibility = ["//visibility:public"],
    out_dir = ".",
)

assemble_npm(
    name= "assemble-npm",
    target = ":driver-nodejs-npm-package"
)

deploy_npm(
    name = "deploy-npm",
    target = ":assemble-npm",
    snapshot = deployment["npm.snapshot"],
    release = deployment["npm.release"],
)

js_binary(
    name = "typedoc_runner_bin",
    data = [":typedoc_runner", ":client-nodejs", "doc/tsconfig.json"],
    entry_point = ":typedoc_runner",
    chdir = "nodejs/dist",
)

js_run_binary(
    name = "driver_doc",
    tool = ":typedoc_runner_bin",
    out_dirs = ["doc.build"],
)

checkstyle_test(
    name = "checkstyle",
    include = glob([
        "*",
        "api/**/*.ts",
        "common/**/*.ts",
        "concept/**/*.ts",
        "connection/**/*.ts",
        "logic/**/*.ts",
        "query/**/*.ts",
        "stream/**/*.ts",
        "user/**/*.ts",
    ]),
    exclude = glob([
        "*.json",
        "*.md",
        ".eslintrc",
        "dist/**/*.*",
        "pnpm-lock.yaml",
    ]),
    license_type = "apache-header",
)

# CI targets that are not declared in any BUILD file, but are called externally
filegroup(
    name = "ci",
    data = [
        "@vaticle_dependencies//distribution/artifact:create-netrc",
        "@vaticle_dependencies//tool/release/notes:create",
        "@vaticle_dependencies//tool/release/notes:validate",
    ],
)
