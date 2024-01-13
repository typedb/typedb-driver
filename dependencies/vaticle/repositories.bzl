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
        remote = "https://github.com/vaticle/dependencies",
        commit = "a4a3bac9515fd51365e02f6aad762f67357e49a5", # sync-marker: do not remove this comment, this is used for sync-dependencies by @vaticle_dependencies
    )

def vaticle_typedb_common():
    git_repository(
        name = "vaticle_typedb_common",
        remote = "https://github.com/vaticle/typedb-common",
        commit = "dbc333528ecdafa5b571344237e831619c3fa5f0",  # sync-marker: do not remove this comment, this is used for sync-dependencies by @vaticle_typedb_common
    )

def vaticle_typeql():
    git_repository(
        name = "vaticle_typeql",
        remote = "https://github.com/vaticle/typeql",
        commit = "3a523f4d7d40b0c5d3b9af68f31a3859215fa671",  # sync-marker: do not remove this comment, this is used for sync-dependencies by @vaticle_typeql
    )

def vaticle_typedb_protocol():
    # needed for workspace status
    VATICLE_TYPEDB_PROTOCOL_VERSION = "a1ebf01a761268dc28c6fd2ab8b6cd825431f3d4" # sync-marker: do not remove this comment, this is used for sync-dependencies by @vaticle_typedb_protocol
    git_repository(
        name = "vaticle_typedb_protocol",
        remote = "https://github.com/vaticle/typedb-protocol",
        commit = "7b223fe459f9f0d1358ca3a71e531308aa328b7e"
    )

def vaticle_typedb_behaviour():
    git_repository(
        name = "vaticle_typedb_behaviour",
        remote = "https://github.com/vaticle/typedb-behaviour",
        commit = "46a16939c1dd94aac6aebc3dd7e50e8280d3dfe5",  # sync-marker: do not remove this comment, this is used for sync-dependencies by @vaticle_typedb_behaviour
    )
