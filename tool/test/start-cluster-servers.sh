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

DEPLOYMENT_ID="test"

ROOT_CA_PATH="$(realpath tool/test/resources/encryption/ext-grpc-root-ca.pem)"
CERT_PATH="$(realpath tool/test/resources/encryption/ext-grpc-certificate.pem)"
KEY_PATH="$(realpath tool/test/resources/encryption/ext-grpc-private-key.pem)"
CONFIG_PATH="$(realpath tool/test/resources/config.yml)"

server_start() {
  local node_id="$1"
  local server_port="${node_id}1729"
  local clustering_port="${node_id}1730"
  local monitoring_port="${node_id}1731"

  local node_dir="./${node_id}"
  local data_dir="${node_dir}/data"
  local clustering_dir="${node_dir}/clustering"

  "${node_dir}/typedb" server \
    --config="${CONFIG_PATH}" \
    --diagnostics.deployment-id "${DEPLOYMENT_ID}" \
    --server.address="0.0.0.0:${server_port}" \
    --server.connection-address="127.0.0.1:${server_port}" \
    --server.http.enabled=false \
    --server.clustering.id="${node_id}" \
    --server.clustering.address="0.0.0.0:${clustering_port}" \
    --server.encryption.enabled="${ENCRYPTION_ENABLED}" \
    --server.encryption.certificate="${CERT_PATH}" \
    --server.encryption.certificate-key="${KEY_PATH}" \
    --server.encryption.ca-certificate="${ROOT_CA_PATH}" \
    --storage.data-directory="${data_dir}" \
    --storage.clustering-directory="${clustering_dir}" \
    --diagnostics.monitoring.port="${monitoring_port}" \
    --development-mode.enabled=true
}

rm -rf $(seq 1 $NODE_COUNT) typedb-cluster-all

bazel run //tool/test:typedb-cluster-extractor -- typedb-cluster-all

echo Successfully unarchived a TypeDB distribution. Creating $NODE_COUNT copies ${1}.
for i in $(seq 1 $NODE_COUNT); do
  rm -rf $i
  cp -r typedb-cluster-all $i || exit 1
done
echo Starting a cluster consisting of $NODE_COUNT servers...
for i in $(seq 1 $NODE_COUNT); do
  server_start $i &
done

ROOT_CA=$ROOT_CA_PATH
export ROOT_CA

POLL_INTERVAL_SECS=0.5
LEADER_SELF_ELECT_TIMEOUT=8
MAX_RETRIES=60
RETRY_NUM=0
while [[ $RETRY_NUM -lt $MAX_RETRIES ]]; do
  RETRY_NUM=$(($RETRY_NUM + 1))
  if [[ $(($RETRY_NUM % 4)) -eq 0 ]]; then
    echo Waiting for TypeDB Cluster servers to start \($(($RETRY_NUM / 2))s\)...
  fi
  ALL_STARTED=1
  for i in $(seq 1 $NODE_COUNT); do
    lsof -i :${i}1729 || ALL_STARTED=0
  done
  if (( $ALL_STARTED )); then
    break
  fi
  sleep $POLL_INTERVAL_SECS
done
if (( ! $ALL_STARTED )); then
  echo Failed to start one or more TypeDB Cluster servers
  exit 1
fi

sleep $LEADER_SELF_ELECT_TIMEOUT
echo $NODE_COUNT TypeDB Cluster database servers started
