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

package grakn.client.concept.remote;

import grakn.client.concept.Attribute;
import grakn.client.concept.AttributeType;
import grakn.client.concept.Thing;
import grakn.client.concept.Type;

import javax.annotation.CheckReturnValue;
import java.util.stream.Stream;

/**
 * A data instance in the graph belonging to a specific Type
 * Instances represent data in the graph.
 * Every instance belongs to a Type which serves as a way of categorising them.
 * Instances can relate to one another via Relation
 */
public interface RemoteThing<
        SomeRemoteThing extends RemoteThing<SomeRemoteThing, SomeRemoteType, SomeThing, SomeType>,
        SomeRemoteType extends RemoteType<SomeRemoteType, SomeRemoteThing, SomeType, SomeThing>,
        SomeThing extends Thing<SomeThing, SomeType, SomeRemoteThing, SomeRemoteType>,
        SomeType extends Type<SomeType, SomeThing, SomeRemoteType, SomeRemoteThing>>
        extends Thing<SomeThing, SomeType, SomeRemoteThing, SomeRemoteType>,
        RemoteConcept<SomeRemoteThing, SomeThing> {
    //------------------------------------- Accessors ----------------------------------

    /**
     * Return the Type of the Concept.
     *
     * @return A Type which is the type of this concept. This concept is an instance of that type.
     */
    @Override
    @CheckReturnValue
    RemoteType<SomeRemoteType, SomeRemoteThing, SomeType, SomeThing> type();

    /**
     * Retrieves a Relations which the Thing takes part in, which may optionally be narrowed to a particular set
     * according to the Role you are interested in.
     *
     * @param roles An optional parameter which allows you to specify the role of the relations you wish to retrieve.
     * @return A set of Relations which the concept instance takes part in, optionally constrained by the Role Type.
     * @see RemoteRole
     * @see RemoteRelation
     */
    @CheckReturnValue
    Stream<RemoteRelation> relations(RemoteRole... roles);

    /**
     * Determine the Roles that this Thing is currently playing.
     *
     * @return A set of all the Roles which this Thing is currently playing.
     * @see RemoteRole
     */
    @CheckReturnValue
    Stream<RemoteRole> roles();

    /**
     * Creates a Relation from this Thing to the provided Attribute.
     * This has the same effect as #relhas(Attribute), but returns the instance itself to allow
     * method chaining.
     *
     * @param attribute The Attribute to which a Relation is created
     * @return The instance itself
     */
    SomeRemoteThing has(Attribute<?> attribute);

    /**
     * Creates a Relation from this instance to the provided Attribute.
     * This has the same effect as #has(Attribute), but returns the new Relation.
     *
     * @param attribute The Attribute to which a Relation is created
     * @return The Relation connecting the Thing and the Attribute
     */
    RemoteRelation relhas(Attribute<?> attribute);

    /**
     * Retrieves a collection of Attribute attached to this Thing
     *
     * @param attributeTypes AttributeTypes of the Attributes attached to this entity
     * @return A collection of AttributeTypes attached to this Thing.
     * @see RemoteAttribute
     */
    @CheckReturnValue
    Stream<RemoteAttribute<?>> attributes(AttributeType<?>... attributeTypes);

    /**
     * Retrieves a collection of Attribute attached to this Thing as a key
     *
     * @param attributeTypes AttributeTypes of the Attributes attached to this entity
     * @return A collection of AttributeTypes attached to this Thing.
     * @see RemoteAttribute
     */
    @CheckReturnValue
    Stream<RemoteAttribute<?>> keys(AttributeType<?>... attributeTypes);

    /**
     * Removes the provided Attribute from this Thing
     *
     * @param attribute the Attribute to be removed
     * @return The Thing itself
     */
    SomeRemoteThing unhas(Attribute<?> attribute);

    /**
     * Used to indicate if this Thing has been created as the result of a Rule inference.
     *
     * @return true if this Thing exists due to a rule
     * @see RemoteRule
     */
    boolean isInferred();

    //------------------------------------- Other ---------------------------------
    @Deprecated
    @CheckReturnValue
    @Override
    default RemoteThing<SomeRemoteThing, SomeRemoteType, SomeThing, SomeType> asThing() {
        return this;
    }

    @Deprecated
    @CheckReturnValue
    @Override
    default boolean isThing() {
        return true;
    }
}
