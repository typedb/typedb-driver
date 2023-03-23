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

import { Concept } from "../api/concept/Concept";
import { Attribute } from "../api/concept/thing/Attribute";
import { Entity } from "../api/concept/thing/Entity";
import { Relation } from "../api/concept/thing/Relation";
import { Thing } from "../api/concept/thing/Thing";
import { AttributeType } from "../api/concept/type/AttributeType";
import { EntityType } from "../api/concept/type/EntityType";
import { RelationType } from "../api/concept/type/RelationType";
import { RoleType } from "../api/concept/type/RoleType";
import { ThingType } from "../api/concept/type/ThingType";
import { Type } from "../api/concept/type/Type";
import { TypeDBTransaction } from "../api/connection/TypeDBTransaction";
import { ErrorMessage } from "../common/errors/ErrorMessage";
import { TypeDBClientError } from "../common/errors/TypeDBClientError";
import INVALID_CONCEPT_CASTING = ErrorMessage.Concept.INVALID_CONCEPT_CASTING;

export abstract class ConceptImpl implements Concept {

    protected abstract get className(): string;

    abstract asRemote(transaction: TypeDBTransaction): Concept.Remote;

    isRemote(): boolean {
        return false;
    }

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

    abstract JSON(): Record<string, boolean | string | number>;
}

export namespace ConceptImpl {

    export abstract class Remote extends ConceptImpl implements Concept.Remote {

        private readonly _transaction: TypeDBTransaction.Extended;

        protected constructor(transaction: TypeDBTransaction.Extended, ..._: any) {
            super();
            if (!transaction) throw new TypeDBClientError(ErrorMessage.Concept.MISSING_TRANSACTION);
            this._transaction = transaction;
        }

        protected get transaction() {
            return this._transaction;
        }

        abstract delete(): Promise<void>;

        abstract isDeleted(): Promise<boolean>;

        asAttribute(): Attribute.Remote {
            return super.asAttribute() as Attribute.Remote;
        }

        asAttributeType(): AttributeType.Remote {
            return super.asAttributeType() as AttributeType.Remote;
        }

        asEntity(): Entity.Remote {
            return super.asEntity() as Entity.Remote;
        }

        asEntityType(): EntityType.Remote {
            return super.asEntityType() as EntityType.Remote;
        }

        asRelation(): Relation.Remote {
            return super.asRelation() as Relation.Remote;
        }

        asRelationType(): RelationType.Remote {
            return super.asRelationType() as RelationType.Remote;
        }

        asRoleType(): RoleType.Remote {
            return super.asRoleType() as RoleType.Remote;
        }

        asThing(): Thing.Remote {
            return super.asThing() as Thing.Remote;
        }

        asThingType(): ThingType.Remote {
            return super.asThingType() as ThingType.Remote;
        }

        asType(): Type.Remote {
            return super.asType() as Type.Remote;
        }
    }
}
