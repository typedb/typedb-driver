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

#include <chrono>
#include <cstdlib>
#include <thread>

#include "common.hpp"
#include "steps.hpp"

namespace TypeDB::BDD {

cucumber_bdd::StepCollection<Context> commonSteps = {
    BDD_STEP("wait (\\d+) seconds", {
        std::this_thread::sleep_for(std::chrono::seconds(atoi(matches[1].str().c_str())));
    }),
    BDD_NOOP("set time-zone is: (.*)"), // TODO: Decide if we want to implement (can be changed only for POSIX).
    BDD_NOOP("typedb has configuration"),
};

}
