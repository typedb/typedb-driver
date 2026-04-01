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

# Python version configurations for multi-version driver builds.
# Order matters! First python toolchain that is registered is used by Bazel's py_binary.
#
# Repository references use Bzlmod naming: @python_3_<minor>
python_versions = [
    {
        "name": "python39",
        "python_version": "3.9",
        "python_headers": "@python_3_9//:python_headers",
        "libpython": "@python_3_9//:libpython",
        "suffix": "39",
    },
    {
        "name": "python310",
        "python_version": "3.10",
        "python_headers": "@python_3_10//:python_headers",
        "libpython": "@python_3_10//:libpython",
        "suffix": "310",
    },
    {
        "name": "python311",
        "python_version": "3.11",
        "python_headers": "@python_3_11//:python_headers",
        "libpython": "@python_3_11//:libpython",
        "suffix": "311",
    },
    {
        "name": "python312",
        "python_version": "3.12",
        "python_headers": "@python_3_12//:python_headers",
        "libpython": "@python_3_12//:libpython",
        "suffix": "312",
    },
    {
        "name": "python313",
        "python_version": "3.13",
        "python_headers": "@python_3_13//:python_headers",
        "libpython": "@python_3_13//:libpython",
        "suffix": "313",
    },
]

def register_all_toolchains():
    """No-op: Python toolchains are now registered via MODULE.bazel."""
    pass
