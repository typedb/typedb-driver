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

load("@vaticle_dependencies//tool/checkstyle:rules.bzl", "checkstyle_test")

def typedb_behaviour_cpp_test(
    name,
    feature,
    deps = [],
    data = [],
    args = [],
    env = {}
):
    native.cc_test(
        name = name + "-core",
        deps = ["//cpp/test/behaviour/steps:core-steps"] + deps,
        data = [feature] + data,
        args = ["$(location " + feature + ")"] + args,
        env = env
    )
    
    native.cc_test(
        name = name + "-enterprise",
        deps = ["//cpp/test/behaviour/steps:enterprise-steps"] + deps,
        data = [feature, "//tool/test/resources:certificates"] + data,
        args = ["$(location " + feature + ")"] + args,
        env = {"ROOT_CA": "tool/test/resources/encryption/ext-grpc-root-ca.pem"} | env,
    )
