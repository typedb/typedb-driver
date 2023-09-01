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

import {TypeAnnotation} from "typedb-protocol/proto/concept";
import {RequestBuilder} from "../../../common/rpc/RequestBuilder";
import {Stream} from "../../../common/util/Stream";
import {TypeDBTransaction} from "../../connection/TypeDBTransaction";
import {Concept} from "../Concept";
import {Thing} from "../thing/Thing";
import {AttributeType} from "./AttributeType";
import {RoleType} from "./RoleType";
import {Type} from "./Type";
import {TypeDBClientError} from "../../../common/errors/TypeDBClientError";
import {ErrorMessage} from "../../../common/errors/ErrorMessage";
import BAD_ANNOTATION = ErrorMessage.Concept.BAD_ANNOTATION;
import ILLEGAL_STATE = ErrorMessage.Internal.ILLEGAL_STATE;
import Transitivity = Concept.Transitivity;
import Annotation = ThingType.Annotation;


export interface ThingType extends Type {
    getSupertype(transaction: TypeDBTransaction): Promise<ThingType>;

    getSupertypes(transaction: TypeDBTransaction): Stream<ThingType>;

    getSubtypes(transaction: TypeDBTransaction): Stream<ThingType>;
    getSubtypes(transaction: TypeDBTransaction, transitivity: Transitivity): Stream<ThingType>;

    getInstances(transaction: TypeDBTransaction): Stream<Thing>;
    getInstances(transaction: TypeDBTransaction, transitivity: Transitivity): Stream<Thing>;

    setAbstract(transaction: TypeDBTransaction): Promise<void>;
    unsetAbstract(transaction: TypeDBTransaction): Promise<void>;

    getOwns(transaction: TypeDBTransaction): Stream<AttributeType>;
    getOwns(transaction: TypeDBTransaction, valueType: Concept.ValueType): Stream<AttributeType>;
    getOwns(transaction: TypeDBTransaction, annotations: Annotation[]): Stream<AttributeType>;
    getOwns(transaction: TypeDBTransaction, valueType: Concept.ValueType, annotations: Annotation[]): Stream<AttributeType>;
    getOwns(transaction: TypeDBTransaction, transitivity: Transitivity): Stream<AttributeType>;
    getOwns(transaction: TypeDBTransaction, valueType: Concept.ValueType, transitivity: Transitivity): Stream<AttributeType>;
    getOwns(transaction: TypeDBTransaction, annotations: Annotation[], transitivity: Transitivity): Stream<AttributeType>;
    getOwns(transaction: TypeDBTransaction, valueType: Concept.ValueType, annotations: Annotation[], transitivity: Transitivity): Stream<AttributeType>;

    getOwnsOverridden(transaction: TypeDBTransaction, attributeType: AttributeType): Promise<AttributeType>;

    setOwns(transaction: TypeDBTransaction, attributeType: AttributeType): Promise<void>;
    setOwns(transaction: TypeDBTransaction, attributeType: AttributeType, annotations: Annotation[]): Promise<void>;
    setOwns(transaction: TypeDBTransaction, attributeType: AttributeType, overriddenType: AttributeType): Promise<void>;
    setOwns(transaction: TypeDBTransaction, attributeType: AttributeType, overriddenType: AttributeType, annotations: Annotation[]): Promise<void>;

    unsetOwns(transaction: TypeDBTransaction, attributeType: AttributeType): Promise<void>;

    getPlays(transaction: TypeDBTransaction): Stream<RoleType>;
    getPlays(transaction: TypeDBTransaction, transitivity: Transitivity): Stream<RoleType>;

    getPlaysOverridden(transaction: TypeDBTransaction, role: RoleType): Promise<RoleType>;

    setPlays(transaction: TypeDBTransaction, role: RoleType): Promise<void>;
    setPlays(transaction: TypeDBTransaction, role: RoleType, overriddenType: RoleType): Promise<void>;

    unsetPlays(transaction: TypeDBTransaction, role: RoleType): Promise<void>;

    getSyntax(transaction: TypeDBTransaction): Promise<string>;
}

export namespace ThingType {
    export function proto(thing_type: ThingType) {
        if (thing_type.isEntity()) return RequestBuilder.Type.ThingType.protoThingTypeEntityType(thing_type.label);
        else if (thing_type.isRelation()) return RequestBuilder.Type.ThingType.protoThingTypeRelationType(thing_type.label);
        else if (thing_type.isAttribute()) return RequestBuilder.Type.ThingType.protoThingTypeAttributeType(thing_type.label);
        else throw new TypeDBClientError(ILLEGAL_STATE.message());
    }

    export class Annotation {
        public static KEY = new Annotation("key");
        public static UNIQUE = new Annotation("unique");

        private readonly name: string;

        private constructor(name: string) {
            this.name = name;
        }

        public static parse(string: string): Annotation {
            if (string == Annotation.KEY.name) return Annotation.KEY;
            else if (string == Annotation.UNIQUE.name) return Annotation.KEY;
            else throw new TypeDBClientError(BAD_ANNOTATION.message(string));
        }

        public toString(): string {
            return "[annotation: " + this.name + "]";
        }
    }

    export namespace Annotation {
        export function proto(annotation: Annotation): TypeAnnotation {
            if (annotation == Annotation.KEY) {
                return RequestBuilder.Type.Annotation.annotationKey();
            } else if (annotation == Annotation.UNIQUE) {
                return RequestBuilder.Type.Annotation.annotationUnique();
            } else {
                throw new TypeDBClientError((BAD_ANNOTATION.message(annotation)));
            }
        }
    }
}
