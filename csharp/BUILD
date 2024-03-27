# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.

load("@rules_dotnet//dotnet:defs.bzl", "csharp_binary", "csharp_library")
load("@vaticle_bazel_distribution//docs:doxygen/rules.bzl", "doxygen_docs")
load("@vaticle_dependencies//tool/checkstyle:rules.bzl", "checkstyle_test")
load(":build_opts.bzl", "target_framework", "target_frameworks", "targeting_packs", "nullable_context")
load(":nuget_pack.bzl", "nuget_pack")
load(":rules.bzl", "swig_native_csharp_library")
load("//csharp:docs_structure.bzl", "dir_mapping")
load("//tool/docs:csharp/rules.bzl", "doxygen_csharp_to_adoc")


swig_native_csharp_library(
    name = "typedb_driver_pinvoke",
    lib = "//c:typedb_driver_clib_headers",
    namespace = "TypeDB.Driver.Pinvoke",
    interface = "//c:typedb_driver.i",
    includes = ["//c:swig/typedb_driver_csharp.swg"],
    enable_cxx = True,
    nullable_context = nullable_context,
    target_frameworks = target_frameworks,
    targeting_packs = targeting_packs,
    visibility = ["//visibility:public"],
#    platforms = { # TODO: We'll need to build for different platforms?
#        "@vaticle_bazel_distribution//platform:is_linux_arm64": "linux-arm64",
#        "@vaticle_bazel_distribution//platform:is_linux_x86_64": "linux-x86_64",
#        "@vaticle_bazel_distribution//platform:is_mac_arm64": "macosx-arm64",
#        "@vaticle_bazel_distribution//platform:is_mac_x86_64": "macosx-x86_64",
#        "@vaticle_bazel_distribution//platform:is_windows_x86_64": "windows-x86_64",
#    },
)


csharp_library(
    name = "driver-csharp",
    srcs = glob(["*.cs"]),
    resources = ["//:LICENSE"],
    deps = [
        "//csharp/Api:api",
        "//csharp/Common:common",
        "//csharp/Connection:connection",
        "//csharp:typedb_driver_pinvoke",
    ],
    out = "TypeDB.Driver",
    visibility = ["//visibility:public"],
    target_frameworks = target_frameworks,
    targeting_packs = targeting_packs,
)


nuget_pack(
    name = "driver-csharp-pack",
    files = {
#        "//common/images:selenium_logo_small.png": "icon.png",
        ":typedb_driver_native": "libtypedb_driver.dylib",
        "libtypedb_driver.so": "libtypedb_driver.so",
        "typedb_driver.dll": "typedb_driver.dll",
    },
    id = "TypeDB.Driver",
    libs = {
        ":driver-csharp": "TypeDB.Driver",
        ":typedb_driver_pinvoke": "TypeDB.Driver.Pinvoke",
        "//csharp/Api:api": "TypeDB.Driver.Api",
        "//csharp/Common:common": "TypeDB.Driver.Common",
        "//csharp/Connection:connection": "TypeDB.Driver.Connection",
        "//csharp/Concept:concept": "TypeDB.Driver.Concept",
        "//csharp/Logic:logic": "TypeDB.Driver.Logic",
        "//csharp/Query:query": "TypeDB.Driver.Query",
        "//csharp/User:user": "TypeDB.Driver.User",
    },
    osx_native_lib = "libtypedb_driver.dylib",
    linux_native_lib = "libtypedb_driver.so",
    win_native_lib = "typedb_driver.dll",
    target_framework = target_framework,
    nuspec_template = "TypeDB.Driver.nuspec",
    tags = [
#        "block-network",
    ],
    version = "0.0.1",
    visibility = ["//visibility:public"],
)


filegroup(
    name = "docs_source_files",
    srcs = [":TypeDB.cs"],
)


# TODO: Not hermetic - requires doxygen on the host
doxygen_docs(
    name = "docs_html",
    project_name = "TypeDB C# Driver",
    sources = [":docs_source_files", "//csharp/Api:docs_source_files", "//csharp/Common:docs_source_files"],
    main_page_md = ":README.md",
    tags = ["manual"],
)


doxygen_csharp_to_adoc(
    name = "docs_adoc",
    data = [":docs_html"],
    docs_dirs = dir_mapping,
    output_dir = 'csharp/docs',
    tags = ["manual"]
)


checkstyle_test(
    name = "checkstyle",
    size = "small",
    include = glob(["*"]),
    exclude = glob([
        "README.md",
        "docs/**/*.adoc",
    ]),
    license_type = "apache-header",
)
