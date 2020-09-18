package grakn.client.rpc.response;

import grakn.protocol.TransactionProto;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class SingleResponseCollector implements ResponseCollector {
    private volatile Response response;
    private final CountDownLatch latch = new CountDownLatch(1);

    @Override
    public boolean onResponse(Response response) {
        this.response = response;
        latch.countDown();
        return true;
    }

    public boolean isDone() {
        return response != null;
    }

    public TransactionProto.Transaction.Res receive() throws InterruptedException {
        latch.await();
        return response.ok();
    }

    public TransactionProto.Transaction.Res receive(final long timeout, final TimeUnit unit) throws InterruptedException {
        latch.await(timeout, unit);
        return response.ok();
    }
}
