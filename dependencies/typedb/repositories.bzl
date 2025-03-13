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

def typedb_dependencies():
    git_repository(
        name = "typedb_dependencies",
        remote = "https://github.com/typedb/typedb-dependencies",
        commit = "cf9c1707c7896d61ff97bbf60b1880852ad42353",  # sync-marker: do not remove this comment, this is used for sync-dependencies by @typedb_dependencies
    )

def typedb_protocol():
    # TODO: Temp, return typedb
    git_repository(
        name = "typedb_protocol",
        remote = "https://github.com/farost/typedb-protocol",
        commit = "b4b7ee87b08c16831a21629f81974347f38cce5c",  # sync-marker: do not remove this comment, this is used for sync-dependencies by @typedb_protocol
    )
#    git_repository(
#        name = "typedb_protocol",
#        remote = "https://github.com/typedb/typedb-protocol",
#        tag = "3.0.0",  # sync-marker: do not remove this comment, this is used for sync-dependencies by @typedb_protocol
#    )

def typedb_behaviour():
    git_repository(
        name = "typedb_behaviour",
        remote = "https://github.com/typedb/typedb-behaviour",
        commit = "a5ca738d691e7e7abec0a69e68f6b06310ac2168",  # sync-marker: do not remove this comment, this is used for sync-dependencies by @typedb_behaviour
    )
