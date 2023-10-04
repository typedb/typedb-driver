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

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import com.vaticle.typedb.driver.api.TypeDBTransaction;
import com.vaticle.typedb.driver.api.concept.Concept;
import com.vaticle.typedb.driver.api.concept.type.AttributeType;
import com.vaticle.typedb.driver.api.concept.type.RoleType;
import com.vaticle.typedb.driver.api.concept.type.ThingType;
import com.vaticle.typedb.driver.api.concept.type.ThingType.Annotation;

import javax.annotation.CheckReturnValue;
import java.util.Set;
import java.util.stream.Stream;

public interface Thing extends Concept {
    /**
     * Retrieves the unique id of the <code>Thing</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * thing.getIID();
     * </pre>
     */
    @CheckReturnValue
    String getIID();

    /**
     * Retrieves the type which this <code>Thing</code> belongs to.
     *
     * <h3>Examples</h3>
     * <pre>
     * thing.getType();
     * </pre>
     */
    @CheckReturnValue
    ThingType getType();

    /**
     * Checks if this <code>Thing</code> is inferred by a [Reasoning Rule].
     *
     * <h3>Examples</h3>
     * <pre>
     * thing.isInferred();
     * </pre>
     */
    @CheckReturnValue
    boolean isInferred();

    /**
     * Checks if the concept is a <code>Thing</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * thing.isThing();
     * </pre>
     */
    @Override
    @CheckReturnValue
    default boolean isThing() {
        return true;
    }

    /**
     * Casts the concept to <code>Thing</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * thing.asThing();
     * </pre>
     */
    @Override
    @CheckReturnValue
    default Thing asThing() {
        return this;
    }

    /**
     * Retrieves a <code>Thing</code> as JSON.
     *
     * <h3>Examples</h3>
     * <pre>
     * thing.toJSON();
     * </pre>
     */
    @Override
    default JsonObject toJSON() {
        return Json.object().add("type", getType().getLabel().scopedName());
    }

    /**
     * Assigns an <code>Attribute</code> to be owned by this <code>Thing</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * thing.setHas(transaction, attribute);
     * </pre>
     *
     * @param transaction The current transaction
     * @param attribute The <code>Attribute</code> to be owned by this <code>Thing</code>.
     */
    void setHas(TypeDBTransaction transaction, Attribute attribute);

    /**
     * Unassigns an <code>Attribute</code> from this <code>Thing</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * thing.unsetHas(transaction, attribute);
     * </pre>
     *
     * @param transaction The current transaction
     * @param attribute The <code>Attribute</code> to be disowned from this <code>Thing</code>.
     */
    void unsetHas(TypeDBTransaction transaction, Attribute attribute);

    /**
     * Retrieves the <code>Attribute</code>s that this <code>Thing</code> owns,
     * optionally filtered by <code>AttributeType</code>s.
     *
     * <h3>Examples</h3>
     * <pre>
     * thing.getHas(transaction)
     thing.getHas(transaction, attributeType=attributeType,
     annotations=set(Annotation.key()))
     * </pre>
     *
     * @param transaction The current transaction
     * @param attributeTypes The <code>AttributeType</code>s to filter the attributes by
     */
    @CheckReturnValue
    Stream<? extends Attribute> getHas(TypeDBTransaction transaction, AttributeType... attributeTypes);

    /**
     * Retrieves the <code>Attribute</code>s that this <code>Thing</code> owns,
     * filtered by <code>Annotation</code>s.
     *
     * <h3>Examples</h3>
     * <pre>
     * thing.getHas(transaction);
     * thing.getHas(transaction, annotations=set(Annotation.key()));
     * </pre>
     *
     * @param transaction The current transaction
     * @param annotations Only retrieve attributes with all given <code>Annotation</code>s
     */
    @CheckReturnValue
    Stream<? extends Attribute> getHas(TypeDBTransaction transaction, Set<Annotation> annotations);

    /**
     * Retrieves all the <code>Relations</code> which this <code>Thing</code> plays a role in,
     * optionally filtered by one or more given roles.
     *
     * <h3>Examples</h3>
     * <pre>
     * thing.getRelations(transaction, roleTypes);
     * </pre>
     *
     * @param transaction The current transaction
     * @param roleTypes The array of roles to filter the relations by.
     */
    @CheckReturnValue
    Stream<? extends Relation> getRelations(TypeDBTransaction transaction, RoleType... roleTypes);

    /**
     * Retrieves the roles that this <code>Thing</code> is currently playing.
     *
     * <h3>Examples</h3>
     * <pre>
     * thing.getPlaying(transaction);
     * </pre>
     *
     * @param transaction The current transaction
     */
    @CheckReturnValue
    Stream<? extends RoleType> getPlaying(TypeDBTransaction transaction);

    /**
     * Deletes this <code>Thing</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * thing.delete(transaction);
     * </pre>
     *
     * @param transaction The current transaction
     */
    void delete(TypeDBTransaction transaction);

    /**
     * Checks if this <code>Thing</code> is deleted.
     *
     * <h3>Examples</h3>
     * <pre>
     * thing.isDeleted(transaction)
     * </pre>
     *
     * @param transaction The current transaction
     */
    @CheckReturnValue
    boolean isDeleted(TypeDBTransaction transaction);
}
