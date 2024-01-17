load("@rules_dotnet//dotnet:defs.bzl", "csharp_binary")

#load(":build_opts.bzl", "target_frameworks", "targeting_packs")
package(default_visibility = ["//visibility:public"])

csharp_binary(
    name = "hello",
    srcs = ["hello.cs"],
    #    runtime_identifier = "osx-arm64",
    target_frameworks = ["net6.0"],
    targeting_packs = ["@paket.rules_dotnet_nuget_packages//microsoft.netcore.app.ref"],
    #    deps = [":hello-lib"],
)
