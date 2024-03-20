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

load("@io_bazel_rules_kotlin//kotlin:jvm.bzl", "kt_jvm_binary")

def doxygen_csharp_to_adoc(name, data, docs_dirs, output_dir, args = [], **kwargs):
    extended_args = ["$(location %s)" % target for target in data] + [
        "--output",
        output_dir,
    ] + ["--dir=%s=%s" % (filename, docs_dirs[filename]) for filename in docs_dirs] + args
    kt_jvm_binary(
        name = name,
        srcs = [
            "//tool/docs:csharp/DoxygenParserCsharp.kt",
        ],
        main_class = "com.vaticle.typedb.driver.tool.docs.csharp.DoxygenParserCsharpKt",
        args = extended_args,
        deps = [
            "//tool/docs:html_docs_to_adoc_lib",
            "@maven//:org_jsoup_jsoup",
            "@maven//:info_picocli_picocli",
        ],
        data = data,
        visibility = ["//visibility:public"],
        **kwargs
    )
