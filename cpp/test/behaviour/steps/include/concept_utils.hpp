/*
 * Copyright (C) 2022 Vaticle
 *
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

#include "common.hpp"
namespace TypeDB::BDD {

template <typename T>
std::string label(const std::unique_ptr<T>& concept) {
    if constexpr (std::is_same_v<T, RoleType>) return concept->getScope() + ":" + concept->getName();
    else return concept->getLabel();
}

std::unique_ptr<AttributeType> attrType(Context& context, const std::string& label);
std::unique_ptr<EntityType> entityType(Context& context, const std::string& label);
std::unique_ptr<RelationType> relationType(Context& context, const std::string& label);
std::unique_ptr<RoleType> roleType(Context& context, const std::string& scopedLabel);

}  // namespace TypeDB::BDD
