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

load("@graknlabs_bazel_distribution//artifact:rules.bzl", "artifact_file")
load("@graknlabs_dependencies//distribution:deployment.bzl", "deployment_private")

def graknlabs_grakn_core_artifact():
    artifact_file(
        name = "graknlabs_grakn_core_artifact",
        group_name = "graknlabs_grakn_core",
        artifact_name = "grakn-core-server-linux-{version}.tar.gz",
        tag_source = deployment_private["artifact.release"],
        commit_source = deployment_private["artifact.snapshot"],
        commit = "fa7d459906bbb85c4e9298a67c5170159d4de11c",
    )
