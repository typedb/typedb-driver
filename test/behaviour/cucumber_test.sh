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
GRAKN_DISTRO=$1
if test -d grakn_distribution; then
  echo Existing distribution detected. Cleaning.
  rm -rf grakn_distribution
fi
mkdir grakn_distribution
if [[ $GRAKN_DISTRO == *"cluster"* ]]; then
  PRODUCT=Cluster
else
  PRODUCT=Core
fi
echo Attempting to unarchive Grakn $PRODUCT distribution from $GRAKN_DISTRO
if [[ ${GRAKN_DISTRO: -7} == ".tar.gz" ]]; then
  tar -xf $GRAKN_DISTRO -C ./grakn_distribution
else
  if [[ ${GRAKN_DISTRO: -4} == ".zip" ]]; then
    unzip -q $GRAKN_DISTRO -d ./grakn_distribution
  else
    echo Supplied artifact file was not in a recognised format. Only .tar.gz and .zip artifacts are acceptable.
    exit 1
  fi
fi
DIRECTORY=$(ls ./grakn_distribution)
echo Successfully unarchived Grakn $PRODUCT distribution.
echo Starting Grakn $PRODUCT Server
mkdir ./grakn_distribution/"$DIRECTORY"/grakn_test
if [[ $PRODUCT == "Core" ]]; then
  ./grakn_distribution/"$DIRECTORY"/grakn server --data grakn_test &
else
  ./grakn_distribution/"$DIRECTORY"/grakn server --address "127.0.0.1:1729:1730" &
fi
echo Unarchiving client.
tar -xf client-nodejs.tar.gz
echo Client unarchived.
echo Unarchiving Grakn $PRODUCT behaviour step implementations.
if [[ $PRODUCT == "Core" ]]; then
  tar -xf test/behaviour/behaviour-steps-core.tar.gz -C test
else
  tar -xf test/behaviour/behaviour-steps-cluster.tar.gz -C test
fi
echo Grakn $PRODUCT behaviour step implementations unarchived.
POLL_INTERVAL_SECS=0.5
MAX_RETRIES=60
RETRY_NUM=0
while [[ $RETRY_NUM -lt $MAX_RETRIES ]]; do
  RETRY_NUM=$(($RETRY_NUM + 1))
  if [[ $(($RETRY_NUM % 4)) -eq 0 ]]; then
    echo Waiting for Grakn $PRODUCT server to start \($(($RETRY_NUM / 2))s\)...
  fi
  lsof -i :1729 && STARTED=1 || STARTED=0
  if [[ $STARTED -eq 1 ]]; then
    break
  fi
  sleep $POLL_INTERVAL_SECS
done
if [[ $STARTED -eq 0 ]]; then
  echo Failed to start Grakn $PRODUCT server
  exit 1
fi
echo Grakn $PRODUCT database server started
node ./node_modules/.bin/cucumber-js ./external/graknlabs_behaviour/**/*.feature --require './**/*.js' --tags 'not @ignore and not @ignore-client-nodejs' --format @cucumber/pretty-formatter && export RESULT=0 || export RESULT=1
echo Tests concluded with exit value $RESULT
echo Stopping server.
kill $(jps | awk '/GraknServer/ {print $1}')
exit $RESULT
