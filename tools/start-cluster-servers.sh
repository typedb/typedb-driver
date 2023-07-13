#!/usr/bin/env bash
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

set -e

function server_start() {
  ./${1}/typedb cluster \
    --storage.data=server/data \
    --server.address=localhost:${1}1729 \
    --server.internal-address.zeromq=localhost:${1}1730 \
    --server.internal-address.grpc=localhost:${1}1731 \
    --server.peers.peer-1.address=localhost:11729 \
    --server.peers.peer-1.internal-address.zeromq=localhost:11730 \
    --server.peers.peer-1.internal-address.grpc=localhost:11731 \
    --server.peers.peer-2.address=localhost:21729 \
    --server.peers.peer-2.internal-address.zeromq=localhost:21730 \
    --server.peers.peer-2.internal-address.grpc=localhost:21731 \
    --server.peers.peer-3.address=localhost:31729 \
    --server.peers.peer-3.internal-address.zeromq=localhost:31730 \
    --server.peers.peer-3.internal-address.grpc=localhost:31731 \
    --server.encryption.enable=true
}

rm -rf 1 2 3 typedb-cluster-all

bazel run //rust/tests:typedb-cluster-extractor -- typedb-cluster-all
echo Successfully unarchived TypeDB distribution. Creating 3 copies.
cp -r typedb-cluster-all 1 && cp -r typedb-cluster-all 2 && cp -r typedb-cluster-all 3
echo Starting a cluster consisting of 3 servers...
server_start 1 &
server_start 2 &
server_start 3 &

ROOT_CA=`realpath typedb-cluster-all/server/conf/encryption/ext-root-ca.pem`
export ROOT_CA

POLL_INTERVAL_SECS=0.5
MAX_RETRIES=60
RETRY_NUM=0
while [[ $RETRY_NUM -lt $MAX_RETRIES ]]; do
  RETRY_NUM=$(($RETRY_NUM + 1))
  if [[ $(($RETRY_NUM % 4)) -eq 0 ]]; then
    echo Waiting for TypeDB Cluster servers to start \($(($RETRY_NUM / 2))s\)...
  fi
  lsof -i :11729 && STARTED1=1 || STARTED1=0
  lsof -i :21729 && STARTED2=1 || STARTED2=0
  lsof -i :31729 && STARTED3=1 || STARTED3=0
  if [[ $STARTED1 -eq 1 && $STARTED2 -eq 1 && $STARTED3 -eq 1 ]]; then
    break
  fi
  sleep $POLL_INTERVAL_SECS
done
if [[ $STARTED1 -eq 0 || $STARTED2 -eq 0 || $STARTED3 -eq 0 ]]; then
  echo Failed to start one or more TypeDB Cluster servers
  exit 1
fi
sleep 10
echo 3 TypeDB Cluster database servers started
