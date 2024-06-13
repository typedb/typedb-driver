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

export BAZEL_JAVA_HOME=$(bazel run //tool/test:echo-java-home)
NODE_COUNT=${1:-1}

peers=
for i in $(seq 1 $NODE_COUNT); do
  peers="${peers} --server.peers.peer-${i}.address=localhost:${i}1729"
  peers="${peers} --server.peers.peer-${i}.internal-address.zeromq=localhost:${i}1730"
  peers="${peers} --server.peers.peer-${i}.internal-address.grpc=localhost:${i}1731"
done

function server_start() {
  JAVA_HOME=$BAZEL_JAVA_HOME ./${1}/typedb server \
    --storage.data=server/data \
    --server.address=localhost:${1}1729 \
    --server.internal-address.zeromq=localhost:${1}1730 \
    --server.internal-address.grpc=localhost:${1}1731 \
    $(echo $peers) \
    --server.encryption.enable=true \
    --server.encryption.file.enable=true \
    --server.encryption.file.external-grpc.private-key=`realpath tool/test/resources/encryption/ext-grpc-private-key.pem` \
    --server.encryption.file.external-grpc.certificate=`realpath tool/test/resources/encryption/ext-grpc-certificate.pem` \
    --server.encryption.file.external-grpc.root-ca=`realpath tool/test/resources/encryption/ext-grpc-root-ca.pem` \
    --server.encryption.file.internal-grpc.private-key=`realpath tool/test/resources/encryption/int-grpc-private-key.pem` \
    --server.encryption.file.internal-grpc.certificate=`realpath tool/test/resources/encryption/int-grpc-certificate.pem` \
    --server.encryption.file.internal-grpc.root-ca=`realpath tool/test/resources/encryption/int-grpc-root-ca.pem` \
    --server.encryption.file.internal-zmq.private-key=`realpath tool/test/resources/encryption/int-zmq-private-key` \
    --server.encryption.file.internal-zmq.public-key=`realpath tool/test/resources/encryption/int-zmq-public-key` \
    --development-mode.enable=true
}

rm -rf $(seq 1 $NODE_COUNT) typedb-cloud-all

bazel run //tool/test:typedb-cloud-extractor -- typedb-cloud-all
echo Successfully unarchived TypeDB distribution. Creating $NODE_COUNT copies.
for i in $(seq 1 $NODE_COUNT); do
  cp -r typedb-cloud-all $i || exit 1
done
echo Starting a cloud consisting of $NODE_COUNT servers...
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
    echo Waiting for TypeDB Cloud servers to start \($(($RETRY_NUM / 2))s\)...
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
  echo Failed to start one or more TypeDB Cloud servers
  exit 1
fi
echo $NODE_COUNT TypeDB Cloud database servers started
