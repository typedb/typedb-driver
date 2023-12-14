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
])

load("@vaticle_bazel_distribution//npm:rules.bzl", "assemble_npm", "deploy_npm")
load("@vaticle_dependencies//tool/checkstyle:rules.bzl", "checkstyle_test")
load("@vaticle_dependencies//distribution:deployment.bzl", "deployment")
load("@aspect_rules_ts//ts:defs.bzl", "ts_project")
load("@aspect_rules_js//js:defs.bzl", "js_binary")
load("@aspect_rules_js//npm:defs.bzl", "npm_link_package", "npm_package")
load("@aspect_bazel_lib//lib:jq.bzl", "jq")
load("@npm//:defs.bzl", "npm_link_all_packages")
load("//nodejs:tool/typedoc/rules.bzl", "typedoc_docs")
load("//tool/docs:nodejs/rules.bzl", "typedoc_to_adoc")
load("//nodejs:docs_structure.bzl", "dir_mapping")

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

genrule(
    name = "version",
    outs = ["dist/VERSION"],
    srcs = ["//:VERSION"],
    cmd = "cp $< $@",
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
    assets = [":version"],
    visibility = ["//visibility:public"],
    out_dir = "dist",
)

jq(
    name = "package",
    srcs = ["package.json"],
    filter = "|".join([
        # Don't directly reference $STAMP as it's only set when stamping
        # This 'as' syntax results in $stamp being null in unstamped builds.
        "$ARGS.named.STAMP as $stamp",
        # Provide a default using the "alternative operator" in case $stamp is null.
        ".dependencies[\"typedb-protocol\"] = $stamp.STABLE_PROTOCOL_VERSION",
        ".version = ($stamp.STABLE_VERSION // \"0.0.0\")"
    ]),
)

npm_package(
    name = "driver-nodejs-npm-package",
    srcs = [":driver-nodejs", ":package", ":version"],
    include_runfiles = False,
    package = "typedb-driver",
    replace_prefixes = { "dist/": "" },
    visibility = ["//visibility:public"],
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

# This rule should be in tool/typedoc, but there is a problem with the path configurations
# when typedoc script is placed below the modules being documented.
ts_project(
    name = "typedoc_runner",
    srcs = glob([
        "tool/typedoc/TypeDocRunner.ts",
    ]),
    tsconfig = "tool/typedoc/tsconfig.json",
    declaration = True,
    deps = [
        ":node_modules/typedoc",
        ":node_modules/@types/node",
    ],
    visibility = ["//visibility:public"],
    out_dir = ".",
)

js_binary(
    name = "typedoc_runner_bin",
    data = [
        ":typedoc_runner",
    ],
    entry_point = ":typedoc_runner",
    chdir = "nodejs",
)

typedoc_docs(
    name = "docs_html",
    script = ":typedoc_runner_bin",
    deps = [
        ":typedb-protocol",
        ":node_modules/@grpc/grpc-js",
        ":node_modules/@types/node",
        ":node_modules/@types/uuid",
    ],
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
        "tsconfig.json",
        "tool/**/*.json",
    ]),
    out = "driver_docs_typedoc",
)

typedoc_to_adoc(
    name = "docs_adoc",
    data = [":docs_html"],
    docs_dirs = dir_mapping,
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
        "tool/typedoc/*",
    ]),
    exclude = glob([
        "*.json",
        "tool/typedoc/*.json",
        "*.md",
        ".eslintrc",
        "dist/**/*.*",
        "pnpm-lock.yaml",
        "docs/**/*.adoc",
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
