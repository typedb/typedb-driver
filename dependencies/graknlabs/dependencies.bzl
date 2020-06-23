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

load("@bazel_tools//tools/build_defs/repo:git.bzl", "git_repository")

def graknlabs_graql():
    git_repository(
        name = "graknlabs_graql",
        remote = "https://github.com/lolski/graql",
        commit = "30d2d01141be4b16a24a4cd255aba04aaccf20cf" # sync-marker: do not remove this comment, this is used for sync-dependencies by @graknlabs_graql
    )

def graknlabs_common():
    git_repository(
        name = "graknlabs_common",
        remote = "https://github.com/lolski/common",
        commit = "60c5f9b2929452ba3011e6bc2f8e67da645eeee1" # sync-marker: do not remove this comment, this is used for sync-dependencies by @graknlabs_common
    )

def graknlabs_dependencies():
    git_repository(
        name = "graknlabs_dependencies",
        remote = "https://github.com/lolski/dependencies",
        commit = "c1a12d49054e782953b899d5528825e546a5b76c", # sync-marker: do not remove this comment, this is used for sync-dependencies by @graknlabs_dependencies
    )

def graknlabs_grakn_core():
    native.local_repository(
        name = "graknlabs_grakn_core",
        path = "/Users/lolski/grakn.ai/graknlabs/grakn"
    )
    # git_repository(
    #     name = "graknlabs_grakn_core",
    #     remote = "https://github.com/graknlabs/grakn",
    #     commit = "1.8.0", # sync-marker: do not remove this comment, this is used for sync-dependencies by @graknlabs_grakn_core
    # )

def graknlabs_protocol():
    git_repository(
        name = "graknlabs_protocol",
        remote = "https://github.com/lolski/protocol",
        commit = "344b232cb4bc23140124abd011370663bcd26776", # sync-marker: do not remove this comment, this is used for sync-dependencies by @graknlabs_protocol
    )

def graknlabs_verification():
    native.local_repository(
        name = "graknlabs_verification",
        path = "/Users/lolski/grakn.ai/graknlabs/verification"
    )
    # git_repository(
    #     name = "graknlabs_verification",
    #     remote = "https://github.com/graknlabs/verification",
    #     commit = "4530de873599c5d0dded6d81393fbe57816ef57c", # sync-marker: do not remove this comment, this is used for sync-dependencies by @graknlabs_verification
    # )

def graknlabs_grabl_tracing():
    native.local_repository(
        name = "graknlabs_grabl_tracing",
        path = "/Users/lolski/grakn.ai/graknlabs/grabl-tracing"
    )
    # git_repository(
    #     name = "graknlabs_grabl_tracing",
    #     remote = "https://github.com/lolski/grabl-tracing",
    #     commit = "0031b39eebea2e2844a37d82800f456547deff3d"  # sync-marker: do not remove this comment, this is used for sync-dependencies by @graknlabs_grabl_tracing
    # )
