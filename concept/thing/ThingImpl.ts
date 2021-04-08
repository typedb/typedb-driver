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
import {ThingType} from "../../api/concept/type/ThingType";
import {AttributeType} from "../../api/concept/type/AttributeType";
import {Relation} from "../../api/concept/thing/Relation";
import {Attribute} from "../../api/concept/thing/Attribute";
import {RemoteThing, Thing} from "../../api/concept/thing/Thing";
import {RoleType} from "../../api/concept/type/RoleType";
import {AttributeImpl, ConceptImpl, EntityImpl, RelationImpl, RoleTypeImpl} from "../../dependencies_internal";
import {Stream} from "../../common/util/Stream";
import {RequestBuilder} from "../../common/rpc/RequestBuilder";
import {ErrorMessage} from "../../common/errors/ErrorMessage";
import {GraknClientError} from "../../common/errors/GraknClientError";
import {Transaction as TransactionProto} from "grakn-protocol/common/transaction_pb";
import {Thing as ThingProto, Type as TypeProto} from "grakn-protocol/common/concept_pb";
import BAD_ENCODING = ErrorMessage.Concept.BAD_ENCODING;
import BAD_VALUE_TYPE = ErrorMessage.Concept.BAD_VALUE_TYPE;

export abstract class ThingImpl extends ConceptImpl implements Thing {
    private readonly _iid: string;
    private readonly _isInferred: boolean;

    protected constructor(iid: string, isInferred: boolean) {
        super();
        if (!iid) throw new GraknClientError(ErrorMessage.Concept.MISSING_IID.message());
        this._iid = iid;
        this._isInferred = isInferred;
    }

    abstract asRemote(transaction: GraknTransaction): RemoteThing;

    equals(concept: Concept): boolean {
        if (concept.isType()) return false;
        else return (concept as Thing).getIID() === this._iid;
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

}


export abstract class RemoteThingImpl extends ThingImpl implements RemoteThing {

    private _transaction: GraknTransaction.Extended;

    protected constructor(transaction: GraknTransaction.Extended, iid: string, isInferred: boolean) {
        super(iid, isInferred);
        this._transaction = transaction;
    }

    abstract asRemote(transaction: GraknTransaction): RemoteThing;

    abstract getType(): ThingType;

    async delete(): Promise<void> {
        const request = RequestBuilder.Thing.deleteReq(this.getIID());
        await this.execute(request);
    }

    getHas(): Stream<Attribute<AttributeType.ValueClass>>;
    getHas(onlyKey: boolean): Stream<Attribute<AttributeType.ValueClass>>;
    getHas(attributeType: AttributeType.Boolean): Stream<Attribute.Boolean>;
    getHas(attributeType: AttributeType.Long): Stream<Attribute.Long>;
    getHas(attributeType: AttributeType.Double): Stream<Attribute.Double>;
    getHas(attributeType: AttributeType.String): Stream<Attribute.String>;
    getHas(attributeType: AttributeType.DateTime): Stream<Attribute.DateTime>;
    getHas(attributeTypes: AttributeType[]): Stream<Attribute<AttributeType.ValueClass>>;
    getHas(onlyKeyAttrTypeAttrTypes?: boolean | AttributeType | AttributeType[])
        : Stream<Attribute<AttributeType.ValueClass>> | Stream<Attribute.Boolean> | Stream<Attribute.Long>
        | Stream<Attribute.Double> | Stream<Attribute.String> | Stream<Attribute.DateTime> {
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
            else if (arg.isLong) return attributes as Stream<Attribute.Long>;
            else if (arg.isDouble) return attributes as Stream<Attribute.Double>;
            else if (arg.isString) return attributes as Stream<Attribute.String>;
            else if (arg.isDateTime()) return attributes as Stream<Attribute.DateTime>;
            else throw new GraknClientError(BAD_VALUE_TYPE.message(arg));
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
        return !(await this._transaction.concepts().getThing(this.getIID()));
    }

    async setHas(attribute: Attribute<AttributeType.ValueClass>): Promise<void> {
        const request = RequestBuilder.Thing.setHasReq(this.getIID(), Thing.proto(attribute));
        await this.execute(request);
    }

    async unsetHas(attribute: Attribute<AttributeType.ValueClass>): Promise<void> {
        const request = RequestBuilder.Thing.unsetHasReq(this.getIID(), Thing.proto(attribute));
        await this.execute(request);
    }

    protected async execute(request: TransactionProto.Req): Promise<ThingProto.Res> {
        return (await this._transaction.rpcExecute(request)).getThingRes();
    }

    protected stream(request: TransactionProto.Req): Stream<ThingProto.ResPart> {
        return this._transaction.rpcStream(request).map((res) => res.getThingResPart());
    }

}

export namespace ThingImpl {

    export function of(thingProto: ThingProto) {
        switch (thingProto.getType().getEncoding()) {
            case TypeProto.Encoding.ENTITY_TYPE:
                return EntityImpl.of(thingProto);
            case TypeProto.Encoding.RELATION_TYPE:
                return RelationImpl.of(thingProto);
            case TypeProto.Encoding.ATTRIBUTE_TYPE:
                return AttributeImpl.of(thingProto);
            default:
                throw new GraknClientError(BAD_ENCODING.message(thingProto.getType().getEncoding()));
        }
    }
}