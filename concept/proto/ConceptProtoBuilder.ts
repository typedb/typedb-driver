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

// TODO: Can we get rid of this?

import ConceptProto from "grakn-protocol/protobuf/concept_pb";
import {
    Type,
    Thing,
    AttributeType,
    RoleTypeImpl,
    EntityImpl,
    RelationImpl,
    AttributeImpl,
    EntityTypeImpl,
    RelationTypeImpl,
    AttributeTypeImpl,
    ThingTypeImpl, GraknClientError, ErrorMessage, Bytes,
} from "../../dependencies_internal";

export namespace ConceptProtoBuilder {

    export function thing(thing: Thing): ConceptProto.Thing {
        return new ConceptProto.Thing()
            .setIid(Bytes.hexStringToBytes(thing.getIID()))
            .setEncoding(thingEncoding(thing));
    }

    export function type(type: Type): ConceptProto.Type {
        const typeProto = new ConceptProto.Type()
            .setLabel(type.getLabel())
            .setEncoding(typeEncoding(type));

        if (type instanceof RoleTypeImpl) {
            typeProto.setScope(type.getScope());
        }

        return typeProto;
    }

    export function types(types: Type[]): ConceptProto.Type[] {
        return types.map(type);
    }

    // The 'attributeValue' functions are split up like this to avoid ambiguity between Long and Double
    export function booleanAttributeValue(value: boolean): ConceptProto.Attribute.Value {
        return new ConceptProto.Attribute.Value().setBoolean(value);
    }

    export function longAttributeValue(value: number): ConceptProto.Attribute.Value {
        return new ConceptProto.Attribute.Value().setLong(value);
    }

    export function doubleAttributeValue(value: number): ConceptProto.Attribute.Value {
        return new ConceptProto.Attribute.Value().setDouble(value);
    }

    export function stringAttributeValue(value: string): ConceptProto.Attribute.Value {
        return new ConceptProto.Attribute.Value().setString(value);
    }

    export function dateTimeAttributeValue(value: Date): ConceptProto.Attribute.Value {
        return new ConceptProto.Attribute.Value().setDateTime(value.getTime());
    }

    export function valueType(valueType: AttributeType.ValueType): ConceptProto.AttributeType.ValueType {
        switch (valueType) {
            case AttributeType.ValueType.OBJECT:
                return ConceptProto.AttributeType.ValueType.OBJECT;
            case AttributeType.ValueType.BOOLEAN:
                return ConceptProto.AttributeType.ValueType.BOOLEAN;
            case AttributeType.ValueType.LONG:
                return ConceptProto.AttributeType.ValueType.LONG;
            case AttributeType.ValueType.DOUBLE:
                return ConceptProto.AttributeType.ValueType.DOUBLE;
            case AttributeType.ValueType.STRING:
                return ConceptProto.AttributeType.ValueType.STRING;
            case AttributeType.ValueType.DATETIME:
                return ConceptProto.AttributeType.ValueType.DATETIME;
        }
    }

    export function thingEncoding(thing: Thing): ConceptProto.Thing.Encoding {
        if (thing instanceof EntityImpl) {
            return ConceptProto.Thing.Encoding.ENTITY;
        } else if (thing instanceof RelationImpl) {
            return ConceptProto.Thing.Encoding.RELATION;
        } else if (thing instanceof AttributeImpl) {
            return ConceptProto.Thing.Encoding.ATTRIBUTE;
        } else {
            throw new GraknClientError(ErrorMessage.Concept.BAD_ENCODING.message(thing))
        }
    }

    export function typeEncoding(type: Type): ConceptProto.Type.Encoding {
        if (type instanceof EntityTypeImpl) {
            return ConceptProto.Type.Encoding.ENTITY_TYPE;
        } else if (type instanceof RelationTypeImpl) {
            return ConceptProto.Type.Encoding.RELATION_TYPE;
        } else if (type instanceof AttributeTypeImpl) {
            return ConceptProto.Type.Encoding.ATTRIBUTE_TYPE;
        } else if (type instanceof RoleTypeImpl) {
            return ConceptProto.Type.Encoding.ROLE_TYPE;
        } else if (type instanceof ThingTypeImpl) {
            return ConceptProto.Type.Encoding.THING_TYPE;
        } else {
            throw new GraknClientError(ErrorMessage.Concept.BAD_ENCODING.message(type))
        }
    }
}
