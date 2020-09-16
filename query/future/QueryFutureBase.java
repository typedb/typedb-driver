package grakn.client.query.future;

import grakn.client.common.exception.GraknClientException;
import grakn.client.query.QueryFuture;
import grakn.client.rpc.RPCIterator;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

abstract class QueryFutureBase<T> implements QueryFuture<T> {

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return false; // Can't cancel
    }

    @Override
    public boolean isCancelled() {
        return false; // Can't cancel
    }

    @Override
    public boolean isDone() {
        return getIterator().isStarted();
    }

    @Override
    public T get() {
        getIterator().waitForStart();
        return getInternal();
    }

    @Override
    public T get(long timeout, TimeUnit unit) {
        try {
            getIterator().waitForStart(timeout, unit);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new GraknClientException(ex);
        } catch (TimeoutException ex) {
            throw new GraknClientException(ex);
        }
        return getInternal();
    }

    protected abstract RPCIterator getIterator();

    protected abstract T getInternal();
}
