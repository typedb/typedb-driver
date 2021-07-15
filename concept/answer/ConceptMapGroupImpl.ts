/*
 * Copyright (C) 2021 Vaticle
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

import { ConceptMapGroup as MapGroupProto } from "typedb-protocol/common/answer_pb";
import { ConceptMap } from "../../api/answer/ConceptMap";
import { ConceptMapGroup } from "../../api/answer/ConceptMapGroup";
import { Concept } from "../../api/concept/Concept";
import { ThingImpl, TypeImpl } from "../../dependencies_internal";
import { ConceptMapImpl } from "./ConceptMapImpl";

export class ConceptMapGroupImpl implements ConceptMapGroup {

    private readonly _owner: Concept;
    private readonly _conceptMaps: ConceptMap[];

    constructor(owner: Concept, conceptMaps: ConceptMap[]) {
        this._owner = owner;
        this._conceptMaps = conceptMaps;
    }

    get owner(): Concept {
        return this._owner;
    }

    get conceptMaps(): ConceptMap[] {
        return this._conceptMaps;
    }
}

export namespace ConceptMapGroupImpl {

    export function of(mapGroupProto: MapGroupProto): ConceptMapGroup {
        let owner: Concept;
        if (mapGroupProto.getOwner().hasThing()) owner = ThingImpl.of(mapGroupProto.getOwner().getThing());
        else owner = TypeImpl.of(mapGroupProto.getOwner().getType());
        return new ConceptMapGroupImpl(owner, mapGroupProto.getConceptMapsList()
            .map((conceptMapProto) => ConceptMapImpl.of(conceptMapProto)) as ConceptMap[]);
    }
}
