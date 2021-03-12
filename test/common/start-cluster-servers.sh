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

bazel run //test:grakn-cluster-extractor -- grakn_distribution
echo Successfully unarchived Grakn distribution. Creating 3 copies.
cp -r grakn_distribution/$GRAKN/ 1 && cp -r grakn_distribution/$GRAKN/ 2 && cp -r grakn_distribution/$GRAKN/ 3
echo Starting 3 Grakn servers.
./1/grakn server --data data --address 127.0.0.1:11729:11730 --peer 127.0.0.1:11729:11730 --peer 127.0.0.1:21729:21730 --peer 127.0.0.1:31729:31730 &
./2/grakn server --data data --address 127.0.0.1:21729:21730 --peer 127.0.0.1:11729:11730 --peer 127.0.0.1:21729:21730 --peer 127.0.0.1:31729:31730 &
./3/grakn server --data data --address 127.0.0.1:31729:31730 --peer 127.0.0.1:11729:11730 --peer 127.0.0.1:21729:21730 --peer 127.0.0.1:31729:31730 &

POLL_INTERVAL_SECS=0.5
MAX_RETRIES=60
RETRY_NUM=0
while [[ $RETRY_NUM -lt $MAX_RETRIES ]]; do
  RETRY_NUM=$(($RETRY_NUM + 1))
  if [[ $(($RETRY_NUM % 4)) -eq 0 ]]; then
    echo Waiting for Grakn Cluster servers to start \($(($RETRY_NUM / 2))s\)...
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
  echo Failed to start one or more Grakn Cluster servers
  exit 1
fi
echo 3 Grakn Cluster database servers started
