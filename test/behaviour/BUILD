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

exports_files([
    "cucumber_test.sh",
    "tsconfig-test.json",
])

genrule(
    name = "behavioural-steps-compiled",
    outs = ["behavioural-steps.tar.gz"],
    cmd = "tar -xf $(location //:client-nodejs-compiled); npx tsc --build tsconfig-test.json; tar -cf $(@D)/behavioural-steps.tar.gz dist-test;",
    tools = [
        "//:behavioural-steps",
        "//:client-nodejs-compiled",
        "//:package.json",
        "//:package-lock.json",
    ],
    visibility = ["//visibility:public"],
)

load("@graknlabs_dependencies//tool/checkstyle:rules.bzl", "checkstyle_test")
checkstyle_test(
    name = "checkstyle",
    include = glob(["*"]),
    license_type = "apache",
)
