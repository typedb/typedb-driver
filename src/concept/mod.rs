/*
 * Copyright (C) 2021 Vaticle
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

// enum Concept {
//     Type(Type),
//     Thing(Thing),
// }
//
// enum Type {
//     ThingType(ThingType),
//     RoleType(RoleType),
// }
//
// enum Thing {
//     Entity(Entity),
//     Relation(Relation),
//     Attribute(Attribute),
// }
//
// impl Thing {
//     fn get_iid(&self) {
//         match self {
//             other => other.0.iid
//         }
//     }
// }
//
// struct Entity {
//     iid: String
// }
//
// struct Relation {
//     iid: String
// }
//
// struct Attribute {
//     iid: String,
//     value: AttributeValue
// }
//
// enum AttributeValue {
//     Boolean(bool),
//     Long(i64),
//     Double(f64),
//     String(String),
//     DateTime(Instant)
// }
//
// struct ThingType {
//     label: String
// }
//
// struct RoleType {
//     label: ScopedLabel,
// }
//
// struct ScopedLabel {
//     scope: String,
//     name: String
// }

pub trait ConceptApi {
    fn is_deleted(&self) -> bool;
}
