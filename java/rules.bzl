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

load("@typedb_dependencies//builder/swig:java.bzl", "swig_java")

def swig_native_java_library(name, library_name_with_platform, platforms, maven_coordinates, tags=[], **kwargs):
    # generate identical libraries with different maven coordinate tags, since we can't 'select' tags
    for platform in platforms.values():
        platform_specific_name = library_name_with_platform.replace("{platform}", platform)
        swig_java(
            name = "__" + platform_specific_name,
            shared_lib_name = platform_specific_name,
            tags = tags,
            **kwargs,
        )

        native.java_library(
            name = platform_specific_name + "__native-as__do_not_reference",
            srcs = ["__" + platform_specific_name + "__swig"],
            resources = ["lib" + platform_specific_name],
            tags = tags + ["maven_coordinates=" + maven_coordinates.replace("{platform}", platform)],
        )

    native.alias(
        name = name,
        actual = select({
            config: library_name_with_platform.replace("{platform}", platform) + "__native-as__do_not_reference"
            for config, platform in platforms.items()
        })
    )
