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

import {Entity} from "../../api/concept/thing/Entity";
import {EntityType} from "../../api/concept/type/EntityType";
import {TypeDBTransaction} from "../../api/connection/TypeDBTransaction";
import {Bytes} from "../../common/util/Bytes";
import {EntityTypeImpl, ThingImpl} from "../../dependencies_internal";
import {Entity as EntityProto} from "typedb-protocol/proto/concept";

export class EntityImpl extends ThingImpl implements Entity {
    private readonly _type: EntityType;

    constructor(iid: string, inferred: boolean, type: EntityType) {
        super(iid, inferred);
        this._type = type;
    }

    protected get className(): string {
        return "Entity";
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

    async isDeleted(transaction: TypeDBTransaction): Promise<boolean> {
        return !(await transaction.concepts.getEntity(this.iid));
    }
}

export namespace EntityImpl {
    export function ofEntityProto(proto: EntityProto): Entity {
        if (!proto) return null;
        const iid = Bytes.bytesToHexString(proto.iid);
        return new EntityImpl(iid, proto.inferred, EntityTypeImpl.ofEntityTypeProto(proto.entity_type));
    }
}
