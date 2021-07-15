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


import { TypeDBTransaction } from "../connection/TypeDBTransaction";
import { Attribute } from "./thing/Attribute";
import { Entity } from "./thing/Entity";
import { Relation } from "./thing/Relation";
import { Thing } from "./thing/Thing";
import { AttributeType } from "./type/AttributeType";
import { EntityType } from "./type/EntityType";
import { RelationType } from "./type/RelationType";
import { RoleType } from "./type/RoleType";
import { ThingType } from "./type/ThingType";
import { Type } from "./type/Type";

export interface Concept {

    asRemote(transaction: TypeDBTransaction): Concept.Remote;

    isRemote(): boolean;

    isType(): boolean;

    isRoleType(): boolean;

    isThingType(): boolean;

    isEntityType(): boolean;

    isAttributeType(): boolean;

    isRelationType(): boolean;

    isThing(): boolean;

    isEntity(): boolean;

    isAttribute(): boolean;

    isRelation(): boolean;

    asType(): Type;

    asThingType(): ThingType;

    asEntityType(): EntityType;

    asAttributeType(): AttributeType;

    asRelationType(): RelationType;

    asRoleType(): RoleType;

    asThing(): Thing;

    asEntity(): Entity;

    asAttribute(): Attribute;

    asRelation(): Relation;

    equals(concept: Concept): boolean;
}

export namespace Concept {

    export interface Remote extends Concept {

        delete(): Promise<void>;

        isDeleted(): Promise<boolean>;

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
    }
}
