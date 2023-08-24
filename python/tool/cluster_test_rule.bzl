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

def _rule_implementation(ctx):
    """
    Implementation of the rule typedb_cluster_py_test.
    """

    # Store the path of the test source file. It is recommended to only have one source file.
    test_src = ctx.files.srcs[0].path
#
#    # behave requires a 'steps' folder to exist in the test root directory.
#    steps_out_dir = ctx.files.feats[0].dirname + "/steps"

    typedb_cluster_distro = str(ctx.files.native_typedb_cluster_artifact[0].short_path)

    # TODO: This code is, mostly, copied from our TypeDB behave test
    cmd = "set -xe && TYPEDB_ARCHIVE=%s" % typedb_cluster_distro
    cmd += """
            function server_start() {
              ./${1}/typedb cluster \
                --storage.data=server/data \
                --server.address=localhost:${1}1729 \
                --server.internal-address.zeromq=localhost:${1}1730 \
                --server.internal-address.grpc=localhost:${1}1731 \
                --server.peers.peer-1.address=localhost:11729 \
                --server.peers.peer-1.internal-address.zeromq=localhost:11730 \
                --server.peers.peer-1.internal-address.grpc=localhost:11731 \
                --server.peers.peer-2.address=localhost:21729 \
                --server.peers.peer-2.internal-address.zeromq=localhost:21730 \
                --server.peers.peer-2.internal-address.grpc=localhost:21731 \
                --server.peers.peer-3.address=localhost:31729 \
                --server.peers.peer-3.internal-address.zeromq=localhost:31730 \
                --server.peers.peer-3.internal-address.grpc=localhost:31731 \
                --server.encryption.enable=true
            }
            if test -d typedb_distribution; then
             echo Existing distribution detected. Cleaning.
             rm -rf typedb_distribution
            fi
            mkdir typedb_distribution
            echo Attempting to unarchive TypeDB distribution from $TYPEDB_ARCHIVE
            if [[ ${TYPEDB_ARCHIVE: -7} == ".tar.gz" ]]; then
             tar -xf $TYPEDB_ARCHIVE -C ./typedb_distribution
            else
             if [[ ${TYPEDB_ARCHIVE: -4} == ".zip" ]]; then
               unzip -q $TYPEDB_ARCHIVE -d ./typedb_distribution
             else
               echo Supplied artifact file was not in a recognised format. Only .tar.gz and .zip artifacts are acceptable.
               exit 1
             fi
            fi
            TYPEDB=$(ls ./typedb_distribution)
            echo Successfully unarchived TypeDB distribution. Creating 3 copies.
            cp -r typedb_distribution/$TYPEDB/ 1 && cp -r typedb_distribution/$TYPEDB/ 2 && cp -r typedb_distribution/$TYPEDB/ 3
            echo Starting 3 TypeDB servers.
            server_start 1 &
            server_start 2 &
            server_start 3 &

            ROOT_CA=`realpath typedb_distribution/$TYPEDB/server/conf/encryption/ext-root-ca.pem`
            export ROOT_CA

            POLL_INTERVAL_SECS=0.5
            MAX_RETRIES=60
            RETRY_NUM=0
            while [[ $RETRY_NUM -lt $MAX_RETRIES ]]; do
             RETRY_NUM=$(($RETRY_NUM + 1))
             if [[ $(($RETRY_NUM % 4)) -eq 0 ]]; then
               echo Waiting for TypeDB Cluster servers to start \\($(($RETRY_NUM / 2))s\\)...
             fi
             lsof -i :11729 && STARTED1=1 || STARTED1=0
             lsof -i :21729 && STARTED2=1 || STARTED2=0
             lsof -i :31729 && STARTED3=1 || STARTED3=0
             if [[ $STARTED1 -eq 1 && $STARTED2 -eq 1 && $STARTED3 -eq 1 ]]; then
               break
             fi
             sleep $POLL_INTERVAL_SECS
            done
            if [[ $STARTED1 -eq 0 || $STARTED2 -eq 0 || $STARTED3 -eq 0 ]]; then
             echo Failed to start one or more TypeDB Cluster servers
             exit 1
            fi
            echo 3 TypeDB Cluster database servers started

           """

    cmd += "cd python && python3 -m unittest %s && export RESULT=0 || export RESULT=1" % test_src.split("/", 1)[1]
    cmd += """
            echo Tests concluded with exit value $RESULT
            echo Stopping servers.
            procs=$(jps | awk '/TypeDBNode/ {print $1}' | paste -sd " " -)
            if [ -n "$procs" ]; then
             kill $procs
            fi
            exit $RESULT
           """

    # We want a test target so make it create an executable output.
    # https://bazel.build/versions/master/docs/skylark/rules.html#test-rules
    ctx.actions.write(
        # Access the executable output file using ctx.outputs.executable.
        output=ctx.outputs.executable,
        content=cmd,
        is_executable=True
    )

    # The executable output is added automatically to this target.

    # Add the feature and step files for behave to the runfiles.
    # https://bazel.build/versions/master/docs/skylark/rules.html#runfiles
    return [DefaultInfo(
        # The shell executable - the output of this rule - can use these files at runtime.
        runfiles = ctx.runfiles(files = ctx.files.srcs + ctx.files.deps + ctx.files.native_typedb_cluster_artifact)
    )]

"""
Documentation

Args:
  name:
    A unique name for this rule.
  feats:
    Feature files used to run this target.
  steps:
    Files containing the mapping of feature steps to actual system API calls.
  background:
    The environment.py file containing test setup and tear-down methods.
  deps:
    System to test.
"""
typedb_cluster_py_test = rule(
    implementation=_rule_implementation,
    attrs={
        "srcs": attr.label_list(mandatory=True,allow_empty=False,allow_files=True),
        "deps": attr.label_list(mandatory=True,allow_empty=False),
        "native_typedb_cluster_artifact": attr.label(mandatory=True)
    },
    test=True,
)
