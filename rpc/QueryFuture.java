package grakn.client.rpc;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public interface QueryFuture<T> extends Future<T> {
    @Override
    T get();

    @Override
    T get(long timeout, TimeUnit unit);
}
