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
load("//dependencies/graknlabs:dependencies.bzl", "graknlabs_dependencies")
graknlabs_dependencies()

load("@graknlabs_dependencies//builder/bazel:deps.bzl","bazel_common", "bazel_deps", "bazel_toolchain")
bazel_common()
bazel_deps()
bazel_toolchain()

load("@graknlabs_dependencies//builder/grpc:deps.bzl", grpc_deps = "deps")
grpc_deps()
load("@com_github_grpc_grpc//bazel:grpc_deps.bzl",
com_github_grpc_grpc_deps = "grpc_deps")
com_github_grpc_grpc_deps()
load("@stackb_rules_proto//java:deps.bzl", "java_grpc_compile")
java_grpc_compile()
load("@stackb_rules_proto//node:deps.bzl", "node_grpc_compile")
node_grpc_compile()

load("@graknlabs_dependencies//builder/java:deps.bzl", java_deps = "deps")
java_deps()

load("@graknlabs_dependencies//builder/nodejs:deps.bzl", nodejs_deps = "deps")
nodejs_deps()
load("@build_bazel_rules_nodejs//:defs.bzl", "node_repositories")
node_repositories()

load("@graknlabs_dependencies//builder/python:deps.bzl", python_deps = "deps")
python_deps()
load("@rules_python//python:pip.bzl", "pip_repositories", "pip3_import")
pip_repositories()
pip3_import(
    name = "graknlabs_dependencies_ci_pip",
    requirements = "@graknlabs_dependencies//tools:requirements.txt",
)
load("@graknlabs_dependencies_ci_pip//:requirements.bzl",
graknlabs_dependencies_ci_pip_install = "pip_install")
graknlabs_dependencies_ci_pip_install()

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

load("@graknlabs_dependencies//distribution/docker:deps.bzl", docker_deps = "deps")
docker_deps()

load("@graknlabs_dependencies//tools/checkstyle:deps.bzl", checkstyle_deps = "deps")
checkstyle_deps()

load("@graknlabs_dependencies//tools/sonarcloud:deps.bzl", "sonarcloud_dependencies")
sonarcloud_dependencies()

load("@graknlabs_dependencies//tools/unuseddeps:deps.bzl", unuseddeps_deps = "deps")
unuseddeps_deps()

#########################
# Load @graknlabs_graql #
#########################
load("//dependencies/graknlabs:dependencies.bzl", "graknlabs_graql")
graknlabs_graql()

load("@graknlabs_graql//dependencies/compilers:dependencies.bzl", "antlr_dependencies")
antlr_dependencies()

load("@rules_antlr//antlr:deps.bzl", "antlr_dependencies")
antlr_dependencies()

load("@graknlabs_graql//dependencies/maven:dependencies.bzl",
graknlabs_graql_maven_dependencies = "maven_dependencies")
graknlabs_graql_maven_dependencies()

##########################
# Load @graknlabs_common #
##########################
load("//dependencies/graknlabs:dependencies.bzl", "graknlabs_common")
graknlabs_common()

############################
# Load @graknlabs_protocol #
############################
load("//dependencies/graknlabs:dependencies.bzl", "graknlabs_protocol")
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
load("//dependencies/graknlabs:dependencies.bzl", "graknlabs_grabl_tracing")
graknlabs_grabl_tracing()

##############################
# Load @graknlabs_grakn_core #
##############################
load("//dependencies/graknlabs:dependencies.bzl", "graknlabs_grakn_core")
graknlabs_grakn_core()

load("@graknlabs_grakn_core//dependencies/graknlabs:dependencies.bzl",
"graknlabs_common", "graknlabs_console")
graknlabs_common()
graknlabs_console()

load("@graknlabs_grakn_core//dependencies/maven:dependencies.bzl",
graknlabs_grakn_core_maven_dependencies = "maven_dependencies")
graknlabs_grakn_core_maven_dependencies()

################################
# Load @graknlabs_verification #
################################
load("//dependencies/graknlabs:dependencies.bzl", "graknlabs_verification")
graknlabs_verification()

########################
# Load Maven Artifacts #
########################
# load("@graknlabs_dependencies//library/maven:rules.bzl", "maven")
# load("//dependencies/maven:artifacts.bzl", "artifacts")
# maven(artifacts)
load("//dependencies/maven:dependencies.bzl", "maven_dependencies")
maven_dependencies()

#########################
# Create Workspace Refs #
#########################
load("@graknlabs_bazel_distribution//common:rules.bzl", "workspace_refs")
workspace_refs(
    name = "graknlabs_client_java_workspace_refs"
)

# ###########################
# # Grakn Labs dependencies #
# ###########################

# load("//dependencies/graknlabs:dependencies.bzl",
# "graknlabs_graql", "graknlabs_dependencies", "graknlabs_common", "graknlabs_protocol", "graknlabs_verification", "graknlabs_grakn_core", "graknlabs_grabl_tracing")
# graknlabs_graql()
# graknlabs_dependencies()
# graknlabs_common()
# graknlabs_protocol()
# graknlabs_verification()
# graknlabs_grakn_core()
# graknlabs_grabl_tracing()

# load("@graknlabs_dependencies//distribution:deps.bzl", distribution_deps = "deps")
# distribution_deps()

# load("@graknlabs_dependencies//tools/unuseddeps:deps.bzl", unuseddeps_deps = "deps")
# unuseddeps_deps()


# ###########################
# # Load Bazel dependencies #
# ###########################

# load("@graknlabs_dependencies//builder/bazel:deps.bzl", "bazel_common", "bazel_deps", "bazel_toolchain")
# bazel_common()
# bazel_deps()
# bazel_toolchain()


# #################################
# # Load Build Tools dependencies #
# #################################

# load("@graknlabs_dependencies//tools/checkstyle:deps.bzl", checkstyle_deps = "deps")
# checkstyle_deps()

# load("@rules_python//python:pip.bzl", "pip_repositories", "pip3_import")
# pip_repositories()

# pip3_import(
#     name = "graknlabs_dependencies_ci_pip",
#     requirements = "@graknlabs_dependencies//ci:requirements.txt",
# )
# load("@graknlabs_dependencies_ci_pip//:requirements.bzl",
# graknlabs_dependencies_ci_pip_install = "pip_install")
# graknlabs_dependencies_ci_pip_install()


# #####################################
# # Load Java dependencies from Maven #
# #####################################

# load("//dependencies/maven:dependencies.bzl", "maven_dependencies")
# maven_dependencies()


# ##############################
# # Load Protocol dependencies #
# ##############################

# load("@graknlabs_dependencies//builder/grpc:deps.bzl", grpc_deps = "deps")
# grpc_deps()

# load("@com_github_grpc_grpc//bazel:grpc_deps.bzl",
# com_github_grpc_grpc_deps = "grpc_deps")
# com_github_grpc_grpc_deps()

# load("@stackb_rules_proto//java:deps.bzl", "java_grpc_compile")
# java_grpc_compile()


# ################################
# # Load Grakn Core dependencies #
# ################################

# load("@graknlabs_grakn_core//dependencies/graknlabs:dependencies.bzl",
# "graknlabs_common", "graknlabs_console")
# graknlabs_common()
# graknlabs_console()

# load("@graknlabs_grakn_core//dependencies/maven:dependencies.bzl",
# graknlabs_grakn_core_maven_dependencies = "maven_dependencies")
# graknlabs_grakn_core_maven_dependencies()

# load("@graknlabs_dependencies//bazel:dependencies.bzl", "bazel_rules_docker")
# bazel_rules_docker()


# ###########################
# # Load Graql dependencies #
# ###########################

# load("@graknlabs_graql//dependencies/compilers:dependencies.bzl", "antlr_dependencies")
# antlr_dependencies()

# load("@rules_antlr//antlr:deps.bzl", "antlr_dependencies")
# antlr_dependencies()

# load("@graknlabs_graql//dependencies/maven:dependencies.bzl",
# graknlabs_graql_maven_dependencies = "maven_dependencies")
# graknlabs_graql_maven_dependencies()


# ##################################
# # Load Distribution dependencies #
# ##################################

# load("@graknlabs_bazel_distribution//github:dependencies.bzl", "tcnksm_ghr")
# tcnksm_ghr()

# load("@graknlabs_bazel_distribution//common:dependencies.bzl", "bazelbuild_rules_pkg")
# bazelbuild_rules_pkg()

# #####################################
# # Load Bazel common workspace rules #
# #####################################

# # TODO: Figure out why this cannot be loaded at earlier at the top of the file
# load("@com_github_google_bazel_common//:workspace_defs.bzl", "google_common_workspace_rules")
# google_common_workspace_rules()

# # Generate a JSON document of commit hashes of all external workspace dependencies
# load("@graknlabs_bazel_distribution//common:rules.bzl", "workspace_refs")
# workspace_refs(
#     name = "graknlabs_client_java_workspace_refs"
# )
