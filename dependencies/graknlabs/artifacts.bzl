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

load("@graknlabs_dependencies//distribution/artifact:rules.bzl", "native_artifact_files")
load("@graknlabs_dependencies//distribution:deployment.bzl", "deployment", "deployment_private")

def graknlabs_grakn_core_artifacts():
    native_artifact_files(
        name = "graknlabs_grakn_core_artifact",
        group_name = "graknlabs_grakn_core",
        artifact_name = "grakn-core-server-{platform}-{version}.{ext}",
        tag_source = deployment["artifact.release"],
        commit_source = deployment["artifact.snapshot"],
        commit = "ebb64a3046545d7bcbd5c886395527af32a60f11",
    )

def graknlabs_grakn_cluster_artifacts():
    native_artifact_files(
        name = "graknlabs_grakn_cluster_artifact",
        group_name = "graknlabs_grakn_cluster",
        artifact_name = "grakn-cluster-server-{platform}-{version}.{ext}",
        tag_source = deployment_private["artifact.release"],
        commit_source = deployment_private["artifact.snapshot"],
        commit = "2b27a6f6fa2a3c13ec64d34c9471e9ef1cfc9768",
    )
