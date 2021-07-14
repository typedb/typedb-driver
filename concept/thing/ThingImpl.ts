/*
 * Copyright (C) 2021 Vaticle
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

import { Thing as ThingProto, Type as TypeProto } from "typedb-protocol/common/concept_pb";
import { Transaction as TransactionProto } from "typedb-protocol/common/transaction_pb";
import { Concept } from "../../api/concept/Concept";
import { Attribute } from "../../api/concept/thing/Attribute";
import { Relation } from "../../api/concept/thing/Relation";
import { Thing } from "../../api/concept/thing/Thing";
import { AttributeType } from "../../api/concept/type/AttributeType";
import { RoleType } from "../../api/concept/type/RoleType";
import { ThingType } from "../../api/concept/type/ThingType";
import { TypeDBTransaction } from "../../api/connection/TypeDBTransaction";
import { ErrorMessage } from "../../common/errors/ErrorMessage";
import { TypeDBClientError } from "../../common/errors/TypeDBClientError";
import { RequestBuilder } from "../../common/rpc/RequestBuilder";
import { Stream } from "../../common/util/Stream";
import { AttributeImpl, ConceptImpl, EntityImpl, RelationImpl, RoleTypeImpl } from "../../dependencies_internal";
import BAD_ENCODING = ErrorMessage.Concept.BAD_ENCODING;
import BAD_VALUE_TYPE = ErrorMessage.Concept.BAD_VALUE_TYPE;

export abstract class ThingImpl extends ConceptImpl implements Thing {
    private readonly _iid: string;
    private readonly _isInferred: boolean;

    protected constructor(iid: string, isInferred: boolean) {
        super();
        if (!iid) throw new TypeDBClientError(ErrorMessage.Concept.MISSING_IID);
        this._iid = iid;
        this._isInferred = isInferred;
    }

    abstract asRemote(transaction: TypeDBTransaction): Thing.Remote;

    equals(concept: Concept): boolean {
        if (concept.isType()) return false;
        else return concept.asThing().getIID() === this._iid;
    }

    toString(): string {
        return `${this.className}[iid:${this._iid}]`;
    }

    getIID(): string {
        return this._iid;
    }

    abstract getType(): ThingType;

    isInferred(): boolean {
        return this._isInferred;
    }

    isThing(): boolean {
        return true;
    }

    asThing(): Thing {
        return this;
    }
}

export namespace ThingImpl {

    export abstract class Remote extends ConceptImpl.Remote implements Thing.Remote {

        private readonly _iid: string;
        private readonly _isInferred: boolean;

        protected constructor(transaction: TypeDBTransaction.Extended, iid: string, isInferred: boolean, ..._: any) {
            super(transaction);
            if (!iid) throw new TypeDBClientError(ErrorMessage.Concept.MISSING_IID);
            this._iid = iid;
            this._isInferred = isInferred;
        }

        abstract asRemote(transaction: TypeDBTransaction): Thing.Remote;

        equals(concept: Concept): boolean {
            if (concept.isType()) return false;
            else return concept.asThing().getIID() === this._iid;
        }

        toString(): string {
            return `${this.className}[iid:${this._iid}]`;
        }

        getIID(): string {
            return this._iid;
        }

        abstract getType(): ThingType;

        isInferred(): boolean {
            return this._isInferred;
        }

        isThing(): boolean {
            return true;
        }

        asThing(): Thing.Remote {
            return this;
        }

        async delete(): Promise<void> {
            const request = RequestBuilder.Thing.deleteReq(this.getIID());
            await this.execute(request);
        }

        getHas(): Stream<Attribute>;
        getHas(onlyKey: boolean): Stream<Attribute>;
        getHas(attributeType: AttributeType.Boolean): Stream<Attribute.Boolean>;
        getHas(attributeType: AttributeType.Long): Stream<Attribute.Long>;
        getHas(attributeType: AttributeType.Double): Stream<Attribute.Double>;
        getHas(attributeType: AttributeType.String): Stream<Attribute.String>;
        getHas(attributeType: AttributeType.DateTime): Stream<Attribute.DateTime>;
        getHas(attributeTypes: AttributeType[]): Stream<Attribute>;
        getHas(onlyKeyAttrTypeAttrTypes?: boolean | AttributeType | AttributeType[]): Stream<Attribute> {
            let isSingleAttrType = false;
            let request;
            if (typeof onlyKeyAttrTypeAttrTypes === "undefined") {
                request = RequestBuilder.Thing.getHasReq(this.getIID(), false);
            } else if (typeof onlyKeyAttrTypeAttrTypes === "boolean") {
                request = RequestBuilder.Thing.getHasReq(this.getIID(), onlyKeyAttrTypeAttrTypes);
            } else if (onlyKeyAttrTypeAttrTypes instanceof Array) {
                const attrTypesProto = onlyKeyAttrTypeAttrTypes.map((attrType) => ThingType.proto(attrType));
                request = RequestBuilder.Thing.getHasByTypeReq(this.getIID(), attrTypesProto);
            } else {
                request = RequestBuilder.Thing.getHasByTypeReq(this.getIID(), [ThingType.proto(onlyKeyAttrTypeAttrTypes)]);
                isSingleAttrType = true;
            }
            const attributes = this.stream(request).flatMap((resPart) => Stream.array(resPart.getThingGetHasResPart().getAttributesList()))
                .map((attrProto) => AttributeImpl.of(attrProto));
            if (isSingleAttrType) {
                const arg = onlyKeyAttrTypeAttrTypes as AttributeType;
                if (arg.isBoolean()) return attributes as Stream<Attribute.Boolean>;
                else if (arg.isLong()) return attributes as Stream<Attribute.Long>;
                else if (arg.isDouble()) return attributes as Stream<Attribute.Double>;
                else if (arg.isString()) return attributes as Stream<Attribute.String>;
                else if (arg.isDateTime()) return attributes as Stream<Attribute.DateTime>;
                else throw new TypeDBClientError(BAD_VALUE_TYPE.message(arg));
            } else {
                return attributes;
            }
        }

        getPlaying(): Stream<RoleType> {
            const request = RequestBuilder.Thing.getPlayingReq(this.getIID());
            return this.stream(request)
                .flatMap((resPart) => Stream.array(resPart.getThingGetPlayingResPart().getRoleTypesList()))
                .map((res) => RoleTypeImpl.of(res));
        }

        getRelations(roleTypes?: RoleType[]): Stream<Relation> {
            if (!roleTypes) roleTypes = [];
            const request = RequestBuilder.Thing.getRelationsReq(this.getIID(), roleTypes.map((roleType) => RoleType.proto(roleType)));
            return this.stream(request)
                .flatMap((resPart) => Stream.array(resPart.getThingGetRelationsResPart().getRelationsList()))
                .map((res) => RelationImpl.of(res));
        }

        async isDeleted(): Promise<boolean> {
            return !(await this.transaction.concepts().getThing(this.getIID()));
        }

        async setHas(attribute: Attribute): Promise<void> {
            const request = RequestBuilder.Thing.setHasReq(this.getIID(), Thing.proto(attribute));
            await this.execute(request);
        }

        async unsetHas(attribute: Attribute): Promise<void> {
            const request = RequestBuilder.Thing.unsetHasReq(this.getIID(), Thing.proto(attribute));
            await this.execute(request);
        }

        protected async execute(request: TransactionProto.Req): Promise<ThingProto.Res> {
            return (await this.transaction.rpcExecute(request, false)).getThingRes();
        }

        protected stream(request: TransactionProto.Req): Stream<ThingProto.ResPart> {
            return this.transaction.rpcStream(request).map((res) => res.getThingResPart());
        }
    }

    export function of(thingProto: ThingProto) {
        switch (thingProto.getType().getEncoding()) {
            case TypeProto.Encoding.ENTITY_TYPE:
                return EntityImpl.of(thingProto);
            case TypeProto.Encoding.RELATION_TYPE:
                return RelationImpl.of(thingProto);
            case TypeProto.Encoding.ATTRIBUTE_TYPE:
                return AttributeImpl.of(thingProto);
            default:
                throw new TypeDBClientError(BAD_ENCODING.message(thingProto.getType().getEncoding()));
        }
    }
}
