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
        remote = "https://github.com/flyingsilverfin/dependencies",
        commit = "703e75f2b8bc5ec4c8d427c0dcc96313e950d36a", # sync-marker: do not remove this comment, this is used for sync-dependencies by @vaticle_dependencies
    )

def vaticle_typedb_common():
    git_repository(
        name = "vaticle_typedb_common",
        remote = "https://github.com/vaticle/typedb-common",
        tag = "2.18.0" # sync-marker: do not remove this comment, this is used for sync-dependencies by @vaticle_typedb_common
    )

def vaticle_typeql():
    git_repository(
        name = "vaticle_typeql",
        remote = "https://github.com/vaticle/typeql",
        tag = "2.18.0", # sync-marker: do not remove this comment, this is used for sync-dependencies by @vaticle_typeql
    )

def vaticle_typedb_protocol():
    git_repository(
        name = "vaticle_typedb_protocol",
        remote = "https://github.com/flyingsilverfin/typedb-protocol",
        commit = "f12e0e8f6ce761bfee3070d6b705212b70137ca1", # sync-marker: do not remove this comment, this is used for sync-dependencies by @vaticle_typedb_protocol
    )
#    native.local_repository(
#        name = "vaticle_typedb_protocol",
#        path = "../typedb-protocol",
#    )

def vaticle_typedb_behaviour():
    git_repository(
        name = "vaticle_typedb_behaviour",
        remote = "https://github.com/vaticle/typedb-behaviour",
        commit = "90b4addcd71a398e9e52e322021c49310e66a47a" # sync-marker: do not remove this comment, this is used for sync-dependencies by @vaticle_typedb_behaviour
    )

def vaticle_factory_tracing():
    git_repository(
        name = "vaticle_factory_tracing",
        remote = "https://github.com/flyingsilverfin/factory-tracing",
        commit = "1b9213a246f81a5817f3037608c5de7bede5b4cb"  # sync-marker: do not remove this comment, this is used for sync-dependencies by @vaticle_factory_tracing
    )
#    native.local_repository(
#        name = "vaticle_factory_tracing",
#        path = "../factory-tracing",
#    )