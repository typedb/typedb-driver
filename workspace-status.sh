#!/bin/bash
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

tag=$(git describe --exact-match --tags $(git log -n1 --pretty='%h') 2>/dev/null)
if [ ! -z "$tag" ]; then
    echo STABLE_VERSION $tag
else
    echo STABLE_VERSION 0.0.0-$(git rev-parse HEAD)
fi

workspace=$(realpath $(readlink bazel-typedb-driver)/../..)
workspace_refs=$workspace/external/vaticle_typedb_driver_workspace_refs/refs.json
typedb_protocol_version=$(grep -o '"vaticle_typedb_protocol":"[^"]*"' $workspace_refs | sed 's/.*:"\(.*\)"/\1/')

echo STABLE_PROTOCOL_VERSION $typedb_protocol_version

# TODO parse workspace refs (in jq?)
#echo STABLE_WORKSPACE_REFS $(cat $workspace_refs)
