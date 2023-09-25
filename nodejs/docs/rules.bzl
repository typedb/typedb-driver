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

def _copy_to_bin(ctx, src, dst):
    ctx.actions.run_shell(
        inputs = [src],
        outputs = [dst],
        command = "cp -f '{}' '{}'".format(src.path, dst.path),
    )


def _typedoc_doc_impl(ctx):
    dsts = []
    for src in ctx.files.srcs:
        dst = ctx.actions.declare_file(src.short_path)
        _copy_to_bin(ctx, src, dst)
        dsts.append(dst)

    print(ctx.files.srcs)
    ctx.actions.run(
        inputs = [ctx.executable.script] + ctx.files.deps + dsts,
        outputs = [ctx.outputs.out],
        arguments = [ctx.outputs.out.basename],
        executable = ctx.executable.script,
        env = {"BAZEL_BINDIR": ctx.bin_dir.path},
    )

    return DefaultInfo(files = depset([ctx.outputs.out]))


_typedoc_doc = rule(
    attrs = {
        "script": attr.label(
            mandatory = True,
            executable = True,
            cfg = "exec",
            doc = "Python script for running typedoc",
        ),
        "deps": attr.label_list(
            mandatory = True,
            allow_files = True,
            doc = "Dependencies for typedoc",
        ),
        "srcs": attr.label_list(
            mandatory = True,
            allow_files = True,
            doc = "Client source files",
        ),
        "out": attr.output(
            mandatory = True,
            doc = "Output directory",
        ),
    },
    implementation = _typedoc_doc_impl,
    doc = """
        Creates an HTML documentation for python module using typedoc.
        """

)


def typedoc_doc(name, script, deps, srcs, out):
    _typedoc_doc(
        name = name,
        script = script,
        deps = deps,
        srcs = srcs,
        out = out,
    )
