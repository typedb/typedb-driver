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

package grakn.client.rpc.common;

import grakn.client.common.exception.GraknClientException;
import grakn.common.collection.ConcurrentSet;
import grakn.common.concurrent.NamedThreadFactory;
import grakn.protocol.TransactionProto;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.StampedLock;

import static grakn.client.common.exception.ErrorMessage.Client.CLIENT_CLOSED;

public class TransactionRequestBatcher implements AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(TransactionRequestBatcher.class);
    private static final int BATCH_WINDOW_MILLIS = 1;

    private final ArrayList<Executor> executors;
    private final AtomicInteger executorIndex;
    private final ReadWriteLock accessLock;
    private volatile boolean isOpen;

    public TransactionRequestBatcher(int executors, NamedThreadFactory threadFactory) {
        this.executors = new ArrayList<>(executors);
        this.executorIndex = new AtomicInteger(0);
        this.accessLock = new StampedLock().asReadWriteLock();
        this.isOpen = true;
        for (int i = 0; i < executors; i++) this.executors.add(new Executor(threadFactory));
    }

    public Executor nextExecutor() {
        return executors.get(executorIndex.getAndUpdate(i -> {
            i++; if (i % executors.size() == 0) i = 0; return i;
        }));
    }

    @Override
    public void close() {
        try {
            accessLock.writeLock().lock();
            if (isOpen) {
                isOpen = false;
                executors.forEach(Executor::clear);
            }
        } finally {
            accessLock.writeLock().unlock();
        }
    }

    public class Executor {

        private final ConcurrentSet<Dispatcher> dispatchers;
        private final AtomicBoolean isRunning;
        private final Semaphore permissionToRun;

        private Executor(ThreadFactory threadFactory) {
            dispatchers = new ConcurrentSet<>();
            isRunning = new AtomicBoolean(false);
            permissionToRun = new Semaphore(0);
            threadFactory.newThread(this::run).start();
        }

        private void mayStartRunning() {
            if (isRunning.compareAndSet(false, true)) permissionToRun.release();
        }

        private void run() {
            while (isOpen) {
                try {
                    permissionToRun.acquire();
                    while (true) {
                        Thread.sleep(BATCH_WINDOW_MILLIS);
                        if (dispatchers.isEmpty()) break;
                        else dispatchers.forEach(Dispatcher::sendBatchedRequests);
                    }
                } catch (InterruptedException e) {
                    LOG.error(e.getMessage(), e);
                } finally {
                    isRunning.set(false);
                }
                if (!dispatchers.isEmpty()) mayStartRunning();
            }
        }

        public void clear() {
            dispatchers.clear();
        }

        public Dispatcher dispatcher(StreamObserver<TransactionProto.Transaction.Reqs> requestObserver) {
            Dispatcher dispatcher = new Dispatcher(requestObserver);
            dispatchers.add(dispatcher);
            return dispatcher;
        }

        public class Dispatcher implements AutoCloseable {

            private final StreamObserver<TransactionProto.Transaction.Reqs> requestObserver;
            private final ConcurrentLinkedQueue<TransactionProto.Transaction.Req> requestQueue;

            private Dispatcher(StreamObserver<TransactionProto.Transaction.Reqs> requestObserver) {
                this.requestObserver = requestObserver;
                requestQueue = new ConcurrentLinkedQueue<>();
            }

            private synchronized void sendBatchedRequests() {
                if (requestQueue.isEmpty() || !isOpen) return;
                TransactionProto.Transaction.Req request;
                ArrayList<TransactionProto.Transaction.Req> requests = new ArrayList<>(requestQueue.size() * 2);
                while ((request = requestQueue.poll()) != null) requests.add(request);
                requestObserver.onNext(TransactionProto.Transaction.Reqs.newBuilder().addAllTransactionReqs(requests).build());
            }

            public void dispatch(TransactionProto.Transaction.Req requestProto) {
                try {
                    accessLock.readLock().lock();
                    if (!isOpen) throw new GraknClientException(CLIENT_CLOSED);
                    requestQueue.add(requestProto);
                    mayStartRunning();
                } finally {
                    accessLock.readLock().unlock();
                }
            }

            public void dispatchNow(TransactionProto.Transaction.Req requestProto) {
                try {
                    accessLock.readLock().lock();
                    if (!isOpen) throw new GraknClientException(CLIENT_CLOSED);
                    requestQueue.add(requestProto);
                    sendBatchedRequests();
                } finally {
                    accessLock.readLock().unlock();
                }
            }

            @Override
            public void close() {
                requestObserver.onCompleted();
                dispatchers.remove(this);
            }
        }
    }
}
