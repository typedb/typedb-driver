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

load("@vaticle_dependencies//builder/swig:go.bzl", "swig_go")
load("@io_bazel_rules_go//go:def.bzl", "go_library")

def swig_native_go_library(name, visibility, tags=[], **kwargs):
    swig_go(
        name = "__" + name,
        shared_lib_name = name,
        tags = tags,
        **kwargs,
    )

#    https://github.com/bazelbuild/rules_go/blob/master/docs/go/core/rules.md#attributes-2

    native.alias(
        name = name,
        actual = "__" + name,
        visibility = visibility
    )



