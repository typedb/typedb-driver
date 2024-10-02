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

package com.typedb.driver.concept.thing;

import com.typedb.driver.api.concept.thing.Attribute;
import com.typedb.driver.api.concept.value.Value;
import com.typedb.driver.concept.type.AttributeTypeImpl;
import com.typedb.driver.concept.value.ValueImpl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Optional;

import static com.typedb.driver.jni.typedb_driver.attribute_get_type;
import static com.typedb.driver.jni.typedb_driver.attribute_get_value;

public class AttributeImpl extends ThingImpl implements Attribute {
    public AttributeImpl(com.typedb.driver.jni.Concept concept) {
        super(concept);
    }

    @Override
    public AttributeTypeImpl getType() {
        return new AttributeTypeImpl(attribute_get_type(nativeObject));
    }

    @Override
    public Value getValue() {
        return new ValueImpl(attribute_get_value(nativeObject));
    }

    @Override
    public boolean isBoolean() {
        return getValue().isBoolean();
    }

    @Override
    public boolean isLong() {
        return getValue().isLong();
    }

    @Override
    public boolean isDouble() {
        return getValue().isDouble();
    }

    @Override
    public boolean isDecimal() {
        return getValue().isDecimal();
    }

    @Override
    public boolean isString() {
        return getValue().isString();
    }

    @Override
    public boolean isDate() {
        return getValue().isDate();
    }

    @Override
    public boolean isDatetime() {
        return getValue().isDatetime();
    }

    @Override
    public boolean isDatetimeTZ() {
        return getValue().isDatetimeTZ();
    }

    @Override
    public boolean isDuration() {
        return getValue().isDuration();
    }

    @Override
    public boolean isStruct() {
        return getValue().isStruct();
    }

    @Override
    public Object asUntyped() {
        return getValue().asUntyped();
    }

    @Override
    public boolean asBoolean() {
        return getValue().asBoolean();
    }

    @Override
    public long asLong() {
        return getValue().asLong();
    }

    @Override
    public double asDouble() {
        return getValue().asDouble();
    }

    @Override
    public BigDecimal asDecimal() {
        return getValue().asDecimal();
    }

    @Override
    public String asString() {
        return getValue().asString();
    }

    @Override
    public LocalDate asDate() {
        return getValue().asDate();
    }

    @Override
    public LocalDateTime asDatetime() {
        return getValue().asDatetime();
    }

    @Override
    public ZonedDateTime asDatetimeTZ() {
        return getValue().asDatetimeTZ();
    }

    @Override
    public com.typedb.driver.common.Duration asDuration() {
        return getValue().asDuration();
    }

    @Override
    public Map<String, Optional<Value>> asStruct() {
        return getValue().asStruct();
    }

    @Override
    public int hashCode() {
        if (hash == 0) hash = getValue().hashCode();
        return hash;
    }
}
