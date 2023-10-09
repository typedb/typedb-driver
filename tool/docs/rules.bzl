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

def html_docs_parser(name, data, language, feature=""):
    script_name = language.title() + "DocsParser"

    kt_jvm_binary(
        name = name,
        srcs = [
            "//tool/docs:" + language + "/" + script_name + ".kt",
            "//tool/docs:common/AsciiDocBuilder.kt",
            "//tool/docs:common/AsciiDocTableBuilder.kt",
            "//tool/docs:common/Class.kt",
            "//tool/docs:common/EnumConstant.kt",
            "//tool/docs:common/Method.kt",
            "//tool/docs:common/Util.kt",
            "//tool/docs:common/Variable.kt",
        ],
        main_class = "com.vaticle.typedb.client.tool.doc." + language + "." + script_name + "Kt",
        args = [
            "$(location %s)" % data,
            "%s/docs" % language,
            feature,
        ],
        deps = [
            "@maven//:org_jsoup_jsoup",
        ],
        data = [data],
        visibility = ["//visibility:public"],
    )
