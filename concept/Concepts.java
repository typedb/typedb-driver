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

package grakn.client.concept;

import grakn.client.concept.thing.Thing;
import grakn.client.concept.type.AttributeType;
import grakn.client.concept.type.EntityType;
import grakn.client.concept.type.RelationType;
import grakn.client.concept.type.RoleType;
import grakn.client.concept.type.Rule;
import grakn.client.concept.type.ThingType;
import grakn.client.concept.type.Type;
import grakn.protocol.ConceptProto;
import grakn.protocol.TransactionProto;
import graql.lang.pattern.Pattern;

import javax.annotation.Nullable;
import java.util.function.Function;
import java.util.stream.Stream;

public interface Concepts {

    ThingType.Remote getRootType();

    EntityType.Remote getRootEntityType();

    RelationType.Remote getRootRelationType();

    AttributeType.Remote getRootAttributeType();

    RoleType.Remote getRootRoleType();

    Rule.Remote getRootRule();

    EntityType.Remote putEntityType(String label);

    @Nullable
    EntityType.Remote getEntityType(String label);

    RelationType.Remote putRelationType(String label);

    @Nullable
    RelationType.Remote getRelationType(String label);

    AttributeType.Remote putAttributeType(String label, AttributeType.ValueType valueType);

    @Nullable
    AttributeType.Remote getAttributeType(String label);

    Rule.Remote putRule(String label, Pattern when, Pattern then);

    @Nullable
    Rule.Remote getRule(String label);

    @Nullable
    Type.Remote getType(String label);

    @Nullable
    Thing.Remote getThing(String iid);

    TransactionProto.Transaction.Res runThingMethod(String iid, ConceptProto.ThingMethod.Req thingMethod);

    TransactionProto.Transaction.Res runTypeMethod(String label, ConceptProto.TypeMethod.Req typeMethod);

    <T> Stream<T> iterateThingMethod(String iid, ConceptProto.ThingMethod.Iter.Req thingMethod, Function<ConceptProto.ThingMethod.Iter.Res, T> responseReader);

    <T> Stream<T> iterateTypeMethod(String label, ConceptProto.TypeMethod.Iter.Req typeMethod, Function<ConceptProto.TypeMethod.Iter.Res, T> responseReader);
}
