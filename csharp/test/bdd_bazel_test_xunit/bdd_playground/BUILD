load("@rules_dotnet//dotnet:defs.bzl", "csharp_test", "csharp_nunit_test")
load("//csharp:build_opts.bzl", "target_frameworks", "targeting_packs")

csharp_test(
    name = "test",
    srcs = ["Main.cs", "Steps/HelloLibStepDefinitions.cs"],
    data = ["Features/HelloLib.feature"],
    deps = [
        "@paket.csharp_deps//gherkin",
        "@paket.csharp_deps//xunit.runner.utility",
        "@paket.csharp_deps//xunit.gherkin.quick",
        "//csharp/test/bdd_bazel_test_xunit/bdd_impl:hello-lib",
    ],
    runtime_identifier = "any",
    visibility = ["//visibility:public"],
    target_frameworks = target_frameworks,
    targeting_packs = targeting_packs,
)