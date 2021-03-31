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

load("@bazel_tools//tools/build_defs/repo:git.bzl", "git_repository")
def graknlabs_dependencies():
    git_repository(
        name = "graknlabs_dependencies",
        remote = "https://github.com/graknlabs/dependencies",
        commit = "36bc9911558dc4a81f1f467d5a080033b62852fe", # sync-marker: do not remove this comment, this is used for sync-dependencies by @graknlabs_dependencies
    )

#TODO: MOVE NATIVE_GRAKN_ARTIFACT INTO DEPENDENCIES, THEN REMOVE THIS DEPENDENCY
def graknlabs_common():
    git_repository(
        name = "graknlabs_common",
        remote = "https://github.com/graknlabs/common",
        tag = "2.0.0" # sync-marker: do not remove this comment, this is used for sync-dependencies by @graknlabs_common
    )

def graknlabs_behaviour():
    git_repository(
        name = "graknlabs_behaviour",
        remote = "https://github.com/graknlabs/behaviour",
        commit = "f5277aaf442894f15d8185c577d3c8dec04fd6ad", # sync-marker: do not remove this comment, this is used for sync-dependencies by @graknlabs_behaviour
    )
