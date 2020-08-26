package grakn.client.test.behaviour.concept;

import grakn.client.concept.Concepts;

import static grakn.client.test.behaviour.connection.ConnectionSteps.tx;

public class ConceptSteps {

    public static Concepts concepts() {
        return tx().concepts();
    }
}
