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

import {AttributeType} from "../../api/concept/type/AttributeType";
import {RoleType} from "../../api/concept/type/RoleType";
import {ThingType} from "../../api/concept/type/ThingType";
import {TypeDBTransaction} from "../../api/connection/TypeDBTransaction";
import {ErrorMessage} from "../../common/errors/ErrorMessage";
import {TypeDBClientError} from "../../common/errors/TypeDBClientError";
import {Label} from "../../common/Label";
import {RequestBuilder} from "../../common/rpc/RequestBuilder";
import {Stream} from "../../common/util/Stream";
import {AttributeTypeImpl, EntityTypeImpl, RelationTypeImpl, RoleTypeImpl, TypeImpl} from "../../dependencies_internal";
import {Concept} from "../../api/concept/Concept";
import {
    AttributeType as AttributeTypeProto,
    ThingType as ThingTypeProto,
    ThingTypeRes,
    ThingTypeResPart,
    ThingTypeRoot as ThingTypeRootProto,
    TypeAnnotation,
    TypeTransitivity,
    ValueType as ValueTypeProto,
} from "typedb-protocol/proto/concept";
import {TransactionReq} from "typedb-protocol/proto/transaction";
import assert from "assert";
import {Thing} from "../../api/concept/thing/Thing";
import BAD_ENCODING = ErrorMessage.Concept.BAD_ENCODING;
import Annotation = ThingType.Annotation;
import Transitivity = Concept.Transitivity;
import ValueType = Concept.ValueType;

export abstract class ThingTypeImpl extends TypeImpl implements ThingType {
    protected constructor(name: string, root: boolean, abstract: boolean) {
        super(Label.of(name), root, abstract);
    }

    protected get className(): string {
        return "ThingType";
    }

    isThingType(): boolean {
        return true;
    }

    asThingType(): ThingType {
        return this;
    }

    abstract getSupertype(transaction: TypeDBTransaction): Promise<ThingType>;

    abstract getSupertypes(transaction: TypeDBTransaction): Stream<ThingType>;

    abstract getSubtypes(transaction: TypeDBTransaction): Stream<ThingType>;
    abstract getSubtypes(transaction: TypeDBTransaction, transitivity: Transitivity): Stream<ThingType>;

    abstract getInstances(transaction: TypeDBTransaction): Stream<Thing>;
    abstract getInstances(transaction: TypeDBTransaction, transitivity: Transitivity): Stream<Thing>;

    async delete(transaction: TypeDBTransaction): Promise<void> {
        await this.execute(transaction, RequestBuilder.Type.ThingType.deleteReq(this.label));
    }

    async setLabel(transaction: TypeDBTransaction, newLabel: string): Promise<void> {
        await this.execute(transaction, RequestBuilder.Type.ThingType.setLabelReq(this.label, newLabel));
    }

    async setAbstract(transaction: TypeDBTransaction): Promise<void> {
        await this.execute(transaction, RequestBuilder.Type.ThingType.setAbstractReq(this.label));
    }

    async unsetAbstract(transaction: TypeDBTransaction): Promise<void> {
        await this.execute(transaction, RequestBuilder.Type.ThingType.unsetAbstractReq(this.label));
    }

    getOwns(transaction: TypeDBTransaction): Stream<AttributeType>;
    getOwns(transaction: TypeDBTransaction, valueType: Concept.ValueType): Stream<AttributeType>;
    getOwns(transaction: TypeDBTransaction, annotations: Annotation[]): Stream<AttributeType>;
    getOwns(transaction: TypeDBTransaction, valueType: Concept.ValueType, annotations: Annotation[]): Stream<AttributeType>;
    getOwns(transaction: TypeDBTransaction, transitivity: Transitivity): Stream<AttributeType>;
    getOwns(transaction: TypeDBTransaction, valueType: Concept.ValueType, transitivity: Transitivity): Stream<AttributeType>;
    getOwns(transaction: TypeDBTransaction, annotations: Annotation[], transitivity: Transitivity): Stream<AttributeType>;
    getOwns(transaction: TypeDBTransaction, valueType: Concept.ValueType, annotations: Annotation[], transitivity: Transitivity): Stream<AttributeType>
    getOwns(
        transaction: TypeDBTransaction,
        valueTypeOrAnnotationsOrTransitivity?: ValueType | Annotation[] | Transitivity,
        annotationsOrTransitivity?: Annotation[] | Transitivity,
        maybeTransitivity?: Transitivity,
    ): Stream<AttributeType> {
        let valueType: ValueTypeProto;
        let annotations: TypeAnnotation[] = [];
        let transitivity: TypeTransitivity;

        if (valueTypeOrAnnotationsOrTransitivity instanceof ValueType) {
            valueType = valueTypeOrAnnotationsOrTransitivity.proto();

            valueTypeOrAnnotationsOrTransitivity = annotationsOrTransitivity;
            annotationsOrTransitivity = maybeTransitivity;
            maybeTransitivity = undefined;
        }

        if (Array.isArray(valueTypeOrAnnotationsOrTransitivity)) {
            assert(typeof maybeTransitivity === "undefined");

            annotations = (valueTypeOrAnnotationsOrTransitivity as Annotation[]).map(Annotation.proto);

            valueTypeOrAnnotationsOrTransitivity = annotationsOrTransitivity;
            annotationsOrTransitivity = undefined;
        }

        if (valueTypeOrAnnotationsOrTransitivity instanceof Transitivity) {
            assert(typeof annotationsOrTransitivity === "undefined");
            assert(typeof maybeTransitivity === "undefined");

            transitivity = valueTypeOrAnnotationsOrTransitivity.proto();
        }

        const request = RequestBuilder.Type.ThingType.getOwnsReq(this.label, valueType, annotations, transitivity);
        return this.stream(transaction, request).flatMap(
            resPart => Stream.array(resPart.thing_type_get_owns_res_part.attribute_types)
        ).map(AttributeTypeImpl.ofAttributeTypeProto);
    }

    async getOwnsOverridden(transaction: TypeDBTransaction, attributeType: AttributeType): Promise<AttributeType> {
        const res = await this.execute(transaction, RequestBuilder.Type.ThingType.getOwnsOverriddenReq(this.label, AttributeType.proto(attributeType)));
        return AttributeTypeImpl.ofAttributeTypeProto(res.thing_type_get_owns_overridden_res.attribute_type);
    }

    setOwns(transaction: TypeDBTransaction, attributeType: AttributeType): Promise<void>;
    setOwns(transaction: TypeDBTransaction, attributeType: AttributeType, annotations: Annotation[]): Promise<void>;
    setOwns(transaction: TypeDBTransaction, attributeType: AttributeType, overriddenType: AttributeType): Promise<void>;
    setOwns(transaction: TypeDBTransaction, attributeType: AttributeType, overriddenType: AttributeType, annotations: Annotation[]): Promise<void>;
    async setOwns(
        transaction: TypeDBTransaction,
        attributeType: AttributeType,
        overriddenTypeOrAnnotations?: AttributeType | Annotation[],
        maybeAnnotations?: Annotation[],
    ): Promise<void> {
        let overriddenType: AttributeTypeProto = null;
        let annotations: TypeAnnotation[] = [];

        if (typeof overriddenTypeOrAnnotations === "undefined") {
            assert(typeof maybeAnnotations === "undefined");
        } else {
            if (Array.isArray(overriddenTypeOrAnnotations)) {
                assert(typeof maybeAnnotations === "undefined");
                annotations = overriddenTypeOrAnnotations.map(Annotation.proto);
            } else {
                overriddenType = AttributeType.proto(overriddenTypeOrAnnotations);
                if (Array.isArray(maybeAnnotations)) {
                    annotations = maybeAnnotations.map(Annotation.proto);
                }
            }
        }

        await this.execute(transaction, RequestBuilder.Type.ThingType.setOwnsReq(
            this.label,
            AttributeType.proto(attributeType),
            overriddenType,
            annotations,
        ));
    }

    async unsetOwns(transaction: TypeDBTransaction, attributeType: AttributeType): Promise<void> {
        await this.execute(transaction, RequestBuilder.Type.ThingType.unsetOwnsReq(this.label, AttributeType.proto(attributeType)));
    }

    getPlays(transaction: TypeDBTransaction): Stream<RoleType>;
    getPlays(transaction: TypeDBTransaction, transitivity: Transitivity): Stream<RoleType>;
    getPlays(transaction: TypeDBTransaction, transitivity?: Transitivity): Stream<RoleType> {
        if (!transitivity) transitivity = Transitivity.TRANSITIVE;
        return this.stream(transaction, RequestBuilder.Type.ThingType.getPlaysReq(this.label, transitivity.proto())).flatMap(
            resPart => Stream.array(resPart.thing_type_get_plays_res_part.role_types)
        ).map(RoleTypeImpl.ofRoleTypeProto);
    }

    async getPlaysOverridden(transaction: TypeDBTransaction, role: RoleType): Promise<RoleType> {
        const res = await this.execute(transaction, RequestBuilder.Type.ThingType.getPlaysOverriddenReq(this.label, RoleType.proto(role)));
        return RoleTypeImpl.ofRoleTypeProto(res.thing_type_get_plays_overridden_res.role_type);
    }

    setPlays(transaction: TypeDBTransaction, role: RoleType): Promise<void>;
    setPlays(transaction: TypeDBTransaction, role: RoleType, overriddenRole: RoleType): Promise<void>;
    async setPlays(transaction: TypeDBTransaction, role: RoleType, overriddenRole?: RoleType): Promise<void> {
        let overriddenProto;
        if (typeof overriddenRole !== "undefined") overriddenProto = RoleType.proto(overriddenRole);
        await this.execute(transaction, RequestBuilder.Type.ThingType.setPlaysReq(this.label, RoleType.proto(role), overriddenProto));
    }

    async unsetPlays(transaction: TypeDBTransaction, role: RoleType): Promise<void> {
        await this.execute(transaction, RequestBuilder.Type.ThingType.unsetPlaysReq(this.label, RoleType.proto(role)));
    }

    async getSyntax(transaction: TypeDBTransaction): Promise<string> {
        const res = await this.execute(transaction, RequestBuilder.Type.ThingType.getSyntaxReq(this.label));
        return res.thing_type_get_syntax_res.syntax;
    }

    protected async execute(transaction: TypeDBTransaction, request: TransactionReq): Promise<ThingTypeRes> {
        const ext = transaction as TypeDBTransaction.Extended;
        return (await ext.rpcExecute(request, false)).type_res.thing_type_res;
    }

    protected stream(transaction: TypeDBTransaction, request: TransactionReq): Stream<ThingTypeResPart> {
        const ext = transaction as TypeDBTransaction.Extended;
        return ext.rpcStream(request).map((res) => res.type_res_part.thing_type_res_part);
    }
}

export namespace ThingTypeImpl {
    export class Root extends ThingTypeImpl {
        constructor() {
            super("thing", true, true);
        }

        async isDeleted(transaction: TypeDBTransaction): Promise<boolean> {
            return false;
        }

        async getSupertype(transaction: TypeDBTransaction): Promise<ThingType> {
            return null;
        }

        getSupertypes(transaction: TypeDBTransaction): Stream<ThingType> {
            return Stream.array([this])
        }

        getSubtypes(transaction: TypeDBTransaction): Stream<ThingType>;
        getSubtypes(transaction: TypeDBTransaction, transitivity: Transitivity): Stream<ThingType>;
        getSubtypes(transaction: TypeDBTransaction, transitivity?: Transitivity): Stream<ThingType> {
            if (!transitivity) transitivity = Transitivity.TRANSITIVE;
            const roots: Stream<ThingType> = Stream.promises([
                transaction.concepts.getRootEntityType() as Promise<ThingType>,
                transaction.concepts.getRootRelationType() as Promise<ThingType>,
                transaction.concepts.getRootAttributeType() as Promise<ThingType>,
            ]);
            if (transitivity == Transitivity.EXPLICIT) return roots;
            else return roots.flatMap(tt => tt.getSubtypes(transaction, transitivity).map(t => t as ThingType));
        }

        getInstances(transaction: TypeDBTransaction): Stream<Thing>;
        getInstances(transaction: TypeDBTransaction, transitivity: Transitivity): Stream<Thing>;
        getInstances(transaction: TypeDBTransaction, transitivity?: Transitivity): Stream<Thing> {
            if (!transitivity) transitivity = Transitivity.TRANSITIVE;
            if (transitivity == Transitivity.EXPLICIT) return Stream.array([]);
            else {
                const roots: Stream<ThingType> = Stream.promises([
                    transaction.concepts.getRootEntityType() as Promise<ThingType>,
                    transaction.concepts.getRootRelationType() as Promise<ThingType>,
                    transaction.concepts.getRootAttributeType() as Promise<ThingType>,
                ]);
                return roots.flatMap(tt => tt.getInstances(transaction, transitivity).map(t => t as Thing));
            }
        }
    }

    export namespace Root {
        export function ofThingTypeRootProto(_: ThingTypeRootProto): Root {
            return new Root();
        }
    }

    export function ofThingTypeProto(proto: ThingTypeProto): ThingType {
        if (proto.has_entity_type) return EntityTypeImpl.ofEntityTypeProto(proto.entity_type);
        else if (proto.has_relation_type) return RelationTypeImpl.ofRelationTypeProto(proto.relation_type);
        else if (proto.has_attribute_type) return AttributeTypeImpl.ofAttributeTypeProto(proto.attribute_type);
        else if (proto.has_thing_type_root) return ThingTypeImpl.Root.ofThingTypeRootProto(proto.thing_type_root);
        else throw new TypeDBClientError(BAD_ENCODING.message(proto));
    }
}
