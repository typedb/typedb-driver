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

#pragma once

#include <regex>

#include "cucumber/messages/pickle_step.hpp"

namespace cucumber_bdd {

template <typename CTX>
struct StepDefinition {
    const std::regex regex;
    void (*impl)(CTX& context, cucumber::messages::pickle_step& step, const std::smatch& matches);
};

template <typename CTX>
using StepCollection = const std::vector<StepDefinition<CTX>>;

template <typename CTX>
struct ResolvedStep {
    cucumber::messages::pickle_step step;
    const StepDefinition<CTX>* definition;
};

}  // namespace cucumber_bdd
