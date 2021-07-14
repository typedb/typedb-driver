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

import { Stream } from "../../../common/util/Stream";
import { TypeDBTransaction } from "../../connection/TypeDBTransaction";
import { AttributeType } from "../type/AttributeType";
import { EntityType } from "../type/EntityType";
import { RelationType } from "../type/RelationType";
import { RoleType } from "../type/RoleType";
import { ThingType } from "../type/ThingType";
import { Type } from "../type/Type";
import { Attribute } from "./Attribute";
import { Entity } from "./Entity";
import { Thing } from "./Thing";

export interface Relation extends Thing {

    asRemote(transaction: TypeDBTransaction): Relation.Remote;

    getType(): RelationType;
}

export namespace Relation {

    export interface Remote extends Relation, Thing.Remote {

        asRemote(transaction: TypeDBTransaction): Relation.Remote;

        getType(): RelationType;

        asType(): Type.Remote;

        asThingType(): ThingType.Remote;

        asEntityType(): EntityType.Remote;

        asAttributeType(): AttributeType.Remote;

        asRelationType(): RelationType.Remote;

        asRoleType(): RoleType.Remote;

        asThing(): Thing.Remote;

        asEntity(): Entity.Remote;

        asAttribute(): Attribute.Remote;

        asRelation(): Relation.Remote;

        addPlayer(roleType: RoleType, player: Thing): Promise<void>;

        removePlayer(roleType: RoleType, player: Thing): Promise<void>;

        getPlayers(roleTypes?: RoleType[]): Stream<Thing>;

        getPlayersByRoleType(): Promise<Map<RoleType, Thing[]>>;

        getRelating(): Stream<RoleType>;
    }
}

