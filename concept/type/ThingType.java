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

import java.util.stream.Stream;

public interface ThingType extends Type {

    @Override
    ThingType.Remote asRemote(Grakn.Transaction transaction);

    interface Local extends Type.Local, ThingType {
    }

    interface Remote extends Type.Remote, ThingType {

        @Override
        ThingType.Local getSupertype();

        @Override
        Stream<? extends ThingType.Local> getSupertypes();

        @Override
        Stream<? extends ThingType.Local> getSubtypes();

        Stream<? extends Thing.Local> getInstances();

        @Override
        void setLabel(String label);

        void setAbstract();

        void unsetAbstract();

        void setPlays(RoleType role);

        void setPlays(RoleType roleType, RoleType overriddenType);

        void setOwns(AttributeType attributeType, AttributeType otherType, boolean isKey);

        default void setOwns(AttributeType attributeType, AttributeType overriddenType) {
            setOwns(attributeType, overriddenType, false);
        }

        default void setOwns(AttributeType attributeType, boolean isKey) {
            setOwns(attributeType, null, isKey);
        }

        default void setOwns(AttributeType attributeType) {
            setOwns(attributeType, false);
        }

        Stream<? extends RoleType.Local> getPlays();

        default Stream<? extends AttributeType.Local> getOwns() {
            return getOwns(false);
        }

        default Stream<? extends AttributeType.Local> getOwns(ValueType valueType) {
            return getOwns(valueType, false);
        }

        default Stream<? extends AttributeType.Local> getOwns(boolean keysOnly) {
            return getOwns(null, keysOnly);
        }

        Stream<? extends AttributeType.Local> getOwns(ValueType valueType, boolean keysOnly);

        void unsetPlays(RoleType role);

        void unsetOwns(AttributeType attributeType);
    }
}
