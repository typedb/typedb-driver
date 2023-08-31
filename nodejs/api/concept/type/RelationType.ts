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
import {Relation} from "../thing/Relation";
import {RoleType} from "./RoleType";
import {ThingType} from "./ThingType";
import {RequestBuilder} from "../../../common/rpc/RequestBuilder";
import Transitivity = Concept.Transitivity;

export interface RelationType extends ThingType {
    create(transaction: TypeDBTransaction): Promise<Relation>;

    getSupertype(transaction: TypeDBTransaction): Promise<RelationType | null>;
    setSupertype(transaction: TypeDBTransaction, type: RelationType): Promise<void>;

    getSupertypes(transaction: TypeDBTransaction): Stream<RelationType>;

    getSubtypes(transaction: TypeDBTransaction): Stream<RelationType>;
    getSubtypes(transaction: TypeDBTransaction, transitivity: Transitivity): Stream<RelationType>;

    getInstances(transaction: TypeDBTransaction): Stream<Relation>;
    getInstances(transaction: TypeDBTransaction, transitivity: Transitivity): Stream<Relation>;

    getRelates(transaction: TypeDBTransaction): Stream<RoleType>;
    getRelates(transaction: TypeDBTransaction, transitivity: Transitivity): Stream<RoleType>;

    getRelatesForRoleLabel(transaction: TypeDBTransaction, roleLabel: string): Promise<RoleType | null>;

    getRelatesOverridden(transaction: TypeDBTransaction, roleLabel: string): Promise<RoleType | null>;

    setRelates(transaction: TypeDBTransaction, roleLabel: string, overriddenLabel?: string): Promise<void>;
    unsetRelates(transaction: TypeDBTransaction, roleLabel: string): Promise<void>;
}

export namespace RelationType {
    export function proto(relationType: RelationType) {
        return RequestBuilder.Type.RelationType.protoRelationType(relationType.label);
    }
}
