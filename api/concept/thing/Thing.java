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

package grakn.client.api.concept.thing;

import grakn.client.api.GraknTransaction;
import grakn.client.api.concept.Concept;
import grakn.client.api.concept.type.AttributeType;
import grakn.client.api.concept.type.RoleType;
import grakn.client.api.concept.type.ThingType;

import javax.annotation.CheckReturnValue;
import java.util.stream.Stream;

public interface Thing extends Concept {

    @CheckReturnValue
    String getIID();

    @CheckReturnValue
    ThingType getType();

    @Override
    @CheckReturnValue
    default boolean isThing() {
        return true;
    }

    @Override
    @CheckReturnValue
    Thing.Remote asRemote(GraknTransaction transaction);

    interface Remote extends Concept.Remote, Thing {

        void setHas(Attribute<?> attribute);

        void unsetHas(Attribute<?> attribute);

        @CheckReturnValue
        boolean isInferred();

        @CheckReturnValue
        Stream<? extends Attribute<?>> getHas(boolean onlyKey);

        @CheckReturnValue
        Stream<? extends Attribute.Boolean> getHas(AttributeType.Boolean attributeType);

        @CheckReturnValue
        Stream<? extends Attribute.Long> getHas(AttributeType.Long attributeType);

        @CheckReturnValue
        Stream<? extends Attribute.Double> getHas(AttributeType.Double attributeType);

        @CheckReturnValue
        Stream<? extends Attribute.String> getHas(AttributeType.String attributeType);

        @CheckReturnValue
        Stream<? extends Attribute.DateTime> getHas(AttributeType.DateTime attributeType);

        @CheckReturnValue
        Stream<? extends Attribute<?>> getHas(AttributeType... attributeTypes);

        @CheckReturnValue
        Stream<? extends Relation> getRelations(RoleType... roleTypes);

        @CheckReturnValue
        Stream<? extends RoleType> getPlaying();
    }
}
