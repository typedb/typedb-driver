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

package com.typedb.driver.concept.instance;

import com.typedb.driver.api.concept.instance.Attribute;
import com.typedb.driver.api.concept.value.Value;
import com.typedb.driver.common.exception.TypeDBDriverException;
import com.typedb.driver.concept.type.AttributeTypeImpl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Optional;

import static com.typedb.driver.common.exception.ErrorMessage.Internal.NULL_CONCEPT_PROPERTY;
import static com.typedb.driver.common.util.Objects.className;
import static com.typedb.driver.jni.typedb_driver.attribute_get_type;

public class AttributeImpl extends InstanceImpl implements Attribute {
    public AttributeImpl(com.typedb.driver.jni.Concept concept) {
        super(concept);
    }

    @Override
    public AttributeTypeImpl getType() {
        return new AttributeTypeImpl(attribute_get_type(nativeObject));
    }

    @Override
    public Value getValue() {
        return tryGetValue().orElseThrow(() -> new TypeDBDriverException(NULL_CONCEPT_PROPERTY, className(this.getClass())));
    }

    @Override
    public String getValueType() {
        return getValue().getType();
    }

    @Override
    public boolean getBoolean() {
        return getValue().getBoolean();
    }

    @Override
    public long getLong() {
        return getValue().getLong();
    }

    @Override
    public double getDouble() {
        return getValue().getDouble();
    }

    @Override
    public BigDecimal getDecimal() {
        return getValue().getDecimal();
    }

    @Override
    public String getString() {
        return getValue().getString();
    }

    @Override
    public LocalDate getDate() {
        return getValue().getDate();
    }

    @Override
    public LocalDateTime getDatetime() {
        return getValue().getDatetime();
    }

    @Override
    public ZonedDateTime getDatetimeTZ() {
        return getValue().getDatetimeTZ();
    }

    @Override
    public com.typedb.driver.common.Duration getDuration() {
        return getValue().getDuration();
    }

    @Override
    public Map<String, Optional<Value>> getStruct() {
        return getValue().getStruct();
    }

    @Override
    public int hashCode() {
        if (hash == 0) hash = getValue().hashCode();
        return hash;
    }
}
