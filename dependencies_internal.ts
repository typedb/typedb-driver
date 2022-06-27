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

/*
 * This file determines the order in which class modules are loaded at runtime.
 * Superclasses must always be loaded before their subclasses, otherwise an error will occur
 * whenever either class is instantiated at runtime.
 * NOTE: this does not apply to interfaces, which are erased at runtime.
 *
 * A good to way to find circular dependencies is to install `madge` and run it on the `dist` folder
 * containing the resulting `.js` files (can be forced with `npx tsc`).
 * Then reduce multi-hop circular dependencies one by one by pointing both circular dependents at each other via this file.
 */


/* concept.impl */
export * from "./concept/ConceptImpl";

/* concept.thing */
export * from "./concept/thing/ThingImpl";
export * from "./concept/thing/EntityImpl";
export * from "./concept/thing/RelationImpl";
export * from "./concept/thing/AttributeImpl";

/* concept.type */
export * from "./concept/type/TypeImpl";
export * from "./concept/type/ThingTypeImpl";
export * from "./concept/type/AttributeTypeImpl";
export * from "./concept/type/EntityTypeImpl";
export * from "./concept/type/RelationTypeImpl";
export * from "./concept/type/RoleTypeImpl";

/* cluster components */
export * from "./connection/cluster/FailsafeTask";
export * from "./connection/cluster/ClusterDatabase";
export * from "./connection/cluster/ClusterUser";
export * from "./connection/cluster/ClusterUserManager";
