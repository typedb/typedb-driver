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

import {
    ConceptMap as ConceptMapProto,
    Explainable as ExplainableProto, Explainables,
    Explainables as ExplainablesProto
} from "grakn-protocol/common/answer_pb";
import {Concept as ConceptProto} from "grakn-protocol/common/concept_pb";
import {ConceptMap} from "../../api/answer/ConceptMap";
import {Concept} from "../../api/concept/Concept";
import {ThingImpl, TypeImpl} from "../../dependencies_internal";
import {ErrorMessage} from "../../common/errors/ErrorMessage";
import {GraknClientError} from "../../common/errors/GraknClientError";

export class ConceptMapImpl implements ConceptMap {

    private _concepts: Map<string, Concept>;
    private _explainables: ConceptMap.Explainables;

    constructor(concepts: Map<string, Concept>, explainables: ConceptMap.Explainables) {
        this._concepts = concepts;
        this._explainables = explainables;
    }

    concepts(): IterableIterator<Concept> {
        return this._concepts.values();
    }

    get(variable: string): Concept {
        return this._concepts.get(variable);
    }

    map(): Map<string, Concept> {
        return this._concepts;
    }

    explainables(): ConceptMap.Explainables {
        return this._explainables;
    }

}

/* eslint no-inner-declarations: "off" */
export namespace ConceptMapImpl {

    import NONEXISTENT_EXPLAINABLE_CONCEPT = ErrorMessage.Query.NONEXISTENT_EXPLAINABLE_CONCEPT;
    import NONEXISTENT_EXPLAINABLE_OWNERSHIP = ErrorMessage.Query.NONEXISTENT_EXPLAINABLE_OWNERSHIP;
    import Owned = Explainables.Owned;

    function ofExplainables(proto: ExplainablesProto): ConceptMap.Explainables {
        const relations = new Map<string, ConceptMap.Explainable>();
        proto.getRelationsMap().forEach((explainable: ExplainableProto, variable: string) =>
            relations.set(variable, ofExplainable(explainable))
        );
        const attributes = new Map<string, ConceptMap.Explainable>();
        proto.getAttributesMap().forEach((explainable: ExplainableProto, variable: string) =>
            relations.set(variable, ofExplainable(explainable))
        );

        const ownerships = new Map<[string, string], ConceptMap.Explainable>();
        proto.getOwnershipsMap().forEach((owned: Owned, owner: string) =>
            owned.getOwnedMap().forEach((explainable: ExplainableProto, attribute: string) => {
                ownerships.set([owner, attribute], ofExplainable(explainable))
            })
        );
        return new ExplainablesImpl(relations, attributes, ownerships)
    }

    export function of(proto: ConceptMapProto): ConceptMap {
        const variableMap = new Map<string, Concept>();
        proto.getMapMap().forEach((protoConcept: ConceptProto, resLabel: string) => {
            let concept;
            if (protoConcept.hasThing()) concept = ThingImpl.of(protoConcept.getThing());
            else concept = TypeImpl.of(protoConcept.getType());
            variableMap.set(resLabel, concept);
        })
        return new ConceptMapImpl(variableMap, ofExplainables(proto.getExplainables()));
    }

    function ofExplainable(proto: ExplainableProto): ConceptMap.Explainable {
        return new ExplainableImpl(proto.getConjunction(), proto.getId());
    }

    export class ExplainablesImpl implements ConceptMap.Explainables {
        private _relations: Map<string, ConceptMap.Explainable>;
        private _attributes: Map<string, ConceptMap.Explainable>;
        private _ownerships: Map<[string, string], ConceptMap.Explainable>;

        constructor(relations: Map<string, ConceptMap.Explainable>, attributes: Map<string, ConceptMap.Explainable>,
                    ownerships: Map<[string, string], ConceptMap.Explainable>) {
            this._relations = relations;
            this._attributes = attributes;
            this._ownerships = ownerships;
        }

        relation(variable: string): ConceptMap.Explainable {
            const explainable = this._relations.get(variable);
            if (!explainable) throw new GraknClientError(NONEXISTENT_EXPLAINABLE_CONCEPT.message(variable));
            return explainable;
        }

        attribute(variable: string): ConceptMap.Explainable {
            const explainable = this._attributes.get(variable);
            if (!explainable) throw new GraknClientError(NONEXISTENT_EXPLAINABLE_CONCEPT.message(variable));
            return explainable;
        }

        ownership(owner: string, attribute: string): ConceptMap.Explainable {
            for (const entry of this._ownerships) {
                if (entry[0][0] === owner && entry[0][1] === attribute) return entry[1];
            }
            throw new GraknClientError(NONEXISTENT_EXPLAINABLE_OWNERSHIP.message(owner, attribute));
        }

        relations(): Map<string, ConceptMap.Explainable> {
            return this._relations;
        }

        attributes(): Map<string, ConceptMap.Explainable> {
            return this._attributes;
        }

        ownerships(): Map<[string, string], ConceptMap.Explainable> {
            return this._ownerships;
        }

    }

    export class ExplainableImpl implements ConceptMap.Explainable {
        private _conjunction: string;
        private _id: number;

        constructor(conjunction: string, id: number) {
            this._conjunction = conjunction;
            this._id = id;
        }

        conjunction(): string {
            return this._conjunction;
        }

        id(): number {
            return this._id;
        }

    }

}