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
    Thing,
    RemoteThing,
    Attribute,
    Type,
    AttributeType,
    BooleanAttributeType,
    DateTimeAttributeType,
    DoubleAttributeType,
    LongAttributeType,
    StringAttributeType,
    RoleType,
    Grakn,
    ThingTypeImpl,
    RoleTypeImpl,
    Stream,
    RelationImpl,
    TypeImpl,
    ConceptProtoReader,
    ConceptProtoBuilder,
    RPCTransaction,
    BooleanAttributeTypeImpl,
    DateTimeAttributeTypeImpl,
    DoubleAttributeTypeImpl,
    StringAttributeTypeImpl,
    LongAttributeTypeImpl,
    AttributeImpl,
    BooleanAttributeImpl,
    DateTimeAttributeImpl,
    DoubleAttributeImpl,
    LongAttributeImpl,
    StringAttributeImpl,
    GraknClientError,
    ErrorMessage,
} from "../../../dependencies_internal";
import ConceptProto from "graknlabs-grpc-protocol/protobuf/concept_pb";
import Transaction = Grakn.Transaction;
import TransactionProto from "graknlabs-grpc-protocol/protobuf/transaction_pb";
import ValueClass = AttributeType.ValueClass;


export abstract class ThingImpl implements Thing {
    private readonly _iid: string;

    protected constructor(iid: string) {
        if (!iid) throw new GraknClientError(ErrorMessage.Concept.MISSING_IID.message());
        this._iid = iid;
    }

    getIID(): string {
        return this._iid;
    }

    isRemote(): boolean {
        return false;
    }

    toString(): string {
        return `${ThingImpl.name}[iid:${this._iid}]`;
    }

    abstract asRemote(transaction: Transaction): RemoteThing;
}

export abstract class RemoteThingImpl implements RemoteThing {
    private readonly _iid: string;
    private readonly _rpcTransaction: RPCTransaction;

    protected constructor(transaction: Transaction, iid: string) {
        if (!transaction) throw new GraknClientError(ErrorMessage.Concept.MISSING_TRANSACTION.message());
        if (!iid) throw new GraknClientError(ErrorMessage.Concept.MISSING_IID.message());
        this._iid = iid;
        this._rpcTransaction = transaction as RPCTransaction;
    }

    getIID(): string {
        return this._iid;
    }

    async getType(): Promise<ThingTypeImpl> {
        const response = await this.execute(new ConceptProto.Thing.Req().setThingGetTypeReq(new ConceptProto.Thing.GetType.Req()));
        return ConceptProtoReader.type(response.getThingGetTypeRes().getThingType()) as ThingTypeImpl;
    }

    async isInferred(): Promise<boolean> {
        return (await this.execute(new ConceptProto.Thing.Req().setThingIsInferredReq(
            new ConceptProto.Thing.IsInferred.Req()))).getThingIsInferredRes().getInferred();
    }

    isRemote(): boolean {
        return true;
    }

    getHas(onlyKey: boolean): Stream<AttributeImpl<ValueClass>>;
    getHas(attributeType: BooleanAttributeType): Stream<BooleanAttributeImpl>;
    getHas(attributeType: LongAttributeType): Stream<LongAttributeImpl>;
    getHas(attributeType: DoubleAttributeType): Stream<DoubleAttributeImpl>;
    getHas(attributeType: StringAttributeType): Stream<StringAttributeImpl>;
    getHas(attributeType: DateTimeAttributeType): Stream<DateTimeAttributeImpl>;
    getHas(attributeTypes: AttributeType[]): Stream<AttributeImpl<ValueClass>>;
    getHas(): Stream<AttributeImpl<ValueClass>>;
    getHas(arg?: boolean | Type | AttributeType[]): Stream<AttributeImpl<ValueClass>> | Stream<BooleanAttributeImpl> | Stream<LongAttributeImpl>
        | Stream<DoubleAttributeImpl> | Stream<StringAttributeImpl> | Stream<DateTimeAttributeImpl> {
        if (typeof arg === "undefined") {
            const method = new ConceptProto.Thing.Req().setThingGetHasReq(new ConceptProto.Thing.GetHas.Req());
            return this.thingStream(method, res => res.getThingGetHasRes().getAttributeList()) as Stream<AttributeImpl<ValueClass>>;
        }
        if (typeof arg === "boolean") {
            const method = new ConceptProto.Thing.Req().setThingGetHasReq(new ConceptProto.Thing.GetHas.Req().setKeysOnly(arg));
            return this.thingStream(method, res => res.getThingGetHasRes().getAttributeList()) as Stream<AttributeImpl<ValueClass>>;
        }
        if (Array.isArray(arg)) {
            const method = new ConceptProto.Thing.Req()
                .setThingGetHasReq(new ConceptProto.Thing.GetHas.Req().setAttributeTypesList(ConceptProtoBuilder.types(arg)));
            return this.thingStream(method, res => res.getThingGetHasRes().getAttributeList()) as Stream<AttributeImpl<ValueClass>>;
        }
        const method = new ConceptProto.Thing.Req()
            .setThingGetHasReq(new ConceptProto.Thing.GetHas.Req().setAttributeTypesList([ConceptProtoBuilder.type(arg)]));
        const stream = this.thingStream(method, res => res.getThingGetHasRes().getAttributeList());
        if (arg instanceof BooleanAttributeTypeImpl) return stream as Stream<BooleanAttributeImpl>;
        if (arg instanceof LongAttributeTypeImpl) return stream as Stream<LongAttributeImpl>;
        if (arg instanceof DoubleAttributeTypeImpl) return stream as Stream<DoubleAttributeImpl>;
        if (arg instanceof StringAttributeTypeImpl) return stream as Stream<StringAttributeImpl>;
        if (arg instanceof DateTimeAttributeTypeImpl) return stream as Stream<DateTimeAttributeImpl>;
        throw new GraknClientError(ErrorMessage.Concept.BAD_VALUE_TYPE.message(arg))
    }

    getPlays(): Stream<RoleTypeImpl> {
        const method = new ConceptProto.Thing.Req().setThingGetPlaysReq(new ConceptProto.Thing.GetPlays.Req());
        return this.typeStream(method, res => res.getThingGetPlaysRes().getRoleTypeList()) as Stream<RoleTypeImpl>;
    }

    getRelations(): Stream<RelationImpl>;
    getRelations(roleTypes: RoleType[] = []): Stream<RelationImpl> {
        const method = new ConceptProto.Thing.Req().setThingGetRelationsReq(
            new ConceptProto.Thing.GetRelations.Req().setRoleTypesList(ConceptProtoBuilder.types(roleTypes)));
        return this.thingStream(method, res => res.getThingGetRelationsRes().getRelationList()) as Stream<RelationImpl>;
    }

    async setHas(attribute: Attribute<AttributeType.ValueClass>): Promise<void> {
        await this.execute(new ConceptProto.Thing.Req().setThingSetHasReq(
            new ConceptProto.Thing.SetHas.Req().setAttribute(ConceptProtoBuilder.thing(attribute))));
    }

    async unsetHas(attribute: Attribute<AttributeType.ValueClass>): Promise<void> {
        await this.execute(new ConceptProto.Thing.Req().setThingUnsetHasReq(
            new ConceptProto.Thing.UnsetHas.Req().setAttribute(ConceptProtoBuilder.thing(attribute))));
    }

    async delete(): Promise<void> {
        await this.execute(new ConceptProto.Thing.Req().setThingDeleteReq(new ConceptProto.Thing.Delete.Req()));
    }

    async isDeleted(): Promise<boolean> {
        return !(await this._rpcTransaction.concepts().getThing(this._iid));
    }

    protected get transaction(): Transaction {
        return this._rpcTransaction;
    }

    protected typeStream(method: ConceptProto.Thing.Req, typeGetter: (res: ConceptProto.Thing.Res) => ConceptProto.Type[]): Stream<TypeImpl> {
        const request = new TransactionProto.Transaction.Req().setThingReq(method.setIid(this._iid));
        return (this._rpcTransaction).stream(request, res => typeGetter(res.getThingRes()).map(ConceptProtoReader.type));
    }

    protected thingStream(method: ConceptProto.Thing.Req, thingGetter: (res: ConceptProto.Thing.Res) => ConceptProto.Thing[]): Stream<ThingImpl> {
        const request = new TransactionProto.Transaction.Req().setThingReq(method.setIid(this._iid));
        return this._rpcTransaction.stream(request, res => thingGetter(res.getThingRes()).map(ConceptProtoReader.thing));
    }

    protected execute(method: ConceptProto.Thing.Req): Promise<ConceptProto.Thing.Res> {
        const request = new TransactionProto.Transaction.Req().setThingReq(method.setIid(this._iid));
        return this._rpcTransaction.execute(request, res => res.getThingRes());
    }

    toString(): string {
        return `${RemoteThingImpl.name}[iid:${this._iid}]`;
    }

    abstract asRemote(transaction: Transaction): RemoteThing;
}
