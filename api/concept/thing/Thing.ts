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

import {TypeDBTransaction} from "../../connection/TypeDBTransaction";
import {Relation} from "./Relation";
import {Attribute} from "./Attribute";
import {RoleType} from "../type/RoleType";
import {ThingType} from "../type/ThingType";
import {AttributeType} from "../type/AttributeType";
import {Concept, RemoteConcept} from "../Concept";
import {Stream} from "../../../common/util/Stream";
import {RequestBuilder} from "../../../common/rpc/RequestBuilder";

export interface Thing extends Concept {

    asRemote(transaction: TypeDBTransaction): RemoteThing;

    getIID(): string;

    getType(): ThingType;

    isInferred(): boolean;
}

export interface RemoteThing extends Thing, RemoteConcept {

    asRemote(transaction: TypeDBTransaction): RemoteThing;

    setHas(attribute: Attribute<AttributeType.ValueClass>): Promise<void>;

    unsetHas(attribute: Attribute<AttributeType.ValueClass>): Promise<void>;

    getHas(onlyKey: boolean): Stream<Attribute<AttributeType.ValueClass>>;

    getHas(attributeType: AttributeType.Boolean): Stream<Attribute.Boolean>;

    getHas(attributeType: AttributeType.Long): Stream<Attribute.Long>;

    getHas(attributeType: AttributeType.Double): Stream<Attribute.Double>;

    getHas(attributeType: AttributeType.String): Stream<Attribute.String>;

    getHas(attributeType: AttributeType.DateTime): Stream<Attribute.DateTime>;

    getHas(): Stream<Attribute<AttributeType.ValueClass>>;

    getHas(attributeTypes: AttributeType[]): Stream<Attribute<AttributeType.ValueClass>>;

    getPlaying(): Stream<RoleType>;

    getRelations(): Stream<Relation>;

    getRelations(roleTypes: RoleType[]): Stream<Relation>;

}

export namespace Thing {

    export function proto(thing: Thing) {
        return RequestBuilder.Thing.protoThing(thing.getIID());
    }

}
