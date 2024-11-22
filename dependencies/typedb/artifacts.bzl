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
        commit = "4237d1429d7f97e709a3b1ddc2c30ce3c3b792a0"
    )

#def typedb_cloud_artifact():
#    native_artifact_files(
#        name = "typedb_cloud_artifact",
#        group_name = "typedb-cloud-server-{platform}",
#        artifact_name = "typedb-cloud-server-{platform}-{version}.{ext}",
#        tag_source = deployment_private["artifact"]["release"]["download"],
#        commit_source = deployment_private["artifact"]["snapshot"]["download"],
#        tag = "e4e4fee9d488e2a6e89e29716b98e3213d228809",
#    )

#maven_artifacts = {
#    'com.typedb:typedb-runner': '2.28.3',
#    'com.typedb:typedb-cloud-runner': '2.28.3',
#}
