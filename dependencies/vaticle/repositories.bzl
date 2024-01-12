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

load("@bazel_tools//tools/build_defs/repo:git.bzl", "git_repository")

def vaticle_dependencies():
    git_repository(
        name = "vaticle_dependencies",
        remote = "https://github.com/krishnangovindraj/dependencies",
        commit = "3855727ed3f6d93ccc5f176f4414e0142ca718a2", # sync-marker: do not remove this comment, this is used for sync-dependencies by @vaticle_dependencies
    )

def vaticle_typedb_common():
    git_repository(
        name = "vaticle_typedb_common",
        remote = "https://github.com/krishnangovindraj/typedb-common",
        commit = "731c7d149c40e9ae1430a29cea4c3819a68d3b2d",  # sync-marker: do not remove this comment, this is used for sync-dependencies by @vaticle_typedb_common
    )

def vaticle_typeql():
    git_repository(
        name = "vaticle_typeql",
        remote = "https://github.com/krishnangovindraj/typeql",
        commit = "2212a98f5eb7ec3f9e5389831cf2ceebd63d5044",  # sync-marker: do not remove this comment, this is used for sync-dependencies by @vaticle_typeql
    )

def vaticle_typedb_protocol():
    # needed for workspace status
    VATICLE_TYPEDB_PROTOCOL_VERSION = "2.25.2" # sync-marker: do not remove this comment, this is used for sync-dependencies by @vaticle_typedb_protocol
    git_repository(
        name = "vaticle_typedb_protocol",
        remote = "https://github.com/krishnangovindraj/typedb-protocol",
        commit = "7b223fe459f9f0d1358ca3a71e531308aa328b7e"
    )

def vaticle_typedb_behaviour():
    git_repository(
        name = "vaticle_typedb_behaviour",
        remote = "https://github.com/vaticle/typedb-behaviour",
        commit = "46a16939c1dd94aac6aebc3dd7e50e8280d3dfe5",  # sync-marker: do not remove this comment, this is used for sync-dependencies by @vaticle_typedb_behaviour
    )
