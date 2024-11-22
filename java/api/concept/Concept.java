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

package com.typedb.driver.api.concept;

import com.typedb.driver.api.concept.instance.Attribute;
import com.typedb.driver.api.concept.instance.Entity;
import com.typedb.driver.api.concept.instance.Instance;
import com.typedb.driver.api.concept.instance.Relation;
import com.typedb.driver.api.concept.type.AttributeType;
import com.typedb.driver.api.concept.type.EntityType;
import com.typedb.driver.api.concept.type.RelationType;
import com.typedb.driver.api.concept.type.RoleType;
import com.typedb.driver.api.concept.type.Type;
import com.typedb.driver.api.concept.value.Value;
import com.typedb.driver.common.Duration;
import com.typedb.driver.common.exception.TypeDBDriverException;

import javax.annotation.CheckReturnValue;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Optional;

import static com.typedb.driver.common.exception.ErrorMessage.Concept.INVALID_CONCEPT_CASTING;
import static com.typedb.driver.common.util.Objects.className;

public interface Concept {
    int DECIMAL_SCALE = 19;

    /**
     * Checks if the concept is a <code>Type</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * concept.isType();
     * </pre>
     */
    @CheckReturnValue
    default boolean isType() {
        return false;
    }

    /**
     * Checks if the concept is an <code>EntityType</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * concept.isEntityType();
     * </pre>
     */
    @CheckReturnValue
    default boolean isEntityType() {
        return false;
    }

    /**
     * Checks if the concept is an <code>AttributeType</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * concept.isAttributeType();
     * </pre>
     */
    @CheckReturnValue
    default boolean isAttributeType() {
        return false;
    }

    /**
     * Checks if the concept is a <code>RelationType</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * concept.isRelationType();
     * </pre>
     */
    @CheckReturnValue
    default boolean isRelationType() {
        return false;
    }

    /**
     * Checks if the concept is a <code>RoleType</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * concept.isRoleType();
     * </pre>
     */
    @CheckReturnValue
    default boolean isRoleType() {
        return false;
    }

    /**
     * Checks if the concept is an <code>Instance</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * concept.isInstance();
     * </pre>
     */
    @CheckReturnValue
    default boolean isInstance() {
        return false;
    }

    /**
     * Checks if the concept is an <code>Entity</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * concept.isEntity();
     * </pre>
     */
    @CheckReturnValue
    default boolean isEntity() {
        return false;
    }

    /**
     * Checks if the concept is a <code>Relation</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * concept.isRelation();
     * </pre>
     */
    @CheckReturnValue
    default boolean isRelation() {
        return false;
    }

    /**
     * Checks if the concept is an <code>Attribute</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * concept.isAttribute();
     * </pre>
     */
    @CheckReturnValue
    default boolean isAttribute() {
        return false;
    }

    /**
     * Checks if the concept is a <code>Value</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * concept.isValue();
     * </pre>
     */
    @CheckReturnValue
    default boolean isValue() {
        return false;
    }


    /**
     * Casts the concept to <code>Type</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * concept.asType();
     * </pre>
     */
    default Type asType() {
        throw new TypeDBDriverException(INVALID_CONCEPT_CASTING, className(this.getClass()), className(Type.class));
    }

    /**
     * Casts the concept to <code>EntityType</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * concept.asEntityType();
     * </pre>
     */
    default EntityType asEntityType() {
        throw new TypeDBDriverException(INVALID_CONCEPT_CASTING, className(this.getClass()), className(EntityType.class));
    }

    /**
     * Casts the concept to <code>RelationType</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * concept.asRelationType();
     * </pre>
     */
    default RelationType asRelationType() {
        throw new TypeDBDriverException(INVALID_CONCEPT_CASTING, className(this.getClass()), className(RelationType.class));
    }

    /**
     * Casts the concept to <code>AttributeType</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * concept.asAttributeType();
     * </pre>
     */
    default AttributeType asAttributeType() {
        throw new TypeDBDriverException(INVALID_CONCEPT_CASTING, className(this.getClass()), className(AttributeType.class));
    }

    /**
     * Casts the concept to <code>RoleType</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * concept.asRoleType();
     * </pre>
     */
    default RoleType asRoleType() {
        throw new TypeDBDriverException(INVALID_CONCEPT_CASTING, className(this.getClass()), className(RoleType.class));
    }

    /**
     * Casts the concept to <code>Instance</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * concept.asInstance();
     * </pre>
     */
    default Instance asInstance() {
        throw new TypeDBDriverException(INVALID_CONCEPT_CASTING, className(this.getClass()), className(Instance.class));
    }

    /**
     * Casts the concept to <code>Entity</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * concept.asEntity();
     * </pre>
     */
    default Entity asEntity() {
        throw new TypeDBDriverException(INVALID_CONCEPT_CASTING, className(this.getClass()), className(Entity.class));
    }

    /**
     * Casts the concept to <code>Relation</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * concept.asRelation();
     * </pre>
     */
    default Relation asRelation() {
        throw new TypeDBDriverException(INVALID_CONCEPT_CASTING, className(this.getClass()), className(Relation.class));
    }

    /**
     * Casts the concept to <code>Attribute</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * concept.asAttribute();
     * </pre>
     */
    default Attribute asAttribute() {
        throw new TypeDBDriverException(INVALID_CONCEPT_CASTING, className(this.getClass()), className(Attribute.class));
    }

    /**
     * Casts the concept to <code>Value</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * concept.asValue();
     * </pre>
     */
    default Value asValue() {
        throw new TypeDBDriverException(INVALID_CONCEPT_CASTING, className(this.getClass()), className(Value.class));
    }

    /**
     * Returns <code>true</code> if the value which this <code>Concept</code> holds is of type <code>boolean</code>
     * or if this <code>Concept</code> is an <code>AttributeType</code> of type <code>boolean</code>.
     * Otherwise, returns <code>false</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * concept.isBoolean()
     * </pre>
     */
    @CheckReturnValue
    boolean isBoolean();

    /**
     * Returns <code>true</code> if the value which this <code>Concept</code> holds is of type <code>long</code>
     * or if this <code>Concept</code> is an <code>AttributeType</code> of type <code>long</code>.
     * Otherwise, returns <code>false</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * concept.isLong();
     * </pre>
     */
    @CheckReturnValue
    boolean isLong();

    /**
     * Returns <code>true</code> if the value which this <code>Concept</code> holds is of type <code>double</code>
     * or if this <code>Concept</code> is an <code>AttributeType</code> of type <code>double</code>.
     * Otherwise, returns <code>false</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * concept.isDouble();
     * </pre>
     */
    @CheckReturnValue
    boolean isDouble();

    /**
     * Returns <code>true</code> if the value which this <code>Concept</code> holds is of type <code>decimal</code>
     * or if this <code>Concept</code> is an <code>AttributeType</code> of type <code>decimal</code>.
     * Otherwise, returns <code>false</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * concept.isDecimal();
     * </pre>
     */
    @CheckReturnValue
    boolean isDecimal();

    /**
     * Returns <code>true</code> if the value which this <code>Concept</code> holds is of type <code>string</code>
     * or if this <code>Concept</code> is an <code>AttributeType</code> of type <code>string</code>.
     * Otherwise, returns <code>false</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * concept.isString();
     * </pre>
     */
    @CheckReturnValue
    boolean isString();

    /**
     * Returns <code>true</code> if the value which this <code>Concept</code> holds is of type <code>date</code>
     * or if this <code>Concept</code> is an <code>AttributeType</code> of type <code>date</code>.
     * Otherwise, returns <code>false</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * concept.isDate();
     * </pre>
     */
    @CheckReturnValue
    boolean isDate();

    /**
     * Returns <code>true</code> if the value which this <code>Concept</code> holds is of type <code>datetime</code>
     * or if this <code>Concept</code> is an <code>AttributeType</code> of type <code>datetime</code>.
     * Otherwise, returns <code>false</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * concept.isDatetime();
     * </pre>
     */
    @CheckReturnValue
    boolean isDatetime();

    /**
     * Returns <code>true</code> if the value which this <code>Concept</code> holds is of type <code>datetime-tz</code>
     * or if this <code>Concept</code> is an <code>AttributeType</code> of type <code>datetime-tz</code>.
     * Otherwise, returns <code>false</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * concept.isDatetimeTZ();
     * </pre>
     */
    @CheckReturnValue
    boolean isDatetimeTZ();

    /**
     * Returns <code>true</code> if the value which this <code>Concept</code> holds is of type <code>duration</code>
     * or if this <code>Concept</code> is an <code>AttributeType</code> of type <code>duration</code>.
     * Otherwise, returns <code>false</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * concept.isDuration();
     * </pre>
     */
    @CheckReturnValue
    boolean isDuration();

    /**
     * Returns <code>true</code> if the value which this <code>Concept</code> holds is of type <code>struct</code>
     * or if this <code>Concept</code> is an <code>AttributeType</code> of type <code>struct</code>.
     * Otherwise, returns <code>false</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * concept.isStruct();
     * </pre>
     */
    @CheckReturnValue
    boolean isStruct();

    // TODO: Could be useful to have isStruct(struct_name)

    /**
     * Returns a <code>boolean</code> value of this <code>Concept</code>.
     * If it's not a <code>Value</code> or it has another type, returns <code>null</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * concept.tryGetBoolean();
     * </pre>
     */
    Optional<Boolean> tryGetBoolean();

    /**
     * Returns a <code>long</code> value of this <code>Concept</code>.
     * If it's not a <code>Value</code> or it has another type, returns <code>null</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * concept.tryGetLong();
     * </pre>
     */
    Optional<Long> tryGetLong();

    /**
     * Returns a <code>double</code> value of this <code>Concept</code>.
     * If it's not a <code>Value</code> or it has another type, returns <code>null</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * concept.tryGetDouble();
     * </pre>
     */
    Optional<Double> tryGetDouble();

    /**
     * Returns a <code>decimal</code> value of this <code>Concept</code>.
     * If it's not a <code>Value</code> or it has another type, returns <code>null</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * concept.tryGetDecimal();
     * </pre>
     */
    Optional<BigDecimal> tryGetDecimal();

    /**
     * Returns a <code>string</code> value of this <code>Concept</code>.
     * If it's not a <code>Value</code> or it has another type, returns <code>null</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * concept.tryGetString();
     * </pre>
     */
    Optional<String> tryGetString();

    /**
     * Returns a <code>date</code> value of this <code>Concept</code>.
     * If it's not a <code>Value</code> or it has another type, returns <code>null</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * concept.tryGetDate();
     * </pre>
     */
    Optional<LocalDate> tryGetDate();

    /**
     * Returns a <code>datetime</code> value of this <code>Concept</code>.
     * If it's not a <code>Value</code> or it has another type, returns <code>null</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * concept.tryGetDatetime();
     * </pre>
     */
    Optional<LocalDateTime> tryGetDatetime();

    /**
     * Returns a <code>datetime-tz</code> value of this <code>Concept</code>.
     * If it's not a <code>Value</code> or it has another type, returns <code>null</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * concept.tryGetDatetimeTZ();
     * </pre>
     */
    Optional<ZonedDateTime> tryGetDatetimeTZ();

    /**
     * Returns a <code>duration</code> value of this <code>Concept</code>.
     * If it's not a <code>Value</code> or it has another type, returns <code>null</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * concept.tryGetDuration();
     * </pre>
     */
    Optional<Duration> tryGetDuration();

    /**
     * Returns a <code>struct</code> value of this <code>Concept</code>.
     * If it's not a <code>Value</code> or it has another type, returns <code>null</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * concept.tryGetStruct();
     * </pre>
     */
    Optional<Map<String, Optional<Value>>> tryGetStruct();

    /**
     * Retrieves the unique label of the concept.
     * If this is an <code>Instance</code>, return the label of the type of this instance ("unknown" if type fetching is disabled).
     * If this is a <code>Value</code>, return the label of the value type of the value.
     * If this is a <code>Type</code>, return the label of the type.
     *
     * <h3>Examples</h3>
     * <pre>
     * concept.getLabel();
     * </pre>
     */
    @CheckReturnValue
    String getLabel();

    /**
     * Retrieves the unique label of the concept.
     * If this is an <code>Instance</code>, return the label of the type of this instance (<code>null</code> if type fetching is disabled).
     * Returns <code>null</code> if type fetching is disabled.
     * If this is a <code>Value</code>, return the label of the value type of the value.
     * If this is a <code>Type</code>, return the label of the type.
     *
     * <h3>Examples</h3>
     * <pre>
     * concept.tryGetLabel();
     * </pre>
     */
    @CheckReturnValue
    Optional<String> tryGetLabel();

    /**
     * Retrieves the unique id of the <code>Concept</code>. Returns <code>null</code> if absent.
     *
     * <h3>Examples</h3>
     * <pre>
     * concept.tryGetIID();
     * </pre>
     */
    @CheckReturnValue
    Optional<String> tryGetIID();

    /**
     * Retrieves the <code>String</code> describing the value type of this <code>Concept</code>.
     * Returns <code>null</code> if not absent.
     *
     * <h3>Examples</h3>
     * <pre>
     * concept.tryGetValueType();
     * </pre>
     */
    @CheckReturnValue
    Optional<String> tryGetValueType();

    /**
     * Retrieves the value which this <code>Concept</code> holds.
     * Returns <code>null</code> if this <code>Concept</code> does not hold any value.
     *
     * <h3>Examples</h3>
     * <pre>
     * concept.tryGetValue();
     * </pre>
     */
    @CheckReturnValue
    Optional<Value> tryGetValue();
}
