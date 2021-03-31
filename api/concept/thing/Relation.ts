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
import {RelationType} from "../type/RelationType";
import {RoleType} from "../type/RoleType";
import {RemoteThing, Thing} from "./Thing";
import {Stream} from "../../../common/util/Stream";

export interface Relation extends Thing {

    asRemote(transaction: GraknTransaction): RemoteRelation;

    getType(): RelationType;

}

export interface RemoteRelation extends Relation, RemoteThing {

    asRemote(transaction: GraknTransaction): RemoteRelation;

    getType(): RelationType;

    addPlayer(roleType: RoleType, player: Thing): Promise<void>;

    removePlayer(roleType: RoleType, player: Thing): Promise<void>;

    getPlayers(roleTypes?: RoleType[]): Stream<Thing>;

    getPlayersByRoleType(): Promise<Map<RoleType, Thing[]>>;

}
