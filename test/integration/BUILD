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

load("@graknlabs_dependencies//tool/checkstyle:rules.bzl", "checkstyle_test")
load("@graknlabs_common//test/server:rules.bzl", "grakn_test")

grakn_test(
    name = "test-client-query",
    srcs = ["ClientQueryTest.java"],
    test_class = "grakn.client.test.integration.ClientQueryTest",
    deps = [
        # Internal dependencies
        "//:client-java",

        # External dependencies from @graknlabs
        "@graknlabs_graql//java:graql",
        "@graknlabs_graql//java/common",
        "@graknlabs_graql//java/query",

        # External dependencies from Maven
        "@maven//:org_hamcrest_hamcrest_library",
        "@maven//:org_slf4j_slf4j_api",
    ],
    grakn_artifact = "@graknlabs_grakn_core_artifact//file",
)

checkstyle_test(
    name = "checkstyle",
    targets = [
        ":test-client-query",
    ],
)
