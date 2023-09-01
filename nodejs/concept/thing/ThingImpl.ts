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

import {Concept} from "../../api/concept/Concept";
import {Attribute} from "../../api/concept/thing/Attribute";
import {Relation} from "../../api/concept/thing/Relation";
import {Thing} from "../../api/concept/thing/Thing";
import {AttributeType} from "../../api/concept/type/AttributeType";
import {RoleType} from "../../api/concept/type/RoleType";
import {ThingType} from "../../api/concept/type/ThingType";
import {TypeDBTransaction} from "../../api/connection/TypeDBTransaction";
import {ErrorMessage} from "../../common/errors/ErrorMessage";
import {TypeDBClientError} from "../../common/errors/TypeDBClientError";
import {RequestBuilder} from "../../common/rpc/RequestBuilder";
import {Stream} from "../../common/util/Stream";
import {
    AttributeImpl,
    AttributeTypeImpl,
    ConceptImpl,
    EntityImpl,
    RelationImpl,
    RoleTypeImpl,
} from "../../dependencies_internal";
import {
    AttributeType as AttributeTypeProto,
    Thing as ThingProto,
    ThingRes,
    ThingResPart,
    TypeAnnotation
} from "typedb-protocol/proto/concept";
import assert from "assert";
import {TransactionReq} from "typedb-protocol/proto/transaction";
import Annotation = ThingType.Annotation;
import BAD_ENCODING = ErrorMessage.Concept.BAD_ENCODING;

export abstract class ThingImpl extends ConceptImpl implements Thing {
    private readonly _iid: string;
    private readonly _inferred: boolean;

    protected constructor(iid: string, inferred: boolean) {
        super();
        if (!iid) throw new TypeDBClientError(ErrorMessage.Concept.MISSING_IID);
        this._iid = iid;
        this._inferred = inferred;
    }

    equals(concept: Concept): boolean {
        if (!concept.isThing()) return false;
        else return concept.asThing().iid === this._iid;
    }

    toString(): string {
        return `${this.className}[iid:${this._iid}]`;
    }

    get iid(): string {
        return this._iid;
    }

    abstract get type(): ThingType;

    get inferred(): boolean {
        return this._inferred;
    }

    isThing(): boolean {
        return true;
    }

    asThing(): Thing {
        return this;
    }

    toJSONRecord(): Record<string, boolean | string | number> {
        return {type: this.type.label.name};
    }

    async delete(transaction: TypeDBTransaction): Promise<void> {
        const request = RequestBuilder.Thing.deleteReq(this.iid);
        await this.execute(transaction, request);
    }

    abstract isDeleted(transaction: TypeDBTransaction): Promise<boolean>;

    getHas(transaction: TypeDBTransaction): Stream<Attribute>;
    getHas(transaction: TypeDBTransaction, annotations: Annotation[]): Stream<Attribute>;
    getHas(transaction: TypeDBTransaction, attributeType: AttributeType): Stream<Attribute>;
    getHas(transaction: TypeDBTransaction, attributeTypes: AttributeType[]): Stream<Attribute>;
    getHas(transaction: TypeDBTransaction, attributeTypes: AttributeType[], annotations: Annotation[]): Stream<Attribute>;
    getHas(
        transaction: TypeDBTransaction,
        annotationOrAttrTypeOrAttrTypes?: Annotation[] | AttributeType | AttributeType[],
        maybeAnnotations?: Annotation[],
    ): Stream<Attribute> {
        let attributeTypes: AttributeTypeProto[] = [];
        let annotations: TypeAnnotation[] = [];

        if (typeof annotationOrAttrTypeOrAttrTypes === "undefined") {
            assert(typeof maybeAnnotations === "undefined");
        } else if (Array.isArray(annotationOrAttrTypeOrAttrTypes)) {
            const asArray = annotationOrAttrTypeOrAttrTypes as Array<any>;
            if (asArray.length == 0 || asArray[0] instanceof AttributeTypeImpl) {
                attributeTypes = (annotationOrAttrTypeOrAttrTypes as AttributeType[]).map(attrType => AttributeType.proto(attrType));
            } else {
                assert(typeof maybeAnnotations === "undefined");
                maybeAnnotations = annotationOrAttrTypeOrAttrTypes as Annotation[];
            }
        } else {
            assert(annotationOrAttrTypeOrAttrTypes instanceof AttributeTypeImpl);
            assert(typeof maybeAnnotations === "undefined");
            attributeTypes = [AttributeType.proto(annotationOrAttrTypeOrAttrTypes)];
        }

        if (Array.isArray(maybeAnnotations)) {
            annotations = maybeAnnotations.map(annotation => Annotation.proto(annotation));
        }

        const request = RequestBuilder.Thing.getHasReq(this.iid, attributeTypes, annotations);
        return this.stream(transaction, request).flatMap(
            (resPart) => Stream.array(resPart.thing_get_has_res_part.attributes)
        ).map(AttributeImpl.ofAttributeProto);
    }

    async setHas(transaction: TypeDBTransaction, attribute: Attribute): Promise<void> {
        const request = RequestBuilder.Thing.setHasReq(this.iid, Attribute.proto(attribute));
        await this.execute(transaction, request);
    }

    async unsetHas(transaction: TypeDBTransaction, attribute: Attribute): Promise<void> {
        const request = RequestBuilder.Thing.unsetHasReq(this.iid, Attribute.proto(attribute));
        await this.execute(transaction, request);
    }

    getPlaying(transaction: TypeDBTransaction): Stream<RoleType> {
        const request = RequestBuilder.Thing.getPlayingReq(this.iid);
        return this.stream(transaction, request)
            .flatMap((resPart) => Stream.array(resPart.thing_get_playing_res_part.role_types))
            .map((res) => RoleTypeImpl.ofRoleTypeProto(res));
    }

    getRelations(transaction: TypeDBTransaction, roleTypes?: RoleType[]): Stream<Relation> {
        if (!roleTypes) roleTypes = [];
        const request = RequestBuilder.Thing.getRelationsReq(this.iid, roleTypes.map((roleType) => RoleType.proto(roleType)));
        return this.stream(transaction, request)
            .flatMap((resPart) => Stream.array(resPart.thing_get_relations_res_part.relations))
            .map((res) => RelationImpl.ofRelationProto(res));
    }

    protected async execute(transaction: TypeDBTransaction, request: TransactionReq): Promise<ThingRes> {
        const ext = transaction as TypeDBTransaction.Extended;
        return (await ext.rpcExecute(request, false)).thing_res;
    }

    protected stream(transaction: TypeDBTransaction, request: TransactionReq): Stream<ThingResPart> {
        const ext = transaction as TypeDBTransaction.Extended;
        return ext.rpcStream(request).map((res) => res.thing_res_part);
    }
}

export namespace ThingImpl {
    export function ofThingProto(proto: ThingProto): Thing {
        if (proto.has_entity) return EntityImpl.ofEntityProto(proto.entity);
        else if (proto.has_relation) return RelationImpl.ofRelationProto(proto.relation);
        else if (proto.has_attribute) return AttributeImpl.ofAttributeProto(proto.attribute);
        else throw new TypeDBClientError(BAD_ENCODING.message(proto));
    }
}
