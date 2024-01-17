load("@rules_dotnet//dotnet:defs.bzl", "csharp_binary", "csharp_library")

#load(":build_opts.bzl", "target_frameworks", "targeting_packs")
package(default_visibility = ["//visibility:public"])

csharp_library(
    name = "hello-lib",
    srcs = ["lib.cs"],
    target_frameworks = ["net6.0"],
    #runtime_identifier = "osx-arm64",
    targeting_packs = ["@paket.rules_dotnet_nuget_packages//microsoft.netcore.app.ref"]
)

csharp_binary(
    name = "hello",
    srcs = ["hello.cs"],
    #    runtime_identifier = "osx-arm64",
    target_frameworks = ["net6.0"],
    targeting_packs = ["@paket.rules_dotnet_nuget_packages//microsoft.netcore.app.ref"],
    deps = [":hello-lib"],
)

