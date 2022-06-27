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

import { Type as TypeProto } from "typedb-protocol/common/concept_pb";
import { Transaction as TransactionProto } from "typedb-protocol/common/transaction_pb";
import { Concept } from "../../api/concept/Concept";
import { Type } from "../../api/concept/type/Type";
import { TypeDBTransaction } from "../../api/connection/TypeDBTransaction";
import { ErrorMessage } from "../../common/errors/ErrorMessage";
import { TypeDBClientError } from "../../common/errors/TypeDBClientError";
import { Label } from "../../common/Label";
import { RequestBuilder } from "../../common/rpc/RequestBuilder";
import { Stream } from "../../common/util/Stream";
import { ConceptImpl, RoleTypeImpl, ThingTypeImpl } from "../../dependencies_internal";
import MISSING_LABEL = ErrorMessage.Concept.MISSING_LABEL;

export abstract class TypeImpl extends ConceptImpl implements Type {

    private readonly _label: Label;
    private readonly _root: boolean;

    protected constructor(label: Label, root: boolean) {
        super();
        if (!label) throw new TypeDBClientError(MISSING_LABEL);
        this._label = label;
        this._root = root;
    }

    abstract asRemote(transaction: TypeDBTransaction): Type.Remote;

    get root(): boolean {
        return this._root;
    }

    get label(): Label {
        return this._label;
    }

    isType(): boolean {
        return true;
    }

    asType(): Type {
        return this;
    }

    equals(concept: Concept): boolean {
        if (!concept.isType()) return false;
        return concept.asType().label.equals(this.label);
    }

    toString(): string {
        return `${this.className}[label:${this._label}]`;
    }
}

export namespace TypeImpl {
    export function of(typeProto: TypeProto) {
        if (!typeProto) return null;
        switch (typeProto.getEncoding()) {
            case TypeProto.Encoding.ROLE_TYPE:
                return RoleTypeImpl.of(typeProto);
            default:
                return ThingTypeImpl.of(typeProto);
        }
    }

    export abstract class Remote extends ConceptImpl.Remote implements Type.Remote {

        private _label: Label;
        private readonly _root: boolean;

        protected constructor(transaction: TypeDBTransaction.Extended, label: Label, root: boolean) {
            super(transaction);
            if (!label) throw new TypeDBClientError(ErrorMessage.Concept.MISSING_LABEL);
            this._label = label;
            this._root = root;
        }

        abstract asRemote(transaction: TypeDBTransaction): Type.Remote;

        get root(): boolean {
            return this._root;
        }

        get label(): Label {
            return this._label;
        }

        isType(): boolean {
            return true;
        }

        asType(): Type.Remote {
            return this;
        }

        equals(concept: Concept): boolean {
            if (!concept.isType()) return false;
            return concept.asType().label.equals(this.label);
        }

        toString(): string {
            return `${this.className}[label:${this._label}]`;
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
            this._label = new Label(this.label.scope, label);
        }

        async isAbstract(): Promise<boolean> {
            const request = RequestBuilder.Type.isAbstractReq(this._label);
            return this.execute(request).then((res) => res.getTypeIsAbstractRes().getAbstract());
        }

        protected async execute(request: TransactionProto.Req): Promise<TypeProto.Res> {
            return (await this.transaction.rpcExecute(request, false)).getTypeRes();
        }

        protected stream(request: TransactionProto.Req): Stream<TypeProto.ResPart> {
            const resPartStream = this.transaction.rpcStream(request);
            return resPartStream.map((res) => res.getTypeResPart());
        }
    }
}
