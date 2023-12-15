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

#include "typedb/common/error_message.hpp"
#include "typedb/common/exception.hpp"
#include "typedb/common/future.hpp"
#include "typedb/common/iterator.hpp"
#include "typedb/common/native.hpp"

#include "typedb/connection/database_manager.hpp"
#include "typedb/connection/driver.hpp"
#include "typedb/connection/options.hpp"
#include "typedb/connection/session.hpp"
#include "typedb/connection/transaction.hpp"

#include "typedb/user/user.hpp"
#include "typedb/user/user_manager.hpp"

#include "typedb/query/query_manager.hpp"

#include "typedb/logic/explainable.hpp"
#include "typedb/logic/explanation.hpp"
#include "typedb/logic/logic_manager.hpp"
#include "typedb/logic/rule.hpp"

#include "typedb/answer/concept_map.hpp"
#include "typedb/answer/concept_map_group.hpp"
#include "typedb/answer/json.hpp"
#include "typedb/answer/value_future.hpp"
#include "typedb/answer/value_group.hpp"

#include "typedb/concept/concept.hpp"
#include "typedb/concept/concept_manager.hpp"
#include "typedb/concept/thing/attribute.hpp"
#include "typedb/concept/thing/entity.hpp"
#include "typedb/concept/thing/relation.hpp"
#include "typedb/concept/thing/thing.hpp"
#include "typedb/concept/type/attribute_type.hpp"
#include "typedb/concept/type/entity_type.hpp"
#include "typedb/concept/type/relation_type.hpp"
#include "typedb/concept/type/role_type.hpp"
#include "typedb/concept/type/thing_type.hpp"
#include "typedb/concept/type/type.hpp"
#include "typedb/concept/value/value.hpp"
