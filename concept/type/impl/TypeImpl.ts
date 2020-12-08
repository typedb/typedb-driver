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
    RemoteType,
    Type,
    Grakn,
    RPCTransaction,
    Stream,
    ThingImpl,
    ConceptProtoBuilder,
    ConceptProtoReader,
    GraknClientError,
    ErrorMessage,
} from "../../../dependencies_internal";
import ConceptProto from "graknlabs-protocol/protobuf/concept_pb";
import TransactionProto from "graknlabs-protocol/protobuf/transaction_pb";
import Transaction = Grakn.Transaction;

export abstract class TypeImpl implements Type {
    private readonly _label: string;
    private readonly _root: boolean;

    protected constructor(label: string, root: boolean) {
        if (!label) throw new GraknClientError(ErrorMessage.Concept.MISSING_LABEL.message());

        this._label = label;
        this._root = root;
    }

    getLabel(): string {
        return this._label;
    }

    isRoot(): boolean {
        return this._root;
    }

    isRemote(): boolean {
        return false;
    }

    toString(): string {
        return `${this.constructor.name}[label:${this._label}]`;
    }

    abstract asRemote(transaction: Transaction): RemoteType;
}

export abstract class RemoteTypeImpl implements RemoteType {
    private readonly _rpcTransaction: RPCTransaction;
    private _label: string;
    private readonly _isRoot: boolean;

    protected constructor(transaction: Transaction, label: string, isRoot: boolean) {
        if (!transaction) throw new GraknClientError(ErrorMessage.Concept.MISSING_TRANSACTION.message());
        if (!label) throw new GraknClientError(ErrorMessage.Concept.MISSING_LABEL.message());
        this._rpcTransaction = transaction as RPCTransaction;
        this._label = label;
        this._isRoot = isRoot;
    }

    getLabel(): string {
        return this._label;
    }

    isRoot(): boolean {
        return this._isRoot;
    }

    isRemote(): boolean {
        return true;
    }

    async setLabel(label: string): Promise<void> {
        await this.execute(new ConceptProto.Type.Req()
            .setTypeSetLabelReq(new ConceptProto.Type.SetLabel.Req().setLabel(label)));
        this._label = label;
    }

    async isAbstract(): Promise<boolean> {
        return (await this.execute(new ConceptProto.Type.Req()
            .setTypeIsAbstractReq(new ConceptProto.Type.IsAbstract.Req())))
            .getTypeIsAbstractRes().getAbstract();
    }

    protected async setSupertype(type: Type): Promise<void> {
        await this.execute(new ConceptProto.Type.Req()
            .setTypeSetSupertypeReq(new ConceptProto.Type.SetSupertype.Req().setType(ConceptProtoBuilder.type(type))));
    }

    async getSupertype(): Promise<TypeImpl> {
        const response: ConceptProto.Type.GetSupertype.Res = (await this.execute(new ConceptProto.Type.Req()
            .setTypeGetSupertypeReq(new ConceptProto.Type.GetSupertype.Req())))
            .getTypeGetSupertypeRes();

        return response.getResCase() === ConceptProto.Type.GetSupertype.Res.ResCase.TYPE ? ConceptProtoReader.type(response.getType()) : null;
    }

    getSupertypes(): Stream<TypeImpl> {
        const method = new ConceptProto.Type.Req().setTypeGetSupertypesReq(new ConceptProto.Type.GetSupertypes.Req());
        return this.typeStream(method, res => res.getTypeGetSupertypesRes().getTypeList());
    }

    getSubtypes(): Stream<TypeImpl> {
        const method = new ConceptProto.Type.Req().setTypeGetSubtypesReq(new ConceptProto.Type.GetSubtypes.Req());
        return this.typeStream(method, res => res.getTypeGetSubtypesRes().getTypeList());
    }

    async delete(): Promise<void> {
        await this.execute(new ConceptProto.Type.Req().setTypeDeleteReq(new ConceptProto.Type.Delete.Req()));
    }

    async isDeleted(): Promise<boolean> {
        return !(await this._rpcTransaction.concepts().getType(this._label));
    }

    protected get transaction(): Transaction {
        return this._rpcTransaction;
    }

    protected typeStream(method: ConceptProto.Type.Req, typeGetter: (res: ConceptProto.Type.Res) => ConceptProto.Type[]): Stream<TypeImpl> {
        const request = new TransactionProto.Transaction.Req().setTypeReq(method.setLabel(this._label));
        return this._rpcTransaction.stream(request, res => typeGetter(res.getTypeRes()).map(ConceptProtoReader.type));
    }

    protected thingStream(method: ConceptProto.Type.Req, thingGetter: (res: ConceptProto.Type.Res) => ConceptProto.Thing[]): Stream<ThingImpl> {
        const request = new TransactionProto.Transaction.Req().setTypeReq(method.setLabel(this._label));
        return this._rpcTransaction.stream(request, res => thingGetter(res.getTypeRes()).map(ConceptProtoReader.thing));
    }

    protected execute(method: ConceptProto.Type.Req): Promise<ConceptProto.Type.Res> {
        const request = new TransactionProto.Transaction.Req().setTypeReq(method.setLabel(this._label));
        return this._rpcTransaction.execute(request, res => res.getTypeRes());
    }

    toString(): string {
        return `${this.constructor.name}[label:${this._label}]`;
    }

    abstract asRemote(transaction: Transaction): RemoteTypeImpl;
}
