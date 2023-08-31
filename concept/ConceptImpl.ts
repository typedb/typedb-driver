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

import {Attribute} from "../api/concept/thing/Attribute";
import {AttributeType} from "../api/concept/type/AttributeType";
import {Concept} from "../api/concept/Concept";
import {Entity} from "../api/concept/thing/Entity";
import {EntityType} from "../api/concept/type/EntityType";
import {ErrorMessage} from "../common/errors/ErrorMessage";
import {Relation} from "../api/concept/thing/Relation";
import {RelationType} from "../api/concept/type/RelationType";
import {RoleType} from "../api/concept/type/RoleType";
import {Thing} from "../api/concept/thing/Thing";
import {ThingType} from "../api/concept/type/ThingType";
import {Type} from "../api/concept/type/Type";
import {TypeDBClientError} from "../common/errors/TypeDBClientError";
import {Value} from "../api/concept/value/Value";
import INVALID_CONCEPT_CASTING = ErrorMessage.Concept.INVALID_CONCEPT_CASTING;

export abstract class ConceptImpl implements Concept {
    protected abstract get className(): string;

    isType(): boolean {
        return false;
    }

    isRoleType(): boolean {
        return false;
    }

    isThingType(): boolean {
        return false;
    }

    isEntityType(): boolean {
        return false;
    }

    isAttributeType(): boolean {
        return false;
    }

    isRelationType(): boolean {
        return false;
    }

    isThing(): boolean {
        return false;
    }

    isEntity(): boolean {
        return false;
    }

    isAttribute(): boolean {
        return false;
    }

    isRelation(): boolean {
        return false;
    }

    isValue(): boolean {
        return false;
    }

    asAttribute(): Attribute {
        throw new TypeDBClientError(INVALID_CONCEPT_CASTING.message(this.className, "Attribute"));
    }

    asAttributeType(): AttributeType {
        throw new TypeDBClientError(INVALID_CONCEPT_CASTING.message(this.className, "AttributeType"));
    }

    asEntity(): Entity {
        throw new TypeDBClientError(INVALID_CONCEPT_CASTING.message(this.className, "Entity"));
    }

    asEntityType(): EntityType {
        throw new TypeDBClientError(INVALID_CONCEPT_CASTING.message(this.className, "EntityType"));
    }

    asRelation(): Relation {
        throw new TypeDBClientError(INVALID_CONCEPT_CASTING.message(this.className, "Relation"));
    }

    asValue(): Value {
        throw new TypeDBClientError(INVALID_CONCEPT_CASTING.message(this.className, "Value"));
    }

    asRelationType(): RelationType {
        throw new TypeDBClientError(INVALID_CONCEPT_CASTING.message(this.className, "RelationType"));
    }

    asRoleType(): RoleType {
        throw new TypeDBClientError(INVALID_CONCEPT_CASTING.message(this.className, "RoleType"));
    }

    asThing(): Thing {
        throw new TypeDBClientError(INVALID_CONCEPT_CASTING.message(this.className, "Thing"));
    }

    asThingType(): ThingType {
        throw new TypeDBClientError(INVALID_CONCEPT_CASTING.message(this.className, "ThingType"));
    }

    asType(): Type {
        throw new TypeDBClientError(INVALID_CONCEPT_CASTING.message(this.className, "Type"));
    }

    abstract equals(concept: Concept): boolean;

    abstract toJSONRecord(): Record<string, boolean | string | number>;
}
