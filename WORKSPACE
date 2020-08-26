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

workspace(
    name = "graknlabs_client_nodejs",
    managed_directories = {"@npm": ["node_modules"]},
)

################################
# Load @graknlabs_dependencies #
################################

load("//dependencies/graknlabs:repositories.bzl", "graknlabs_dependencies")
graknlabs_dependencies()

# Load //builder/bazel for RBE
load("@graknlabs_dependencies//builder/bazel:deps.bzl", "bazel_toolchain")
bazel_toolchain()

# Load //builder/java
load("@graknlabs_dependencies//builder/java:deps.bzl", java_deps = "deps")
java_deps()

# Load //builder/kotlin
load("@graknlabs_dependencies//builder/kotlin:deps.bzl", kotlin_deps = "deps")
kotlin_deps()
load("@io_bazel_rules_kotlin//kotlin:kotlin.bzl", "kotlin_repositories", "kt_register_toolchains")
kotlin_repositories()
kt_register_toolchains()

# Load //builder/python
load("@graknlabs_dependencies//builder/python:deps.bzl", python_deps = "deps")
python_deps()
load("@rules_python//python:pip.bzl", "pip_repositories", "pip3_import")
pip_repositories()

# Load //builder/nodejs
load("@graknlabs_dependencies//builder/nodejs:deps.bzl", nodejs_deps = "deps")
nodejs_deps()
load("@build_bazel_rules_nodejs//:index.bzl", "yarn_install")

# Load //builder/grpc
load("@graknlabs_dependencies//builder/grpc:deps.bzl", grpc_deps = "deps")
grpc_deps()
load("@com_github_grpc_grpc//bazel:grpc_deps.bzl",
com_github_grpc_grpc_deps = "grpc_deps")
com_github_grpc_grpc_deps()
load("@stackb_rules_proto//node:deps.bzl", "node_grpc_compile")
node_grpc_compile()

# Load //tool/common
load("@graknlabs_dependencies//tool/common:deps.bzl", "graknlabs_dependencies_ci_pip",
    graknlabs_dependencies_tool_maven_artifacts = "maven_artifacts")
graknlabs_dependencies_ci_pip()
load("@graknlabs_dependencies_ci_pip//:requirements.bzl", "pip_install")
pip_install()

######################################
# Load @graknlabs_bazel_distribution #
######################################

load("@graknlabs_dependencies//distribution:deps.bzl", "graknlabs_bazel_distribution")
graknlabs_bazel_distribution()

# Load //common
load("@graknlabs_bazel_distribution//common:deps.bzl", "rules_pkg")
rules_pkg()
load("@rules_pkg//:deps.bzl", "rules_pkg_dependencies")
rules_pkg_dependencies()

# Load //pip
load("@graknlabs_bazel_distribution//pip:deps.bzl", pip_deps = "deps")
pip_deps()
load("@graknlabs_bazel_distribution_pip//:requirements.bzl", graknlabs_bazel_distribution_pip_install = "pip_install")
graknlabs_bazel_distribution_pip_install()

# Load //github
load("@graknlabs_bazel_distribution//github:deps.bzl", github_deps = "deps")
github_deps()

################################
# Load @graknlabs dependencies #
################################

# Load repositories
load("//dependencies/graknlabs:repositories.bzl", "graknlabs_protocol")
graknlabs_protocol()

# Load artifacts
load("//dependencies/graknlabs:artifacts.bzl", "graknlabs_grakn_core_artifact")
graknlabs_grakn_core_artifact()

# Load package.json
yarn_install(
    name = "npm",
    package_json = "//:package.json",
    yarn_lock = "//:yarn.lock"
)

############################
# Load @maven dependencies #
############################

load("@graknlabs_dependencies//library/maven:rules.bzl", "maven")
maven(graknlabs_dependencies_tool_maven_artifacts)

################################################
# Create @graknlabs_client_java_workspace_refs #
################################################
load("@graknlabs_bazel_distribution//common:rules.bzl", "workspace_refs")
workspace_refs(
    name = "graknlabs_client_nodejs_workspace_refs"
)
