import {When, Then} from "@cucumber/cucumber";
import assert from "assert";
import {analyzed, doAnalyze, setAnalyzed} from "./context";
import {assertNotError} from "./params";

import {QueryConstraintAny, QueryVertex, FunctionReturnStructure, FunctionStructure, PipelineStage, PipelineStructure, ConjunctionIndex} from "../../../dist/index.cjs";
// import {QueryConstraintAny, QueryVertex} from "../../../src";
// import {FunctionReturnStructure, FunctionStructure, PipelineStage, PipelineStructure} from "../../../src/analyze";


When('get answers of typeql analyze', async function (query: string) {
    const results = await doAnalyze(query).then(assertNotError);
    setAnalyzed(results.ok);
});

When('typeql analyze{may_error}', async function (mayError: string, query: string) {
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
});

Then('analyzed query pipeline structure is:', function (expectedFunctor: string) {
    const context = new FunctorEncoder(analyzed.structure.query);
    const actualFunctor = encodePipeline(analyzed.structure.query, context);
    assert.equal(normalizeFunctorForCompare(actualFunctor), normalizeFunctorForCompare(expectedFunctor))
});

Then('analyzed query preamble contains', function (expectedFunctor: string) {
    // TODO: Implement preamble checking
});

Then('analyzed query pipeline annotations are', function (expectedFunctor: string) {
    // TODO: Implement pipeline annotations checking
});

Then('analyzed preamble annotations contains', function (expectedFunctor: string) {
    // TODO: Implement preamble annotations checking
});

Then('analyzed fetch annotations are', function (expectedFunctor: string) {
    // TODO: Implement fetch annotations checking
});

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
            return encoder.makeFunctor("Isa", constraint.instance, constraint.type);
        case "isa!":
            return encoder.makeFunctor("IsaExact", constraint.instance, constraint.type);
        case "has":
            return encoder.makeFunctor("Has", constraint.owner, constraint.attribute);
        case "links":
            return encoder.makeFunctor("Links", constraint.relation, constraint.player, constraint.role);
        case "sub":
            return encoder.makeFunctor("Sub", constraint.subtype, constraint.supertype);
        case "sub!":
            return encoder.makeFunctor("SubExact", constraint.subtype, constraint.supertype);
        case "owns":
            return encoder.makeFunctor("Owns", constraint.owner, constraint.attribute);
        case "relates":
            return encoder.makeFunctor("Relates", constraint.relation, constraint.role);
        case "plays":
            return encoder.makeFunctor("Plays", constraint.player, constraint.role);
        case "expression":
            return encoder.makeFunctor("Expression", constraint.text, constraint.assigned, constraint.arguments);
        case "functionCall":
            return encoder.makeFunctor("FunctionCall", constraint.name, constraint.assigned, constraint.arguments);
        case "comparison":
            return encoder.makeFunctor("Comparison", constraint.lhs, constraint.rhs, constraint.comparator);
        case "is":
            return encoder.makeFunctor("Is", constraint.lhs, constraint.rhs);
        case "iid":
            return encoder.makeFunctor("Iid", constraint.concept, constraint.iid);
        case "kind":
            return encoder.makeFunctor("Kind", constraint.type, "kind");
        case "value":
            return encoder.makeFunctor("Value", constraint.attributeType, constraint.valueType);
        case "label":
            return encoder.makeFunctor("Label", constraint.type, constraint.label);
    }
}

function encodeVariable(id: string, encoder: FunctorEncoder) {
    const v = encoder.pipeline.variables[id];
    if (v != undefined) {
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
        case "value":
            return vertex.value.value;
        // TODO: NamedRole when it comes
    }
    throw new Error("Unknown constraint vertex type");
}

function encodeConjunction(index: number, encoder: FunctorEncoder): string {
    const constraints = encoder.pipeline.conjunctions[index];
    return encoder.encodeAsList(constraints.map(c => encodeConstraint(c, encoder)));
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
        case "sort":
            return encoder.makeFunctor(variant, stage.variables.map(v => encodeVariable(v, encoder)));
        case "require":
            return encoder.makeFunctor(variant, stage.variables.map(v => encodeVariable(v, encoder)));
        case "offset":
            return encoder.makeFunctor(variant, stage.offset);
        case "limit":
            return encoder.makeFunctor(variant, stage.limit);
        case "distinct":
            return encoder.makeFunctor(variant);
        case "reduce":
            return encoder.makeFunctor(variant, stage.reducers.map(v => encodeVariable(v, encoder)), stage.groupby.map(v => encodeVariable(v, encoder)));
        default:
            throw new Error(`Unknown pipeline stage variant: ${variant}`);
    }
}

function encodeReturnOperation(returnOp: FunctionReturnStructure, encoder: FunctorEncoder): string {
    switch (returnOp.tag) {
        case "stream":
            return encoder.makeFunctor("Stream", returnOp.variables);
        case "single":
            return encoder.makeFunctor("Single", returnOp.selector, returnOp.variables);
        case "check":
            return encoder.makeFunctor("Check", "");
        case "reduce":
            return encoder.makeFunctor("Reduce", returnOp.reducers);
    }
}

function encodePipeline(pipeline: PipelineStructure, context: FunctorEncoder): string {
    return context.encodeAsList(pipeline.pipeline.map(stage => encodePipelineStage(stage, context)));
}

function normalizeFunctorForCompare(functor: string): string {
    return functor.toLowerCase().replace(/\s/g, "");
}
