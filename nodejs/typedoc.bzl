load("@rules_nodejs//:providers.bzl", "run_node")

def _typedoc_impl(ctx):
    args = ctx.actions.args()

    args.add("--logLevel", "Warn")
    args.add("--name", ctx.attr.name)
    args.add("--entryPoints", " ".join([f.short_path for f in ctx.files.entry_points]))

    if ctx.attr.json:
        args.add("--json", ctx.outputs.output.path)

    if ctx.file.tsconfig:
        args.add("--tsconfig", ctx.file.tsconfig)

    run_node(
        ctx = ctx,
        inputs = ctx.files.entry_points,
        outputs = [ctx.outputs.output],
        arguments = [args],
        tools = depset(ctx.files.data),
        executable = "typedoc",
    )

typedoc = rule(
    implementation = _typedoc_impl,
    attrs = {
        "entry_points": attr.label_list(
            mandatory = True,
            allow_files = True,
        ),
        "json": attr.bool(
            default = False,
        ),
        "data": attr.label_list(
            mandatory = False,
            default = [],
            allow_files = True,
        ),
        "tsconfig": attr.label(
            allow_single_file = True,
        ),
        "output": attr.output(mandatory = True),
        "typedoc": attr.label(
            default = Label("@npm//typedoc/bin:typedoc"),
            executable = True,
            cfg = "exec",
        ),
    },
)
