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

NODE_COUNT=${1:-1}
ENCRYPTION_ENABLED=${2:-true}

# TODO: Update configs
#peers=
#for i in $(seq 1 $NODE_COUNT); do
#  peers="${peers} --server.peers.peer-${i}.address=127.0.0.1:${i}1729"
#  peers="${peers} --server.peers.peer-${i}.internal-address.zeromq=127.0.0.1:${i}1730"
#  peers="${peers} --server.peers.peer-${i}.internal-address.grpc=127.0.0.1:${i}1731"
#done

function server_start() {
  ./${1}/typedb server \
    --server.address=127.0.0.1:${1}1729 \
    --server.encryption.enabled=$ENCRYPTION_ENABLED \
    --server.encryption.certificate=`realpath tool/test/resources/encryption/ext-grpc-certificate.pem` \
    --server.encryption.certificate-key=`realpath tool/test/resources/encryption/ext-grpc-private-key.pem` \
    --server.encryption.ca-certificate=`realpath tool/test/resources/encryption/ext-grpc-root-ca.pem` \
    --diagnostics.monitoring.port ${1}1732 \
    --development-mode.enabled true
#    --storage.data=server/data \
#    --server.internal-address.zeromq=127.0.0.1:${1}1730 \
#    --server.internal-address.grpc=127.0.0.1:${1}1731 \
#    $(echo $peers) \
#    --server.encryption.enable=true \
#    --server.encryption.file.enable=true \
#    --server.encryption.file.external-grpc.private-key=`realpath tool/test/resources/encryption/ext-grpc-private-key.pem` \
#    --server.encryption.file.external-grpc.certificate=`realpath tool/test/resources/encryption/ext-grpc-certificate.pem` \
#    --server.encryption.file.external-grpc.root-ca=`realpath tool/test/resources/encryption/ext-grpc-root-ca.pem` \
#    --server.encryption.file.internal-grpc.private-key=`realpath tool/test/resources/encryption/int-grpc-private-key.pem` \
#    --server.encryption.file.internal-grpc.certificate=`realpath tool/test/resources/encryption/int-grpc-certificate.pem` \
#    --server.encryption.file.internal-grpc.root-ca=`realpath tool/test/resources/encryption/int-grpc-root-ca.pem` \
#    --server.encryption.file.internal-zmq.private-key=`realpath tool/test/resources/encryption/int-zmq-private-key` \
#    --server.encryption.file.internal-zmq.public-key=`realpath tool/test/resources/encryption/int-zmq-public-key` \
}

rm -rf $(seq 1 $NODE_COUNT) typedb-cluster-all

#bazel run //tool/test:typedb-cluster-extractor -- typedb-cluster-all
bazel run //tool/test:typedb-extractor -- typedb-cluster-all

echo Successfully unarchived a TypeDB distribution. Creating $NODE_COUNT copies ${1}.
for i in $(seq 1 $NODE_COUNT); do
  cp -r typedb-cluster-all $i || exit 1
done
echo Starting a cluster consisting of $NODE_COUNT servers...
for i in $(seq 1 $NODE_COUNT); do
  server_start $i &
done

ROOT_CA=`realpath tool/test/resources/encryption/ext-grpc-root-ca.pem`
export ROOT_CA

POLL_INTERVAL_SECS=0.5
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
echo $NODE_COUNT TypeDB Cluster database servers started
