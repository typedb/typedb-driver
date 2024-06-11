# To update external required dependencies with gazelle, follow these steps:
# 1. Add the desired packages to your go.mod file with go get
# 2. Run this shell script from the base directory typedb-driver to add the dependencies in go/deps.bzl with
# bazel go_repositories
# 3. Add the import to your go file

# go get PACKAGE NAME, eg
go get github.com/cucumber/godog

bazel run //go:gazelle-update-repos