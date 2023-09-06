load("//dependencies/vaticle:repositories.bzl", "vaticle_dependencies")
load("@rules_python//python:repositories.bzl", "python_register_toolchains")

python_versions = [
    {
        "name": "python39",
        "python_version": "3.9",
        "python_headers": "@python39//:python_headers",
        "libpython": "@python39//:libpython",
        "suffix": "39",
    },
    {
        "name": "python310",
        "python_version": "3.10",
        "python_headers": "@python310//:python_headers",
        "libpython": "@python310//:libpython",
        "suffix": "310",
    },
    {
        "name": "python311",
        "python_version": "3.11",
        "python_headers": "@python311//:python_headers",
        "libpython": "@python311//:libpython",
        "suffix": "311",
    },
]

def register_all_toolchains():
    for version in python_versions:
        python_register_toolchains(name=version["name"], python_version=version["python_version"])
