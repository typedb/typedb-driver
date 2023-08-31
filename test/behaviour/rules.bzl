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

load("@npm//:@cucumber/cucumber/package_json.bzl", cucumber_bin = "bin")

def behaviour_test_ts_config():
    return {
        "compilerOptions": {
            "target": "es2019",
            "module": "commonjs",
            "moduleResolution": "node",
            "esModuleInterop": True,
            "skipLibCheck": True,
            "forceConsistentCasingInFileNames": True,
        }
    }

def node_cucumber_test(name, features, data, steps, **kwargs):
    cucumber_bin.cucumber_js_test(
        name = name,
        data = [
            "//:node_modules/@cucumber/cucumber",
            "//:typedb-protocol-override",
            "@//test/behaviour/config:parameters",
        ] + data + features + [steps],
        no_copy_to_bin = features,
        fixed_args = [
            "--publish-quiet", "--strict",
            "--tags 'not @ignore and not @ignore-typedb and not @ignore-typedb-client-nodejs and not @ignore-client-nodejs'",
            "--require", "test/**/*.js",
        ] + ["$(location {})".format(feature) for feature in features],
        **kwargs,
    )

def typedb_behaviour_node_test(name, **kwargs):
    node_cucumber_test(
        name = name + "-core",
        steps = "@//test/behaviour/connection:steps-core",
        **kwargs,
    )

    node_cucumber_test(
        name = name + "-cluster",
        steps = "@//test/behaviour/connection:steps-cluster",
        **kwargs,
    )
