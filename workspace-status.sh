#!/bin/bash
tag=$(git describe --exact-match --tags $(git log -n1 --pretty='%h') 2>/dev/null)
if [ ! -z "$tag" ]; then
    echo STABLE_VERSION $tag
else
    echo STABLE_VERSION 0.0.0-$(git rev-parse HEAD)
fi

workspace=$(realpath $(readlink bazel-typedb-driver)/../..)
workspace_refs=$workspace/external/vaticle_typedb_driver_workspace_refs/refs.json
typedb_protocol_version=$(grep -o '"vaticle_typedb_protocol":"[^"]*"' $workspace_refs | sed 's/.*:"\(.*\)"/\1/')

echo STABLE_PROTOCOL_VERSION $typedb_protocol_version

# TODO
#echo STABLE_WORKSPACE_REFS $(cat $workspace_refs)
