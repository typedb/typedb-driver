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

load("@vaticle_dependencies//distribution/artifact:rules.bzl", "native_artifact_files")
load("@vaticle_dependencies//distribution:deployment.bzl", "deployment", "deployment_private")

def vaticle_typedb_artifact():
    native_artifact_files(
        name = "vaticle_typedb_artifact",
        group_name = "typedb-server-{platform}",
        artifact_name = "typedb-server-{platform}-{version}.{ext}",
        tag_source = deployment["artifact"]["release"]["download"],
        commit_source = deployment["artifact"]["snapshot"]["download"],
        tag = "2.29.1",
    )

def vaticle_typedb_cloud_artifact():
    # Commented out - requires private repo access
    # native_artifact_files(
    #     name = "vaticle_typedb_cloud_artifact",
    #     group_name = "typedb-cloud-server-{platform}",
    #     artifact_name = "typedb-cloud-server-{platform}-{version}.{ext}",
    #     tag_source = deployment_private["artifact"]["release"]["download"],
    #     commit_source = deployment_private["artifact"]["snapshot"]["download"],
    #     commit = "4a28b7a3228df1e656ddd655938114c726733a08",
    # )
    pass

maven_artifacts = {
    'com.vaticle.typedb:typedb-runner': '2.29.1',
    # 'com.vaticle.typedb:typedb-cloud-runner': '0.0.0-4a28b7a3228df1e656ddd655938114c726733a08',  # Private
}
