/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package grakn.client.connection;

import grabl.tracing.client.GrablTracingThreadStatic;
import grabl.tracing.client.GrablTracingThreadStatic.ThreadTrace;
import grakn.client.common.exception.GraknClientException;
import grakn.protocol.GraknGrpc;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static grabl.tracing.client.GrablTracingThreadStatic.traceOnThread;
import static grakn.client.common.exception.ErrorMessage.Connection.CONNECTION_CLOSED;
import static grakn.client.common.exception.ErrorMessage.Connection.TRANSACTION_LISTENER_TERMINATED;
import static grakn.protocol.TransactionProto.Transaction.Req;
import static grakn.protocol.TransactionProto.Transaction.Res;

/**
 * Wrapper making transaction calls to the Grakn RPC Server - handles sending a stream of Transaction.Req and
 * receiving a stream of Transaction.Res.
 * A request is sent with the #send(Transaction.Req)} method, and you can block for a response with the
 * #receive() method.
 * {@code
 * try (GraknTransceiver tx = GraknTransceiver.create(stub) {
 * tx.send(openMessage);
 * Transaction.Res doneMessage = tx.receive().ok();
 * tx.send(commitMessage);
 * StatusRuntimeException validationError = tx.receive.error();
 * }
 * }
 */
class GraknTransceiver implements AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(GraknTransceiver.class);

    private final StreamObserver<Req> requestSender;
    private final ResponseListener responseListener;

    private GraknTransceiver(StreamObserver<Req> requestSender, ResponseListener responseListener) {
        this.requestSender = requestSender;
        this.responseListener = responseListener;
    }

    static GraknTransceiver create(GraknGrpc.GraknStub stub) {
        ResponseListener responseListener = new ResponseListener();
        StreamObserver<Req> requestSender = stub.transaction(responseListener);
        return new GraknTransceiver(requestSender, responseListener);
    }

    /**
     * Send a request and return immediately.
         * This method is non-blocking - it returns immediately.
     */
    private void send(Req request, ResponseCollector collector) {

        LOG.trace("send:{}", request);

        // We must add the response collectors in exact the same order we send the requests
        synchronized (this) {
            synchronized (responseListener) {
                if (responseListener.terminated) {
                    throw new GraknClientException(CONNECTION_CLOSED);
                }
                responseListener.addCollector(collector); // Must add collector first to be watertight
            }
            requestSender.onNext(request);
        }
    }

    Res sendAndReceive(Req request) throws InterruptedException {
        try (ThreadTrace trace = traceOnThread("sendAndReceive")) {
            SingleResponseCollector collector = new SingleResponseCollector();
            send(request, collector);
            return collector.receive();
        }
    }

    void sendAndReceiveMultipleAsync(Req request, MultiResponseCollector collector) {
        try (ThreadTrace trace = GrablTracingThreadStatic.traceOnThread("sendAndReceiveMultipleAsync")) {
            send(request, collector);
        }
    }

    @Override
    public void close() {
        try {
            requestSender.onCompleted();
            responseListener.onCompleted();
        } catch (IllegalStateException e) {
            //IGNORED
            //This is needed to handle the fact that:
            //1. Commits can lead to transaction closures and
            //2. Error can lead to connection closures but the transaction may stay open
            //When this occurs a "half-closed" state is thrown which we can safely ignore
        }
    }

    synchronized boolean isOpen() {
        return !responseListener.terminated;
    }

    /**
     * Interface for collecting responses from a specific request
     */
    interface ResponseCollector {
        /**
         * Collect a response.
         * @param response the next response for this collector to collect.
         * @return true if this is the last response, false if more responses are expected.
         */
        boolean onResponse(Response response);
    }

    /**
     * Simple response collector for when a single result is expected.
     */
    private static class SingleResponseCollector implements ResponseCollector {
        private volatile Response response;
        private final CountDownLatch latch = new CountDownLatch(1);

        @Override
        public boolean onResponse(Response response) {
            this.response = response;
            latch.countDown();
            return true;
        }

        public Res receive() throws InterruptedException {
            latch.await();
            return response.ok();
        }
    }

    /**
     * Advanced abstract multi-response collector. The {@link #isLastResponse(Res)} method must be
     * overridden in a sub-class because the last response must be known by the GRPC response receiving thread in order
     * to keep it from blocking other collectors for later responses.
     */
    public static abstract class MultiResponseCollector implements ResponseCollector {
        private volatile boolean started;
        private final BlockingQueue<Response> received = new LinkedBlockingQueue<>();

        @Override
        public boolean onResponse(Response response) {
            started = true;
            received.add(response);
            Res nullableRes = response.nullableOk();
            return nullableRes == null || isLastResponse(nullableRes);
        }

        public Res take() throws InterruptedException {
            return received.take().ok();
        }

        public Res poll(long timeout, TimeUnit unit) throws InterruptedException,
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

        /**
         * Implement to inform the GRPC thread when it has received the last response.
         * This is called from the GRPC thread, not the main client thread.
         *
         * @param response The next response.
         * @return true if this is the last response, false if more responses are expected.
         */
        protected abstract boolean isLastResponse(Res response);
    }

    /**
     * A StreamObserver that pushes received responses to the corresponding collector.
     */
    private static class ResponseListener implements StreamObserver<Res> {

        private volatile ResponseCollector currentCollector;
        private final BlockingQueue<ResponseCollector> collectorQueue = new LinkedBlockingQueue<>();
        private volatile boolean terminated = false;

        synchronized void addCollector(ResponseCollector collector) {
            if (terminated) throw new GraknClientException(TRANSACTION_LISTENER_TERMINATED);
            collectorQueue.add(collector);
        }

        private synchronized void dispatchResponse(Response res) {
            if (currentCollector == null) {
                try {
                    currentCollector = collectorQueue.poll(1, TimeUnit.SECONDS);
                    if (currentCollector == null) {
                        LOG.error("Unexpected response: {}", res);
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

        @Override
        public synchronized void onNext(Res value) {
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
                LOG.info("Does this happen often?");
                dispatchResponse(Response.completed());
            }
        }
    }

    /**
     * A response from the gRPC server, that may be a successful response #ok(Transaction.Res), an error
     * {#error(StatusRuntimeException)} or a "completed" message #completed().
     */
    public static class Response {

        private final Res nullableOk;
        private final StatusRuntimeException nullableError;

        Response(@Nullable Res nullableOk, @Nullable StatusRuntimeException nullableError) {
            this.nullableOk = nullableOk;
            this.nullableError = nullableError;
        }

        private static Response create(@Nullable Res response, @Nullable StatusRuntimeException error) {
            if (!(response == null || error == null)) {
                throw new GraknClientException(new IllegalArgumentException("One of Transaction.Res or StatusRuntimeException must be null"));
            }
            return new Response(response, error);
        }

        static Response completed() {
            return create(null, null);
        }

        static Response error(StatusRuntimeException error) {
            return create(null, error);
        }

        static Response ok(Res response) {
            return create(response, null);
        }

        @Nullable
        Res nullableOk() {
            return nullableOk;
        }

        @Nullable
        StatusRuntimeException nullableError() {
            return nullableError;
        }

        public final Type type() {
            if (nullableOk() != null) {
                return Type.OK;
            } else if (nullableError() != null) {
                return Type.ERROR;
            } else {
                return Type.COMPLETED;
            }
        }

        /**
         * If this is a successful response, retrieve it.
         *
         * @throws GraknClientException if this is not a successful response
         */
        public final Res ok() {
            if (nullableOk != null) {
                return nullableOk;
            } else if (nullableError != null) {
                // TODO: parse different GRPC errors into specific GraknClientException
                throw new GraknClientException(nullableError);
            } else {
                throw new GraknClientException("Transaction interrupted, all running queries have been stopped.");
            }
        }

        /**
         * If this is an error, retrieve it.
         *
         * @throws GraknClientException if this is not an error
         */
        public final Exception error() {
            Exception throwable = nullableError();
            if (throwable == null) {
                throw new GraknClientException(new IllegalStateException("Expected error not found: " + toString()));
            } else {
                return throwable;
            }
        }

        @Override
        public String toString() {
            return getClass().getSimpleName() + "{nullableOk=" + nullableOk + ", nullableError=" + nullableError + "}";
        }

        @Override
        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (o instanceof GraknTransceiver.Response) {
                GraknTransceiver.Response that = (GraknTransceiver.Response) o;
                return ((this.nullableOk == null) ? (that.nullableOk() == null) : this.nullableOk.equals(that.nullableOk()))
                        && ((this.nullableError == null) ? (that.nullableError() == null) : this.nullableError.equals(that.nullableError()));
            }
            return false;
        }

        @Override
        public int hashCode() {
            int h = 1;
            h *= 1000003;
            h ^= (nullableOk == null) ? 0 : this.nullableOk.hashCode();
            h *= 1000003;
            h ^= (nullableError == null) ? 0 : this.nullableError.hashCode();
            return h;
        }

        /**
         * Enum indicating the type of Response.
         */
        public enum Type {
            OK, ERROR, COMPLETED
        }
    }
}
