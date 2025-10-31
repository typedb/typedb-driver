import { When, Then } from "@cucumber/cucumber";
import assert from "assert";
import {analyzed, doAnalyze, setAnalyzed} from "./context";
import {assertNotError} from "./params";
import { Pipeline } from "../../../dist/index.cjs";

When('get answers of typeql analyze', async function(query: string) {
    const results = await doAnalyze(query).then(assertNotError);
    setAnalyzed(results.ok);
});

When('typeql analyze{may_error}', async function(mayError: string, query: string) {
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

Then('analyzed query pipeline structure is:', function(expectedFunctor: string) {
    const pipeline = analyzed.structure.pipeline;
    const context = { pipeline: pipeline };
    const actualFunctor = encodePipeline(pipeline, context);
    assert.equal(normalizeFunctorForCompare(actualFunctor), normalizeFunctorForCompare(expectedFunctor))
});

Then('analyzed query preamble contains', function(expectedFunctor: string) {
    // TODO: Implement preamble checking
});

Then('analyzed query pipeline annotations are', function(expectedFunctor: string) {
    // TODO: Implement pipeline annotations checking
});

Then('analyzed preamble annotations contains', function(expectedFunctor: string) {
    // TODO: Implement preamble annotations checking
});

Then('analyzed fetch annotations are', function(expectedFunctor: string) {
    // TODO: Implement fetch annotations checking
});


interface FunctorEncodingContext {
    pipeline: Pipeline,
}

function encodePipeline(pipeline: Pipeline, context: FunctorEncodingContext): string {
    return "TODO";
}

function normalizeFunctorForCompare(functor: string): string {
    return functor.toLowerCase().replace(/\s/g, "");
}
