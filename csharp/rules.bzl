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

load("@typedb_dependencies//builder/swig:csharp.bzl", "swig_csharp")
load("@typedb_bazel_distribution//nuget:rules.bzl", "nuget_pack")

def swig_native_csharp_library(name, native_lib_name, namespace, nullable_context, target_frameworks, targeting_packs, visibility, tags=[], **kwargs):
    swig_csharp(
        name = "__" + name,
        native_lib_name = native_lib_name,
        namespace = namespace,
        nullable_context = nullable_context,
        target_frameworks = target_frameworks,
        targeting_packs = targeting_packs,
        tags = tags,
        **kwargs,
    )

    native.alias(
        name = name,
        actual = "__" + name,
        visibility = visibility,
    )

def swig_native_nuget_pack(name, id, libs, native_libs, target_framework, nuspec_template, platforms, visibility, files={}, **kwargs):
    # generate identical libraries with different platform names, since we can't 'select' it
    for platform in platforms.values():
        nuget_pack(
            name = "_{}_as_{}_do_not_reference".format(name, platform),
            id = id,
            files = files,
            libs = libs,
            native_libs = native_libs,
            target_framework = target_framework,
            nuspec_template = nuspec_template,
            platform = platform,
            **kwargs,
        )

    native.alias(
        name = name,
        actual = select({
            config: "_{}_as_{}_do_not_reference".format(name, platform)
            for config, platform in platforms.items()
        }),
        visibility = visibility,
    )
