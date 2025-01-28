#!/bin/bash
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

languages=("rust" "java" "python")

base_targets=(
    "//{language}:example_core"
    "//{language}:readme_example_core"
    "//{language}:example_cloud"
    "//{language}:readme_example_cloud"
)

for lang in "${languages[@]}"; do
    echo "Updating README for $lang"

    for target in "${base_targets[@]}"; do
        substituted_target="${target//\{language\}/$lang}"
        echo "Running: bazel run $substituted_target"
        bazel run "$substituted_target"

        if [ $? -ne 0 ]; then
            echo "Error running: bazel run $substituted_target"
            exit 1
        fi
    done
done

echo "All README files updated successfully."
