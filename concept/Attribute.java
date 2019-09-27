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

package grakn.client.concept;

import grakn.client.GraknClient;
import grakn.protocol.session.ConceptProto;

import javax.annotation.CheckReturnValue;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Iterator;
import java.util.stream.Stream;

/**
 * Client implementation of Attribute
 *
 * @param <D> The data type of this attribute
 */
public class Attribute<D> extends Thing<Attribute, AttributeType<D>> {

    Attribute(GraknClient.Transaction tx, ConceptId id) {
        super(tx, id);
    }

    static <D> Attribute<D> construct(GraknClient.Transaction tx, ConceptId id) {
        return new Attribute<>(tx, id);
    }

    public final D value() {
        ConceptProto.Method.Req method = ConceptProto.Method.Req.newBuilder()
                .setAttributeValueReq(ConceptProto.Attribute.Value.Req.getDefaultInstance()).build();

        ConceptProto.ValueObject value = runMethod(method).getAttributeValueRes().getValue();
        return castValue(value);
    }

    @SuppressWarnings("unchecked")
    private D castValue(ConceptProto.ValueObject value) {
        switch (value.getValueCase()) {
            case DATE:
                return (D) LocalDateTime.ofInstant(Instant.ofEpochMilli(value.getDate()), ZoneId.of("Z"));
            case STRING:
                return (D) value.getString();
            case BOOLEAN:
                return (D) (Boolean) value.getBoolean();
            case INTEGER:
                return (D) (Integer) value.getInteger();
            case LONG:
                return (D) (Long) value.getLong();
            case FLOAT:
                return (D) (Float) value.getFloat();
            case DOUBLE:
                return (D) (Double) value.getDouble();
            case VALUE_NOT_SET:
                return null;
            default:
                throw new IllegalArgumentException("Unexpected value for attribute: " + value);
        }
    }

    public final Stream<Thing> owners() {
        ConceptProto.Method.Req method = ConceptProto.Method.Req.newBuilder()
                .setAttributeOwnersReq(ConceptProto.Attribute.Owners.Req.getDefaultInstance()).build();

        int iteratorId = runMethod(method).getAttributeOwnersIter().getId();
        return conceptStream(iteratorId, res -> res.getAttributeOwnersIterRes().getThing()).map(Concept::asThing);
    }

    public final AttributeType.DataType<D> dataType() {
        return type().dataType();
    }

    final AttributeType<D> asCurrentType(Concept concept) {
        return concept.asAttributeType();
    }

    final Attribute asCurrentBaseType(Concept other) {
        return other.asAttribute();
    }

    Thing owner() {
        Iterator<Thing> owners = owners().iterator();
        if (owners.hasNext()) {
            return owners.next();
        } else {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    @Deprecated
    @CheckReturnValue
    @Override
    Attribute asAttribute() {
        return this;
    }

    @Deprecated
    @CheckReturnValue
    @Override
    boolean isAttribute() {
        return true;
    }
}
