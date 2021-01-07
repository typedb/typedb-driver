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

import AnswerProto from "grakn-protocol/protobuf/answer_pb";
import ConceptProto from "grakn-protocol/protobuf/concept_pb";

import {
    Concept,
    ErrorMessage,
    GraknClientError, ThingImpl, TypeImpl,
} from "../../dependencies_internal";

export class ConceptMap {
    private readonly _map: Map<string, Concept>;

    constructor(map: Map<string, Concept>){
        this._map = map;
    }

    static of(res: AnswerProto.ConceptMap): ConceptMap {
        const variableMap = new Map<string, Concept>();
        res.getMapMap().forEach((resConcept: ConceptProto.Concept, resLabel: string) => {
            let concept;
            if (resConcept.hasThing()) concept = ThingImpl.of(resConcept.getThing());
            else concept = TypeImpl.of(resConcept.getType());
            variableMap.set(resLabel, concept);
        })
        return new ConceptMap(variableMap);
    }

    map(): Map<string, Concept> {return this._map;}
    concepts(): IterableIterator<Concept> {return this._map.values();}

    get(variable: string): Concept {
        const concept = this._map.get(variable);
        if (concept == null) throw new GraknClientError(ErrorMessage.Query.VARIABLE_DOES_NOT_EXIST.message(variable))
        return concept;
    }

    toString(): string {
        let output = "";
        for (const entry of this._map.entries()) {
            output += `[${entry[0]}/${entry[1]}]`
        }
        return output;
    }
}
