import {When, Then} from "@cucumber/cucumber";
import assert from "assert";
import {analyzed, doAnalyze, setAnalyzed} from "./context";
import {assertNotError, EXPECT_ERROR_CONTAINING, MayError} from "./params";

import {
    QueryConstraintAny,
    QueryVertex,
    FunctionReturnStructure,
    FunctionStructure,
    PipelineStage,
    PipelineStructure,
    Reducer,
} from "../../../dist/index.cjs";
// import {QueryConstraintAny, QueryVertex} from "../../../src";
// import {FunctionReturnStructure, FunctionStructure, PipelineStage, PipelineStructure, Reducer} from "../../../src/analyze";


When('get answers of typeql analyze', async function (query: string) {
    const results = await doAnalyze(query).then(assertNotError);
    setAnalyzed(results.ok);
});

const analyzeMayError = async function (mayError: MayError, query: string) {
    const results = await doAnalyze(query);
    if (mayError) assert.notEqual(results.err, undefined);
    else assert.notEqual(results.ok, undefined);
    try {
        await this.tx().analyze(this.text).resolve();
    } catch (e) {
        if (!mayError) {
            throw e;
        }
    }
}
When('typeql analyze{may_error}', analyzeMayError);
When(`typeql analyze${EXPECT_ERROR_CONTAINING}`, analyzeMayError);

Then('analyzed query pipeline structure is:', function (expectedFunctor: string) {
    const context = new FunctorEncoder(analyzed.structure.query);
    const actualFunctor = encodePipeline(analyzed.structure.query, context);
    assert.equal(normalizeFunctorForCompare(actualFunctor), normalizeFunctorForCompare(expectedFunctor))
});

Then('analyzed query preamble contains:', function (expectedFunctor: string) {
    const preambleFunctors = analyzed.structure.preamble.map(func => {
        const encoder = new FunctorEncoder(func.body);
        return encodeFunction(func, encoder);
    });

    const normalizedExpected = normalizeFunctorForCompare(expectedFunctor);
    const found = preambleFunctors.some(actual =>
        normalizeFunctorForCompare(actual) === normalizedExpected
    );
    assert.ok(found, `Expected to find functor in preamble: ${normalizedExpected}\nFound: ${preambleFunctors.join('\n')}`);
});

Then('analyzed query pipeline annotations are:', function (expectedFunctor: string) {
    assert.ok(false, "Implement me")
});

Then('analyzed preamble annotations contains:', function (expectedFunctor: string) {
    assert.ok(false, "Implement me")
});

Then('analyzed fetch annotations are:', function (expectedFunctor: string) {
    assert.ok(false, "Implement me")
});

function normalizeFunctorForCompare(functor: string): string {
    return functor.toLowerCase().replace(/\s/g, "");
}

// FunctorEncoder class
class FunctorEncoder {
    pipeline: PipelineStructure;

    constructor(pipeline: PipelineStructure) {
        this.pipeline = pipeline;
    }

    makeFunctor(name: string, ...args: any[]): string {
        const encodedArgs = args.map(arg => this.mayEncode(arg)).join(", ");
        return `${name}(${encodedArgs})`;
    }

    encodeAsList(elements: any[]): string {
        return `[${elements.map(e => this.mayEncode(e)).join(", ")}]`;
    }

    encodeAsDict(elements: Record<string, any>): string {
        const entries = Object.entries(elements)
            .map(([k, v]) => `${this.mayEncode(k)} : ${this.mayEncode(v)}`)
            .sort();
        return `{${entries.join(", ")}}`;
    }

    mayEncode(e: any): string {
        if (e === null || e === undefined) {
            return "null";
        } else if (typeof e === "string") {
            return e;
        } else if (Array.isArray(e)) {
            return this.encodeAsList(e);
        } else if (typeof e === "object" && e !== null) {
            if (typeof e.encodeAsFunctor === "function") {
                return e.encodeAsFunctor(this);
            } else if (e.constructor === Object) {
                return this.encodeAsDict(e);
            }
        }
        return String(e);
    }
}

// Helper functions
function encodeConstraint(constraint: QueryConstraintAny, encoder: FunctorEncoder): string {
    switch (constraint.tag) {
        case "isa":
            return encoder.makeFunctor("Isa",
                encodeConstraintVertex(constraint.instance, encoder),
                encodeConstraintVertex(constraint.type, encoder));
        case "isa!":
            return encoder.makeFunctor("IsaExact",
                encodeConstraintVertex(constraint.instance, encoder),
                encodeConstraintVertex(constraint.type, encoder));
        case "has":
            return encoder.makeFunctor("Has",
                encodeConstraintVertex(constraint.owner, encoder),
                encodeConstraintVertex(constraint.attribute, encoder));
        case "links":
            return encoder.makeFunctor("Links",
                encodeConstraintVertex(constraint.relation, encoder),
                encodeConstraintVertex(constraint.player, encoder),
                encodeConstraintVertex(constraint.role, encoder));
        case "sub":
            return encoder.makeFunctor("Sub",
                encodeConstraintVertex(constraint.subtype, encoder),
                encodeConstraintVertex(constraint.supertype, encoder));
        case "sub!":
            return encoder.makeFunctor("SubExact",
                encodeConstraintVertex(constraint.subtype, encoder),
                encodeConstraintVertex(constraint.supertype, encoder));
        case "owns":
            return encoder.makeFunctor("Owns",
                encodeConstraintVertex(constraint.owner, encoder),
                encodeConstraintVertex(constraint.attribute, encoder));
        case "relates":
            return encoder.makeFunctor("Relates",
                encodeConstraintVertex(constraint.relation, encoder),
                encodeConstraintVertex(constraint.role, encoder));
        case "plays":
            return encoder.makeFunctor("Plays",
                encodeConstraintVertex(constraint.player, encoder),
                encodeConstraintVertex(constraint.role, encoder));
        case "expression":
            return encoder.makeFunctor("Expression",
                constraint.text,
                encodeConstraintVertex(constraint.assigned[0], encoder), // TODO: When we break HTTP
                constraint.arguments.map(v => encodeConstraintVertex(v, encoder)));
        case "functionCall":
            return encoder.makeFunctor("FunctionCall",
                constraint.name,
                constraint.assigned.map(v => encodeConstraintVertex(v, encoder)),
                constraint.arguments.map(v => encodeConstraintVertex(v, encoder)));
        case "comparison":
            return encoder.makeFunctor("Comparison",
                encodeConstraintVertex(constraint.lhs, encoder),
                encodeConstraintVertex(constraint.rhs, encoder),
                constraint.comparator);
        case "is":
            return encoder.makeFunctor("Is",
                encodeConstraintVertex(constraint.lhs, encoder),
                encodeConstraintVertex(constraint.rhs, encoder));
        case "iid":
            return encoder.makeFunctor("Iid",
                encodeConstraintVertex(constraint.concept, encoder),
                constraint.iid);
        case "kind":
            return encoder.makeFunctor("Kind",
                constraint.kind,
                encodeConstraintVertex(constraint.type, encoder)
            );
        case "value":
            return encoder.makeFunctor("Value",
                encodeConstraintVertex(constraint.attributeType, encoder),
                constraint.valueType);
        case "label":
            return encoder.makeFunctor("Label",
                encodeConstraintVertex(constraint.type, encoder),
                constraint.label);
        case "or":
            return encoder.makeFunctor(
                "or",
                encoder.encodeAsList(constraint.branches.map(c => encodeConjunction(c, encoder)))
            );
        case "not":
            return encoder.makeFunctor("not", encodeConjunction(constraint.conjunction, encoder));
        case "try":
            return encoder.makeFunctor("try", encodeConjunction(constraint.conjunction, encoder));
    }
}

function encodeVariable(id: string, encoder: FunctorEncoder) {
    const v = encoder.pipeline.variables[id];
    if (v != null && v.name != null) {
        return "$" + v.name;
    } else {
        return "$_";
    }
}

function encodeConstraintVertex(vertex: QueryVertex, encoder: FunctorEncoder): string {
    switch (vertex.tag) {
        case "variable":
            return encodeVariable(vertex.id, encoder);
        case "label":
            return vertex.type.label;
        case "value": {
            if (vertex.valueType == "string") {
                return `"${vertex.value}"`;
            } else {
                return vertex.value;
            }
        }
        // TODO: NamedRole when it comes
    }
    throw new Error("Unknown constraint vertex type");
}

function encodeConjunction(index: number, encoder: FunctorEncoder): string {
    const constraints = encoder.pipeline.conjunctions[index];
    return encoder.encodeAsList(constraints.map(c => encodeConstraint(c, encoder)));
}

function encodeReducer(reducer: Reducer, encoder: FunctorEncoder): string {
    return encoder.makeFunctor(
        "Reducer",
        reducer.reducer,
        reducer.arguments.map(v => encodeVariable(v, encoder))
    );
}

function encodePipelineStage(stage: PipelineStage, encoder: FunctorEncoder): string {
    const variant = stage.tag;
    switch (stage.tag) {
        case "match":
        case "insert":
        case "put":
        case "update":
            return encoder.makeFunctor(variant, encodeConjunction(stage.block, encoder));
        case "delete":
            return encoder.makeFunctor(variant, stage.deletedVariables.map(v => encodeVariable(v, encoder)), encodeConjunction(stage.block, encoder));
        case "select":
            return encoder.makeFunctor(variant, stage.variables.map(v => encodeVariable(v, encoder)));
        case "sort":{
            const sortVariables = stage.variables.map(v => encoder.makeFunctor(
                v.ascending? "Asc" : "Desc",
                encodeVariable(v.variable, encoder),
            ));
            return encoder.makeFunctor(variant, encoder.encodeAsList(sortVariables));
        }
        case "require":
            return encoder.makeFunctor(variant, stage.variables.map(v => encodeVariable(v, encoder)));
        case "offset":
            return encoder.makeFunctor(variant, stage.offset);
        case "limit":
            return encoder.makeFunctor(variant, stage.limit);
        case "distinct":
            return encoder.makeFunctor(variant);
        case "reduce": {
            const reduceAssigns = stage.reducers.map(reduceAssign => encoder.makeFunctor(
                "ReduceAssign",
                encodeVariable(reduceAssign.assigned, encoder),
                encodeReducer(reduceAssign.reducer, encoder)
            ));
            return encoder.makeFunctor(
                variant,
                encoder.encodeAsList(reduceAssigns),
                stage.groupby.map(v => encodeVariable(v, encoder))
            );
        }
    }
    throw new Error(`Unknown pipeline stage variant: ${variant}`);
}

function encodeReturnOperation(returnOp: FunctionReturnStructure, encoder: FunctorEncoder): string {
    switch (returnOp.tag) {
        case "stream":
            return encoder.makeFunctor(
                "Stream",
                encoder.encodeAsList(returnOp.variables.map(v => encodeVariable(v, encoder)))
            );
        case "single":
            return encoder.makeFunctor(
                "Single",
                returnOp.selector,
                encoder.encodeAsList(returnOp.variables.map(v => encodeVariable(v, encoder)))
            );
        case "check":
            return encoder.makeFunctor("Check", "");
        case "reduce":
            return encoder.makeFunctor(
                "Reduce",
                encoder.encodeAsList(returnOp.reducers.map(r => encodeReducer(r, encoder)))
            );
    }
}

function encodePipeline(pipeline: PipelineStructure, encoder: FunctorEncoder): string {
    return encoder.makeFunctor("Pipeline", encoder.encodeAsList(pipeline.pipeline.map(stage => encodePipelineStage(stage, encoder))));
}

function encodeFunction(func: FunctionStructure, encoder: FunctorEncoder): string {
    const encodedArgs = func.arguments.map(arg =>
        encodeVariable(arg, encoder)
    );

    const encodedBody = encodePipeline(func.body, encoder);
    const encodedReturn = encodeReturnOperation(func.returns, encoder);

    return encoder.makeFunctor(
        "Function",
        encoder.encodeAsList(encodedArgs),
        encodedReturn,
        encodedBody
    );
}
