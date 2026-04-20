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

load("@typedb_dependencies//distribution/artifact:rules.bzl", "native_artifact_files")
load("@typedb_dependencies//distribution:deployment.bzl", "deployment", "deployment_private")

def typedb_artifact():
    native_artifact_files(
        name = "typedb_artifact",
        group_name = "typedb-all-{platform}",
        artifact_name = "typedb-all-{platform}-{version}.{ext}",
        tag_source = deployment["artifact"]["release"]["download"],
        commit_source = deployment["artifact"]["snapshot"]["download"],
        commit = "53156d7aed0193163f29587bcc4b47f28665e216"
    )

def typedb_cluster_artifact():
    native_artifact_files(
        name = "typedb_cluster_artifact",
        group_name = "typedb-cluster-all-{platform}",
        artifact_name = "typedb-cluster-all-{platform}-{version}.tar.gz", # TODO: Make {ext} instead of tar.gz
        tag_source = deployment_private["artifact"]["release"]["download"],
        commit_source = deployment_private["artifact"]["snapshot"]["download"],
        commit = "d5e64113bc33c0c1e06a651e6f83ce7c46c9253a",
    )
