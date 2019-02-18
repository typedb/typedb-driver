load("@bazel_tools//tools/build_defs/repo:git.bzl", "git_repository")

git_repository(
    name = "graknlabs_grakn_core",
    remote = "https://github.com/graknlabs/grakn",
    commit = "f0959ee2681b5c2fe850aa7b697c35b2c2d4d720"
)

git_repository(
    name = "build_bazel_rules_nodejs",
    remote = "https://github.com/graknlabs/rules_nodejs.git",
    commit = "ac3f6854365f119130186f971588514ccff503ab",
)

load("@build_bazel_rules_nodejs//:package.bzl", "rules_nodejs_dependencies")
rules_nodejs_dependencies()

# Load NPM dependencies for Node.js programs
load("@build_bazel_rules_nodejs//:defs.bzl", "node_repositories", "npm_install")
node_repositories(package_json = ["//:package.json"])
npm_install(
    name = "nodejs_dependencies",
    package_json = "//:package.json",
    data = [
      "@build_bazel_rules_nodejs//internal/babel_library:package.json",
      "@build_bazel_rules_nodejs//internal/babel_library:babel.js",
      "@build_bazel_rules_nodejs//internal/babel_library:yarn.lock",
    ],
)

load("@graknlabs_grakn_core//dependencies/compilers:dependencies.bzl", "grpc_dependencies")
grpc_dependencies()

load("@com_github_grpc_grpc//bazel:grpc_deps.bzl", com_github_grpc_grpc_bazel_grpc_deps = "grpc_deps")
com_github_grpc_grpc_bazel_grpc_deps()

# Load GRPC Node.js dependencies
load("@stackb_rules_proto//node:deps.bzl", "node_grpc_compile")
node_grpc_compile()

git_repository(
    name="graknlabs_bazel_distribution",
    remote="https://github.com/graknlabs/bazel-distribution",
    commit="bef0d2638a19b710a54d5786855aa9ef60fea238"
)



# ----- @graknlabs_grakn deps -----
git_repository(
 name="com_github_google_bazel_common",
 remote="https://github.com/graknlabs/bazel-common",
 commit="550f0490798a4e4b6c5ff8cac3b6f5c2a5e81e21",
)

load("@com_github_google_bazel_common//:workspace_defs.bzl", "google_common_workspace_rules")
google_common_workspace_rules()

load("@graknlabs_grakn_core//dependencies/maven:dependencies.bzl", maven_dependencies_for_build = "maven_dependencies")
maven_dependencies_for_build()

load("@graknlabs_grakn_core//dependencies/maven:dependencies.bzl", maven_dependencies_for_build = "maven_dependencies")
maven_dependencies_for_build()

# Load Graql dependencies
load("@graknlabs_grakn_core//dependencies/git:dependencies.bzl", "graknlabs_graql")
graknlabs_graql()

# Load ANTLR dependencies for Bazel
load("@graknlabs_graql//dependencies/compilers:dependencies.bzl", "antlr_dependencies")
antlr_dependencies()

# Load ANTLR dependencies for ANTLR programs
load("@rules_antlr//antlr:deps.bzl", "antlr_dependencies")
antlr_dependencies()

load("@graknlabs_graql//dependencies/maven:dependencies.bzl", graql_dependencies = "maven_dependencies")
graql_dependencies()

load("@stackb_rules_proto//java:deps.bzl", "java_grpc_compile")
java_grpc_compile()
