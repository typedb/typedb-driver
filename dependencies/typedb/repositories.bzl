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
        commit = "959bcdbfac995582812b334ba719b190367e4625",  # sync-marker: do not remove this comment, this is used for sync-dependencies by @typedb_dependencies
    )

def typedb_protocol():
    git_repository(
        name = "typedb_protocol",
        remote = "https://github.com/typedb/typedb-protocol",
        commit = "9e46e089a005d6ca9f017ffb482337b9d5718695",  # sync-marker: do not remove this comment, this is used for sync-dependencies by @typedb_protocol
    )

def typedb_behaviour():
    # TODO: Return typedb
    git_repository(
        name = "typedb_behaviour",
        remote = "https://github.com/farost/typedb-behaviour",
        commit = "6b018844b9c0334e3fc3d2e4096f70f5ac29ba69",  # sync-marker: do not remove this comment, this is used for sync-dependencies by @typedb_behaviour
    )
