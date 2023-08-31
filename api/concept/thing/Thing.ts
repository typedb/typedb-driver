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

import {RequestBuilder} from "../../../common/rpc/RequestBuilder";
import {Stream} from "../../../common/util/Stream";
import {TypeDBTransaction} from "../../connection/TypeDBTransaction";
import {Concept} from "../Concept";
import {AttributeType} from "../type/AttributeType";
import {RoleType} from "../type/RoleType";
import {ThingType} from "../type/ThingType";
import {Attribute} from "./Attribute";
import {Relation} from "./Relation";
import {ErrorMessage} from "../../../common/errors/ErrorMessage";
import {TypeDBClientError} from "../../../common/errors/TypeDBClientError";
import Annotation = ThingType.Annotation;
import ILLEGAL_STATE = ErrorMessage.Internal.ILLEGAL_STATE;

export interface Thing extends Concept {
    readonly iid: string;

    readonly type: ThingType;

    readonly inferred: boolean;

    delete(transaction: TypeDBTransaction): Promise<void>;

    isDeleted(transaction: TypeDBTransaction): Promise<boolean>;

    getHas(transaction: TypeDBTransaction): Stream<Attribute>;

    getHas(transaction: TypeDBTransaction, annotations: Annotation[]): Stream<Attribute>;

    getHas(transaction: TypeDBTransaction, attributeType: AttributeType): Stream<Attribute>;

    getHas(transaction: TypeDBTransaction, attributeTypes: AttributeType[]): Stream<Attribute>;

    getHas(transaction: TypeDBTransaction, attributeTypes: AttributeType[], annotations: Annotation[]): Stream<Attribute>;

    setHas(transaction: TypeDBTransaction, attribute: Attribute): Promise<void>;

    unsetHas(transaction: TypeDBTransaction, attribute: Attribute): Promise<void>;

    getRelations(transaction: TypeDBTransaction): Stream<Relation>;

    getRelations(transaction: TypeDBTransaction, roleTypes: RoleType[]): Stream<Relation>;

    getPlaying(transaction: TypeDBTransaction): Stream<RoleType>;
}

export namespace Thing {
    export function proto(thing: Thing) {
        if (thing.isEntity()) return RequestBuilder.Thing.protoThingEntity(thing.iid);
        else if (thing.isRelation()) return RequestBuilder.Thing.protoThingRelation(thing.iid);
        else if (thing.isAttribute()) return RequestBuilder.Thing.protoThingAttribute(thing.iid);
        else throw new TypeDBClientError(ILLEGAL_STATE.message());
    }
}
