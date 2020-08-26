#
# Copyright (C) 2020 Grakn Labs
#
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU Affero General Public License as
# published by the Free Software Foundation, either version 3 of the
# License, or (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU Affero General Public License for more details.
#
# You should have received a copy of the GNU Affero General Public License
# along with this program.  If not, see <https://www.gnu.org/licenses/>.
#

exports_files([
    "package.json",
    "yarn.lock",
    "VERSION",
    "RELEASE_TEMPLATE.md",
])

load("@build_bazel_rules_nodejs//:index.bzl", "pkg_npm", "nodejs_binary")
load("@npm//@bazel/jasmine:index.bzl", "jasmine_node_test")
load("@graknlabs_bazel_distribution//npm:rules.bzl", "assemble_npm", "deploy_npm")
load("@graknlabs_bazel_distribution//github:rules.bzl", "deploy_github")
load("@graknlabs_bazel_distribution//artifact:rules.bzl", "artifact_extractor")

load("@graknlabs_dependencies//tool/release:rules.bzl", "release_validate_deps")
load("@graknlabs_dependencies//distribution:deployment.bzl", "deployment")
load("//:deployment.bzl", github_deployment = "deployment")


pkg_npm(
    name = "client-nodejs",
    srcs = glob([
       "package.json",
       "src/*.js",
       "src/service/Keyspace/KeyspaceService.js",
       "src/service/Session/*.js",
       "src/service/Session/util/*.js",
       "src/service/Session/concept/*.js",
       "package.json",
       "README.md",
       ".npmignore"
    ]),
    deps = [
        "@graknlabs_protocol//grpc/nodejs:protocol",
        "@npm//grpc",
        "@npm//google-protobuf"
    ],
    visibility = ["//visibility:public"],
    vendor_external = [
        "graknlabs_protocol"
    ]
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
    organisation = deployment["npm.snapshot"],
    repository = deployment["npm.release"],
)

NODEJS_TEST_DEPENDENCIES = [
    ":client-nodejs",
    "@npm//fs-extra",
    "@npm//google-protobuf",
    "@npm//grpc",
    "@npm//tmp",
    "@npm//properties-reader",
    "@npm//jasmine-reporters",
]

genrule(
    name = "grakn-artifact-path",
    srcs = ["@graknlabs_grakn_core_artifact//file"],
    outs = ["grakn-artifact-path.txt"],
    cmd = "echo $(location @graknlabs_grakn_core_artifact//file) > \"$@\"",
)

NODEJS_TEST_DATA = [
    ":grakn-artifact-path",
    "@graknlabs_grakn_core_artifact//file",
    "tests/support/basic-genealogy.gql",
]

jasmine_node_test(
    name = "keyspace-test",
    srcs = [
        "tests/support/GraknTestEnvironment.js",
        "tests/service/keyspace/Keyspace.test.js",
    ],
    deps = NODEJS_TEST_DEPENDENCIES,
    data = NODEJS_TEST_DATA,
)

jasmine_node_test(
    name = "concept-test",
    srcs = [
        "tests/support/GraknTestEnvironment.js",
        "tests/service/session/transaction/Concept.test.js",
    ],
    deps = NODEJS_TEST_DEPENDENCIES,
    data = NODEJS_TEST_DATA,
)

jasmine_node_test(
    name = "schemaconcept-test",
    srcs = [
        "tests/support/GraknTestEnvironment.js",
        "tests/service/session/transaction/SchemaConcept.test.js",
    ],
    deps = NODEJS_TEST_DEPENDENCIES,
    data = NODEJS_TEST_DATA,
)

jasmine_node_test(
    name = "rule-test",
    srcs = [
        "tests/support/GraknTestEnvironment.js",
        "tests/service/session/transaction/Rule.test.js",
    ],
    deps = NODEJS_TEST_DEPENDENCIES,
    data = NODEJS_TEST_DATA,
)

jasmine_node_test(
    name = "type-test",
    srcs = [
        "tests/support/GraknTestEnvironment.js",
        "tests/service/session/transaction/Type.test.js",
    ],
    deps = NODEJS_TEST_DEPENDENCIES,
    data = NODEJS_TEST_DATA,
)

jasmine_node_test(
    name = "attribute-test",
    srcs = [
        "tests/support/GraknTestEnvironment.js",
        "tests/service/session/transaction/Attribute.test.js",
    ],
    deps = NODEJS_TEST_DEPENDENCIES,
    data = NODEJS_TEST_DATA,
)

jasmine_node_test(
    name = "attributetype-test",
    srcs = [
        "tests/support/GraknTestEnvironment.js",
        "tests/service/session/transaction/AttributeType.test.js",
    ],
    deps = NODEJS_TEST_DEPENDENCIES,
    data = NODEJS_TEST_DATA,
)

jasmine_node_test(
    name = "role-test",
    srcs = [
        "tests/support/GraknTestEnvironment.js",
        "tests/service/session/transaction/Role.test.js",
    ],
    deps = NODEJS_TEST_DEPENDENCIES,
    data = NODEJS_TEST_DATA,
)

jasmine_node_test(
    name = "grakntx-test",
    srcs = [
        "tests/support/GraknTestEnvironment.js",
        "tests/service/session/transaction/GraknTx.test.js",
    ],
    deps = NODEJS_TEST_DEPENDENCIES,
    data = NODEJS_TEST_DATA,
)

jasmine_node_test(
    name = "relation-test",
    srcs = [
        "tests/support/GraknTestEnvironment.js",
        "tests/service/session/transaction/Relation.test.js",
    ],
    deps = NODEJS_TEST_DEPENDENCIES,
    data = NODEJS_TEST_DATA,
)

jasmine_node_test(
    name = "relationtype-test",
    srcs = [
        "tests/support/GraknTestEnvironment.js",
        "tests/service/session/transaction/RelationType.test.js",
    ],
    deps = NODEJS_TEST_DEPENDENCIES,
    data = NODEJS_TEST_DATA,
)

jasmine_node_test(
    name = "thing-test",
    srcs = [
        "tests/support/GraknTestEnvironment.js",
        "tests/service/session/transaction/Thing.test.js",
    ],
    deps = NODEJS_TEST_DEPENDENCIES,
    data = NODEJS_TEST_DATA,
)

jasmine_node_test(
    name = "entitytype-test",
    srcs = [
        "tests/support/GraknTestEnvironment.js",
        "tests/service/session/transaction/EntityType.test.js",
    ],
    deps = NODEJS_TEST_DEPENDENCIES,
    data = NODEJS_TEST_DATA,
)

jasmine_node_test(
    name = "committx-test",
    srcs = [
        "tests/support/GraknTestEnvironment.js",
        "tests/service/session/transaction/CommitTx.test.js",
    ],
    deps = NODEJS_TEST_DEPENDENCIES,
    data = NODEJS_TEST_DATA,
)

jasmine_node_test(
    name = "query-test",
    srcs = [
        "tests/support/GraknTestEnvironment.js",
        "tests/service/session/transaction/Query.test.js",
    ],
    deps = NODEJS_TEST_DEPENDENCIES,
    data = NODEJS_TEST_DATA,
)

test_suite(
    name = "test-integration",
    tests = [
        ":keyspace-test",
        ":concept-test",
        ":schemaconcept-test",
        ":rule-test",
        ":type-test",
        ":attribute-test",
        ":attributetype-test",
        ":role-test",
        ":grakntx-test",
        ":relation-test",
        ":relationtype-test",
        ":thing-test",
        ":entitytype-test",
        ":committx-test",
        ":query-test",
    ]
)

artifact_extractor(
    name = "grakn-extractor",
    artifact = "@graknlabs_grakn_core_artifact//file",
)

release_validate_deps(
    name = "release-validate-deps",
    refs = "@graknlabs_client_nodejs_workspace_refs//:refs.json",
    tagged_deps = [
        "graknlabs_protocol",
    ],
    tags = ["manual"]  # in order for bazel test //... to not fail
)
