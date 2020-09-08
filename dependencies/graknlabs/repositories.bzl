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
#    git_repository(
#        name = "graknlabs_graql",
#        remote = "https://github.com/graknlabs/graql",
#        commit = "5ec4fc0e5acc2582fedd2e3f9fe1259b9d08f341" # sync-marker: do not remove this comment, this is used for sync-dependencies by @graknlabs_graql
#    )
    native.local_repository(
        name = "graknlabs_graql",
        path = "../graql",
    )

def graknlabs_common():
#    git_repository(
#        name = "graknlabs_common",
#        remote = "https://github.com/graknlabs/common",
#        commit = "182d9769d8b6e6fb922fe93cb6d874ddea45df86" # sync-marker: do not remove this comment, this is used for sync-dependencies by @graknlabs_common
#    )
    native.local_repository(
        name = "graknlabs_common",
        path = "../common",
    )

def graknlabs_bazel_distribution():
    git_repository(
        name = "graknlabs_bazel_distribution",
        remote = "https://github.com/graknlabs/bazel-distribution",
        commit = "30e30cec9e3fe4821103cafbd240ee9862e262ea" # sync-marker: do not remove this comment, this is used for sync-dependencies by @graknlabs_bazel_distribution
    )

def graknlabs_dependencies():
#    git_repository(
#        name = "graknlabs_dependencies",
#        remote = "https://github.com/graknlabs/dependencies",
#        commit = "a6dd66c50b77153b11473919e39e4a687534d126", # sync-marker: do not remove this comment, this is used for sync-dependencies by @graknlabs_dependencies
#    )
    native.local_repository(
        name = "graknlabs_dependencies",
        path = "../dependencies",
    )

def graknlabs_protocol():
    git_repository(
        name = "graknlabs_protocol",
        remote = "https://github.com/graknlabs/protocol",
        commit = "6871914abd01dca2c941b88534e0341986acc1df", # sync-marker: do not remove this comment, this is used for sync-dependencies by @graknlabs_protocol
    )

def graknlabs_behaviour():
    git_repository(
        name = "graknlabs_behaviour",
        remote = "https://github.com/graknlabs/behaviour",
        commit = "3364f55caf6a25603b917e5ac15aa97332b1cfd4"
    )

def graknlabs_grabl_tracing():
    git_repository(
        name = "graknlabs_grabl_tracing",
        remote = "https://github.com/graknlabs/grabl-tracing",
        commit = "ceb94d4da4a179b1c6861e5f1daad6201c78d434"  # sync-marker: do not remove this comment, this is used for sync-dependencies by @graknlabs_grabl_tracing
    )
