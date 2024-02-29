#
# Copyright (C) 2022 Vaticle
#
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
#

load("@rules_dotnet//dotnet:defs.bzl", "csharp_library", "csharp_test")
load("@rules_dotnet//dotnet:defs.bzl", "csharp_nunit_test")
load("//csharp:build_opts.bzl", "nullable_context")
load("//csharp/Test:build_opts.bzl", "behaviour_tests_deps")


def csharp_behaviour_test(
        name,
        test_files,
        steps_files,
        features,
        deps,
        target_frameworks,
        targeting_packs,
        add_certificates = False,
        **kwargs):
    certificates = ["//tool/test/resources:certificates"] if add_certificates else []

    csharp_test(
        name = name,
        srcs = test_files + steps_files + ["//csharp/Test/Behaviour/Util:TestRunner.cs"],
        data = features + certificates,
        deps = deps + behaviour_tests_deps,
        target_frameworks = target_frameworks,
        targeting_packs = targeting_packs,
        runtime_identifier = "any",
        nullable = nullable_context,
        visibility = ["//visibility:public"],
        **kwargs,
    )


def csharp_integration_test(name, srcs, deps, target_frameworks, targeting_packs, **kwargs):
    csharp_nunit_test(
        name = name,
        srcs = srcs,
        deps = deps,
        target_frameworks = target_frameworks,
        targeting_packs = targeting_packs,
        runtime_identifier = "any",
        nullable = nullable_context,
        visibility = ["//visibility:public"],
        **kwargs,
    )