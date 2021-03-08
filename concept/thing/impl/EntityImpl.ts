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
    ThingImpl,
    RemoteThingImpl,
    Entity,
    RemoteEntity,
    EntityTypeImpl,
    GraknClient, ThingTypeImpl, Bytes,
} from "../../../dependencies_internal";
import Transaction = GraknClient.Transaction;
import ConceptProto from "grakn-protocol/protobuf/concept_pb";

export class EntityImpl extends ThingImpl implements Entity {
    private readonly _type: EntityTypeImpl;

    protected constructor(iid: string, type: EntityTypeImpl) {
        super(iid);
        this._type = type;
    }

    static of(protoThing: ConceptProto.Thing): EntityImpl {
        return new EntityImpl(Bytes.bytesToHexString(protoThing.getIid_asU8()), EntityTypeImpl.of(protoThing.getType()));
    }

    getType(): EntityTypeImpl {
        return this._type;
    }

    asRemote(transaction: Transaction): RemoteEntityImpl {
        return new RemoteEntityImpl(transaction, this.getIID(), this._type);
    }

    isEntity(): boolean {
        return true;
    }
}

export class RemoteEntityImpl extends RemoteThingImpl implements RemoteEntity {
    private readonly _type: EntityTypeImpl;

    constructor(transaction: Transaction, iid: string, type: EntityTypeImpl) {
        super(transaction, iid);
        this._type = type;
    }

    public asRemote(transaction: Transaction): RemoteEntityImpl {
        return new RemoteEntityImpl(transaction, this.getIID(), this._type);
    }

    getType(): EntityTypeImpl {
        return this._type;
    }

    isEntity(): boolean {
        return true;
    }
}
