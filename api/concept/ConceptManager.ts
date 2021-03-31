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


import {Thing} from "./thing/Thing";
import {ThingType} from "./type/ThingType";
import {EntityType} from "./type/EntityType";
import {RelationType} from "./type/RelationType";
import {AttributeType} from "./type/AttributeType";

export interface ConceptManager {

    getRootThingType(): Promise<ThingType>;

    getRootEntityType(): Promise<EntityType>;

    getRootRelationType(): Promise<RelationType>;

    getRootAttributeType(): Promise<AttributeType>;

    getThingType(label: string): Promise<ThingType>;

    getThing(iid: string): Promise<Thing>;

    getEntityType(label: string): Promise<EntityType>;

    putEntityType(label: string): Promise<EntityType>;

    getRelationType(label: string): Promise<RelationType>;

    putRelationType(label: string): Promise<RelationType>;

    getAttributeType(label: string): Promise<AttributeType>;

    putAttributeType(label: string, valueType: AttributeType.ValueType): Promise<AttributeType>;

}
