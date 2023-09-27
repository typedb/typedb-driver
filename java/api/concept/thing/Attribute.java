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

package com.vaticle.typedb.driver.api.concept.thing;

import com.eclipsesource.json.JsonObject;
import com.vaticle.typedb.driver.api.TypeDBTransaction;
import com.vaticle.typedb.driver.api.concept.value.Value;
import com.vaticle.typedb.driver.api.concept.type.AttributeType;
import com.vaticle.typedb.driver.api.concept.type.ThingType;

import javax.annotation.CheckReturnValue;
import java.util.stream.Stream;

/**
 * <p>Attribute is an instance of the attribute type and has a value.
 * This value is fixed and unique for every given instance of the attribute type.</p>
 * <p>Attribute type can be uniquely addressed by its type and value.</p>
 */
public interface Attribute extends Thing {
    /**
     * Retrieves the type which this <code>Attribute</code> belongs to.
     *
     * <h3>Examples</h3>
     * <pre>
     * attribute.getType();
     * </pre>
     */
    @Override
    @CheckReturnValue
    AttributeType getType();

    /**
     * Checks if the concept is an <code>Attribute</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * attribute.isAttribute();
     * </pre>
     */
    @Override
    @CheckReturnValue
    default boolean isAttribute() {
        return true;
    }

    /**
     * Casts the concept to <code>Attribute</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * attribute.asAttribute();
     * </pre>
     */
    @Override
    @CheckReturnValue
    default Attribute asAttribute() {
        return this;
    }

    /**
     * Retrieves the value which the <code>Attribute</code> instance holds.
     *
     * <h3>Examples</h3>
     * <pre>
     * attribute.getValue();
     * </pre>
     */
    Value getValue();

    /**
     * Retrieves this <code>Attribute</code> as JSON.
     *
     * <h3>Examples</h3>
     * <pre>
     * attribute.toJson();
     * </pre>
     */
    @Override
    default JsonObject toJSON() {
        return getValue().toJSON().add("type", getType().getLabel().scopedName());
    }

    /**
     * Retrieves the instances that own this <code>Attribute</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * attribute.getOwners(transaction);
     * </pre>
     *
     * @param transaction The current transaction
     */
    @CheckReturnValue
    Stream<? extends Thing> getOwners(TypeDBTransaction transaction);

    /**
     * Retrieves the instances that own this <code>Attribute</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * attribute.getOwners(transaction, ownerType);
     * </pre>
     *
     * @param transaction The current transaction
     * @param ownerType Filter results for only owners of the given type
     */
    @CheckReturnValue
    Stream<? extends Thing> getOwners(TypeDBTransaction transaction, ThingType ownerType);
}
