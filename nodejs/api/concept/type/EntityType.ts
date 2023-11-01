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
import {Concept} from "../Concept";
import {Entity} from "../thing/Entity";
import {ThingType} from "./ThingType";
import {RequestBuilder} from "../../../common/rpc/RequestBuilder";
import Transitivity = Concept.Transitivity;

export interface EntityType extends ThingType {
    /** @inheritDoc */
    create(transaction: TypeDBTransaction): Promise<Entity>;

    /** @inheritDoc */
    getSupertype(transaction: TypeDBTransaction): Promise<EntityType>;
    setSupertype(transaction: TypeDBTransaction, superEntityType: EntityType): Promise<void>;

    /** @inheritDoc */
    getSupertypes(transaction: TypeDBTransaction): Stream<EntityType>;

    /** @inheritDoc */
    getSubtypes(transaction: TypeDBTransaction): Stream<EntityType>;
    getSubtypes(transaction: TypeDBTransaction, transitivity: Transitivity): Stream<EntityType>;

    /** @inheritDoc */
    getInstances(transaction: TypeDBTransaction): Stream<Entity>;
    getInstances(transaction: TypeDBTransaction, transitivity: Transitivity): Stream<Entity>;
}

export namespace EntityType {

    export const NAME = "entity";

    export function proto(entityType: EntityType) {
        return RequestBuilder.Type.EntityType.protoEntityType(entityType.label);
    }
}
