load("@bazel_gazelle//:def.bzl", "gazelle")

# for updating dependencies
gazelle(
    name = "gazelle-update-dependencies",
    args = [
        "-from_file=go/go.mod",
        "-to_macro=go/deps.bzl%go_repositories",
        "-prune",
        "-build_file_proto_mode=disable_global",
    ],
    command = "update-repos",
)