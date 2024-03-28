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
    Explainable as ExplainableProto,
    Explainables as ExplainablesProto,
    ExplainablesOwned as ExplainablesOwnedProto
} from "typedb-protocol/proto/answer";
import {Concept as ConceptProto} from "typedb-protocol/proto/concept";
import {ConceptMap} from "../../api/answer/ConceptMap";
import {Concept} from "../../api/concept/Concept";
import {ErrorMessage} from "../../common/errors/ErrorMessage";
import {TypeDBDriverError} from "../../common/errors/TypeDBDriverError";
import {ResponseReader} from "../../common/rpc/ResponseReader";

export class ConceptMapImpl implements ConceptMap {
    private readonly _concepts: Map<string, Concept>;
    private readonly _explainables: ConceptMap.Explainables;

    constructor(concepts: Map<string, Concept>, explainables: ConceptMap.Explainables) {
        this._concepts = concepts;
        this._explainables = explainables;
    }

    variables(): IterableIterator<string> {
        return this._concepts.keys();
    }

    concepts(): IterableIterator<Concept> {
        return this._concepts.values();
    }

    map(): Map<string, Concept> {
        return this._concepts;
    }

    get(variable: string): Concept {
        return this._concepts.get(variable);
    }

    get explainables(): ConceptMap.Explainables {
        return this._explainables;
    }
}

/* eslint no-inner-declarations: "off" */
export namespace ConceptMapImpl {
    import NONEXISTENT_EXPLAINABLE_CONCEPT = ErrorMessage.Query.NONEXISTENT_EXPLAINABLE_CONCEPT;
    import NONEXISTENT_EXPLAINABLE_OWNERSHIP = ErrorMessage.Query.NONEXISTENT_EXPLAINABLE_OWNERSHIP;

    export function of(proto: ConceptMapProto): ConceptMap {
        const variableMap = new Map<string, Concept>();
        proto.map.forEach((proto: ConceptProto, resLabel: string) => variableMap.set(resLabel, ResponseReader.Concept.of(proto)));
        const explainables = proto.has_explainables ? ofExplainables(proto.explainables) : emptyExplainables();
        return new ConceptMapImpl(variableMap, explainables);
    }

    function ofExplainables(proto: ExplainablesProto): ConceptMap.Explainables {
        const relations = new Map<string, ConceptMap.Explainable>();
        proto.relations.forEach((explainable: ExplainableProto, variable: string) =>
            relations.set(variable, ofExplainable(explainable))
        );
        const attributes = new Map<string, ConceptMap.Explainable>();
        proto.attributes.forEach((explainable: ExplainableProto, variable: string) =>
            relations.set(variable, ofExplainable(explainable))
        );
        const ownerships = new Map<[string, string], ConceptMap.Explainable>();
        proto.ownerships.forEach((owned: ExplainablesOwnedProto, owner: string) =>
            owned.owned.forEach((explainable: ExplainableProto, attribute: string) => {
                ownerships.set([owner, attribute], ofExplainable(explainable))
            })
        );
        return new ExplainablesImpl(relations, attributes, ownerships)
    }

    function emptyExplainables() {
        return new ExplainablesImpl(
            new Map<string, ConceptMap.Explainable>(),
            new Map<string, ConceptMap.Explainable>(),
            new Map<[string, string], ConceptMap.Explainable>()
        );
    }

    function ofExplainable(proto: ExplainableProto): ConceptMap.Explainable {
        return new ExplainableImpl(proto.conjunction, proto.id);
    }

    export class ExplainablesImpl implements ConceptMap.Explainables {
        private readonly _relations: Map<string, ConceptMap.Explainable>;
        private readonly _attributes: Map<string, ConceptMap.Explainable>;
        private readonly _ownerships: Map<[string, string], ConceptMap.Explainable>;

        constructor(relations: Map<string, ConceptMap.Explainable>, attributes: Map<string, ConceptMap.Explainable>,
                    ownerships: Map<[string, string], ConceptMap.Explainable>) {
            this._relations = relations;
            this._attributes = attributes;
            this._ownerships = ownerships;
        }

        relation(variable: string): ConceptMap.Explainable {
            const explainable = this._relations.get(variable);
            if (!explainable) throw new TypeDBDriverError(NONEXISTENT_EXPLAINABLE_CONCEPT.message(variable));
            return explainable;
        }

        attribute(variable: string): ConceptMap.Explainable {
            const explainable = this._attributes.get(variable);
            if (!explainable) throw new TypeDBDriverError(NONEXISTENT_EXPLAINABLE_CONCEPT.message(variable));
            return explainable;
        }

        ownership(owner: string, attribute: string): ConceptMap.Explainable {
            for (const entry of this._ownerships) {
                if (entry[0][0] === owner && entry[0][1] === attribute) return entry[1];
            }
            throw new TypeDBDriverError(NONEXISTENT_EXPLAINABLE_OWNERSHIP.message(owner, attribute));
        }

        get relations(): Map<string, ConceptMap.Explainable> {
            return this._relations;
        }

        get attributes(): Map<string, ConceptMap.Explainable> {
            return this._attributes;
        }

        get ownerships(): Map<[string, string], ConceptMap.Explainable> {
            return this._ownerships;
        }
    }

    export class ExplainableImpl implements ConceptMap.Explainable {
        private readonly _conjunction: string;
        private readonly _id: number;

        constructor(conjunction: string, id: number) {
            this._conjunction = conjunction;
            this._id = id;
        }

        get conjunction(): string {
            return this._conjunction;
        }

        get id(): number {
            return this._id;
        }
    }
}
