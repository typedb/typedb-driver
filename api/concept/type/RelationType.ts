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

import { Stream } from "../../../common/util/Stream";
import { TypeDBTransaction } from "../../connection/TypeDBTransaction";
import { Attribute } from "../thing/Attribute";
import { Entity } from "../thing/Entity";
import { Relation } from "../thing/Relation";
import { Thing } from "../thing/Thing";
import { AttributeType } from "./AttributeType";
import { EntityType } from "./EntityType";
import { RoleType } from "./RoleType";
import { ThingType } from "./ThingType";
import { Type } from "./Type";

export interface RelationType extends ThingType {

    asRemote(transaction: TypeDBTransaction): RelationType.Remote;
}

export namespace RelationType {

    export interface Remote extends RelationType, ThingType.Remote {

        asRemote(transaction: TypeDBTransaction): RelationType.Remote;

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

        create(): Promise<Relation>;

        getSubtypes(): Stream<RelationType>;

        setSupertype(relationType: RelationType): Promise<void>;

        getInstances(): Stream<Relation>;

        getRelates(): Stream<RoleType>;

        getRelates(roleLabel: string): Promise<RoleType>;

        getRelates(roleLabel?: string): Promise<RoleType> | Stream<RoleType>;

        getRelatesExplicit(): Stream<RoleType>;

        getRelatesOverridden(roleLabel: string): Promise<RoleType>;

        setRelates(roleLabel: string, overriddenLabel?: string): Promise<void>;

        unsetRelates(roleLabel: string): Promise<void>;
    }
}
