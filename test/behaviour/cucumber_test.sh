#
# Copyright (C) 2021 Vaticle
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
TYPEDB_DISTRO=$1
if test -d typedb_distribution; then
  echo Existing distribution detected. Cleaning.
  rm -rf typedb_distribution
fi
mkdir typedb_distribution
if [[ $TYPEDB_DISTRO == *"cluster"* ]]; then
  PRODUCT=Cluster
else
  PRODUCT=Core
fi
echo Attempting to unarchive TypeDB $PRODUCT distribution from $TYPEDB_DISTRO
if [[ ${TYPEDB_DISTRO: -7} == ".tar.gz" ]]; then
  tar -xf $TYPEDB_DISTRO -C ./typedb_distribution
else
  if [[ ${TYPEDB_DISTRO: -4} == ".zip" ]]; then
    unzip -q $TYPEDB_DISTRO -d ./typedb_distribution
  else
    echo Supplied artifact file was not in a recognised format. Only .tar.gz and .zip artifacts are acceptable.
    exit 1
  fi
fi
DIRECTORY=$(ls ./typedb_distribution)
echo Successfully unarchived TypeDB $PRODUCT distribution.
echo Starting TypeDB $PRODUCT Server
mkdir ./typedb_distribution/"$DIRECTORY"/typedb_test
if [[ $PRODUCT == "Core" ]]; then
  ./typedb_distribution/"$DIRECTORY"/typedb server --data typedb_test &
else
  ./typedb_distribution/"$DIRECTORY"/typedb server --address "127.0.0.1:1729:1730:1731" &
fi
echo Unarchiving client.
tar -xf client-nodejs.tar.gz
echo Client unarchived.
echo Unarchiving TypeDB $PRODUCT behaviour step implementations.
if [[ $PRODUCT == "Core" ]]; then
  tar -xf test/behaviour/behaviour-steps-core.tar.gz -C test
else
  tar -xf test/behaviour/behaviour-steps-cluster.tar.gz -C test
fi
echo TypeDB $PRODUCT behaviour step implementations unarchived.
POLL_INTERVAL_SECS=0.5
MAX_RETRIES=60
RETRY_NUM=0
while [[ $RETRY_NUM -lt $MAX_RETRIES ]]; do
  RETRY_NUM=$(($RETRY_NUM + 1))
  if [[ $(($RETRY_NUM % 4)) -eq 0 ]]; then
    echo Waiting for TypeDB $PRODUCT server to start \($(($RETRY_NUM / 2))s\)...
  fi
  lsof -i :1729 && STARTED=1 || STARTED=0
  if [[ $STARTED -eq 1 ]]; then
    break
  fi
  sleep $POLL_INTERVAL_SECS
done
if [[ $STARTED -eq 0 ]]; then
  echo Failed to start TypeDB $PRODUCT server
  exit 1
fi
echo TypeDB $PRODUCT database server started
node ./node_modules/.bin/cucumber-js ./external/vaticle_typedb_behaviour/**/*.feature --require './**/*.js' --tags 'not @ignore and not @ignore-client-nodejs' --format @cucumber/pretty-formatter && export RESULT=0 || export RESULT=1
echo Tests concluded with exit value $RESULT
echo Stopping server.
if [[ $PRODUCT == "Core" ]]; then
  kill $(jps | awk '/TypeDBServer/ {print $1}')
else
  kill $(jps | awk '/TypeDBNode/ {print $1}')
fi
exit $RESULT
