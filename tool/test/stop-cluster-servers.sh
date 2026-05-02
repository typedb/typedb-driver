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

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
"${SCRIPT_DIR}/cluster-server.sh" stop-all

# Reap log-tailing processes started by start-cluster-servers.sh. Without this,
# orphaned `tail -f` processes hold the CI runner's stdout pipe open after the
# main command exits and the job hangs. CLUSTER_TAIL_PIDS holds the rightmost
# PID of each `tail -f | sed` pipeline; pkill catches the tail siblings (and
# any pipeline whose PID we didn't capture).
if [[ -n "${CLUSTER_TAIL_PIDS:-}" ]]; then
  for pid in $CLUSTER_TAIL_PIDS; do
    kill "$pid" 2>/dev/null || true
  done
fi
pkill -f 'tail -f \./[0-9]+/server\.log' 2>/dev/null || true
