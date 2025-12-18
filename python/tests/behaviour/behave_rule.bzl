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

load("@typedb_driver_pip//:requirements.bzl", "requirement")


def py_behave_test(*, name, background, native_typedb_artifact, steps, feats, deps, data=[], typedb_port, **kwargs):
    prepare_py_behave_directory(
        name = name + "_features",
        background = background,
        feats = feats,
        steps = steps,
    )
    native.py_test( # run behave with the above as data
        name = name,
        data = data + [name + "_features"],
        deps = deps + [requirement("behave"), requirement("PyHamcrest")],
        srcs = ["//python/tests/behaviour:entry_point_behave.py"],
        args = ["$(location :" + name + "_features)", "--no-capture", "-D", "port=" + typedb_port],
        main = "//python/tests/behaviour:entry_point_behave.py",
    )

def typedb_behaviour_py_test_community(name, **kwargs):
    py_behave_test(
        name = name + "-community",
        background = "@//python/tests/behaviour/background:community",
        native_typedb_artifact = "@//tool/test:native-typedb-artifact",
        toolchains = ["@rules_python//python:current_py_toolchain"],
        typedb_port = "1729",
        **kwargs,
    )

def typedb_behaviour_py_test_cluster(name, **kwargs):
    py_behave_test(
        name = name + "-cluster",
        background = "@//python/tests/behaviour/background:cluster",
        native_typedb_artifact = "@//tool/test:native-typedb-artifact", # TODO: Change to cloud artifact when available
        toolchains = ["@rules_python//python:current_py_toolchain"],
        typedb_port = "1729", # TODO: Might want to change back to 11729 when cloud has multiple nodes
        **kwargs,
    )

def typedb_behaviour_py_test(name, **kwargs):
    typedb_behaviour_py_test_community(name, **kwargs)
    typedb_behaviour_py_test_cluster(name, **kwargs)

def _prepare_py_behave_directory_impl(ctx):
    feats_dir = ctx.actions.declare_directory(ctx.attr.name)

    all_inputs = [ctx.file.background] + ctx.files.feats + ctx.files.steps
    all_output_paths_relative_to_feats = (
        [ctx.file.background.basename] +
        [f.basename for f in ctx.files.feats] +
        ["steps/{}".format(f.basename) for f in ctx.files.steps]
    )
    commands = " && ".join(["mkdir -p {}/steps".format(feats_dir.path)] + [
        "cp {} {}/{}".format(s.path, feats_dir.path, d) for (s,d) in zip(all_inputs, all_output_paths_relative_to_feats)
    ])

    ctx.actions.run_shell(
        inputs = all_inputs,
        outputs = [feats_dir],
        command = commands,
    )

    return [
        DefaultInfo(files = depset([feats_dir]))
    ]

prepare_py_behave_directory = rule(
    implementation = _prepare_py_behave_directory_impl,
    attrs = {
        "background": attr.label(allow_single_file = True, mandatory = True),
        "feats": attr.label_list(mandatory = True, allow_files=True),
        "steps": attr.label_list(mandatory = True),
    },
)
