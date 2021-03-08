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

import { Concept, RemoteConcept, GraknClient } from "../../dependencies_internal";

export abstract class ConceptImpl implements Concept {
    abstract asRemote(transaction: GraknClient.Transaction): RemoteConcept

    isRemote(): boolean {
        return false;
    }

    isType(): boolean {
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

    isRoleType(): boolean {
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

    abstract equals(concept: Concept): boolean;

}

export abstract class RemoteConceptImpl implements RemoteConcept {
    abstract asRemote(transaction: GraknClient.Transaction): RemoteConcept
    abstract delete(): Promise<void>
    abstract isDeleted(): Promise<boolean>

    isRemote(): boolean {
        return true;
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

    abstract equals(concept: Concept): boolean;
}
