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
import grakn.client.concept.type.AttributeType;
import grakn.client.concept.type.ThingType;

import java.time.LocalDateTime;
import java.util.stream.Stream;

public interface Attribute<VALUE> extends Thing {

    VALUE getValue();

    @Override
    default boolean isAttribute() {
        return true;
    }

    Attribute.Boolean asBoolean();

    Attribute.Long asLong();

    Attribute.Double asDouble();

    Attribute.String asString();

    Attribute.DateTime asDateTime();

    @Override
    Attribute.Remote<VALUE> asRemote(Grakn.Transaction transaction);

    interface Remote<VALUE> extends Thing.Remote, Attribute<VALUE> {

        Stream<? extends Thing> getOwners();

        Stream<? extends Thing> getOwners(ThingType ownerType);

        @Override
        AttributeType getType();

        @Override
        Attribute.Remote<VALUE> asAttribute();

        @Override
        Attribute.Boolean.Remote asBoolean();

        @Override
        Attribute.Long.Remote asLong();

        @Override
        Attribute.Double.Remote asDouble();

        @Override
        Attribute.String.Remote asString();

        @Override
        Attribute.DateTime.Remote asDateTime();
    }

    interface Boolean extends Attribute<java.lang.Boolean> {

        @Override
        Attribute.Boolean.Remote asRemote(Grakn.Transaction transaction);

        interface Remote extends Attribute.Boolean, Attribute.Remote<java.lang.Boolean> {
        }
    }

    interface Long extends Attribute<java.lang.Long> {

        @Override
        Attribute.Long.Remote asRemote(Grakn.Transaction transaction);

        interface Remote extends Attribute.Long, Attribute.Remote<java.lang.Long> {
        }
    }

    interface Double extends Attribute<java.lang.Double> {

        @Override
        Attribute.Double.Remote asRemote(Grakn.Transaction transaction);

        interface Remote extends Attribute.Double, Attribute.Remote<java.lang.Double> {
        }
    }

    interface String extends Attribute<java.lang.String> {

        @Override
        Attribute.String.Remote asRemote(Grakn.Transaction transaction);

        interface Remote extends Attribute.String, Attribute.Remote<java.lang.String> {
        }
    }

    interface DateTime extends Attribute<LocalDateTime> {

        @Override
        Attribute.DateTime.Remote asRemote(Grakn.Transaction transaction);

        interface Remote extends Attribute.DateTime, Attribute.Remote<LocalDateTime> {
        }
    }
}
