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

artifacts = [
  "ch.qos.logback:logback-classic",
  "ch.qos.logback:logback-core",
  "com.eclipsesource.minimal-json:minimal-json",
  "com.google.code.findbugs:annotations",
  "com.google.code.findbugs:jsr305",
  "commons-io:commons-io",
  "io.cucumber:cucumber-java",
  "io.cucumber:cucumber-junit",
  "io.grpc:grpc-core",
  "io.grpc:grpc-netty",
  "io.grpc:grpc-protobuf",
  "io.grpc:grpc-stub",
  "io.grpc:grpc-testing",
  "io.grpc:grpc-api",
  "io.netty:netty-all",
  "io.netty:netty-codec-http2",
  "io.netty:netty-handler",
  "io.netty:netty-handler-proxy",
  "io.netty:netty-buffer",
  "io.netty:netty-codec",
  "io.netty:netty-codec-http",
  "io.netty:netty-codec-socks",
  "io.netty:netty-common",
  "io.netty:netty-transport",
  "io.netty:netty-resolver",
  "javax.annotation:javax.annotation-api",
  "junit:junit",
  "org.hamcrest:hamcrest-all",
  "org.hamcrest:hamcrest-core",
  "org.hamcrest:hamcrest-library",
  "org.mockito:mockito-core",
  "org.slf4j:jcl-over-slf4j",
  "org.slf4j:slf4j-api",
  "org.slf4j:log4j-over-slf4j",
  "org.slf4j:slf4j-simple",
  "org.zeroturnaround:zt-exec"
]

# Override libraries conflicting with versions defined in @vaticle_dependencies
overrides = {
}
