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
import {TypeDBDriverError} from "../../../common/errors/TypeDBDriverError";
import {ErrorMessage} from "../../../common/errors/ErrorMessage";
import BAD_ANNOTATION = ErrorMessage.Concept.BAD_ANNOTATION;
import ILLEGAL_STATE = ErrorMessage.Internal.ILLEGAL_STATE;
import Transitivity = Concept.Transitivity;
import Annotation = ThingType.Annotation;


export interface ThingType extends Type {
    /**
     * Retrieves the most immediate supertype of the <code>ThingType</code>.
     *
     * ### Examples
     *
     * ```ts
     * thingType.getSupertype(transaction)
     * ```
     *
     * @param transaction - The current transaction
     */
    getSupertype(transaction: TypeDBTransaction): Promise<ThingType>;

    /**
     * Retrieves all supertypes of the <code>ThingType</code>.
     *
     * ### Examples
     *
     * ```ts
     * thingType.getSupertypes(transaction)
     * ```
     *
     * @param transaction - The current transaction
     */
    getSupertypes(transaction: TypeDBTransaction): Stream<ThingType>;

    /**
     * Retrieves all direct and indirect subtypes of the <code>ThingType</code>.
     * Equivalent to getSubtypes(transaction, Transitivity.TRANSITIVE)
     *
     * ### Examples
     *
     * ```ts
     * thingType.getSubtypes(transaction)
     * ```
     *
     * @param transaction - The current transaction
     */
    getSubtypes(transaction: TypeDBTransaction): Stream<ThingType>;
    /**
     * Retrieves all direct and indirect (or direct only) subtypes of the <code>ThingType</code>.
     *
     * ### Examples
     *
     * ```ts
     * thingType.getSubtypes(transaction, Transitivity.EXPLICIT)
     * ```
     *
     * @param transaction - The current transaction
     * @param transitivity - <code>Transitivity.TRANSITIVE</code> for direct and indirect subtypes, <code>Transitivity.EXPLICIT</code> for direct subtypes only
     */
    getSubtypes(transaction: TypeDBTransaction, transitivity: Transitivity): Stream<ThingType>;


    /**
     * Retrieves all direct and indirect <code>Thing</code> objects that are instances of this <code>ThingType</code>.
     * Equivalent to getInstances(transaction, Transitivity.TRANSITIVE)
     *
     * ### Examples
     *
     * ```ts
     * thingType.getInstances(transaction)
     * ```
     *
     * @param transaction - The current transaction
     */
    getInstances(transaction: TypeDBTransaction): Stream<Thing>;
    /**
     * Retrieves all direct and indirect (or direct only) <code>Thing</code> objects that are instances of this <code>ThingType</code>.
     *
     * ### Examples
     *
     * ```ts
     * thingType.getInstances(transaction, Transitivity.EXPLICIT)
     * ```
     *
     * @param transaction - The current transaction
     * @param transitivity - <code>Transitivity.TRANSITIVE</code> for direct and indirect instances, <code>Transitivity.EXPLICIT</code> for direct instances only
     */
    getInstances(transaction: TypeDBTransaction, transitivity: Transitivity): Stream<Thing>;

    /**
     * Set a <code>ThingType</code> to be abstract, meaning it cannot have instances.
     *
     * ### Examples
     *
     * ```ts
     * thingType.setAbstract(transaction)
     * ```
     *
     * @param transaction - The current transaction
     */
    setAbstract(transaction: TypeDBTransaction): Promise<void>;
    /**
     * Set a <code>ThingType</code> to be non-abstract, meaning it can have instances.
     *
     * ### Examples
     *
     * ```ts
     * thingType.unsetAbstract(transaction)
     * ```
     *
     * @param transaction - The current transaction
     */
    unsetAbstract(transaction: TypeDBTransaction): Promise<void>;

    /** {@inheritDoc ThingType#getOwns:(7)} */
    getOwns(transaction: TypeDBTransaction): Stream<AttributeType>;
    /** {@inheritDoc ThingType#getOwns:(7)} */
    getOwns(transaction: TypeDBTransaction, valueType: Concept.ValueType): Stream<AttributeType>;
    /** {@inheritDoc ThingType#getOwns:(7)} */
    getOwns(transaction: TypeDBTransaction, annotations: Annotation[]): Stream<AttributeType>;
    /** {@inheritDoc ThingType#getOwns:(7)} */
    getOwns(transaction: TypeDBTransaction, valueType: Concept.ValueType, annotations: Annotation[]): Stream<AttributeType>;
    /** {@inheritDoc ThingType#getOwns:(7)} */
    getOwns(transaction: TypeDBTransaction, transitivity: Transitivity): Stream<AttributeType>;
    /** {@inheritDoc ThingType#getOwns:(7)} */
    getOwns(transaction: TypeDBTransaction, valueType: Concept.ValueType, transitivity: Transitivity): Stream<AttributeType>;
    /** {@inheritDoc ThingType#getOwns:(7)} */
    getOwns(transaction: TypeDBTransaction, annotations: Annotation[], transitivity: Transitivity): Stream<AttributeType>;
    /**
     * Retrieves <code>AttributeType</code> that the instances of this <code>ThingType</code> are allowed to own directly or via inheritance.
     *
     * ### Examples
     *
     * ```ts
     * thingType.getOwns(transaction)
     * thingType.getOwns(transaction, valueType, Transitivity.EXPLICIT,[Annotation.KEY])
     * ```
     *
     * @param transaction - The current transaction
     * @param valueType - If specified, only attribute types of this <code>ValueType</code> will be retrieved.
     * @param transitivity - <code>Transitivity.TRANSITIVE</code> for direct and inherited ownership, <code>Transitivity.EXPLICIT</code> for direct ownership only
     * @param annotations - Only retrieve attribute types owned with annotations.
     */
    getOwns(transaction: TypeDBTransaction, valueType: Concept.ValueType, annotations: Annotation[], transitivity: Transitivity): Stream<AttributeType>;

     /**
     * Retrieves an <code>AttributeType</code>, ownership of which is overridden for this <code>ThingType</code> by a given <code>attribute_type</code>.
     *
     * ### Examples
     *
     * ```ts
     * thingType.getOwnsOverridden(transaction, attributeType)
     * ```
     *
     * @param transaction - The current transaction
     * @param attributeType - The <code>AttributeType</code> that overrides requested <code>AttributeType</code>
     */
    getOwnsOverridden(transaction: TypeDBTransaction, attributeType: AttributeType): Promise<AttributeType>;

    /** {@inheritDoc ThingType#setOwns:(3)} */
    setOwns(transaction: TypeDBTransaction, attributeType: AttributeType): Promise<void>;
    /** {@inheritDoc ThingType#setOwns:(3)} */
    setOwns(transaction: TypeDBTransaction, attributeType: AttributeType, annotations: Annotation[]): Promise<void>;
    /** {@inheritDoc ThingType#setOwns:(3)} */
    setOwns(transaction: TypeDBTransaction, attributeType: AttributeType, overriddenType: AttributeType): Promise<void>;
    /**
     * Allows the instances of this <code>ThingType</code> to own the given <code>AttributeType</code>.
     *
     * ### Examples
     *
     * ```ts
     * thingType.setOwns(transaction, attributeType)
     * thingType.setOwns(transaction, attributeType, overriddenType,[Annotation.KEY])
     * ```
     *
     * @param transaction - The current transaction
     * @param attributeType - The <code>AttributeType</code> to be owned by the instances of this type.
     * @param overriddenType - The <code>AttributeType</code> that this attribute ownership overrides, if applicable.
     * @param annotations - Adds annotations to the ownership.
     */
    setOwns(transaction: TypeDBTransaction, attributeType: AttributeType, overriddenType: AttributeType, annotations: Annotation[]): Promise<void>;

    /**
     * Disallows the instances of this <code>ThingType</code> from owning the given <code>AttributeType</code>.
     *
     * ### Examples
     *
     * ```ts
     * thingType.unsetOwns(transaction, attributeType)
     * ```
     *
     * @param transaction - The current transaction
     * @param attributeType - The <code>AttributeType</code> to not be owned by the type.
     */
    unsetOwns(transaction: TypeDBTransaction, attributeType: AttributeType): Promise<void>;

    /** {@inheritDoc ThingType#getPlays:(1)} */
    getPlays(transaction: TypeDBTransaction): Stream<RoleType>;
    /**
     * Retrieves all direct and inherited (or direct only) roles that are allowed to be played by the instances of this <code>ThingType</code>.
     *
     * ### Examples
     *
     * ```ts
     * thingType.getPlays(transaction)
     * thingType.getPlays(transaction, Transitivity.EXPLICIT)
     * ```
     *
     * @param transaction - The current transaction
     * @param transitivity - <code>Transitivity.TRANSITIVE</code> for direct and indirect playing, <code>Transitivity.EXPLICIT</code> for direct playing only
     */
    getPlays(transaction: TypeDBTransaction, transitivity: Transitivity): Stream<RoleType>;

    /**
     * Retrieves a <code>RoleType</code> that is overridden by the given <code>role_type</code> for this <code>ThingType</code>.
     *
     * ### Examples
     *
     * ```ts
     * thingType.getPlaysOverridden(transaction, role)
     * ```
     *
     * @param transaction - The current transaction
     * @param role - The <code>RoleType</code> that overrides an inherited role
     */
    getPlaysOverridden(transaction: TypeDBTransaction, role: RoleType): Promise<RoleType>;

    /** {@inheritDoc ThingType#setPlays:(1)} */
    setPlays(transaction: TypeDBTransaction, role: RoleType): Promise<void>;
    /**
     * Allows the instances of this <code>ThingType</code> to play the given role.
     *
     * ### Examples
     *
     * ```ts
     * thingType.setPlays(transaction, role)
     * thingType.setPlays(transaction, role, overriddenType)
     * ```
     *
     * @param transaction - The current transaction
     * @param role - The role to be played by the instances of this type
     * @param overriddenType - The role type that this role overrides, if applicable
     */
    setPlays(transaction: TypeDBTransaction, role: RoleType, overriddenType: RoleType): Promise<void>;

    /**
     * Disallows the instances of this <code>ThingType</code> from playing the given role.
     *
     * ### Examples
     *
     * ```ts
     * thingType.unsetPlays(transaction, role)
     * ```
     *
     * @param transaction - The current transaction
     * @param role - The role to not be played by the instances of this type.
     */
    unsetPlays(transaction: TypeDBTransaction, role: RoleType): Promise<void>;

    /**
     * Produces a pattern for creating this <code>ThingType</code> in a <code>define</code> query.
     *
     * ### Examples
     *
     * ```ts
     * thingType.getSyntax(transaction)
     * ```
     *
     * @param transaction - The current transaction
     */
    getSyntax(transaction: TypeDBTransaction): Promise<string>;
}

export namespace ThingType {

    export const NAiME = "thing";

    export function proto(thing_type: ThingType) {
        if (thing_type.isEntity()) return RequestBuilder.Type.ThingType.protoThingTypeEntityType(thing_type.label);
        else if (thing_type.isRelation()) return RequestBuilder.Type.ThingType.protoThingTypeRelationType(thing_type.label);
        else if (thing_type.isAttribute()) return RequestBuilder.Type.ThingType.protoThingTypeAttributeType(thing_type.label);
        else throw new TypeDBDriverError(ILLEGAL_STATE.message());
    }

    /** Annotations for ownership declarations. */
    export class Annotation {
        /** Annotation to specify the attribute owned is a KEY */
        public static KEY = new Annotation("key");
        /** Annotation to specify the owned is UNIQUE */
        public static UNIQUE = new Annotation("unique");

        private readonly name: string;

        /** @ignore */
        private constructor(name: string) {
            this.name = name;
        }

        /**
        * Returns the relevant <code>Annotation</code> given the name as a string
        * @param string - name of the attribute as a string. e.g.: "key", "unique"
        */
        public static parse(string: string): Annotation {
            if (string == Annotation.KEY.name) return Annotation.KEY;
            else if (string == Annotation.UNIQUE.name) return Annotation.KEY;
            else throw new TypeDBDriverError(BAD_ANNOTATION.message(string));
        }

        /** Printable string */
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
                throw new TypeDBDriverError((BAD_ANNOTATION.message(annotation)));
            }
        }
    }
}
