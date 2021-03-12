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

# Load //builder/nodejs
load("@graknlabs_dependencies//builder/nodejs:deps.bzl", nodejs_deps = "deps")
nodejs_deps()
load("@build_bazel_rules_nodejs//:index.bzl", "node_repositories", "npm_install")

# Load //tool/checkstyle
load("@graknlabs_dependencies//tool/checkstyle:deps.bzl", checkstyle_deps = "deps")
checkstyle_deps()

####################
# Load npm modules #
####################


# Load package.json
node_repositories(
    preserve_symlinks = False,
)
npm_install(
    name = "npm",
    package_json = "//:package.json",
    package_lock_json = "//:package-lock.json",
)

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

# Load Unused Deps
load("@graknlabs_dependencies//tool/unuseddeps:deps.bzl", unuseddeps_deps = "deps")
unuseddeps_deps()

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

# Load //github
load("@graknlabs_bazel_distribution//github:deps.bzl", github_deps = "deps")
github_deps()

################################
# Load @graknlabs dependencies #
################################

# Load repositories
load("//dependencies/graknlabs:repositories.bzl", "graknlabs_behaviour")
graknlabs_behaviour()

load("//dependencies/graknlabs:repositories.bzl", "graknlabs_common")
graknlabs_common()

# Load artifacts
load("//dependencies/graknlabs:artifacts.bzl", "graknlabs_grakn_core_artifacts", "graknlabs_grakn_cluster_artifacts")
graknlabs_grakn_core_artifacts()
graknlabs_grakn_cluster_artifacts()

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
