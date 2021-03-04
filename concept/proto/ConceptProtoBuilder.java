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

package grakn.client.concept.proto;

import com.google.protobuf.ByteString;
import grakn.client.common.exception.GraknClientException;
import grakn.client.concept.Concept;
import grakn.client.concept.answer.ConceptMap;
import grakn.client.concept.thing.Thing;
import grakn.client.concept.type.AttributeType.ValueType;
import grakn.client.concept.type.Type;
import grakn.protocol.AnswerProto;
import grakn.protocol.ConceptProto;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collection;

import static grakn.client.common.exception.ErrorMessage.Concept.BAD_ATTRIBUTE_VALUE;
import static grakn.common.collection.Bytes.hexStringToBytes;
import static java.util.stream.Collectors.toList;

public abstract class ConceptProtoBuilder {

    public static ConceptProto.Concept concept(Concept concept) {
        ConceptProto.Concept.Builder builder = ConceptProto.Concept.newBuilder();
        if (concept.isThing()) {
            builder.setThing(thing(concept.asThing()));
        } else {
            builder.setType(type(concept.asType()));
        }
        return builder.build();
    }

    public static ConceptProto.Thing thing(Thing thing) {
        return ConceptProto.Thing.newBuilder()
                .setIid(iid(thing.getIID()))
                .build();
    }

    public static ConceptProto.Type type(Type type) {
        ConceptProto.Type.Builder builder = ConceptProto.Type.newBuilder()
                .setLabel(type.getLabel())
                .setEncoding(encoding(type));

        if (type.isRoleType()) {
            builder.setScope(type.asRoleType().getScope());
        }

        return builder.build();
    }

    public static Collection<ConceptProto.Type> types(Collection<? extends Type> types) {
        return types.stream().map(ConceptProtoBuilder::type).collect(toList());
    }

    public static ConceptProto.Attribute.Value attributeValue(Object value) {
        ConceptProto.Attribute.Value.Builder builder = ConceptProto.Attribute.Value.newBuilder();
        if (value instanceof String) {
            builder.setString((String) value);
        } else if (value instanceof Boolean) {
            builder.setBoolean((boolean) value);
        } else if (value instanceof Long) {
            builder.setLong((long) value);
        } else if (value instanceof Double) {
            builder.setDouble((double) value);
        } else if (value instanceof LocalDateTime) {
            builder.setDateTime(((LocalDateTime) value).atZone(ZoneId.of("Z")).toInstant().toEpochMilli());
        } else {
            throw new GraknClientException(BAD_ATTRIBUTE_VALUE.message(value));
        }
        return builder.build();
    }

    public static ConceptProto.AttributeType.ValueType valueType(ValueType valueType) {
        switch (valueType) {
            case OBJECT:
                return ConceptProto.AttributeType.ValueType.OBJECT;
            case BOOLEAN:
                return ConceptProto.AttributeType.ValueType.BOOLEAN;
            case LONG:
                return ConceptProto.AttributeType.ValueType.LONG;
            case DOUBLE:
                return ConceptProto.AttributeType.ValueType.DOUBLE;
            case STRING:
                return ConceptProto.AttributeType.ValueType.STRING;
            case DATETIME:
                return ConceptProto.AttributeType.ValueType.DATETIME;
            default:
                return ConceptProto.AttributeType.ValueType.UNRECOGNIZED;
        }
    }

    public static ByteString iid(String iid) {
        return ByteString.copyFrom(hexStringToBytes(iid));
    }

    private static ConceptProto.Type.Encoding encoding(Type type) {
        if (type.isEntityType()) {
            return ConceptProto.Type.Encoding.ENTITY_TYPE;
        } else if (type.isRelationType()) {
            return ConceptProto.Type.Encoding.RELATION_TYPE;
        } else if (type.isAttributeType()) {
            return ConceptProto.Type.Encoding.ATTRIBUTE_TYPE;
        } else if (type.isRoleType()) {
            return ConceptProto.Type.Encoding.ROLE_TYPE;
        } else if (type.isThingType()) {
            return ConceptProto.Type.Encoding.THING_TYPE;
        } else {
            return ConceptProto.Type.Encoding.UNRECOGNIZED;
        }
    }

    public static AnswerProto.ConceptMap conceptMap(ConceptMap conceptMap) {
        AnswerProto.ConceptMap.Builder conceptMapProto = AnswerProto.ConceptMap.newBuilder();
        conceptMap.map().forEach((var, concept) -> {
            ConceptProto.Concept conceptProto = concept(concept);
            conceptMapProto.putMap(var, conceptProto);
        });
        return conceptMapProto.build();
    }
}
