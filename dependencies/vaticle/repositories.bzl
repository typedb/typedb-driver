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
    git_repository(
        name = "vaticle_dependencies",
        remote = "https://github.com/typedb/dependencies",
        commit = "f0e5ac771ead5ea8d6e768cbd9b6746b64ed00b1",  # sync-marker: do not remove this comment, this is used for sync-dependencies by @vaticle_dependencies
    )

def vaticle_typedb_protocol():
    git_repository(
        name = "vaticle_typedb_protocol",
        remote = "https://github.com/typedb/typedb-protocol",
        tag = "3.0.0-alpha-3",  # sync-marker: do not remove this comment, this is used for sync-dependencies by @vaticle_typedb_protocol
    )

def vaticle_typedb_behaviour():
    # TODO: Return typedb repo after this commit is in
    git_repository(
        name = "vaticle_typedb_behaviour",
        remote = "https://github.com/farost/typedb-behaviour",
        commit = "89df1170b9a147beb8e5c74a1009ccb56935e42e", # sync-marker: do not remove this comment, this is used for sync-dependencies by @vaticle_typedb_behaviour
    )
#    git_repository(
#        name = "vaticle_typedb_behaviour",
#        remote = "https://github.com/typedb/typedb-behaviour",
#        commit = "ac43a25d8116d8932007819473ddc216babfe7aa", # sync-marker: do not remove this comment, this is used for sync-dependencies by @vaticle_typedb_behaviour
#    )
