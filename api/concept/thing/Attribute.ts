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

import {RemoteThing, Thing} from "./Thing";
import {TypeDBTransaction} from "../../connection/TypeDBTransaction";
import {Stream} from "../../../common/util/Stream";
import {ThingType} from "../type/ThingType";
import {AttributeType} from "../type/AttributeType";

export interface Attribute<T extends AttributeType.ValueClass> extends Thing {

    asRemote(transaction: TypeDBTransaction): Attribute.Remote<T>;

    getType(): AttributeType;

    getValue(): T;

    isBoolean(): boolean;

    isLong(): boolean;

    isDouble(): boolean;

    isString(): boolean;

    isDateTime(): boolean;

}

export namespace Attribute {

    export interface Remote<T extends AttributeType.ValueClass> extends Attribute<T>, RemoteThing {

        asRemote(transaction: TypeDBTransaction): Attribute.Remote<T>;

        getType(): AttributeType;

        getOwners(ownerType?: ThingType): Stream<Thing>;

    }

    export interface Boolean extends Attribute<boolean> {

        asRemote(transaction: TypeDBTransaction): Attribute.RemoteBoolean;

        getType(): AttributeType.Boolean;

    }

    export interface RemoteBoolean extends Attribute.Remote<boolean>, Attribute.Boolean {

        asRemote(transaction: TypeDBTransaction): Attribute.RemoteBoolean;

        getType(): AttributeType.Boolean;

    }

    export interface Long extends Attribute<number> {

        asRemote(transaction: TypeDBTransaction): Attribute.RemoteLong;

        getType(): AttributeType.Long;

    }

    export interface RemoteLong extends Attribute.Remote<number>, Attribute.Long {

        asRemote(transaction: TypeDBTransaction): Attribute.RemoteLong;

        getType(): AttributeType.Long;

    }

    export interface Double extends Attribute<number> {

        asRemote(transaction: TypeDBTransaction): Attribute.RemoteDouble;

        getType(): AttributeType.Double;

    }

    export interface RemoteDouble extends Attribute.Remote<number>, Attribute.Double {

        asRemote(transaction: TypeDBTransaction): Attribute.RemoteDouble;

        getType(): AttributeType.Double;

    }

    export interface String extends Attribute<string> {

        asRemote(transaction: TypeDBTransaction): Attribute.RemoteString;

        getType(): AttributeType.String;

    }

    export interface RemoteString extends Attribute.Remote<string>, Attribute.String {

        asRemote(transaction: TypeDBTransaction): Attribute.RemoteString;

        getType(): AttributeType.String;

    }

    export interface DateTime extends Attribute<Date> {

        asRemote(transaction: TypeDBTransaction): Attribute.RemoteDateTime;

        getType(): AttributeType.DateTime;

    }

    export interface RemoteDateTime extends Attribute.Remote<Date>, Attribute.DateTime {

        asRemote(transaction: TypeDBTransaction): Attribute.RemoteDateTime;

        getType(): AttributeType.DateTime;

    }
}
