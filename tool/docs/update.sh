#!/usr/bin/env sh
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

# TODO: Temporarily update only 3.0 drivers
bazel run //rust:docs_adoc
bazel run //java:docs_adoc
bazel run //python:docs_adoc
bazel run //http-ts:docs_adoc
#bazel run //nodejs:docs_adoc
#bazel run //c:docs_adoc
#bazel run //cpp:docs_adoc
#bazel run //csharp:docs_adoc
