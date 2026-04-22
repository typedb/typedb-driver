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

# Cluster server management script for integration tests.
#
# Usage:
#   cluster-server.sh start <node_id>     - Start a server node (background)
#   cluster-server.sh kill <node_id>      - Kill a server node (SIGKILL)
#   cluster-server.sh status <node_id>    - Check if node is alive (exit 0 = alive, 1 = dead)
#   cluster-server.sh await <node_id>     - Block until node is listening (with timeout)
#   cluster-server.sh stop-all            - Stop all cluster nodes
#
# Environment:
#   CLUSTER_DIR         - Directory containing numbered node dirs (default: CWD)
#   ENCRYPTION_ENABLED  - Enable encryption (default: true)
#   DEPLOYMENT_ID       - Deployment ID for diagnostics (default: test)
#
# Assumes:
#   - CWD contains numbered directories (1/, 2/, 3/) with typedb binary
#   - Ports: admin=<id>1728, server=<id>1729, clustering=<id>1730, monitoring=<id>1731, http=<id>8000

set -e

# Ensure common sbin paths are available (lsof may live in /usr/sbin)
export PATH="/usr/sbin:/usr/local/sbin:$PATH"

# Check if a port is listening: returns 0 if listening, 1 otherwise
port_is_listening() {
  local port="$1"
  if command -v lsof >/dev/null 2>&1; then
    lsof -i :"${port}" -sTCP:LISTEN >/dev/null 2>&1
  else
    (echo > /dev/tcp/127.0.0.1/"${port}") >/dev/null 2>&1
  fi
}

# Get PID listening on a port (empty string if none)
pid_on_port() {
  local port="$1"
  if command -v lsof >/dev/null 2>&1; then
    lsof -t -i :"${port}" -sTCP:LISTEN 2>/dev/null || true
  elif [ -r /proc/net/tcp ] || [ -r /proc/net/tcp6 ]; then
    local hex_port
    hex_port=$(printf '%04X' "$port")
    # Find matching lines in /proc/net/tcp{,6} in LISTEN state (0A), extract inode, then find PID
    local inode
    inode=$(awk -v hp="$hex_port" '$2 ~ ":"hp"$" && $4 == "0A" {print $10; exit}' /proc/net/tcp /proc/net/tcp6 2>/dev/null)
    if [ -n "$inode" ] && [ "$inode" != "0" ]; then
      # Search /proc/*/fd for the matching inode
      for fd_dir in /proc/[0-9]*/fd; do
        local pid_dir="${fd_dir%/fd}"
        local pid="${pid_dir##*/}"
        if ls -la "$fd_dir" 2>/dev/null | grep -q "socket:\[$inode\]"; then
          echo "$pid"
          return
        fi
      done
    fi
  fi
}

# Change to the cluster directory if specified (where numbered dirs 1/, 2/, 3/ live)
if [ -n "${CLUSTER_DIR:-}" ]; then
  cd "$CLUSTER_DIR"
fi

ENCRYPTION_ENABLED="${ENCRYPTION_ENABLED:-true}"
DEPLOYMENT_ID="${DEPLOYMENT_ID:-test}"
POLL_INTERVAL_SECS=0.5
MAX_RETRIES=60

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_CA_PATH="$(realpath "${SCRIPT_DIR}/resources/encryption/ext-grpc-root-ca.pem" 2>/dev/null || echo "")"
CERT_PATH="$(realpath "${SCRIPT_DIR}/resources/encryption/ext-grpc-certificate.pem" 2>/dev/null || echo "")"
KEY_PATH="$(realpath "${SCRIPT_DIR}/resources/encryption/ext-grpc-private-key.pem" 2>/dev/null || echo "")"
INT_ROOT_CA_PATH="$(realpath "${SCRIPT_DIR}/resources/encryption/int-grpc-root-ca.pem" 2>/dev/null || echo "")"
INT_CERT_PATH="$(realpath "${SCRIPT_DIR}/resources/encryption/int-grpc-certificate.pem" 2>/dev/null || echo "")"
INT_KEY_PATH="$(realpath "${SCRIPT_DIR}/resources/encryption/int-grpc-private-key.pem" 2>/dev/null || echo "")"
CONFIG_PATH="$(realpath "${SCRIPT_DIR}/resources/config.yml" 2>/dev/null || echo "")"

server_port() {
  echo "${1}1729"
}

server_start() {
  local node_id="$1"
  local server_port="${node_id}1729"
  local clustering_port="${node_id}1730"
  local monitoring_port="${node_id}1731"
  local http_port="${node_id}8000"
  local admin_port="${node_id}1728"

  local node_dir="./${node_id}"
  local data_dir="${node_dir}/data"
  local clustering_dir="${node_dir}/clustering"

  if port_is_listening "${server_port}"; then
    echo "Node ${node_id} already running on port ${server_port}"
    return 0
  fi

  if [ ! -x "${node_dir}/typedb" ]; then
    echo "Error: TypeDB binary not found at ${node_dir}/typedb"
    exit 1
  fi

  local config_args=""
  if [ -n "$CONFIG_PATH" ] && [ -f "$CONFIG_PATH" ]; then
    config_args="--config=${CONFIG_PATH}"
  fi

  local log_file="${node_dir}/server.log"

  nohup "${node_dir}/typedb" server \
    ${config_args} \
    --diagnostics.deployment-id "${DEPLOYMENT_ID}" \
    --server.address="0.0.0.0:${server_port}" \
    --server.connection-address="127.0.0.1:${server_port}" \
    --server.http.enabled=true \
    --server.http.address="0.0.0.0:${http_port}" \
    --server.admin.enabled=true \
    --server.admin.port="${admin_port}" \
    --server.encryption.enabled="${ENCRYPTION_ENABLED}" \
    --server.encryption.certificate="${CERT_PATH}" \
    --server.encryption.certificate-key="${KEY_PATH}" \
    --server.encryption.ca-certificate="${ROOT_CA_PATH}" \
    --server.clustering.id="${node_id}" \
    --server.clustering.address="127.0.0.1:${clustering_port}" \
    --server.clustering.encryption.enabled="${ENCRYPTION_ENABLED}" \
    --server.clustering.encryption.certificate="${INT_CERT_PATH}" \
    --server.clustering.encryption.certificate-key="${INT_KEY_PATH}" \
    --server.clustering.encryption.ca-certificate="${INT_ROOT_CA_PATH}" \
    --storage.data-directory="${data_dir}" \
    --storage.clustering-directory="${clustering_dir}" \
    --diagnostics.monitoring.port="${monitoring_port}" \
    --development-mode.enabled=true > "${log_file}" 2>&1 &
  disown

  echo "Started node ${node_id} (port ${server_port}, log ${log_file})"
}

server_kill() {
  local node_id="$1"
  local port
  port=$(server_port "$node_id")

  local pid
  pid=$(pid_on_port "${port}")

  if [ -z "$pid" ]; then
    echo "No server found on port ${port} (node ${node_id})"
    return 0
  fi

  kill -9 "$pid"
  echo "Killed node ${node_id} (port ${port}, PID ${pid})"
}

server_status() {
  local node_id="$1"
  local port
  port=$(server_port "$node_id")

  if port_is_listening "${port}"; then
    exit 0
  else
    exit 1
  fi
}

server_await() {
  local node_id="$1"
  local port
  port=$(server_port "$node_id")

  local retries=0
  while [ $retries -lt $MAX_RETRIES ]; do
    if port_is_listening "${port}"; then
      echo "Node ${node_id} is ready (port ${port})"
      exit 0
    fi
    retries=$((retries + 1))
    sleep $POLL_INTERVAL_SECS
  done

  echo "Timeout waiting for node ${node_id} (port ${port})"
  exit 1
}

stop_all() {
  for i in 1 2 3 4 5 6 7 8 9; do
    local port
    port=$(server_port "$i")
    local pid
    pid=$(pid_on_port "${port}")
    if [ -n "$pid" ]; then
      kill -9 "$pid" 2>/dev/null || true
      echo "Killed node ${i} (port ${port}, PID ${pid})"
    fi
  done
}

# Main dispatch
COMMAND="${1:-}"
NODE_ID="${2:-}"

case "$COMMAND" in
  start)
    [ -z "$NODE_ID" ] && { echo "Usage: $0 start <node_id>"; exit 1; }
    server_start "$NODE_ID"
    ;;
  kill)
    [ -z "$NODE_ID" ] && { echo "Usage: $0 kill <node_id>"; exit 1; }
    server_kill "$NODE_ID"
    ;;
  status)
    [ -z "$NODE_ID" ] && { echo "Usage: $0 status <node_id>"; exit 1; }
    server_status "$NODE_ID"
    ;;
  await)
    [ -z "$NODE_ID" ] && { echo "Usage: $0 await <node_id>"; exit 1; }
    server_await "$NODE_ID"
    ;;
  stop-all)
    stop_all
    ;;
  *)
    echo "Usage: $0 {start|kill|status|await|stop-all} [node_id]"
    exit 1
    ;;
esac
