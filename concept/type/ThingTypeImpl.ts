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

import { Type as TypeProto } from "typedb-protocol/common/concept_pb";
import { Thing } from "../../api/concept/thing/Thing";
import { AttributeType } from "../../api/concept/type/AttributeType";
import { RoleType } from "../../api/concept/type/RoleType";
import { ThingType } from "../../api/concept/type/ThingType";
import { TypeDBTransaction } from "../../api/connection/TypeDBTransaction";
import { ErrorMessage } from "../../common/errors/ErrorMessage";
import { TypeDBClientError } from "../../common/errors/TypeDBClientError";
import { Label } from "../../common/Label";
import { RequestBuilder } from "../../common/rpc/RequestBuilder";
import { Stream } from "../../common/util/Stream";
import { AttributeTypeImpl, EntityTypeImpl, RelationTypeImpl, RoleTypeImpl, ThingImpl, TypeImpl } from "../../dependencies_internal";
import BAD_ENCODING = ErrorMessage.Concept.BAD_ENCODING;

export class ThingTypeImpl extends TypeImpl implements ThingType {

    constructor(name: string, isRoot: boolean) {
        super(Label.of(name), isRoot);
    }

    protected get className(): string {
        return "ThingType";
    }

    asRemote(transaction: TypeDBTransaction): ThingType.Remote {
        return new ThingTypeImpl.Remote(transaction as TypeDBTransaction.Extended, this.getLabel(), this.isRoot());
    }

    isThingType(): boolean {
        return true;
    }

    asThingType(): ThingType {
        return this;
    }
}

export namespace ThingTypeImpl {

    export function of(thingTypeProto: TypeProto) {
        if (!thingTypeProto) return null;
        switch (thingTypeProto.getEncoding()) {
            case TypeProto.Encoding.ENTITY_TYPE:
                return EntityTypeImpl.of(thingTypeProto);
            case TypeProto.Encoding.RELATION_TYPE:
                return RelationTypeImpl.of(thingTypeProto);
            case TypeProto.Encoding.ATTRIBUTE_TYPE:
                return AttributeTypeImpl.of(thingTypeProto);
            case TypeProto.Encoding.THING_TYPE:
                return new ThingTypeImpl(thingTypeProto.getLabel(), thingTypeProto.getRoot());
            default:
                throw new TypeDBClientError(BAD_ENCODING.message(thingTypeProto.getEncoding()));
        }
    }

    export class Remote extends TypeImpl.Remote implements ThingType.Remote {

        constructor(transaction: TypeDBTransaction.Extended, label: Label, isRoot: boolean) {
            super(transaction, label, isRoot);
        }

        protected get className(): string {
            return "ThingType";
        }

        asRemote(transaction: TypeDBTransaction): ThingType.Remote {
            return new ThingTypeImpl.Remote(transaction as TypeDBTransaction.Extended, this.getLabel(), this.isRoot());
        }

        isThingType(): boolean {
            return true;
        }

        asThingType(): ThingType.Remote {
            return this;
        }

        getSubtypes(): Stream<ThingType> {
            return super.getSubtypes() as Stream<ThingType>;
        }

        getSupertype(): Promise<ThingType> {
            return super.getSupertype() as Promise<ThingType>;
        }

        getSupertypes(): Stream<ThingType> {
            return super.getSupertypes() as Stream<ThingType>;
        }

        getInstances(): Stream<Thing> {
            const request = RequestBuilder.Type.ThingType.getInstancesReq(this.getLabel());
            return this.stream(request)
                .flatMap((resPart) => Stream.array(resPart.getThingTypeGetInstancesResPart().getThingsList()))
                .map((thingProto) => ThingImpl.of(thingProto));
        }

        getOwns(): Stream<AttributeType>;
        getOwns(valueType: AttributeType.ValueType): Stream<AttributeType>;
        getOwns(keysOnly: boolean): Stream<AttributeType>;
        getOwns(valueType: AttributeType.ValueType, keysOnly: boolean): Stream<AttributeType>;
        getOwns(valueTypeOrKeysOnly?: AttributeType.ValueType | boolean, keysOnly?: boolean): Stream<AttributeType> {
            let request;
            if (!valueTypeOrKeysOnly) {
                request = RequestBuilder.Type.ThingType.getOwnsReq(this.getLabel(), false);
            } else if (typeof valueTypeOrKeysOnly === "boolean") {
                request = RequestBuilder.Type.ThingType.getOwnsReq(this.getLabel(), valueTypeOrKeysOnly as boolean)
            } else if (!keysOnly) {
                request = RequestBuilder.Type.ThingType.getOwnsByTypeReq(
                    this.getLabel(), (valueTypeOrKeysOnly as AttributeType.ValueType).proto(), false
                );
            } else {
                request = RequestBuilder.Type.ThingType.getOwnsByTypeReq(
                    this.getLabel(), (valueTypeOrKeysOnly as AttributeType.ValueType).proto(), keysOnly
                );
            }
            return this.stream(request)
                .flatMap((resPart) => Stream.array(resPart.getThingTypeGetOwnsResPart().getAttributeTypesList()))
                .map((attributeTypeProto) => AttributeTypeImpl.of(attributeTypeProto));
        }

        async setOwns(attributeType: AttributeType): Promise<void>;
        async setOwns(attributeType: AttributeType, isKey: boolean): Promise<void>;
        async setOwns(attributeType: AttributeType, overriddenType: AttributeType): Promise<void>;
        async setOwns(attributeType: AttributeType, overriddenTypeOrIsKey?: AttributeType | boolean, isKey?: boolean): Promise<void> {
            let request;
            if (!overriddenTypeOrIsKey) {
                request = RequestBuilder.Type.ThingType.setOwnsReq(this.getLabel(), ThingType.proto(attributeType), false);
            } else if (typeof overriddenTypeOrIsKey === "boolean") {
                request = RequestBuilder.Type.ThingType.setOwnsReq(this.getLabel(), ThingType.proto(attributeType), overriddenTypeOrIsKey as boolean)
            } else if (!isKey) {
                request = RequestBuilder.Type.ThingType.setOwnsOverriddenReq(
                    this.getLabel(), ThingType.proto(attributeType), ThingType.proto((overriddenTypeOrIsKey as AttributeType)), false
                );
            } else {
                request = RequestBuilder.Type.ThingType.setOwnsOverriddenReq(
                    this.getLabel(), ThingType.proto(attributeType), ThingType.proto(overriddenTypeOrIsKey as AttributeType), isKey
                );
            }
            await this.execute(request);
        }

        async unsetOwns(attributeType: AttributeType): Promise<void> {
            const request = RequestBuilder.Type.ThingType.unsetOwnsReq(this.getLabel(), ThingType.proto(attributeType));
            await this.execute(request);
        }

        getPlays(): Stream<RoleType> {
            const request = RequestBuilder.Type.ThingType.getPlaysReq(this.getLabel());
            return this.stream(request)
                .flatMap((resPart) => Stream.array(resPart.getThingTypeGetPlaysResPart().getRolesList()))
                .map((roleProto) => RoleTypeImpl.of(roleProto));
        }

        async setPlays(role: RoleType): Promise<void>;
        async setPlays(role: RoleType, overriddenType?: RoleType): Promise<void> {
            let request;
            if (!overriddenType) {
                request = RequestBuilder.Type.ThingType.setPlaysReq(this.getLabel(), RoleType.proto(role));
            } else {
                request = RequestBuilder.Type.ThingType.setPlaysOverriddenReq(this.getLabel(), RoleType.proto(role), RoleType.proto(overriddenType));
            }
            await this.execute(request);
        }

        async unsetPlays(role: RoleType): Promise<void> {
            const request = RequestBuilder.Type.ThingType.unsetPlaysReq(this.getLabel(), RoleType.proto(role));
            await this.execute(request);
        }

        async setAbstract(): Promise<void> {
            const request = RequestBuilder.Type.ThingType.setAbstractReq(this.getLabel());
            await this.execute(request);
        }

        async unsetAbstract(): Promise<void> {
            const request = RequestBuilder.Type.ThingType.unsetAbstractReq(this.getLabel());
            await this.execute(request);
        }

        async isDeleted(): Promise<boolean> {
            return (await this.transaction.concepts().getThingType(this.getLabel().name())) != null;
        }

        protected async setSupertype(thingType: ThingType): Promise<void> {
            const request = RequestBuilder.Type.ThingType.setSupertypeReq(this.getLabel(), ThingType.proto(thingType));
            await this.execute(request);
        }
    }
}
