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
import grakn.client.common.exception.GraknException;
import grakn.client.concept.Concept;

import javax.annotation.Nullable;
import java.util.stream.Stream;

import static grakn.client.common.exception.ErrorMessage.Concept.INVALID_CONCEPT_CASTING;
import static grakn.common.util.Objects.className;

public interface Type extends Concept {

    String getLabel();

    boolean isRoot();

    ThingType asThingType();

    EntityType asEntityType();

    AttributeType asAttributeType();

    RelationType asRelationType();

    RoleType asRoleType();

    @Override
    Remote asRemote(Grakn.Transaction transaction);

    interface Local extends Type, Concept.Local {

        @Override
        default Type.Local asType() {
            return this;
        }

        @Override
        default ThingType.Local asThingType() {
            throw new GraknException(INVALID_CONCEPT_CASTING.message(this, className(ThingType.class)));
        }

        @Override
        default EntityType.Local asEntityType() {
            throw new GraknException(INVALID_CONCEPT_CASTING.message(this, className(EntityType.class)));
        }

        @Override
        default AttributeType.Local asAttributeType() {
            throw new GraknException(INVALID_CONCEPT_CASTING.message(this, className(AttributeType.class)));
        }

        @Override
        default RelationType.Local asRelationType() {
            throw new GraknException(INVALID_CONCEPT_CASTING.message(this, className(RelationType.class)));
        }

        @Override
        default RoleType.Local asRoleType() {
            throw new GraknException(INVALID_CONCEPT_CASTING.message(this, className(RoleType.class)));
        }
    }

    interface Remote extends Type, Concept.Remote {

        void setLabel(String label);

        boolean isAbstract();

        @Nullable
        Type.Local getSupertype();

        Stream<? extends Type.Local> getSupertypes();

        Stream<? extends Type.Local> getSubtypes();

        @Override
        default Type.Remote asType() {
            return this;
        }

        @Override
        default ThingType.Remote asThingType() {
            throw new GraknException(INVALID_CONCEPT_CASTING.message(this, className(ThingType.class)));
        }

        @Override
        default EntityType.Remote asEntityType() {
            throw new GraknException(INVALID_CONCEPT_CASTING.message(this, className(EntityType.class)));
        }

        @Override
        default RelationType.Remote asRelationType() {
            throw new GraknException(INVALID_CONCEPT_CASTING.message(this, className(RelationType.class)));
        }

        @Override
        default AttributeType.Remote asAttributeType() {
            throw new GraknException(INVALID_CONCEPT_CASTING.message(this, className(AttributeType.class)));
        }

        @Override
        default RoleType.Remote asRoleType() {
            throw new GraknException(INVALID_CONCEPT_CASTING.message(this, className(RoleType.class)));
        }
    }
}
