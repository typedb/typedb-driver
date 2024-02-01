load("//csharp:build_opts.bzl", "target_frameworks", "targeting_packs")
load("//csharp/test:rules.bzl", "csharp_integration_test")


csharp_integration_test(
    name = "test",
    srcs = ["Test.cs"],
    deps = [
        "//csharp/poc-examples/helloer:helloer-lib",
    ],
    target_frameworks = target_frameworks,
    targeting_packs = targeting_packs,
)
