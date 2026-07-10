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

# Only dotnet is a problem, but it was fixed in rules_dotnet#453. We just need to update from 0.14.0 to atleast 0.16.0
load("@bazel_skylib//lib:dicts.bzl", "dicts")
load("@rules_dotnet//dotnet/private:common.bzl", "DEFAULT_RID", "DEFAULT_TFM", "FRAMEWORK_COMPATIBILITY")
load("@rules_dotnet//dotnet/private:rids.bzl", "RUNTIME_GRAPH")

def _impl(_settings, _attr):
    default_framework_compatibility = {"@rules_dotnet//dotnet:framework_compatible_{}".format(framework): False for framework in FRAMEWORK_COMPATIBILITY.keys()}
    default_rid_compatibility = {"@rules_dotnet//dotnet:rid_compatible_{}".format(rid): False for rid in RUNTIME_GRAPH.keys()}
    return dicts.add({"@rules_dotnet//dotnet:target_framework": DEFAULT_TFM}, {"@rules_dotnet//dotnet:rid": DEFAULT_RID}, default_framework_compatibility, default_rid_compatibility)

fixed_default_transition = transition(
    implementation = _impl,
    inputs = ["@rules_dotnet//dotnet:target_framework", "@rules_dotnet//dotnet:rid", "//command_line_option:cpu", "//command_line_option:platforms"],
    outputs = ["@rules_dotnet//dotnet:target_framework", "@rules_dotnet//dotnet:rid"] +
        ["@rules_dotnet//dotnet:framework_compatible_%s" % framework for framework in FRAMEWORK_COMPATIBILITY.keys()] +
        ["@rules_dotnet//dotnet:rid_compatible_%s" % rid for rid in RUNTIME_GRAPH.keys()],
)

def _clib_wrapper_force_config(ctx):
    # re-export whatever providers the actual produces
    return [ctx.attr.actual[CcInfo], ctx.attr.actual[DefaultInfo], ]

clib_wrapper_force_config = rule(
    implementation = _clib_wrapper_force_config,
    cfg = fixed_default_transition,
    attrs = {"actual": attr.label()},
    doc = ("The C# driver rebuilds the C driver with a different config." +
        "Forcing the same config on every language means they can all share the same build"
    ),
)
