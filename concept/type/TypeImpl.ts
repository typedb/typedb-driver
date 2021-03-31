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

import {GraknTransaction} from "../../api/GraknTransaction";
import {Concept} from "../../api/concept/Concept";
import {RemoteType, Type} from "../../api/concept/type/Type";
import {Label} from "../../common/Label";
import {RequestBuilder} from "../../common/rpc/RequestBuilder";
import {Stream} from "../../common/util/Stream";
import {ErrorMessage} from "../../common/errors/ErrorMessage";
import {GraknClientError} from "../../common/errors/GraknClientError";
import {Type as TypeProto} from "grakn-protocol/common/concept_pb";
import {Transaction as TransactionProto} from "grakn-protocol/common/transaction_pb";
import {ConceptImpl, RoleTypeImpl, ThingTypeImpl} from "../../dependencies_internal";
import MISSING_LABEL = ErrorMessage.Concept.MISSING_LABEL;

export abstract class TypeImpl extends ConceptImpl implements Type {

    private readonly _label: Label;
    private readonly _isRoot: boolean;

    protected constructor(label: Label, isRoot: boolean) {
        super();
        if (!label) throw new GraknClientError(MISSING_LABEL.message());
        this._label = label;
        this._isRoot = isRoot;
    }

    abstract asRemote(transaction: GraknTransaction): RemoteType;

    isRoot(): boolean {
        return this._isRoot;
    }

    isType(): boolean {
        return true;
    }

    getLabel(): Label {
        return this._label;
    }

    equals(concept: Concept): boolean {
        if (!concept.isType()) return false;
        return (concept as Type).getLabel().equals(this.getLabel());
    }

    toString(): string {
        return `${this.constructor.name}[label:${this._label}]`;
    }

}


export namespace TypeImpl {
    export function of(typeProto: TypeProto) {
        switch (typeProto.getEncoding()) {
            case TypeProto.Encoding.ROLE_TYPE:
                return RoleTypeImpl.of(typeProto);
            default:
                return ThingTypeImpl.of(typeProto);
        }
    }


    export abstract class RemoteImpl extends ConceptImpl.Remote implements RemoteType {

        private _label: Label;
        private _isRoot: boolean;
        protected _transaction: GraknTransaction.Extended;

        constructor(transaction: GraknTransaction.Extended, label: Label, isRoot: boolean) {
            super();
            if (!transaction) throw new GraknClientError(ErrorMessage.Concept.MISSING_TRANSACTION.message());
            if (!label) throw new GraknClientError(ErrorMessage.Concept.MISSING_LABEL.message());
            this._transaction = transaction;
            this._label = label;
            this._isRoot = isRoot;
        }

        asRemote(transaction: GraknTransaction): RemoteType {
            return this;
        }

        isType(): boolean {
            return true;
        }

        getLabel(): Label {
            return this._label;
        }

        isRoot(): boolean {
            return this._isRoot;
        }

        equals(concept: Concept): boolean {
            if (!concept.isType()) return false;
            return (concept as Type).getLabel().equals(this.getLabel());
        }

        toString(): string {
            return `${this.constructor.name}[label:${this._label}]`;
        }

        async delete(): Promise<void> {
            const request = RequestBuilder.Type.deleteReq(this._label);
            await this.execute(request);
        }

        getSubtypes(): Stream<Type> {
            const request = RequestBuilder.Type.getSubtypesReq(this._label);
            return this.stream(request)
                .flatMap((resPart) => Stream.array(resPart.getTypeGetSubtypesResPart().getTypesList()))
                .map((typeProto) => of(typeProto));
        }
        getSupertype(): Promise<Type> {
            const request = RequestBuilder.Type.getSupertypeReq(this._label);
            return this.execute(request).then((res) => of(res.getTypeGetSupertypeRes().getType()));
        }

        getSupertypes(): Stream<Type> {
            const request = RequestBuilder.Type.getSupertypesReq(this._label);
            return this.stream(request)
                .flatMap((resPart) => Stream.array(resPart.getTypeGetSupertypesResPart().getTypesList()))
                .map((typeProto) => of(typeProto));
        }

        async setLabel(label: string): Promise<void> {
            const request = RequestBuilder.Type.setLabelReq(this._label, label);
            await this.execute(request);
            this._label = new Label(this.getLabel().scope(), label);
        }

        async isAbstract(): Promise<boolean> {
            const request = RequestBuilder.Type.isAbstractReq(this._label);
            return this.execute(request).then((res) => res.getTypeIsAbstractRes().getAbstract());
        }

        protected async execute(request: TransactionProto.Req): Promise<TypeProto.Res> {
            return (await this._transaction.rpcExecute(request)).getTypeRes();
        }

        protected stream(request: TransactionProto.Req): Stream<TypeProto.ResPart> {
            const resPartStream = this._transaction.rpcStream(request);
            return resPartStream.map((res) => res.getTypeResPart());
        }

    }
}