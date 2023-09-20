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
        commit = "cfd3fef650d44a02fc72429336f08a4f24793f8f", # sync-marker: do not remove this comment, this is used for sync-dependencies by @vaticle_dependencies
    )

def vaticle_typedb_common():
    git_repository(
        name = "vaticle_typedb_common",
        remote = "https://github.com/vaticle/typedb-common",
        tag = "2.21.0" # sync-marker: do not remove this comment, this is used for sync-dependencies by @vaticle_typedb_common
    )

def vaticle_typeql():
    git_repository(
        name = "vaticle_typeql",
        remote = "https://github.com/vaticle/typeql",
        commit = "848532c3dc9bddeae0ea5dd5111d0bffd41799ac",  # sync-marker: do not remove this comment, this is used for sync-dependencies by @vaticle_typeql
    )

def vaticle_typedb_protocol():
    git_repository(
        name = "vaticle_typedb_protocol",
        remote = "https://github.com/vaticle/typedb-protocol",
        commit = "f007935da098cda2df1bdc3a97aff6762f75eb4c" # sync-marker: do not remove this comment, this is used for sync-dependencies by @vaticle_typedb_protocol
    )

def vaticle_typedb_behaviour():
    git_repository(
        name = "vaticle_typedb_behaviour",
        remote = "https://github.com/vaticle/typedb-behaviour",
        commit = "55128b02010cb5d6866195778371b0a55b09a9dc",  # sync-marker: do not remove this comment, this is used for sync-dependencies by @vaticle_typedb_behaviour
    )
