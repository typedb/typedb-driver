load("//csharp:build_opts.bzl", "target_frameworks", "targeting_packs")
load("//csharp:rules.bzl", "csharp_behaviour_test")


csharp_behaviour_test(
    name = "test",
    steps = ["Steps/HelloLibStepDefinitions.cs"],
    features = ["Features/HelloLib.feature"],
    deps = ["//csharp/poc-examples/helloer:helloer-lib"],
    target_frameworks = target_frameworks,
    targeting_packs = targeting_packs,
)
