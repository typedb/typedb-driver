package grakn.client.query.future;

import grakn.client.rpc.RPCIterator;
import grakn.protocol.QueryProto;
import grakn.protocol.TransactionProto;

import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class QueryStreamFuture<T> extends QueryFutureBase<Stream<T>> {
    private final RPCIterator iterator;
    private final Function<QueryProto.Query.Iter.Res, T> responseReader;

    public QueryStreamFuture(final RPCIterator iterator, final Function<QueryProto.Query.Iter.Res, T> responseReader) {
        this.iterator = iterator;
        this.responseReader = responseReader;
    }

    @Override
    protected RPCIterator getIterator() {
        return iterator;
    }

    @Override
    protected Stream<T> getInternal() {
        return StreamSupport.stream(((Iterable<TransactionProto.Transaction.Iter.Res>) () -> iterator).spliterator(), false)
                .map(res -> responseReader.apply(res.getQueryIterRes()));
    }
}
