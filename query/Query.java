package grakn.client.query;

import grakn.client.GraknOptions;
import grakn.client.concept.Concept;
import grakn.client.concept.answer.Answer;
import grakn.client.concept.type.ThingType;
import grakn.client.rpc.RPCTransaction;
import grakn.protocol.ConceptProto;
import grakn.protocol.TransactionProto;
import graql.lang.query.GraqlDefine;
import graql.lang.query.GraqlQuery;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static grakn.client.common.ProtoBuilder.options;

public final class Query {

    private final RPCTransaction transaction;

    public Query(final RPCTransaction transaction) {
        this.transaction = transaction;
    }

    public Stream<ThingType> define(final GraqlDefine query) {
        return define(query, new GraknOptions());
    }

    public Stream<ThingType> define(final GraqlDefine query, final GraknOptions options) {
        return query(query, options).flatMap(x -> x.asConceptMap().concepts().stream()).map(x -> x.asType().asThingType());
    }

    public Stream<Answer> query(final GraqlQuery query, final GraknOptions options) {
        final TransactionProto.Transaction.Iter.Req request = TransactionProto.Transaction.Iter.Req.newBuilder()
                .setQueryIterReq(TransactionProto.Transaction.Query.Iter.Req.newBuilder()
                        .setQuery(query.toString())
                        .setOptions(options(options))).build();

        return transaction.transceiver().iterate(request, res -> Answer.of(transaction, res.getQueryIterRes().getAnswer()));
    }
}
