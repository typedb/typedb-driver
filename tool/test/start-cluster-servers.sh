#!/usr/bin/env bash
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

set -e

NODE_COUNT="${1:-1}"
ENCRYPTION_ENABLED="${2:-true}"
export ENCRYPTION_ENABLED

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
CLUSTER_SERVER="${SCRIPT_DIR}/cluster-server.sh"

ROOT_CA_PATH="$(realpath tool/test/resources/encryption/ext-grpc-root-ca.pem)"

rm -rf $(seq 1 $NODE_COUNT) typedb-cluster-all

bazel run //tool/test:typedb-cluster-extractor -- typedb-cluster-all

echo Successfully unarchived a TypeDB distribution. Creating $NODE_COUNT copies.
for i in $(seq 1 $NODE_COUNT); do
  rm -rf $i
  cp -r typedb-cluster-all $i || exit 1
done

echo Starting a cluster consisting of $NODE_COUNT servers...
for i in $(seq 1 $NODE_COUNT); do
  "${CLUSTER_SERVER}" start $i
done

LEADER_SELF_ELECT_TIMEOUT=8
for i in $(seq 1 $NODE_COUNT); do
  "${CLUSTER_SERVER}" await $i
done

sleep $LEADER_SELF_ELECT_TIMEOUT
echo $NODE_COUNT TypeDB Cluster database servers started

# Register peer replicas via admin tool on node 1
if [ "$NODE_COUNT" -gt 1 ]; then
  for i in $(seq 2 $NODE_COUNT); do
    clustering_port="${i}1730"
    echo "Registering replica ${i} at 127.0.0.1:${clustering_port}..."
    ./1/typedb admin --address=127.0.0.1:11728 --command "servers register ${i} 127.0.0.1:${clustering_port}"
  done
fi

ROOT_CA=$ROOT_CA_PATH
export ROOT_CA
