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

package com.typedb.driver.api.concept.type;

import javax.annotation.CheckReturnValue;

/**
 * Attribute types represent properties that other types can own.
 * <p>Attribute types have a value type. This value type is fixed and unique for every given instance
 * of the attribute type.
 * <p>Other types can own an attribute type. That means that instances of these other types can own an instance
 * of this attribute type. This usually means that an object in our domain has a property with the matching value.
 * <p>Multiple types can own the same attribute type, and different instances of the same type or different types
 * can share ownership of the same attribute instance.
 */
public interface AttributeType extends Type {
    /**
     * {@inheritDoc}
     */
    @Override
    @CheckReturnValue
    default boolean isAttributeType() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @CheckReturnValue
    default AttributeType asAttributeType() {
        return this;
    }
}
