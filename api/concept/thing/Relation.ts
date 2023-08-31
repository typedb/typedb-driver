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

import {Stream} from "../../../common/util/Stream";
import {TypeDBTransaction} from "../../connection/TypeDBTransaction";
import {RelationType} from "../type/RelationType";
import {RoleType} from "../type/RoleType";
import {Thing} from "./Thing";
import {RequestBuilder} from "../../../common/rpc/RequestBuilder";

export interface Relation extends Thing {
    readonly type: RelationType;

    addRolePlayer(transaction: TypeDBTransaction, roleType: RoleType, player: Thing): Promise<void>;

    removeRolePlayer(transaction: TypeDBTransaction, roleType: RoleType, player: Thing): Promise<void>;

    getPlayersByRoleType(transaction: TypeDBTransaction): Stream<Thing>;
    getPlayersByRoleType(transaction: TypeDBTransaction, roleTypes: RoleType[]): Stream<Thing>;

    getRolePlayers(transaction: TypeDBTransaction): Promise<Map<RoleType, Thing[]>>;

    getRelating(transaction: TypeDBTransaction): Stream<RoleType>;
}

export namespace Relation {
    export function proto(relation: Relation) {
        return RequestBuilder.Thing.Relation.protoRelation(relation.iid);
    }
}
