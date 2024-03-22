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

load("@bazel_tools//tools/build_defs/repo:git.bzl", "git_repository")

def vaticle_dependencies():
#    git_repository(
#        name = "vaticle_dependencies",
#        remote = "https://github.com/vaticle/dependencies",
#        commit = "7518f8adaf5714512a8af96128b15cca7dedd475", # sync-marker: do not remove this comment, this is used for sync-dependencies by @vaticle_dependencies
#    )
    git_repository(
        name = "vaticle_dependencies",
        remote = "https://github.com/vaticle/dependencies",
        commit = "215564b3d6d97788407930f332fa6da336799ac1", # sync-marker: do not remove this comment, this is used for sync-dependencies by @vaticle_dependencies
    )

def vaticle_typeql():
    git_repository(
        name = "vaticle_typeql",
        remote = "https://github.com/vaticle/typeql",
        commit = "4871520d7f68ff40cd88562d4932d085284ad6fb",  # sync-marker: do not remove this comment, this is used for sync-dependencies by @vaticle_typeql
    )

def vaticle_typedb_protocol():
    git_repository(
        name = "vaticle_typedb_protocol",
        remote = "https://github.com/vaticle/typedb-protocol",
        # NOTE: the sync-marker is also used for workspace status by Bazel!
        commit = "d67b20f81e7b3a19ba48a56ae1bcb95e6b89bf34",  # sync-marker: do not remove this comment, this is used for sync-dependencies by @vaticle_typedb_protocol
    )

def vaticle_typedb_behaviour():
    git_repository(
        name = "vaticle_typedb_behaviour",
        remote = "https://github.com/vaticle/typedb-behaviour",
        commit = "6bd4b47bdb6c4464bdab0d5a547f10f3c2a46f2c",  # sync-marker: do not remove this comment, this is used for sync-dependencies by @vaticle_typedb_behaviour
    )
