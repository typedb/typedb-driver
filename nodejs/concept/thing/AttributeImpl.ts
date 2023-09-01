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

import {Attribute as AttributeProto} from "typedb-protocol/proto/concept";
import {AttributeTypeImpl, ThingImpl} from "../../dependencies_internal";
import {AttributeType} from "../../api/concept/type/AttributeType";
import {Attribute} from "../../api/concept/thing/Attribute";
import {Bytes} from "../../common/util/Bytes";
import {Concept} from "../../api/concept/Concept";
import {RequestBuilder} from "../../common/rpc/RequestBuilder";
import {Stream} from "../../common/util/Stream";
import {ThingType} from "../../api/concept/type/ThingType";
import {Thing} from "../../api/concept/thing/Thing";
import {TypeDBTransaction} from "../../api/connection/TypeDBTransaction";
import {ValueImpl} from "../value/ValueImpl";
import {Value} from "../../api/concept/value/Value";
import ValueType = Concept.ValueType;

export class AttributeImpl extends ThingImpl implements Attribute {
    private readonly _type: AttributeType;

    private readonly _value: Value;

    constructor(iid: string, inferred: boolean, type: AttributeType, value: Value) {
        super(iid, inferred);
        this._type = type;
        this._value = value;
    }

    isAttribute(): boolean {
        return true;
    }

    asAttribute(): Attribute {
        return this;
    }

    protected get className(): string {
        return "Attribute";
    }

    get type(): AttributeType {
        return this._type;
    }

    get valueType(): ValueType {
        return this._type.valueType;
    }

    get value(): boolean | number | string | Date {
        return this._value.value;
    }

    async isDeleted(transaction: TypeDBTransaction): Promise<boolean> {
        return !(await transaction.concepts.getAttribute(this.iid));
    }

    getOwners(transaction: TypeDBTransaction, ownerType?: ThingType): Stream<Thing> {
        let request;
        if (!ownerType) {
            request = RequestBuilder.Thing.Attribute.getOwnersReq(this.iid);
        } else {
            request = RequestBuilder.Thing.Attribute.getOwnersReq(this.iid, ThingType.proto(ownerType));
        }
        return this.stream(transaction, request)
            .flatMap((resPart) => Stream.array(resPart.attribute_get_owners_res_part.things))
            .map((thingProto) => ThingImpl.ofThingProto(thingProto));
    }

    toJSONRecord(): Record<string, boolean | string | number> {
        return {
            type: this.type.label.name,
            value_type: this.type.valueType.name(),
            value: this._value.toJSONRecord()["value"]
        };
    }
}

export namespace AttributeImpl {
    export function ofAttributeProto(proto: AttributeProto): Attribute {
        if (!proto) return null;
        const iid = Bytes.bytesToHexString(proto.iid);
        return new AttributeImpl(
            iid,
            proto.inferred,
            AttributeTypeImpl.ofAttributeTypeProto(proto.attribute_type),
            ValueImpl.ofValueProto(proto.value),
        );
    }
}
