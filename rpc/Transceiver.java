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

package grakn.client.rpc;

import com.google.common.collect.AbstractIterator;
import grabl.tracing.client.GrablTracingThreadStatic;
import grabl.tracing.client.GrablTracingThreadStatic.ThreadTrace;
import grakn.client.exception.GraknClientException;
import grakn.protocol.session.SessionProto;
import grakn.protocol.session.SessionProto.Transaction;
import grakn.protocol.session.SessionServiceGrpc;
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
import java.util.concurrent.atomic.AtomicBoolean;

import static grabl.tracing.client.GrablTracingThreadStatic.traceOnThread;


/**
 * Wrapper making transaction calls to the Grakn RPC Server - handles sending a stream of Transaction.Req and
 * receiving a stream of Transaction.Res.
 * A request is sent with the #send(Transaction.Req)} method, and you can block for a response with the
 * #receive() method.
 * {@code
 * try (Transceiver tx = Transceiver.create(stub) {
 * tx.send(openMessage);
 * Transaction.Res doneMessage = tx.receive().ok();
 * tx.send(commitMessage);
 * StatusRuntimeException validationError = tx.receive.error();
 * }
 * }
 */
public class Transceiver implements AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(Transceiver.class);

    private final StreamObserver<Transaction.Req> requestSender;
    private final ResponseListener responseListener;

    private Transceiver(StreamObserver<Transaction.Req> requestSender, ResponseListener responseListener) {
        this.requestSender = requestSender;
        this.responseListener = responseListener;
    }

    public static Transceiver create(SessionServiceGrpc.SessionServiceStub stub) {
        ResponseListener responseListener = new ResponseListener();
        StreamObserver<Transaction.Req> requestSender = stub.transaction(responseListener);
        return new Transceiver(requestSender, responseListener);
    }

    /**
     * Send a request and return immediately.
         * This method is non-blocking - it returns immediately.
     */
    private void send(Transaction.Req request, ResponseCollector collector) {
        if (responseListener.terminated.get()) {
            throw GraknClientException.connectionClosed();
        }
        LOG.trace("send:{}", request);

        // We must add the response collectors in exact the same order we send the requests
        synchronized (this) {
            responseListener.addCollector(collector); // Must add collector first to be watertight
            requestSender.onNext(request);
        }
    }

    public Transaction.Res sendAndReceive(Transaction.Req request) throws InterruptedException {
        try (ThreadTrace trace = traceOnThread("sendAndReceive")) {
            SingleResponseCollector collector = new SingleResponseCollector();
            send(request, collector);
            return collector.receive();
        }
    }

    public void sendAndReceiveMultipleAsync(Transaction.Req request, MultiResponseCollector collector) {
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

    public boolean isOpen() {
        return !responseListener.terminated.get();
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

        public SessionProto.Transaction.Res receive() throws InterruptedException {
            latch.await();
            return response.ok();
        }
    }

    /**
     * Advanced abstract multi-response collector. The {@link #isLastResponse(Transaction.Res)} method must be
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
            SessionProto.Transaction.Res nullableRes = response.nullableOk();
            return nullableRes == null || isLastResponse(nullableRes);
        }

        public SessionProto.Transaction.Res take() throws InterruptedException {
            return received.take().ok();
        }

        public SessionProto.Transaction.Res poll(long timeout, TimeUnit unit) throws InterruptedException,
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
        protected abstract boolean isLastResponse(SessionProto.Transaction.Res response);
    }

    /**
     * A StreamObserver that pushes received responses to the corresponding collector.
     */
    private static class ResponseListener implements StreamObserver<Transaction.Res> {

        private ResponseCollector currentCollector;
        private final BlockingQueue<ResponseCollector> collectorQueue = new LinkedBlockingQueue<>();
        private final AtomicBoolean terminated = new AtomicBoolean(false);

        void addCollector(ResponseCollector collector) {
            collectorQueue.add(collector);
        }

        private void dispatchResponse(Response res) {
            if (currentCollector == null) {
                try {
                    currentCollector = collectorQueue.take();
                } catch (InterruptedException e) {
                    terminated.set(true);
                    throw new IllegalStateException("Interrupted whilst waiting for a result collector, " +
                            "should never happen since response collectors are queued before send.");
                }
            }

            if (currentCollector.onResponse(res)) {
                currentCollector = null;
            }
        }

        @Override
        public void onNext(Transaction.Res value) {
            dispatchResponse(Response.ok(value));
        }

        @Override
        public void onError(Throwable throwable) {
            terminated.set(true);
            assert throwable instanceof StatusRuntimeException : "The server only yields these exceptions";

            // Exhaust the queue
            while (currentCollector != null || collectorQueue.peek() != null) {
                dispatchResponse(Response.error((Exception) throwable));
            }
        }

        @Override
        public void onCompleted() {
            terminated.set(true);

            // Exhaust the queue
            while (currentCollector != null || collectorQueue.peek() != null) {
                dispatchResponse(Response.completed());
            }
        }
    }

    /**
     * A response from the gRPC server, that may be a successful response #ok(Transaction.Res), an error
     * {#error(StatusRuntimeException)} or a "completed" message #completed().
     */
    public static class Response {

        private final SessionProto.Transaction.Res nullableOk;
        private final Exception nullableError;

        Response(@Nullable SessionProto.Transaction.Res nullableOk, @Nullable Exception nullableError) {
            this.nullableOk = nullableOk;
            this.nullableError = nullableError;
        }

        private static Response create(@Nullable Transaction.Res response, @Nullable Exception error) {
            if (!(response == null || error == null)) {
                throw new IllegalArgumentException("One of Transaction.Res or StatusRuntimeException must be null");
            }
            return new Response(response, error);
        }

        static Response completed() {
            return create(null, null);
        }

        static Response error(Exception error) {
            return create(null, error);
        }

        static Response ok(Transaction.Res response) {
            return create(response, null);
        }

        @Nullable
        SessionProto.Transaction.Res nullableOk() {
            return nullableOk;
        }

        @Nullable
        Exception nullableError() {
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
         * @throws IllegalStateException if this is not a successful response
         */
        public final Transaction.Res ok() {
            if (nullableOk != null) {
                return nullableOk;
            } else if (nullableError != null) {
                // TODO: parse different GRPC errors into specific GraknClientException
                throw GraknClientException.create(nullableError.getMessage(), nullableError);
            } else {
                throw GraknClientException.create("Transaction interrupted, all running queries have been stopped.");
            }
        }

        /**
         * If this is an error, retrieve it.
         *
         * @throws IllegalStateException if this is not an error
         */
        public final Exception error() {
            Exception throwable = nullableError();
            if (throwable == null) {
                throw new IllegalStateException("Expected error not found: " + toString());
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
            if (o instanceof Transceiver.Response) {
                Transceiver.Response that = (Transceiver.Response) o;
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
