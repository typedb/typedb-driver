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
import {Relation} from "../thing/Relation";
import {Thing} from "../thing/Thing";
import {RelationType} from "./RelationType";
import {ThingType} from "./ThingType";
import {Type} from "./Type";
import {Concept} from "../Concept";
import Transitivity = Concept.Transitivity;

export interface RoleType extends Type {
    getSupertype(transaction: TypeDBTransaction): Promise<RoleType | null>;

    getSupertypes(transaction: TypeDBTransaction): Stream<RoleType>;

    getSubtypes(transaction: TypeDBTransaction): Stream<RoleType>;

    getRelationType(transaction: TypeDBTransaction): Promise<RelationType>;
    getRelationTypes(transaction: TypeDBTransaction): Stream<RelationType>;

    getPlayerTypes(transaction: TypeDBTransaction): Stream<ThingType>;
    getPlayerTypes(transaction: TypeDBTransaction, transitivity: Transitivity): Stream<ThingType>;

    getRelationInstances(transaction: TypeDBTransaction): Stream<Relation>;
    getRelationInstances(transaction: TypeDBTransaction, transitivity: Transitivity): Stream<Relation>;

    getPlayerInstances(transaction: TypeDBTransaction): Stream<Thing>;
    getPlayerInstances(transaction: TypeDBTransaction, transitivity: Transitivity): Stream<Thing>;
}

export namespace RoleType {
    export function proto(roleType: RoleType) {
        return RequestBuilder.Type.RoleType.protoRoleType(roleType.label);
    }
}
