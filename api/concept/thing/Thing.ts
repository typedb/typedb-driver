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

import { RequestBuilder } from "../../../common/rpc/RequestBuilder";
import { Stream } from "../../../common/util/Stream";
import { TypeDBTransaction } from "../../connection/TypeDBTransaction";
import { Concept } from "../Concept";
import { AttributeType } from "../type/AttributeType";
import { EntityType } from "../type/EntityType";
import { RelationType } from "../type/RelationType";
import { RoleType } from "../type/RoleType";
import { ThingType } from "../type/ThingType";
import { Type } from "../type/Type";
import { Attribute } from "./Attribute";
import { Entity } from "./Entity";
import { Relation } from "./Relation";

export interface Thing extends Concept {

    asRemote(transaction: TypeDBTransaction): Thing.Remote;

    readonly iid: string;

    readonly type: ThingType;

    readonly inferred: boolean;
}

export namespace Thing {

    import Annotation = ThingType.Annotation;

    export interface Remote extends Thing, Concept.Remote {

        asRemote(transaction: TypeDBTransaction): Thing.Remote;

        setHas(attribute: Attribute): Promise<void>;

        unsetHas(attribute: Attribute): Promise<void>;

        getHas(annotations: Annotation[]): Stream<Attribute>;

        getHas(attributeType: AttributeType.Boolean): Stream<Attribute.Boolean>;

        getHas(attributeType: AttributeType.Long): Stream<Attribute.Long>;

        getHas(attributeType: AttributeType.Double): Stream<Attribute.Double>;

        getHas(attributeType: AttributeType.String): Stream<Attribute.String>;

        getHas(attributeType: AttributeType.DateTime): Stream<Attribute.DateTime>;

        getHas(): Stream<Attribute>;

        getHas(attributeTypes: AttributeType[]): Stream<Attribute>;

        getPlaying(): Stream<RoleType>;

        getRelations(): Stream<Relation>;

        getRelations(roleTypes: RoleType[]): Stream<Relation>;

        asType(): Type.Remote;

        asThingType(): ThingType.Remote;

        asEntityType(): EntityType.Remote;

        asAttributeType(): AttributeType.Remote;

        asRelationType(): RelationType.Remote;

        asRoleType(): RoleType.Remote;

        asThing(): Thing.Remote;

        asEntity(): Entity.Remote;

        asAttribute(): Attribute.Remote;

        asRelation(): Relation.Remote;
    }

    export function proto(thing: Thing) {
        return RequestBuilder.Thing.protoThing(thing.iid);
    }
}
