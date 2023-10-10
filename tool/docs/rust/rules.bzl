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

load("@rules_rust//rust:defs.bzl", "rust_doc")
load("//tool/docs:rules.bzl", "html_docs_parser")

def rust_docs(name, feature):
    # rust_doc from rules_rust < 0.20 produces a zip-archive
    rust_doc(
        name = "docs_html_zip_" + feature,
        crate = ":typedb_driver",
        # TODO: Replace with rustdoc_flags after upgrading rules_rust to >=0.20
        rustc_flags = ["--cfg", "feature=\"" + feature + "\""],
    )

    native.genrule(
        name = "docs_html_" + feature,
        srcs = [":docs_html_zip_" + feature],
        outs = ["docs_html_rustdoc_" + feature],
        cmd = "mkdir $@ && unzip $< -d $@",
    )
