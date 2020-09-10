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
import grakn.client.common.exception.GraknClientException;
import grakn.client.concept.Concept;
import grakn.client.concept.type.AttributeType;
import grakn.client.concept.type.RoleType;
import grakn.client.concept.type.ThingType;

import java.util.stream.Stream;

import static grakn.client.common.exception.ErrorMessage.Concept.INVALID_CONCEPT_CASTING;

public interface Thing extends Concept {

    String getIID();

    Entity asEntity();

    Attribute<?> asAttribute();

    Relation asRelation();

    @Override
    Thing.Remote asRemote(Grakn.Transaction transaction);

    interface Local extends Concept.Local, Thing {

        @Override
        Thing.Local asThing();

        @Override
        Entity.Local asEntity();

        @Override
        Attribute.Local<?> asAttribute();

        @Override
        Relation.Local asRelation();
    }

    interface Remote extends Concept.Remote, Thing {

        ThingType.Local getType();

        void setHas(Attribute<?> attribute);

        void unsetHas(Attribute<?> attribute);

        boolean isInferred();

        Stream<? extends Attribute.Local<?>> getHas(boolean onlyKey);

        Stream<? extends Attribute.Boolean.Local> getHas(AttributeType.Boolean attributeType);

        Stream<? extends Attribute.Long.Local> getHas(AttributeType.Long attributeType);

        Stream<? extends Attribute.Double.Local> getHas(AttributeType.Double attributeType);

        Stream<? extends Attribute.String.Local> getHas(AttributeType.String attributeType);

        Stream<? extends Attribute.DateTime.Local> getHas(AttributeType.DateTime attributeType);

        Stream<? extends Attribute.Local<?>> getHas(AttributeType... attributeTypes);

        Stream<? extends RoleType.Local> getPlays();

        Stream<? extends Relation.Local> getRelations(RoleType... roleTypes);

        @Override
        Thing.Remote asThing();

        @Override
        Entity.Remote asEntity();

        @Override
        Relation.Remote asRelation();

        @Override
        Attribute.Remote<?> asAttribute();
    }
}
