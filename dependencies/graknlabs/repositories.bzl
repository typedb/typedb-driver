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
        commit = "0868c2349040dc89c79eeb2d0198635cd88bbae9", # sync-marker: do not remove this comment, this is used for sync-dependencies by @graknlabs_dependencies
    )

#TODO: MOVE NATIVE_GRAKN_ARTIFACT INTO DEPENDENCIES, THEN REMOVE THIS DEPENDENCY
def graknlabs_common():
    git_repository(
        name = "graknlabs_common",
        remote = "https://github.com/graknlabs/common",
        commit = "fcd45c6a30018e0107d5bbf4c5cd4fba2f10b245" # sync-marker: do not remove this comment, this is used for sync-dependencies by @graknlabs_common
    )

def graknlabs_behaviour():
    git_repository(
        name = "graknlabs_behaviour",
        remote = "https://github.com/graknlabs/behaviour",
        commit = "7039a69aa776ca29511403d39111af36bc451282", # sync-marker: do not remove this comment, this is used for sync-dependencies by @graknlabs_behaviour
    )
