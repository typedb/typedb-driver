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


def py_behave_test(*, name, background, steps, feats, deps, data=[], typedb_port, **kwargs):
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
        **kwargs,
    )

def typedb_behaviour_py_test_community(name, **kwargs):
    py_behave_test(
        name = name + "-community",
        background = "@//python/tests/behaviour/background:community",
        toolchains = ["@rules_python//python:current_py_toolchain"],
        typedb_port = "1729", # TODO: Not needed?
        **kwargs,
    )

def typedb_behaviour_py_test_cluster(name, **kwargs):
    py_behave_test(
        name = name + "-cluster",
        background = "@//python/tests/behaviour/background:cluster",
        toolchains = ["@rules_python//python:current_py_toolchain"],
        typedb_port = "11729", # TODO: Not needed?
        **kwargs,
    )

def typedb_behaviour_py_test(name, **kwargs):
    typedb_behaviour_py_test_community(name, **kwargs)
    typedb_behaviour_py_test_cluster(name, **kwargs)
