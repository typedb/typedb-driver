package grakn.client.rpc.response;

import grakn.protocol.TransactionProto;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class BatchResponseCollector implements ResponseCollector {
    private volatile boolean started;
    private final BlockingQueue<Response> received = new LinkedBlockingQueue<>();

    @Override
    public boolean onResponse(Response response) {
        started = true;
        received.add(response);
        TransactionProto.Transaction.Res nullableRes = response.nullableOk();
        return nullableRes == null || isLastResponse(nullableRes);
    }

    public TransactionProto.Transaction.Res take() throws InterruptedException {
        return received.take().ok();
    }

    public TransactionProto.Transaction.Res poll() throws TimeoutException {
        Response response = received.poll();
        if (response == null) {
            throw new TimeoutException();
        } else {
            return response.ok();
        }
    }

    public TransactionProto.Transaction.Res poll(long timeout, TimeUnit unit) throws InterruptedException,
            TimeoutException {
        Response response = received.poll(timeout, unit);
        if (response == null) {
            throw new TimeoutException();
        } else {
            return response.ok();
        }
    }

    public boolean isStarted() {
        return started;
    }

    boolean isLastResponse(final TransactionProto.Transaction.Res response) {
        final TransactionProto.Transaction.Iter.Res iterRes = response.getIterRes();
        return iterRes.getIteratorID() != 0 || iterRes.getDone();
    }
}
