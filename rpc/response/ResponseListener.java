package grakn.client.rpc.response;

import grakn.client.common.exception.GraknClientException;
import grakn.protocol.TransactionProto;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static grakn.client.common.exception.ErrorMessage.Client.TRANSACTION_LISTENER_TERMINATED;

public class ResponseListener implements StreamObserver<TransactionProto.Transaction.Res> {

    private volatile ResponseCollector currentCollector;
    // TODO: why are we using a queue of collectors and using 'synchronized' to enforce that the collector order
    //       matches the request order? Surely we should be maintaining a concurrent map of requests to response
    //       collectors? If the problem is that we can't identify the matching request when we get back a response,
    //       this is surely something we could solve by adding a request ID to the protocol.
    private final BlockingQueue<ResponseCollector> collectorQueue = new LinkedBlockingQueue<>();
    private volatile boolean terminated = false;

    @Override
    public synchronized void onNext(TransactionProto.Transaction.Res value) {
        dispatchResponse(Response.ok(value));
    }

    @Override
    public synchronized void onError(Throwable throwable) {
        terminated = true;
        assert throwable instanceof StatusRuntimeException : "The server only yields these exceptions";

        // Exhaust the queue
        while (currentCollector != null || collectorQueue.peek() != null) {
            dispatchResponse(Response.error((StatusRuntimeException) throwable));
        }
    }

    @Override
    public synchronized void onCompleted() {
        terminated = true;

        // Exhaust the queue
        while (currentCollector != null || collectorQueue.peek() != null) {
            dispatchResponse(Response.completed());
        }
    }

    public synchronized boolean isTerminated() {
        return terminated;
    }

    public synchronized void addCollector(ResponseCollector collector) {
        if (terminated) throw new GraknClientException(TRANSACTION_LISTENER_TERMINATED);
        collectorQueue.add(collector);
    }

    private synchronized void dispatchResponse(Response res) {
        if (currentCollector == null) {
            try {
                currentCollector = collectorQueue.poll(1, TimeUnit.SECONDS);
                if (currentCollector == null) {
                    terminated = true;
                    return;
                }
            } catch (InterruptedException e) {
                terminated = true;
                // should never happen since response collectors are queued before send
                throw new GraknClientException("Interrupted whilst waiting for a result collector.");
            }
        }

        if (currentCollector.onResponse(res)) {
            currentCollector = null;
        }
    }
}
