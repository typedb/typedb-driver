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

def _sphinx_doc_impl(ctx):
    driver_package = ctx.actions.declare_directory("driver-package")

    ctx.actions.run_shell(
        inputs = ctx.files.target,
        outputs = [driver_package],
        command = 'PACKAGE=$(find . -name "*.tar.gz") && tar -xf ${PACKAGE} && mv */typedb %s' % (driver_package.path),
    )

    args = ctx.actions.args()
    args.add('--output', ctx.outputs.out.path)
    args.add('--package', driver_package.path)
    args.add('--source_dir', ctx.files.srcs[0].dirname)

    ctx.actions.run(
        inputs = [ctx.executable.script, driver_package] + ctx.files.srcs,
        outputs = [ctx.outputs.out],
        arguments = [args],
        executable = ctx.executable.script,
        env = {"PYTHONPATH": driver_package.path},
    )

    return DefaultInfo(files = depset([ctx.outputs.out]))


_sphinx_doc = rule(
    attrs = {
        "script": attr.label(
            mandatory = True,
            executable = True,
            cfg = "exec",
            doc = "Python script for running sphinx",
        ),
        "target": attr.label(
            mandatory = True,
            allow_files = True,
            doc = "Package .tar.gz archive",
        ),
        "srcs": attr.label_list(
            mandatory = True,
            allow_files = True,
            doc = "Additional source files for sphinx",
        ),
        "out": attr.output(
            mandatory = True,
            doc = "Output directory",
        ),
    },
#    executable = True,
    implementation = _sphinx_doc_impl,
    doc = """
        Creates an HTML documentation for python module using Sphinx.
        """

)


def sphinx_doc(name, script, target, srcs, out):
    _sphinx_doc(
        name = name,
        script = script,
        target = target,
        srcs = srcs,
        out = out,
    )
