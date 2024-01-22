#!/bin/bash
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

TAG=$(git describe --exact-match --tags $(git log -n1 --pretty='%h') 2>/dev/null)
if [ ! -z "$TAG" ]; then
    echo STABLE_VERSION $TAG
else
    echo STABLE_VERSION 0.0.0-$(git rev-parse HEAD)
fi

TYPEDB_PROTOCOL_VERSION=$(grep -o '"[^"]*".*sync-marker.*typedb_protocol' dependencies/vaticle/repositories.bzl | sed 's/.*"\(.*\)".*/\1/')
if [ -z "$TYPEDB_PROTOCOL_VERSION" ]; then
  # the following line only prints when the script is run directly
  echo "@vaticle_typedb_protocol version not found in dependencies/vaticle/repositories.bzl, cannot stamp"
  exit 1
elif [[ "$TYPEDB_PROTOCOL_VERSION" =~ ^[0-9a-f]{40}$ ]]; then # SHA
  TYPEDB_PROTOCOL_VERSION=0.0.0-$TYPEDB_PROTOCOL_VERSION
fi
echo STABLE_PROTOCOL_VERSION $TYPEDB_PROTOCOL_VERSION
