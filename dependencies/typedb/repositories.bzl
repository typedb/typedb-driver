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
    # TODO: Return typedb
    git_repository(
        name = "typedb_dependencies",
        remote = "https://github.com/farost/typedb-dependencies",
        commit = "b198f8d11a0b437ee9f9cf1e4eead8bedcdb7312",  # sync-marker: do not remove this comment, this is used for sync-dependencies by @typedb_dependencies
    )

def typedb_protocol():
    # TODO: Return typedb
    git_repository(
        name = "typedb_protocol",
        remote = "https://github.com/farost/typedb-protocol",
        commit = "3779844e23f2f6e022445fb8caa6b2f7cd6d99eb",  # sync-marker: do not remove this comment, this is used for sync-dependencies by @typedb_protocol
    )

def typedb_behaviour():
    # TODO: Return typedb
    git_repository(
        name = "typedb_behaviour",
        remote = "https://github.com/farost/typedb-behaviour",
        commit = "f7c80395b00a1177194d2648e830f41afb059667",  # sync-marker: do not remove this comment, this is used for sync-dependencies by @typedb_behaviour
    )
