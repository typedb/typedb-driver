#
# GRAKN.AI - THE KNOWLEDGE GRAPH
# Copyright (C) 2018 Grakn Labs Ltd
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
    "VERSION",
    "deployment.properties",
    "RELEASE_TEMPLATE.md",
])

load("@build_bazel_rules_nodejs//:defs.bzl", "npm_package", "nodejs_jest_test", "babel_library")
load("@graknlabs_bazel_distribution//npm:rules.bzl", "assemble_npm", "new_deploy_npm")
load("@graknlabs_bazel_distribution//github:rules.bzl", "deploy_github")


babel_library(
    name = 'bundle',
    srcs = glob([
       "src/*.js",
       "src/service/Keyspace/KeyspaceService.js",
       "src/service/Session/*.js",
       "src/service/Session/util/*.js",
       "src/service/Session/concept/*.js",
   ]),
   data = [
       "package.json",
       "README.md",
       ".npmignore"
   ],
   babel = "@nodejs_dependencies//@bazel/babel/bin:babel",
   babelrc = "babel.rc.js"
)


npm_package(
    name = "client-nodejs",
    deps = [
        "@graknlabs_protocol//grpc/nodejs:protocol",
        ":bundle",
        "@nodejs_dependencies//grpc",
        "@nodejs_dependencies//google-protobuf"
    ],
    visibility = ["//visibility:public"],
    vendor_external = [
        "graknlabs_protocol"
    ]
)

assemble_npm(
    name = "assemble-npm",
    target = ":client-nodejs",
    version_file = "//:VERSION",
)

new_deploy_npm(
    name = "deploy-npm",
    target = ":assemble-npm",
    deployment_properties = "@graknlabs_build_tools//:deployment.properties",
)


deploy_github(
    name = "deploy-github",
    release_description = "//:RELEASE_TEMPLATE.md",
    deployment_properties = "//:deployment.properties",
    version_file = "//:VERSION"
)

NODEJS_TEST_DEPENDENCIES = [
    ":client-nodejs",
    "@nodejs_dependencies//fs-extra",
    "@nodejs_dependencies//google-protobuf",
    "@nodejs_dependencies//grpc",
    "@nodejs_dependencies//jest",
    "@nodejs_dependencies//tmp",
    "@nodejs_dependencies//unzipper"
]

NODEJS_TEST_DATA = [
    "@graknlabs_grakn_core//:assemble-mac-zip",
    "tests/support/basic-genealogy.gql"
]

nodejs_jest_test(
    name = "keyspace-test",
    srcs = [
        "tests/support/GraknTestEnvironment.js",
        "tests/service/keyspace/Keyspace.test.js"
    ],
    deps = NODEJS_TEST_DEPENDENCIES,
    data = NODEJS_TEST_DATA,
)

nodejs_jest_test(
    name = "concept-test",
    srcs = [
        "tests/support/GraknTestEnvironment.js",
        "tests/service/session/transaction/Concept.test.js",
    ],
    deps = NODEJS_TEST_DEPENDENCIES,
    data = NODEJS_TEST_DATA,
)

nodejs_jest_test(
    name = "schemaconcept-test",
    srcs = [
        "tests/support/GraknTestEnvironment.js",
        "tests/service/session/transaction/SchemaConcept.test.js",
    ],
    deps = NODEJS_TEST_DEPENDENCIES,
    data = NODEJS_TEST_DATA,
)

nodejs_jest_test(
    name = "rule-test",
    srcs = [
        "tests/support/GraknTestEnvironment.js",
        "tests/service/session/transaction/Rule.test.js",
    ],
    deps = NODEJS_TEST_DEPENDENCIES,
    data = NODEJS_TEST_DATA,
)

nodejs_jest_test(
    name = "type-test",
    srcs = [
        "tests/support/GraknTestEnvironment.js",
        "tests/service/session/transaction/Type.test.js",
    ],
    deps = NODEJS_TEST_DEPENDENCIES,
    data = NODEJS_TEST_DATA,
)

nodejs_jest_test(
    name = "attribute-test",
    srcs = [
        "tests/support/GraknTestEnvironment.js",
        "tests/service/session/transaction/Attribute.test.js",
    ],
    deps = NODEJS_TEST_DEPENDENCIES,
    data = NODEJS_TEST_DATA,
)

nodejs_jest_test(
    name = "attributetype-test",
    srcs = [
        "tests/support/GraknTestEnvironment.js",
        "tests/service/session/transaction/AttributeType.test.js",
    ],
    deps = NODEJS_TEST_DEPENDENCIES,
    data = NODEJS_TEST_DATA,
)

nodejs_jest_test(
    name = "role-test",
    srcs = [
        "tests/support/GraknTestEnvironment.js",
        "tests/service/session/transaction/Role.test.js",
    ],
    deps = NODEJS_TEST_DEPENDENCIES,
    data = NODEJS_TEST_DATA,
)

nodejs_jest_test(
    name = "grakntx-test",
    srcs = [
        "tests/support/GraknTestEnvironment.js",
        "tests/service/session/transaction/GraknTx.test.js",
    ],
    deps = NODEJS_TEST_DEPENDENCIES,
    data = NODEJS_TEST_DATA
)

nodejs_jest_test(
    name = "relation-test",
    srcs = [
        "tests/support/GraknTestEnvironment.js",
        "tests/service/session/transaction/Relation.test.js",
    ],
    deps = NODEJS_TEST_DEPENDENCIES,
    data = NODEJS_TEST_DATA,
)

nodejs_jest_test(
    name = "relationtype-test",
    srcs = [
        "tests/support/GraknTestEnvironment.js",
        "tests/service/session/transaction/RelationType.test.js",
    ],
    deps = NODEJS_TEST_DEPENDENCIES,
    data = NODEJS_TEST_DATA,
)

nodejs_jest_test(
    name = "thing-test",
    srcs = [
        "tests/support/GraknTestEnvironment.js",
        "tests/service/session/transaction/Thing.test.js",
    ],
    deps = NODEJS_TEST_DEPENDENCIES,
    data = NODEJS_TEST_DATA,
)

nodejs_jest_test(
    name = "entitytype-test",
    srcs = [
        "tests/support/GraknTestEnvironment.js",
        "tests/service/session/transaction/EntityType.test.js",
    ],
    deps = NODEJS_TEST_DEPENDENCIES,
    data = NODEJS_TEST_DATA,
)

nodejs_jest_test(
    name = "committx-test",
    srcs = [
        "tests/support/GraknTestEnvironment.js",
        "tests/service/session/transaction/CommitTx.test.js",
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
    ]
)
