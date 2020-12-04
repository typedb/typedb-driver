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

import AnswerProto from "graknlabs-grpc-protocol/protobuf/answer_pb";
import ConceptProto from "graknlabs-grpc-protocol/protobuf/concept_pb";

import {
    AnswerGroup,
    Concept, ConceptProtoReader,
    ErrorMessage,
    GraknClientError,
} from "../../dependencies_internal";

export class ConceptMap {
    private readonly _map: Map<string, Concept>;
    private readonly _queryPattern: string;

    constructor(map: Map<string, Concept>, pattern: string){
        this._map = map;
        this._queryPattern = pattern;
    }

    static of(res: AnswerProto.ConceptMap): ConceptMap {
        const variableMap = new Map<string, Concept>();
        res.getMapMap().forEach((resConcept: ConceptProto.Concept, resLabel: string) => {
            let concept;
            if (resConcept.hasThing()) concept = ConceptProtoReader.thing(resConcept.getThing());
            else concept = ConceptProtoReader.type(resConcept.getType());
            variableMap.set(resLabel, concept);
        })
        const queryPattern = res.getPattern() === "" ? null : res.getPattern();
        return new ConceptMap(variableMap, queryPattern);
    }

    queryPattern(): string {return this._queryPattern;}
    map(): Map<string, Concept> {return this._map;}
    concepts(): IterableIterator<Concept> {return this._map.values();}

    get(variable: string): Concept {
        const concept = this._map.get(variable);
        if (concept == null) throw new GraknClientError(ErrorMessage.Query.VARIABLE_DOES_NOT_EXIST.message(variable))
        return concept;
    }

    toString(): string {
        let output = "";
        for (let entry of this._map.entries()) {
            output += `[${entry[0]}/${entry[1]}]`
        }
        return output;
    }
}
