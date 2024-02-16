#
# Copyright (C) 2022 Vaticle
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

load("@vaticle_dependencies//distribution/artifact:rules.bzl", "native_artifact_files")
load("@vaticle_dependencies//distribution:deployment.bzl", "deployment", "deployment_private")

def vaticle_typedb_artifact():
    native_artifact_files(
        name = "vaticle_typedb_artifact",
        group_name = "typedb-server-{platform}",
        artifact_name = "typedb-server-{platform}-{version}.{ext}",
        tag_source = deployment["artifact"]["release"]["download"],
        commit_source = deployment["artifact"]["snapshot"]["download"],
        commit = "66d8deeea813ac8a0f0aa42d6117b600ce62fe6f",
    )

def vaticle_typedb_cloud_artifact():
    native_artifact_files(
        name = "vaticle_typedb_cloud_artifact",
        group_name = "typedb-cloud-server-{platform}",
        artifact_name = "typedb-cloud-server-{platform}-{version}.{ext}",
        tag_source = deployment_private["artifact"]["release"]["download"],
        commit_source = deployment_private["artifact"]["snapshot"]["download"],
        commit = "ff7170c2ced9870fbc079f2499694c0dee1d3389",
    )

maven_artifacts = {
    'com.vaticle.typedb:typedb-runner': '240c3c108f8cbf6e620e52c4df8730431ec13e3f',
    'com.vaticle.typedb:typedb-cloud-runner': '54c3f23385f356d830e1ab1bb66345acfce103e7',
}
