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

load("//dependencies/vaticle:repositories.bzl", "vaticle_dependencies")
load("@rules_python//python:repositories.bzl", "python_register_toolchains")

python_versions = [
    # Order matters! First python toolchain that is registered is used by Bazel's py_binary. Sphinx is incompatible with py3.8.
    {
        "name": "python39",
        "python_version": "3.9",
        "python_headers": "@python39//:python_headers",
        "libpython": "@python39//:libpython",
        "suffix": "39",
    },
    {
        "name": "python38",
        "python_version": "3.8",
        "python_headers": "@python38//:python_headers",
        "libpython": "@python38//:libpython",
        "suffix": "38",
    },
    {
        "name": "python310",
        "python_version": "3.10",
        "python_headers": "@python310//:python_headers",
        "libpython": "@python310//:libpython",
        "suffix": "310",
    },
    {
        "name": "python311",
        "python_version": "3.11",
        "python_headers": "@python311//:python_headers",
        "libpython": "@python311//:libpython",
        "suffix": "311",
    },
]

def register_all_toolchains():
    for version in python_versions:
        python_register_toolchains(name=version["name"], python_version=version["python_version"], ignore_root_user_error = True)
