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

rm -rf typedb-all

bazel run //tool/test:typedb-extractor -- typedb-all
BAZEL_JAVA_HOME=$(bazel run //tool/test:echo-java-home)
JAVA_HOME=$BAZEL_JAVA_HOME ./typedb-all/typedb server --diagnostics.reporting.errors=false --diagnostics.monitoring.enable=false &

set +e
POLL_INTERVAL_SECS=0.5
RETRY_NUM=10
while [[ $RETRY_NUM -gt 0 ]]; do
  lsof -i :1729 > /dev/null
  if [ $? -eq 0 ]; then exit 0; fi
  ((RETRY_NUM-=1))
  sleep $POLL_INTERVAL_SECS
done
exit 1
