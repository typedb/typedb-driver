package com.typedb.driver.api.analyze;

import java.util.Optional;
import java.util.stream.Stream;


/// An <code>AnalyzedQuery</code> contains the server's representation of the query and preamble functions;
/// as well as the result of types inferred for each variable by type-inference.
public interface AnalyzedQuery {
    /**
     * A representation of the query as a <code>Pipeline</code>
     *
     * @return the query as a pipeline of operations
     */
    Pipeline pipeline();

    /**
     * A representation of the <code>Function</code>s in the preambele of the query
     *
     * @return stream of function definitions
     */
    Stream<? extends Function> preamble();

    /**
     * A representation of the <code>Fetch</code> stage of the query, if it has one
     *
     * @return an Optional containing the fetch stage if present, empty otherwise
     */
    Optional<? extends Fetch> fetch();
}
