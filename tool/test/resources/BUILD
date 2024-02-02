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

load("@vaticle_dependencies//tool/checkstyle:rules.bzl", "checkstyle_test")

filegroup(
    name = "certificates",
    srcs = [
        ":encryption/ext-grpc-root-ca.pem",
        ":encryption/ext-grpc-private-key.pem",
        ":encryption/ext-grpc-certificate.pem",
        ":encryption/int-grpc-root-ca.pem",
        ":encryption/int-grpc-private-key.pem",
        ":encryption/int-grpc-certificate.pem",
        ":encryption/int-zmq-private-key",
        ":encryption/int-zmq-public-key",
        ":encryption/keystore.pkcs12"
    ],
    visibility = [
        "//cpp/test:__subpackages__",
        "//csharp/test:__subpackages__",
        "//common/test/encryption:__subpackages__"
    ],
)

checkstyle_test(
    name = "checkstyle",
    include = glob(["*"]),
    exclude = glob(["encryption/*"]),
    license_type = "apache-header",
)
