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
import { AttributeType } from "../type/AttributeType";
import { EntityType } from "../type/EntityType";
import { RelationType } from "../type/RelationType";
import { RoleType } from "../type/RoleType";
import { ThingType } from "../type/ThingType";
import { Type } from "../type/Type";
import { Entity } from "./Entity";
import { Relation } from "./Relation";
import { Thing } from "./Thing";

export interface Attribute extends Thing {

    asRemote(transaction: TypeDBTransaction): Attribute.Remote;

    readonly type: AttributeType;

    readonly value: boolean | string | number | Date;

    isBoolean(): boolean;

    isLong(): boolean;

    isDouble(): boolean;

    isString(): boolean;

    isDateTime(): boolean;

    asBoolean(): Attribute.Boolean;

    asLong(): Attribute.Long;

    asDouble(): Attribute.Double;

    asString(): Attribute.String;

    asDateTime(): Attribute.DateTime;
}

export namespace Attribute {

    export interface Remote extends Attribute, Thing.Remote {

        asRemote(transaction: TypeDBTransaction): Attribute.Remote;

        readonly type: AttributeType;

        getOwners(ownerType?: ThingType): Stream<Thing>;

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

        asBoolean(): Attribute.Boolean.Remote;

        asLong(): Attribute.Long.Remote;

        asDouble(): Attribute.Double.Remote;

        asString(): Attribute.String.Remote;

        asDateTime(): Attribute.DateTime.Remote;
    }

    export interface Boolean extends Attribute {

        asRemote(transaction: TypeDBTransaction): Attribute.Boolean.Remote;

        readonly type: AttributeType.Boolean;

        readonly value: boolean;
    }

    export namespace Boolean {

        export interface Remote extends Attribute.Remote, Attribute.Boolean {

            asRemote(transaction: TypeDBTransaction): Attribute.Boolean.Remote;

            readonly type: AttributeType.Boolean;

            readonly value: boolean;

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

            asBoolean(): Attribute.Boolean.Remote;

            asLong(): Attribute.Long.Remote;

            asDouble(): Attribute.Double.Remote;

            asString(): Attribute.String.Remote;

            asDateTime(): Attribute.DateTime.Remote;
        }
    }

    export interface Long extends Attribute {

        asRemote(transaction: TypeDBTransaction): Attribute.Long.Remote;

        readonly type: AttributeType.Long;

        readonly value: number;
    }

    export namespace Long {

        export interface Remote extends Attribute.Remote, Attribute.Long {

            asRemote(transaction: TypeDBTransaction): Attribute.Long.Remote;

            readonly type: AttributeType.Long;

            readonly value: number;

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

            asBoolean(): Attribute.Boolean.Remote;

            asLong(): Attribute.Long.Remote;

            asDouble(): Attribute.Double.Remote;

            asString(): Attribute.String.Remote;

            asDateTime(): Attribute.DateTime.Remote;
        }
    }

    export interface Double extends Attribute {

        asRemote(transaction: TypeDBTransaction): Attribute.Double.Remote;

        readonly type: AttributeType.Double;

        readonly value: number;
    }

    export namespace Double {

        export interface Remote extends Attribute.Remote, Attribute.Double {

            asRemote(transaction: TypeDBTransaction): Attribute.Double.Remote;

            readonly type: AttributeType.Double;

            readonly value: number;

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

            asBoolean(): Attribute.Boolean.Remote;

            asLong(): Attribute.Long.Remote;

            asDouble(): Attribute.Double.Remote;

            asString(): Attribute.String.Remote;

            asDateTime(): Attribute.DateTime.Remote;
        }
    }

    export interface String extends Attribute {

        asRemote(transaction: TypeDBTransaction): Attribute.String.Remote;

        readonly type: AttributeType.String;

        readonly value: string;
    }

    export namespace String {

        export interface Remote extends Attribute.Remote, Attribute.String {

            asRemote(transaction: TypeDBTransaction): Attribute.String.Remote;

            readonly type: AttributeType.String;

            readonly value: string;

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

            asBoolean(): Attribute.Boolean.Remote;

            asLong(): Attribute.Long.Remote;

            asDouble(): Attribute.Double.Remote;

            asString(): Attribute.String.Remote;

            asDateTime(): Attribute.DateTime.Remote;
        }
    }

    export interface DateTime extends Attribute {

        asRemote(transaction: TypeDBTransaction): Attribute.DateTime.Remote;

        readonly type: AttributeType.DateTime;

        readonly value: Date;
    }

    export namespace DateTime {

        export interface Remote extends Attribute.Remote, Attribute.DateTime {

            asRemote(transaction: TypeDBTransaction): Attribute.DateTime.Remote;

            readonly type: AttributeType.DateTime;

            readonly value: Date;

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

            asBoolean(): Attribute.Boolean.Remote;

            asLong(): Attribute.Long.Remote;

            asDouble(): Attribute.Double.Remote;

            asString(): Attribute.String.Remote;

            asDateTime(): Attribute.DateTime.Remote;
        }
    }
}
