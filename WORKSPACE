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

workspace(name = "graknlabs_client_java")

################################
# Load @graknlabs_dependencies #
################################
load("//dependencies/graknlabs:repositories.bzl", "graknlabs_dependencies")
graknlabs_dependencies()

# Load Antlr
load("@graknlabs_dependencies//builder/antlr:deps.bzl", antlr_deps = "deps")
antlr_deps()
load("@rules_antlr//antlr:deps.bzl", "antlr_dependencies")
antlr_dependencies()

# Load Bazel
load("@graknlabs_dependencies//builder/bazel:deps.bzl", "bazel_toolchain")
bazel_toolchain()

# Load gRPC
load("@graknlabs_dependencies//builder/grpc:deps.bzl", grpc_deps = "deps")
grpc_deps()
load("@com_github_grpc_grpc//bazel:grpc_deps.bzl",
com_github_grpc_grpc_deps = "grpc_deps")
com_github_grpc_grpc_deps()

# Load Java
load("@graknlabs_dependencies//builder/java:deps.bzl", java_deps = "deps")
java_deps()
load("@graknlabs_dependencies//library/maven:rules.bzl", "maven")

# Load Kotlin
load("@graknlabs_dependencies//builder/kotlin:deps.bzl", kotlin_deps = "deps")
kotlin_deps()
load("@io_bazel_rules_kotlin//kotlin:kotlin.bzl", "kotlin_repositories", "kt_register_toolchains")
kotlin_repositories()
kt_register_toolchains()

# Load Python
load("@graknlabs_dependencies//builder/python:deps.bzl", python_deps = "deps")
python_deps()
load("@rules_python//python:pip.bzl", "pip_repositories", "pip3_import")
pip_repositories()
pip3_import(
    name = "graknlabs_dependencies_ci_pip",
    requirements = "@graknlabs_dependencies//tool:requirements.txt",
)
load("@graknlabs_dependencies_ci_pip//:requirements.bzl",
graknlabs_dependencies_ci_pip_install = "pip_install")
graknlabs_dependencies_ci_pip_install()

# Load Checkstyle
load("@graknlabs_dependencies//tool/checkstyle:deps.bzl", checkstyle_deps = "deps")
checkstyle_deps()

# Load Unused Deps
load("@graknlabs_dependencies//tool/unuseddeps:deps.bzl", unuseddeps_deps = "deps")
unuseddeps_deps()

#####################################################################
# Load @graknlabs_bazel_distribution from (@graknlabs_dependencies) #
#####################################################################
load("@graknlabs_dependencies//distribution:deps.bzl", distribution_deps = "deps")
distribution_deps()

pip3_import(
    name = "graknlabs_bazel_distribution_pip",
    requirements = "@graknlabs_bazel_distribution//pip:requirements.txt",
)
load("@graknlabs_bazel_distribution_pip//:requirements.bzl",
graknlabs_bazel_distribution_pip_install = "pip_install")
graknlabs_bazel_distribution_pip_install()

load("@graknlabs_bazel_distribution//github:dependencies.bzl", "tcnksm_ghr")
tcnksm_ghr()

load("@bazel_tools//tools/build_defs/repo:git.bzl", "git_repository")
git_repository(
    name = "io_bazel_skydoc",
    remote = "https://github.com/graknlabs/skydoc.git",
    branch = "experimental-skydoc-allow-dep-on-bazel-tools",
)

load("@io_bazel_skydoc//:setup.bzl", "skydoc_repositories")
skydoc_repositories()

load("@io_bazel_rules_sass//:package.bzl", "rules_sass_dependencies")
rules_sass_dependencies()

load("@build_bazel_rules_nodejs//:defs.bzl", "node_repositories")
node_repositories()

load("@io_bazel_rules_sass//:defs.bzl", "sass_repositories")
sass_repositories()

load("@graknlabs_bazel_distribution//common:dependencies.bzl", "bazelbuild_rules_pkg")
bazelbuild_rules_pkg()

load("@rules_pkg//:deps.bzl", "rules_pkg_dependencies")
rules_pkg_dependencies()

##########################
# Load @graknlabs_common #
##########################
load("//dependencies/graknlabs:repositories.bzl", "graknlabs_common")
graknlabs_common()

load("//dependencies/graknlabs:repositories.bzl", "graknlabs_common")
load("@graknlabs_common//dependencies/maven:artifacts.bzl", graknlabs_common_artifacts = "artifacts")

#########################
# Load @graknlabs_graql #
#########################
load("//dependencies/graknlabs:repositories.bzl", "graknlabs_graql")
graknlabs_graql()

load("@graknlabs_graql//dependencies/maven:artifacts.bzl", graknlabs_graql_artifacts = "artifacts")

############################
# Load @graknlabs_protocol #
############################
load("//dependencies/graknlabs:repositories.bzl", "graknlabs_protocol")
graknlabs_protocol()

load("@graknlabs_dependencies//builder/grpc:deps.bzl", grpc_deps = "deps")
grpc_deps()

load("@com_github_grpc_grpc//bazel:grpc_deps.bzl",
com_github_grpc_grpc_deps = "grpc_deps")
com_github_grpc_grpc_deps()

load("@stackb_rules_proto//java:deps.bzl", "java_grpc_compile")
java_grpc_compile()

#################################
# Load @graknlabs_grabl_tracing #
#################################
load("//dependencies/graknlabs:repositories.bzl", "graknlabs_grabl_tracing")
graknlabs_grabl_tracing()
load("@graknlabs_grabl_tracing//dependencies/maven:artifacts.bzl", graknlabs_grabl_tracing_artifacts = "artifacts")

##############################
# Load @graknlabs_grakn_core_artifact #
##############################
load("//dependencies/graknlabs:artifacts.bzl", "graknlabs_grakn_core_artifact")
graknlabs_grakn_core_artifact()

################################
# Load @graknlabs_verification #
################################
load("//dependencies/graknlabs:repositories.bzl", "graknlabs_verification")
graknlabs_verification()

###############################
# Load @graknlabs_client_java #
###############################
load("@graknlabs_client_java//dependencies/maven:artifacts.bzl", graknlabs_client_java_artifacts = "artifacts")

###############
# Load @maven #
###############

# Override libraries conflicting with versions defined in @graknlabs_dependencies
OVERRIDES = {
    # @graknlabs_client_java overrides
   "io.netty:netty-all": "4.1.38.Final",
   "io.netty:netty-codec-http2": "4.1.38.Final",
    # @graknlabs_grabl_tracing overrides
   "io.netty:netty-handler": "4.1.38.Final",
   "io.netty:netty-handler-proxy": "4.1.38.Final",
}

maven(
    graknlabs_common_artifacts +
    graknlabs_graql_artifacts +
    graknlabs_grabl_tracing_artifacts +
    graknlabs_client_java_artifacts,
    OVERRIDES
)

################################################
# Create @graknlabs_client_java_workspace_refs #
################################################
load("@graknlabs_bazel_distribution//common:rules.bzl", "workspace_refs")
workspace_refs(
    name = "graknlabs_client_java_workspace_refs"
)