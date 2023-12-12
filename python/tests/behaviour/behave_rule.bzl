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

load("@vaticle_typedb_driver_pip//:requirements.bzl", "requirement")


def py_behave_test(*, name, background, native_typedb_artifact, steps, feats, deps, data=[], typedb_port, **kwargs):
    feats_dir = "features-" + name
    steps_out_dir = feats_dir + "/steps"

    native.genrule(
        name = name + "_features",
        cmd = "mkdir $(@D)/" + feats_dir
                + " && cp $(location %s) $(@D)/%s" % (feats[0], feats_dir)
                + " && cp $(location %s) $(@D)/%s" % (background, feats_dir)
                + " && mkdir $(@D)/" + steps_out_dir + " && "
                + " && ".join(["cp $(location %s) $(@D)/%s" % (step_file, steps_out_dir) for step_file in steps]),
        srcs = steps + [background] + feats,
        outs = [feats_dir],
    ) # create directory structure as above

    native.py_test( # run behave with the above as data
        name = name,
        data = data + [name + "_features"],
        deps = deps + [requirement("behave"), requirement("PyHamcrest")],
        srcs = ["//python/tests/behaviour:entry_point_behave.py"],
        args = ["$(location :" + name + "_features)", "--no-capture", "-D", "port=" + typedb_port],
        main = "//python/tests/behaviour:entry_point_behave.py",
    )


def typedb_behaviour_py_test_core(name, **kwargs):
    py_behave_test(
        name = name + "-core",
        background = "@//python/tests/behaviour/background:core",
        native_typedb_artifact = "@//tool/test:native-typedb-artifact",
        toolchains = ["@rules_python//python:current_py_toolchain"],
        typedb_port = "1729",
        **kwargs,
    )

def typedb_behaviour_py_test_cloud(name, **kwargs):
    py_behave_test(
        name = name + "-cloud",
        background = "@//python/tests/behaviour/background:cloud",
        native_typedb_artifact = "@//tool/test:native-typedb-cloud-artifact",
        toolchains = ["@rules_python//python:current_py_toolchain"],
        typedb_port = "11729",
        **kwargs,
    )

def typedb_behaviour_py_test(name, **kwargs):
    typedb_behaviour_py_test_core(name, **kwargs)
    typedb_behaviour_py_test_cloud(name, **kwargs)
