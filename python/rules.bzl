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

load("@vaticle_bazel_distribution//pip:rules.bzl", "assemble_pip", "deploy_pip")

load("@vaticle_dependencies//builder/swig:python.bzl", "swig_python", "py_native_lib_rename")
load("@vaticle_dependencies//distribution:deployment.bzl", "deployment")


def native_driver_versioned(python_versions):
    for version in python_versions:
        swig_python(
            name = "native_client_python_wrapper" + version["suffix"],
            shared_lib_name = "native_client_python" + version["suffix"],
            import_name = "native_client_python",
            lib = "//c:typedb_client_clib_headers",
            interface = "//c:typedb_client.i",
            includes = ["//c:swig/typedb_client_python.swg"],
            enable_cxx = True,
            python_headers = version["python_headers"],
            libpython = version["libpython"],
            visibility = ["//visibility:public"]
        )

        native.genrule(
            name = "native-client-wrapper" + version["suffix"],
            outs = ["typedb/native_client_wrapper" + version["suffix"] + ".py"],
            srcs = [":native_client_python_wrapper" + version["suffix"]],
            cmd = "cp $< $@",
            visibility = ["//visibility:public"]
        )

        py_native_lib_rename(
            name = "native-client-binary" + version["suffix"],
            out = "typedb/native_client_python" + version["suffix"],
            src = ":native_client_python" + version["suffix"],
            visibility = ["//visibility:public"]
        )

        native.py_library(
            name = "client_python" + version["suffix"],
            srcs = native.glob(["typedb/**/*.py"]) + [":native-client-wrapper" + version["suffix"]],
            data = [":native-client-binary" + version["suffix"]],
            deps = [":native_client_python_wrapper" + version["suffix"]],
            imports = ["."],
            visibility = ["//visibility:public"]
        )

        assemble_pip(
            name = "assemble-pip" + version["suffix"],
            target = ":client_python" + version["suffix"],
            package_name = "typedb-client",
            suffix = version["suffix"],
            classifiers = [
                "Programming Language :: Python",
                "Programming Language :: Python :: " + version["python_version"],
                "License :: OSI Approved :: Apache Software License",
                "Operating System :: OS Independent",
                "Intended Audience :: Developers",
                "Intended Audience :: Science/Research",
                "Environment :: Console",
                "Topic :: Database :: Front-Ends"
            ],
            url = "https://github.com/vaticle/typedb-driver-python/",
            author = "Vaticle",
            author_email = "community@vaticle.com",
            license = "Apache-2.0",
            requirements_file = "//python:requirements.txt",
            keywords = ["typedb", "database", "graph", "knowledgebase", "knowledge-engineering"],
            description = "TypeDB Client for Python",
            long_description_file = "//python:README.md",
        )

        deploy_pip(
            name = "deploy-pip" + version["suffix"],
            target = ":assemble-pip" + version["suffix"],
            snapshot = deployment["pypi.snapshot"],
            release = deployment["pypi.release"],
            suffix = version["suffix"],
            distribution_tag = select({
                "@vaticle_dependencies//util/platform:is_mac_aarch64": "py" + version["suffix"] + "-none-macosx_11_0_arm64",
                "@vaticle_dependencies//util/platform:is_mac_x86_64": "py" + version["suffix"] + "-none-macosx_11_0_x86_64",
                "@vaticle_dependencies//util/platform:is_linux_aarch64": "py" + version["suffix"] + "-none-linux_aarch64",
                "@vaticle_dependencies//util/platform:is_linux_x86_64": "py" + version["suffix"] + "-none-linux_x86_64",
                "@vaticle_dependencies//util/platform:is_windows": "py" + version["suffix"] + "-none-win_amd64",
            }),
        )
