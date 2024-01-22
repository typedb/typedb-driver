load("@rules_dotnet//dotnet:defs.bzl", "csharp_nunit_test")
load("//csharp:build_opts.bzl", "target_frameworks", "targeting_packs")

csharp_nunit_test(
    name = "test",
    srcs = ["Test.cs"],
    deps = [
        "//csharp/poc-examples/helloer:helloer-lib",
    ],
    runtime_identifier = "any",
    visibility = ["//visibility:public"],
    target_frameworks = target_frameworks,
    targeting_packs = targeting_packs,
)