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

package com.vaticle.typedb.driver.concept.type;

import com.vaticle.typedb.driver.api.concept.type.AttributeType;
import com.vaticle.typedb.driver.common.Label;

import static com.vaticle.typedb.driver.jni.typedb_driver.attribute_type_get_label;
import static com.vaticle.typedb.driver.jni.typedb_driver.attribute_type_get_value_type;
import static com.vaticle.typedb.driver.jni.typedb_driver.attribute_type_is_boolean;
import static com.vaticle.typedb.driver.jni.typedb_driver.attribute_type_is_date;
import static com.vaticle.typedb.driver.jni.typedb_driver.attribute_type_is_datetime;
import static com.vaticle.typedb.driver.jni.typedb_driver.attribute_type_is_datetime_tz;
import static com.vaticle.typedb.driver.jni.typedb_driver.attribute_type_is_decimal;
import static com.vaticle.typedb.driver.jni.typedb_driver.attribute_type_is_double;
import static com.vaticle.typedb.driver.jni.typedb_driver.attribute_type_is_duration;
import static com.vaticle.typedb.driver.jni.typedb_driver.attribute_type_is_long;
import static com.vaticle.typedb.driver.jni.typedb_driver.attribute_type_is_string;
import static com.vaticle.typedb.driver.jni.typedb_driver.attribute_type_is_struct;
import static com.vaticle.typedb.driver.jni.typedb_driver.attribute_type_is_untyped;

public class AttributeTypeImpl extends ThingTypeImpl implements AttributeType {
    public AttributeTypeImpl(com.vaticle.typedb.driver.jni.Concept concept) {
        super(concept);
    }

    @Override
    public Label getLabel() {
        return Label.of(attribute_type_get_label(nativeObject));
    }

    @Override
    public String getValueType() {
        return attribute_type_get_value_type(nativeObject);
    }

    @Override
    public boolean isUntyped() {
        return attribute_type_is_untyped(nativeObject);
    }

    @Override
    public boolean isBoolean() {
        return attribute_type_is_boolean(nativeObject);
    }

    @Override
    public boolean isLong() {
        return attribute_type_is_long(nativeObject);
    }

    @Override
    public boolean isDouble() {
        return attribute_type_is_double(nativeObject);
    }

    @Override
    public boolean isDecimal() {
        return attribute_type_is_decimal(nativeObject);
    }

    @Override
    public boolean isString() {
        return attribute_type_is_string(nativeObject);
    }

    @Override
    public boolean isDate() {
        return attribute_type_is_date(nativeObject);
    }

    @Override
    public boolean isDatetime() {
        return attribute_type_is_datetime(nativeObject);
    }

    @Override
    public boolean isDatetimeTZ() {
        return attribute_type_is_datetime_tz(nativeObject);
    }

    @Override
    public boolean isDuration() {
        return attribute_type_is_duration(nativeObject);
    }

    @Override
    public boolean isStruct() {
        return attribute_type_is_struct(nativeObject);
    }
}
