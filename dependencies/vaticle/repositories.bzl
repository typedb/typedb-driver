#
# Copyright (C) 2021 Vaticle
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

def vaticle_typeql_lang_java():
    git_repository(
        name = "vaticle_typeql_lang_java",
        remote = "https://github.com/vaticle/typeql-lang-java",
        tag = "2.4.0", # sync-marker: do not remove this comment, this is used for sync-dependencies by @vaticle_typeql_lang_java
    )

def vaticle_typedb_common():
    git_repository(
        name = "vaticle_typedb_common",
        remote = "https://github.com/vaticle/typedb-common",
        tag = "2.5.0" # sync-marker: do not remove this comment, this is used for sync-dependencies by @vaticle_typedb_common
    )

def vaticle_dependencies():
    git_repository(
        name = "vaticle_dependencies",
        remote = "https://github.com/vaticle/dependencies",
        commit = "69d1fb1f0398b285b51b46391c5b2de45b13789b", # sync-marker: do not remove this comment, this is used for sync-dependencies by @vaticle_dependencies
    )

def vaticle_typedb_protocol():
    git_repository(
        name = "vaticle_typedb_protocol",
        remote = "https://github.com/vaticle/typedb-protocol",
        tag = "2.5.0", # sync-marker: do not remove this comment, this is used for sync-dependencies by @vaticle_typedb_protocol
    )

def vaticle_typedb_behaviour():
    git_repository(
        name = "vaticle_typedb_behaviour",
        remote = "https://github.com/vaticle/typedb-behaviour",
        commit = "042cbf2a6c3b8b9d7a8c3b8d74e796da968523fd", # sync-marker: do not remove this comment, this is used for sync-dependencies by @vaticle_typedb_behaviour
    )

def vaticle_factory_tracing():
    git_repository(
        name = "vaticle_factory_tracing",
        remote = "https://github.com/vaticle/factory-tracing",
        tag = "2.1.0"  # sync-marker: do not remove this comment, this is used for sync-dependencies by @vaticle_factory_tracing
    )
