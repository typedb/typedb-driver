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

# To update external required dependencies with gazelle, follow these steps:
# 1. Specify the desired packages to your go.mod file manually OR with go get (requires installing Go on your local)
# 2. Run this shell script to add the dependencies in go/deps.bzl with bazel go_repositories.
# 3. Add the import to the go file it will be used in
# - (import_path is declared in deps.bzl, where the repositories are defined).

# go get PACKAGE NAME, eg:
# go get github.com/cucumber/godog

bazel run //go/gazelle:update-dependencies
