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
load("@aspect_rules_js//js:defs.bzl", "js_binary", "js_run_binary")
load("@aspect_rules_js//npm:defs.bzl", "npm_package")
load("@aspect_bazel_lib//lib:jq.bzl", "jq")
load("@http-ts_npm//:defs.bzl", "npm_link_all_packages")
load("//http-ts:tool/typedoc/rules.bzl", "typedoc_docs")
load("//http-ts:docs_structure.bzl", "dir_mapping", "static_function_mapping", "class_nesting_prefixes")
load("//tool/docs:httpts/rules.bzl", "typedoc_to_adoc")

npm_link_all_packages(
    name = "node_modules",
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

driver_lib_data = glob(["src/*.ts"]) + [
    "tsup.config.ts",
    "tsconfig.json",
    ":node_modules/typescript",
    ":node_modules/tsup",
]

js_binary(
    name = "tsup_bin",
    entry_point = ":node_modules/tsup/dist/cli-default.js",
    data = driver_lib_data,
    copy_data_to_bin = False,
)

# TODO: should also output index.d.cts (works in filesystem but not in bazel)
js_run_binary(
    name = "driver-lib",
    tool = ":tsup_bin",
    chdir = package_name(),
    srcs = driver_lib_data,
    outs = [
        "dist/index.cjs",
        "dist/index.d.ts",
        "dist/index.mjs",
    ],
    use_execroot_entry_point = False,
    visibility = ["//visibility:public"],
)

# TODO: figure out how to include LICENSE from repo root
npm_package(
    name = "npm-package",
    srcs = [":driver-lib", ":package", "README.md", "LICENSE"],
    include_runfiles = False,
    visibility = ["//visibility:public"],
)

assemble_npm(
    name = "assemble-npm",
    target = ":npm-package",
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
        "src/**/*",
        "tool/**/*",
    ]),
    exclude = glob([
        ".npmrc",
        "**/*.json",
        "**/*.md",
        "pnpm-lock.yaml",
        "tsup.config.ts",
        ".npmrc",
        "LICENSE",
        "docs/**/*",
        "dist/**/*",
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
    chdir = package_name(),
)

typedoc_docs(
    name = "docs_html",
    script = ":typedoc_runner_bin",
    deps = [":node_modules/@types/node"],
    srcs = glob([
        "src/*.ts",
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
