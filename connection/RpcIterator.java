package grakn.client.connection;

import com.google.common.collect.AbstractIterator;
import grakn.client.common.exception.GraknClientException;
import grakn.protocol.TransactionProto;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

import static grakn.client.common.exception.ErrorMessage.Protocol.REQUIRED_FIELD_NOT_SET;

/**
 * A client-side iterator over gRPC messages. Will send TransactionProto.Transaction.Iter.Req messages until
 * TransactionProto.Transaction.Iter.Res returns done as a message.
 *
 * @param <T> class type of objects being iterated
 */
class RpcIterator<T> extends AbstractIterator<T> {

    private Batch currentBatch;
    private volatile boolean started;
    private TransactionProto.Transaction.Iter.Res first;

    private final GraknTransceiver transceiver;
    private final Function<TransactionProto.Transaction.Iter.Res, T> responseReader;
    private final TransactionProto.Transaction.Iter.Req.Options options;

    RpcIterator(final GraknTransceiver transceiver, final TransactionProto.Transaction.Iter.Req req, final Function<TransactionProto.Transaction.Iter.Res, T> responseReader) {
        this.transceiver = transceiver;
        this.responseReader = responseReader;
        options = req.getOptions();
        sendRequest(req);
    }

    private void sendRequest(final TransactionProto.Transaction.Iter.Req req) {
        currentBatch = new Batch();

        final TransactionProto.Transaction.Req transactionReq = TransactionProto.Transaction.Req.newBuilder()
                .setIterReq(req).build();

        transceiver.sendAndReceiveMultipleAsync(transactionReq, currentBatch);
    }

    private void nextBatch(int iteratorID) {
        final TransactionProto.Transaction.Iter.Req iterReq = TransactionProto.Transaction.Iter.Req.newBuilder()
                .setIteratorID(iteratorID)
                .setOptions(options).build();

        sendRequest(iterReq);
    }

    private static class Batch extends GraknTransceiver.MultiResponseCollector {
        @Override
        protected boolean isLastResponse(final TransactionProto.Transaction.Res response) {
            final TransactionProto.Transaction.Iter.Res iterRes = response.getIterRes();
            return iterRes.getIteratorID() != 0 || iterRes.getDone();
        }
    }

    public boolean isStarted() {
        return started;
    }

    public void waitForStart(final long timeout, final TimeUnit unit) throws InterruptedException, TimeoutException {
        if (first != null) {
            throw new GraknClientException(new IllegalStateException("Should not poll RpcIterator multiple times"));
        }

        first = currentBatch.poll(timeout, unit).getIterRes();
    }

    public void waitForStart() {
    }

    @Override
    protected T computeNext() {
        if (first != null) {
            final TransactionProto.Transaction.Iter.Res iterRes = first;
            first = null;
            return responseReader.apply(iterRes);
        }

        final TransactionProto.Transaction.Iter.Res res;
        try {
            res = currentBatch.take().getIterRes();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new GraknClientException(e);
        }
        started = true;
        switch (res.getResCase()) {
            case ITERATORID:
                nextBatch(res.getIteratorID());
                return computeNext();
            case DONE:
                return endOfData();
            case RES_NOT_SET:
                throw new GraknClientException(REQUIRED_FIELD_NOT_SET.message(TransactionProto.Transaction.Iter.Res.class.getCanonicalName()));
            default:
                return responseReader.apply(res);
        }
    }
}
