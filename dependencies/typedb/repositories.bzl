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
     # TODO: Return ref after merge to master, currently points to 'raft-dependencies-addition'
     git_repository(
         name = "typedb_dependencies",
         remote = "https://github.com/typedb/typedb-dependencies",
         commit = "19a70bcad19b9a28814016f183ac3e3a23c1ff0d",  # sync-marker: do not remove this comment, this is used for sync-dependencies by @typedb_dependencies
     )

def typedb_protocol():
    # TODO: Return ref after merge to master
    git_repository(
        name = "typedb_protocol",
        remote = "https://github.com/typedb/typedb-protocol",
        commit = "b007ebc43e307ca4b6354fedbbfc3223361044dc",  # sync-marker: do not remove this comment, this is used for sync-dependencies by @typedb_protocol
    )

def typedb_behaviour():
    # TODO: Update ref after merge to master
    git_repository(
        name = "typedb_behaviour",
        remote = "https://github.com/typedb/typedb-behaviour",
        commit = "9a09881a377fd515b58f1ee4cbbfd04f4bf67fe8",  # sync-marker: do not remove this comment, this is used for sync-dependencies by @typedb_behaviour
    )
