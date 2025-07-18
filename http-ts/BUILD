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

exports_files([
    "pnpm-lock.yaml",
    "package.json",
    "tsconfig.json",
])

load("@typedb_bazel_distribution//npm:rules.bzl", "assemble_npm", "deploy_npm")
load("@typedb_dependencies//tool/checkstyle:rules.bzl", "checkstyle_test")
load("@typedb_dependencies//distribution:deployment.bzl", "deployment")
load("@aspect_rules_ts//ts:defs.bzl", "ts_project")
load("@aspect_rules_js//js:defs.bzl", "js_binary")
load("@aspect_rules_js//npm:defs.bzl", "npm_link_package", "npm_package")
load("@aspect_bazel_lib//lib:jq.bzl", "jq")
load("@http-ts_npm//:defs.bzl", "npm_link_all_packages")
load("//http-ts:tool/typedoc/rules.bzl", "typedoc_docs")
load("//http-ts:docs_structure.bzl", "dir_mapping", "static_function_mapping", "class_nesting_prefixes")
load("//tool/docs:httpts/rules.bzl", "typedoc_to_adoc")

npm_link_all_packages(
    name = "node_modules",
)

ts_project(
    name = "driver-http-ts",
    srcs = glob(["*.ts"]),
    tsconfig = ":tsconfig.json",
    declaration = True,
    deps = [":node_modules/typescript"],
    transpiler = "tsc",
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
        ".version = ($stamp.STABLE_VERSION // \"0.0.0\")"
    ]),
)

npm_package(
    name = "driver-http-ts-npm-package",
    srcs = [":driver-http-ts", ":package", "README.md"],
    include_runfiles = False,
    replace_prefixes = { "dist/": "" },
    visibility = ["//visibility:public"],
)

assemble_npm(
    name= "assemble-npm",
    target = ":driver-http-ts-npm-package",
)

deploy_npm(
    name = "deploy-npm",
    target = ":assemble-npm",
    snapshot = deployment['npm']['snapshot'],
    release = deployment['npm']['release'],
)

checkstyle_test(
    name = "checkstyle",
    include = glob([
        "*",
    ]),
    exclude = glob([
        "*.json",
        "*.md",
        "pnpm-lock.yaml",
    ]),
    license_type = "apache-header",
)

# This rule should be in tool/typedoc, but there is a problem with the path configurations
# when typedoc script is placed below the modules being documented.
ts_project(
    name = "typedoc_runner",
    srcs = glob(["tool/typedoc/TypeDocRunner.ts",]),
    tsconfig = "tool/typedoc/tsconfig.json",
    declaration = True,
    deps = [
        ":node_modules/typedoc",
        ":node_modules/@types/node",
    ],
    transpiler = "tsc",
    visibility = ["//visibility:public"],
    out_dir = ".",
)

js_binary(
    name = "typedoc_runner_bin",
    data = [
        ":typedoc_runner",
    ],
    entry_point = ":typedoc_runner",
    chdir = "http-ts",
)

typedoc_docs(
    name = "docs_html",
    script = ":typedoc_runner_bin",
    deps = [":node_modules/@types/node"],
    srcs = glob([
        "*.ts",
        "tsconfig.json",
        "tool/**/*.json",
    ]),
    out = "driver_docs_typedoc",
)

typedoc_to_adoc(
    name = "docs_adoc",
    data = [":docs_html"],
    docs_dirs = dir_mapping,
    static_functions = static_function_mapping,
    nested_classes = class_nesting_prefixes
)
