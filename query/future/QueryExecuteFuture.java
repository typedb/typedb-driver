package grakn.client.query.future;

import grakn.client.rpc.RPCIterator;
import grakn.protocol.TransactionProto;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

// TODO: we don't need an RPCIterator here - need to rewrite it without that
public class QueryExecuteFuture<T> extends QueryFutureBase<T> {
    private final RPCIterator iterator;
    private final Function<TransactionProto.Transaction.Iter.Res, T> responseReader;

    protected QueryExecuteFuture(final RPCIterator iterator, @Nullable final Function<TransactionProto.Transaction.Iter.Res, T> responseReader) {
        this.iterator = iterator;
        this.responseReader = responseReader;
    }

    @Override
    protected RPCIterator getIterator() {
        return iterator;
    }

    @Override
    protected T getInternal() {
        List<T> result = new ArrayList<>();
        iterator.forEachRemaining(x -> result.add(responseReader.apply(x)));
        return result;
    }
}
