/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

use config::is_cluster;
use serial_test::serial;
use steps::Context;

#[tokio::test]
#[serial]
async fn test() {
    // Bazel specific path: when running the test in bazel, the external data from
    // @typedb_behaviour is stored in a directory that is a sibling to
    // the working directory.
    #[cfg(feature = "bazel")]
    let path = "../typedb_behaviour/driver/connection.feature";

    #[cfg(not(feature = "bazel"))]
    let path = "../../typedb-behaviour/driver/connection.feature";

    assert!(Context::test(path, is_cluster()).await);
}
