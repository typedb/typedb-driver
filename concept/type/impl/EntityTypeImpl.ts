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
    ThingTypeImpl,
    RemoteThingTypeImpl,
    RemoteEntityType,
    Grakn,
    EntityType,
    EntityImpl,
    Stream,
} from "../../../dependencies_internal";
import Transaction = Grakn.Transaction;
import ConceptProto from "grakn-protocol/protobuf/concept_pb";

export class EntityTypeImpl extends ThingTypeImpl implements EntityType {
    protected constructor(label: string, isRoot: boolean) {
        super(label, isRoot);
    }

    static of(typeProto: ConceptProto.Type): EntityTypeImpl {
        return new EntityTypeImpl(typeProto.getLabel(), typeProto.getRoot());
    }

    asRemote(transaction: Transaction): RemoteEntityType {
        return new RemoteEntityTypeImpl(transaction, this.getLabel(), this.isRoot());
    }

    isEntityType(): boolean {
        return true;
    }
}

export class RemoteEntityTypeImpl extends RemoteThingTypeImpl implements RemoteEntityType {
    constructor(transaction: Transaction, label: string, isRoot: boolean) {
        super(transaction, label, isRoot);
    }

    isEntityType(): boolean {
        return true;
    }

    create(): Promise<EntityImpl> {
        const method = new ConceptProto.Type.Req().setEntityTypeCreateReq(new ConceptProto.EntityType.Create.Req());
        return this.execute(method).then(res => EntityImpl.of(res.getEntityTypeCreateRes().getEntity()));
    }

    getSubtypes(): Stream<EntityTypeImpl> {
        return super.getSubtypes() as Stream<EntityTypeImpl>;
    }

    getInstances(): Stream<EntityImpl> {
        return super.getInstances() as Stream<EntityImpl>;
    }

    setSupertype(type: EntityType): Promise<void> {
        return super.setSupertype(type);
    }

    asRemote(transaction: Transaction): RemoteEntityTypeImpl {
        return new RemoteEntityTypeImpl(transaction, this.getLabel(), this.isRoot());
    }
}
