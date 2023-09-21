#
# Copyright (C) 2022 Vaticle
#
# This unpublished material is proprietary to Vaticle.
# All rights reserved. The methods and
# techniques described herein are considered trade secrets
# and/or confidential. Reproduction or distribution, in whole
# or in part, is forbidden except by express written permission
# of Vaticle.
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
        "//test:__subpackages__",
        "//common/test/encryption:__subpackages__"
    ],
)

checkstyle_test(
    name = "checkstyle",
    include = glob(["*"]),
    exclude = glob(["encryption/*"]),
    license_type = "commercial-header",
)
