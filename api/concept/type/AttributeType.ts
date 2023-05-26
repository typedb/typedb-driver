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
import {Attribute} from "../thing/Attribute";
import {Entity} from "../thing/Entity";
import {Relation} from "../thing/Relation";
import {Thing} from "../thing/Thing";
import {EntityType} from "./EntityType";
import {RelationType} from "./RelationType";
import {RoleType} from "./RoleType";
import {ThingType} from "./ThingType";
import {Type} from "./Type";
import {Concept} from "../Concept";
import Annotation = ThingType.Annotation;

export interface AttributeType extends ThingType {

    readonly valueType: Concept.ValueType;

    isBoolean(): boolean;

    isLong(): boolean;

    isDouble(): boolean;

    isString(): boolean;

    isDateTime(): boolean;

    asBoolean(): AttributeType.Boolean;

    asLong(): AttributeType.Long;

    asDouble(): AttributeType.Double;

    asString(): AttributeType.String;

    asDateTime(): AttributeType.DateTime;

    asRemote(transaction: TypeDBTransaction): AttributeType.Remote;
}

/* eslint @typescript-eslint/ban-types: "off" */
export namespace AttributeType {

    export interface Remote extends AttributeType, ThingType.Remote {

        setSupertype(type: AttributeType): Promise<void>;

        getSubtypes(): Stream<AttributeType>;

        getInstances(): Stream<Attribute>;

        getOwners(): Stream<ThingType>;

        getOwners(annotations: Annotation[]): Stream<ThingType>;

        getOwnersExplicit(): Stream<ThingType>;

        getOwnersExplicit(annotations: Annotation[]): Stream<ThingType>;

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

        asBoolean(): AttributeType.Boolean.Remote;

        asLong(): AttributeType.Long.Remote;

        asDouble(): AttributeType.Double.Remote;

        asString(): AttributeType.String.Remote;

        asDateTime(): AttributeType.DateTime.Remote;

        asRemote(transaction: TypeDBTransaction): AttributeType.Remote;
    }

    export interface Boolean extends AttributeType {

        asRemote(transaction: TypeDBTransaction): AttributeType.Boolean.Remote;
    }

    export namespace Boolean {

        export interface Remote extends AttributeType.Boolean, AttributeType.Remote {

            asRemote(transaction: TypeDBTransaction): AttributeType.Boolean.Remote;

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

            asBoolean(): AttributeType.Boolean.Remote;

            asLong(): AttributeType.Long.Remote;

            asDouble(): AttributeType.Double.Remote;

            asString(): AttributeType.String.Remote;

            asDateTime(): AttributeType.DateTime.Remote;

            setSupertype(type: Boolean): Promise<void>;

            getSubtypes(): Stream<Boolean>;

            getInstances(): Stream<Attribute.Boolean>;

            put(value: boolean): Promise<Attribute.Boolean>;

            get(value: boolean): Promise<Attribute.Boolean>;
        }
    }

    export interface Long extends AttributeType {

        asRemote(transaction: TypeDBTransaction): AttributeType.Long.Remote;
    }

    export namespace Long {

        export interface Remote extends AttributeType.Long, AttributeType.Remote {

            asRemote(transaction: TypeDBTransaction): AttributeType.Long.Remote;

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

            asBoolean(): AttributeType.Boolean.Remote;

            asLong(): AttributeType.Long.Remote;

            asDouble(): AttributeType.Double.Remote;

            asString(): AttributeType.String.Remote;

            asDateTime(): AttributeType.DateTime.Remote;

            setSupertype(type: Long): Promise<void>;

            getSubtypes(): Stream<Long>;

            getInstances(): Stream<Attribute.Long>;

            put(value: number): Promise<Attribute.Long>;

            get(value: number): Promise<Attribute.Long>;
        }
    }

    export interface Double extends AttributeType {

        asRemote(transaction: TypeDBTransaction): AttributeType.Double.Remote;
    }

    export namespace Double {

        export interface Remote extends AttributeType.Double, AttributeType.Remote {

            asRemote(transaction: TypeDBTransaction): AttributeType.Double.Remote;

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

            asBoolean(): AttributeType.Boolean.Remote;

            asLong(): AttributeType.Long.Remote;

            asDouble(): AttributeType.Double.Remote;

            asString(): AttributeType.String.Remote;

            asDateTime(): AttributeType.DateTime.Remote;

            setSupertype(type: Double): Promise<void>;

            getSubtypes(): Stream<Double>;

            getInstances(): Stream<Attribute.Double>;

            put(value: number): Promise<Attribute.Double>;

            get(value: number): Promise<Attribute.Double>;
        }
    }

    export interface String extends AttributeType {

        asRemote(transaction: TypeDBTransaction): AttributeType.String.Remote;
    }

    export namespace String {

        export interface Remote extends AttributeType.String, AttributeType.Remote {

            asRemote(transaction: TypeDBTransaction): AttributeType.String.Remote;

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

            asBoolean(): AttributeType.Boolean.Remote;

            asLong(): AttributeType.Long.Remote;

            asDouble(): AttributeType.Double.Remote;

            asString(): AttributeType.String.Remote;

            asDateTime(): AttributeType.DateTime.Remote;

            setSupertype(type: String): Promise<void>;

            getSubtypes(): Stream<String>;

            getInstances(): Stream<Attribute.String>;

            put(value: string): Promise<Attribute.String>;

            get(value: string): Promise<Attribute.String>;

            getRegex(): Promise<string>;

            setRegex(regex: string): Promise<void>;
        }
    }

    export interface DateTime extends AttributeType {

        asRemote(transaction: TypeDBTransaction): AttributeType.DateTime.Remote;
    }

    export namespace DateTime {

        export interface Remote extends AttributeType.DateTime, AttributeType.Remote {

            asRemote(transaction: TypeDBTransaction): AttributeType.DateTime.Remote;

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

            asBoolean(): AttributeType.Boolean.Remote;

            asLong(): AttributeType.Long.Remote;

            asDouble(): AttributeType.Double.Remote;

            asString(): AttributeType.String.Remote;

            asDateTime(): AttributeType.DateTime.Remote;

            setSupertype(type: DateTime): Promise<void>;

            getSubtypes(): Stream<DateTime>;

            getInstances(): Stream<Attribute.DateTime>;

            put(value: Date): Promise<Attribute.DateTime>;

            get(value: Date): Promise<Attribute.DateTime>;
        }
    }
}
