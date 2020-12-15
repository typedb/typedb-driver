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
    Grakn, ThingTypeImpl, Bytes,
} from "../../../dependencies_internal";
import Transaction = Grakn.Transaction;
import ConceptProto from "grakn-protocol/protobuf/concept_pb";

export class EntityImpl extends ThingImpl implements Entity {
    protected constructor(iid: string) {
        super(iid);
    }

    static of(protoThing: ConceptProto.Thing): EntityImpl {
        return new EntityImpl(Bytes.bytesToHexString(protoThing.getIid_asU8()));
    }

    asRemote(transaction: Transaction): RemoteEntityImpl {
        return new RemoteEntityImpl(transaction, this.getIID());
    }
}

export class RemoteEntityImpl extends RemoteThingImpl implements RemoteEntity {
    constructor(transaction: Transaction, iid: string) {
        super(transaction, iid);
    }

    public asRemote(transaction: Transaction): RemoteEntityImpl {
        return new RemoteEntityImpl(transaction, this.getIID());
    }

    async getType(): Promise<EntityTypeImpl> {
        const res = await this.execute(new ConceptProto.Thing.Req().setThingGetTypeReq(new ConceptProto.Thing.GetType.Req()));
        return ThingTypeImpl.of(res.getThingGetTypeRes().getThingType()) as EntityTypeImpl;
    }
}
