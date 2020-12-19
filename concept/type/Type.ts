/*
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

import {
    Concept,
    RemoteConcept,
    Grakn,
    Merge,
    Stream,
} from "../../dependencies_internal";
import Transaction = Grakn.Transaction;

export interface Type extends Concept {
    getLabel(): string;
    isRoot(): boolean;

    isThingType(): boolean;
    isAttributeType(): boolean;
    isEntityType(): boolean;
    isRelationType(): boolean;

    asRemote(transaction: Transaction): RemoteType;
}

export interface RemoteType extends Merge<RemoteConcept, Type> {
    setLabel(label: string): Promise<void>;
    isAbstract(): Promise<boolean>;

    getSupertype(): Promise<Type>;
    getSupertypes(): Stream<Type>;
    getSubtypes(): Stream<Type>;

    asRemote(transaction: Transaction): RemoteType; /* Required for `RemoteType` to take precedence over `RemoteConcept` in sub-interfaces */
}
