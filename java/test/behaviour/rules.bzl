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

load("@vaticle_typedb_common//test:rules.bzl", "typedb_java_test")

def typedb_behaviour_java_test(
        name,
        connection_steps_core,
        connection_steps_cluster,
        steps,
        typedb_artifact_mac,
        typedb_artifact_linux,
        typedb_artifact_windows,
        typedb_cluster_artifact_mac,
        typedb_cluster_artifact_linux,
        typedb_cluster_artifact_windows,
        runtime_deps = [],
        **kwargs):

    typedb_java_test(
        name = name + "-core",
        server_mac_artifact = typedb_artifact_mac,
        server_linux_artifact = typedb_artifact_linux,
        server_windows_artifact = typedb_artifact_windows,
        runtime_deps = runtime_deps + [connection_steps_core] + steps,
        **kwargs,
    )

    typedb_java_test(
        name = name + "-cluster",
        server_mac_artifact = typedb_cluster_artifact_mac,
        server_linux_artifact = typedb_cluster_artifact_linux,
        server_windows_artifact = typedb_cluster_artifact_windows,
        runtime_deps = runtime_deps + [connection_steps_cluster] + steps,
        **kwargs,
    )
