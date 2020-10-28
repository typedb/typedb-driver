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

package grakn.client.concept.type;

import grakn.client.Grakn;
import grakn.client.concept.thing.Thing;
import grakn.client.concept.type.AttributeType.ValueType;

import javax.annotation.CheckReturnValue;
import java.util.stream.Stream;

public interface ThingType extends Type {

    @Override
    ThingType.Remote asRemote(Grakn.Transaction transaction);

    interface Remote extends Type.Remote, ThingType {

        @Override
        ThingType getSupertype();

        @Override
        Stream<? extends ThingType> getSupertypes();

        @Override
        Stream<? extends ThingType> getSubtypes();

        @CheckReturnValue
        Stream<? extends Thing> getInstances();

        @Override
        void setLabel(String label);

        void setAbstract();

        void unsetAbstract();

        void setPlays(RoleType role);

        void setPlays(RoleType roleType, RoleType overriddenType);

        void setOwns(AttributeType attributeType, AttributeType otherType, boolean isKey);

        void setOwns(AttributeType attributeType, AttributeType overriddenType);

        void setOwns(AttributeType attributeType, boolean isKey);

        void setOwns(AttributeType attributeType);

        @CheckReturnValue
        Stream<? extends RoleType> getPlays();

        @CheckReturnValue
        Stream<? extends AttributeType> getOwns();

        @CheckReturnValue
        Stream<? extends AttributeType> getOwns(ValueType valueType);

        @CheckReturnValue
        Stream<? extends AttributeType> getOwns(boolean keysOnly);

        @CheckReturnValue
        Stream<? extends AttributeType> getOwns(ValueType valueType, boolean keysOnly);

        void unsetPlays(RoleType role);

        void unsetOwns(AttributeType attributeType);
    }
}
