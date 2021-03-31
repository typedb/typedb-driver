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


import {GraknTransaction} from "../../GraknTransaction";
import {RemoteThingType, ThingType} from "./ThingType";
import {Stream} from "../../../common/util/Stream";
import {Entity} from "../thing/Entity";

export interface EntityType extends ThingType {

    asRemote(transaction: GraknTransaction): RemoteEntityType;

}

export interface RemoteEntityType extends ThingType, RemoteThingType {

    asRemote(transaction: GraknTransaction): RemoteEntityType;

    create(): Promise<Entity>;

    getInstances(): Stream<Entity>;

    getSubtypes(): Stream<EntityType>;

    setSupertype(superEntityType: EntityType): Promise<void>;

}
