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

package grakn.client.concept.thing;

import grakn.client.Grakn;
import grakn.client.concept.Concept;
import grakn.client.concept.type.AttributeType;
import grakn.client.concept.type.RoleType;
import grakn.client.concept.type.ThingType;

import java.util.stream.Stream;

public interface Thing extends Concept {

    String getIID();

    @Override
    default boolean isThing() {
        return true;
    }

    @Override
    Thing.Remote asRemote(Grakn.Transaction transaction);

    interface Remote extends Concept.Remote, Thing {

        ThingType getType();

        void setHas(Attribute<?> attribute);

        void unsetHas(Attribute<?> attribute);

        boolean isInferred();

        Stream<? extends Attribute<?>> getHas(boolean onlyKey);

        Stream<? extends Attribute.Boolean> getHas(AttributeType.Boolean attributeType);

        Stream<? extends Attribute.Long> getHas(AttributeType.Long attributeType);

        Stream<? extends Attribute.Double> getHas(AttributeType.Double attributeType);

        Stream<? extends Attribute.String> getHas(AttributeType.String attributeType);

        Stream<? extends Attribute.DateTime> getHas(AttributeType.DateTime attributeType);

        Stream<? extends Attribute<?>> getHas(AttributeType... attributeTypes);

        Stream<? extends RoleType> getPlays();

        Stream<? extends Relation> getRelations(RoleType... roleTypes);
    }
}
