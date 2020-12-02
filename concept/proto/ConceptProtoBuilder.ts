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

import ConceptProto from "graknlabs-grpc-protocol/protobuf/concept_pb";
import {
    Type,
    Thing,
    AttributeType,
    RoleTypeImpl,
    EntityImpl ,
    RelationImpl,
    AttributeImpl,
    EntityTypeImpl ,
    RelationTypeImpl ,
    AttributeTypeImpl ,
    ThingTypeImpl,
} from "../../dependencies_internal";

export namespace ConceptProtoBuilder {

    export function thing(thing: Thing): ConceptProto.Thing {
        return new ConceptProto.Thing()
            .setIid(thing.getIID())
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

    export function valueType(valueType: AttributeType.ValueType): ConceptProto.AttributeType.VALUE_TYPE {
        switch (valueType) {
            case AttributeType.ValueType.OBJECT:
                return ConceptProto.AttributeType.VALUE_TYPE.OBJECT;
            case AttributeType.ValueType.BOOLEAN:
                return ConceptProto.AttributeType.VALUE_TYPE.BOOLEAN;
            case AttributeType.ValueType.LONG:
                return ConceptProto.AttributeType.VALUE_TYPE.LONG;
            case AttributeType.ValueType.DOUBLE:
                return ConceptProto.AttributeType.VALUE_TYPE.DOUBLE;
            case AttributeType.ValueType.STRING:
                return ConceptProto.AttributeType.VALUE_TYPE.STRING;
            case AttributeType.ValueType.DATETIME:
                return ConceptProto.AttributeType.VALUE_TYPE.DATETIME;
            default:
                throw "Value type not recognised";
        }
    }

    export function thingEncoding(thing: Thing): ConceptProto.Thing.ENCODING {
        if (thing instanceof EntityImpl) {
            return ConceptProto.Thing.ENCODING.ENTITY;
        } else if (thing instanceof RelationImpl) {
            return ConceptProto.Thing.ENCODING.RELATION;
        } else if (thing instanceof AttributeImpl) {
            return ConceptProto.Thing.ENCODING.ATTRIBUTE;
        } else {
            throw "Unrecognised Thing class";
        }
    }

    export function typeEncoding(type: Type): ConceptProto.Type.ENCODING {
        if (type instanceof EntityTypeImpl) {
            return ConceptProto.Type.ENCODING.ENTITY_TYPE;
        } else if (type instanceof RelationTypeImpl) {
            return ConceptProto.Type.ENCODING.RELATION_TYPE;
        } else if (type instanceof AttributeTypeImpl) {
            return ConceptProto.Type.ENCODING.ATTRIBUTE_TYPE;
        } else if (type instanceof RoleTypeImpl) {
            return ConceptProto.Type.ENCODING.ROLE_TYPE;
        } else if (type instanceof ThingTypeImpl) {
            return ConceptProto.Type.ENCODING.THING_TYPE;
        } else {
            throw "Unrecognised Type class";
        }
    }
}
