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

import { Thing as ThingProto } from "typedb-protocol/common/concept_pb";
import { Entity } from "../../api/concept/thing/Entity";
import { EntityType } from "../../api/concept/type/EntityType";
import { TypeDBTransaction } from "../../api/connection/TypeDBTransaction";
import { Bytes } from "../../common/util/Bytes";
import { EntityTypeImpl, ThingImpl } from "../../dependencies_internal";

export class EntityImpl extends ThingImpl implements Entity {

    private readonly _type: EntityType;

    constructor(iid: string, inferred: boolean, type: EntityType) {
        super(iid, inferred);
        this._type = type;
    }

    protected get className(): string {
        return "Entity";
    }

    asRemote(transaction: TypeDBTransaction): Entity.Remote {
        return new EntityImpl.Remote(transaction as TypeDBTransaction.Extended, this.iid, this.inferred, this.type);
    }

    get type(): EntityType {
        return this._type;
    }

    isEntity(): boolean {
        return true;
    }

    asEntity(): Entity {
        return this;
    }
}

export namespace EntityImpl {

    export function of(thingProto: ThingProto): Entity {
        if (!thingProto) return null;
        const iid = Bytes.bytesToHexString(thingProto.getIid_asU8());
        return new EntityImpl(iid, thingProto.getInferred(), EntityTypeImpl.of(thingProto.getType()));
    }

    export class Remote extends ThingImpl.Remote implements Entity.Remote {

        private readonly _type: EntityType;

        constructor(transaction: TypeDBTransaction.Extended, iid: string, inferred: boolean, type: EntityType) {
            super(transaction, iid, inferred);
            this._type = type;
        }

        protected get className(): string {
            return "Entity";
        }

        asRemote(transaction: TypeDBTransaction): Entity.Remote {
            return new EntityImpl.Remote(transaction as TypeDBTransaction.Extended, this.iid, this.inferred, this.type);
        }

        get type(): EntityType {
            return this._type;
        }

        isEntity(): boolean {
            return true;
        }

        asEntity(): Entity.Remote {
            return this;
        }
    }
}