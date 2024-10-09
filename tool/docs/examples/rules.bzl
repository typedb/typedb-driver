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

load("@io_bazel_rules_kotlin//kotlin:jvm.bzl", "kt_jvm_binary")


def test_to_example(name, input, output, removed_lines, changed_words):
    args = [
        "$(location %s)" % input,
        "--output",
        "%s" % output,
    ] + [
        "--remove-lines=%s" % line for line in removed_lines
    ] + [
        "--change-words=%s=%s" % (from_word, changed_words[from_word]) for from_word in changed_words
    ]

    kt_jvm_binary(
        name = name,
        srcs = [
            "//tool/docs:examples/TestExamplesParser.kt",
        ],
        main_class = "com.typedb.driver.tool.docs.examples.TestExamplesParserKt",
        args = args,
        deps = [
            "@maven//:info_picocli_picocli",
        ],
        data = [input],
        visibility = ["//visibility:public"],
    )

def update_markdown_example(name, input, output, start_marker, end_marker, language_tag):
    args = [
        "$(location %s)" % input,
        "--output",
        "$(location %s)" % output,
        "--start-marker=%s" % start_marker,
        "--end-marker=%s" % end_marker,
        "--language=%s" % language_tag,
    ]

    kt_jvm_binary(
        name = name,
        srcs = [
            "//tool/docs:examples/MarkdownCodeUpdater.kt",
        ],
        main_class = "com.typedb.driver.tool.docs.examples.MarkdownCodeUpdaterKt",
        args = args,
        deps = [
            "@maven//:info_picocli_picocli",
        ],
        data = [input, output],
        visibility = ["//visibility:public"],
    )
