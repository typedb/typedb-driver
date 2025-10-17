package com.typedb.driver.test.behaviour.query;

import com.typedb.driver.analyze.AnalyzedQueryImpl;
import com.typedb.driver.analyze.Pipeline;
import com.typedb.driver.test.behaviour.config.Parameters;
import com.typedb.driver.test.behaviour.util.FunctorEncoder;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

import static com.typedb.driver.test.behaviour.connection.ConnectionStepsBase.tx;
import static org.junit.Assert.assertTrue;

public class AnalyzeSteps {
    private static AnalyzedQueryImpl analyzedQuery;
    @When("get answers of typeql analyze")
    public void get_answers_of_typeql_analyze(String query) {
        analyzedQuery = null;
        analyzedQuery = (AnalyzedQueryImpl)tx().analyze(query).resolve();
    }

    @Then("typeql analyze{may_error}")
    public void typeql_analyze(Parameters.MayError mayError, String query) {
        mayError.check(() -> tx().analyze(query).resolve());
    }


    @Then("analyzed query pipeline structure is:")
    public void analyzed_query_pipeline_structure_is(String expectedFunctor) {
        String actualFunctor = new FunctorEncoder.StructureEncoder(analyzedQuery.pipeline()).encode(analyzedQuery.pipeline());
        assertEquals(FunctorEncoder.normalizeForCompare(expectedFunctor), FunctorEncoder.normalizeForCompare(actualFunctor));
    }

    @Then("analyzed query preamble contains:")
    public void analyzed_query_preamble_contains(String docString) {
        String expectedFunctor = FunctorEncoder.normalizeForCompare(docString);
        List<String> preambleFunctors = analyzedQuery.preamble().map(func -> {
            String actualFunctor = new FunctorEncoder.StructureEncoder(func.body()).encode(func);
            return FunctorEncoder.normalizeForCompare(actualFunctor);
        }).collect(Collectors.toList());

        assertTrue(
            String.format("Did not find %s in %s", expectedFunctor, preambleFunctors.stream().collect(Collectors.joining(","))),
            preambleFunctors.contains(expectedFunctor)
        );
    }


    @Then("analyzed query pipeline annotations are:")
    public void analyzed_query_pipeline_annotations_are(String expectedFunctor) {
        String actualFunctor = new FunctorEncoder.AnnotationsEncoder(analyzedQuery.pipeline()).encode(analyzedQuery.pipeline());
        assertEquals(FunctorEncoder.normalizeForCompare(expectedFunctor), FunctorEncoder.normalizeForCompare(actualFunctor));
    }

    @Then("analyzed preamble annotations contains:")
    public void analyzed_preamble_annotations_contains(String docString) {
        // Write code here that turns the phrase above into concrete actions
        String expectedFunctor = FunctorEncoder.normalizeForCompare(docString);
        List<String> preambleFunctors = analyzedQuery.preamble().map(func -> {
            String actualFunctor = new FunctorEncoder.AnnotationsEncoder(func.body()).encode(func);
            return FunctorEncoder.normalizeForCompare(actualFunctor);
        }).collect(Collectors.toList());

        assertTrue(
                String.format("Did not find %s in %s", expectedFunctor, preambleFunctors.stream().collect(Collectors.joining(","))),
                preambleFunctors.contains(expectedFunctor)
        );
    }

    @Then("analyzed fetch annotations are:")
    public void analyzed_fetch_annotations_are(String expectedFunctor) {
        // Write code here that turns the phrase above into concrete actions
        String actualFunctor = new FunctorEncoder.AnnotationsEncoder(analyzedQuery.pipeline()).encode(analyzedQuery.fetch().get());
        assertEquals(FunctorEncoder.normalizeForCompare(expectedFunctor), FunctorEncoder.normalizeForCompare(actualFunctor));
    }


}
