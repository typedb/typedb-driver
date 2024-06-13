# To update external required dependencies with gazelle, follow these steps:
# 1. Specify the desired packages to your go.mod file manually OR with go get (requires installing Go on your local)
# 2. Run this shell script to add the dependencies in go/deps.bzl with bazel go_repositories.
# 3. Add the import to the go file it will be used in
# - (import_path is declared in deps.bzl, where the repositories are defined).

# go get PACKAGE NAME, eg:
# go get github.com/cucumber/godog

bazel run //go/gazelle:gazelle-update-dependencies