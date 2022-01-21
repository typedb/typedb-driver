/*
 * Copyright (C) 2021 Vaticle
 *
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

package com.vaticle.typedb.client.stream;

import com.vaticle.typedb.client.common.exception.TypeDBClientException;
import com.vaticle.typedb.common.collection.Either;
import io.grpc.StatusRuntimeException;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedTransferQueue;

import static com.vaticle.typedb.client.common.exception.ErrorMessage.Client.TRANSACTION_CLOSED;
import static com.vaticle.typedb.client.common.exception.ErrorMessage.Client.TRANSACTION_CLOSED_WITH_ERRORS;
import static com.vaticle.typedb.client.common.exception.ErrorMessage.Internal.UNEXPECTED_INTERRUPTION;

public class ResponseCollector<R> {

    private final ConcurrentMap<UUID, Queue<R>> collectors;

    public ResponseCollector() {
        collectors = new ConcurrentHashMap<>();
    }

    synchronized Queue<R> queue(UUID requestId) {
        Queue<R> collector = new Queue<>();
        collectors.put(requestId, collector);
        return collector;
    }

    Queue<R> get(UUID requestId) {
        return collectors.get(requestId);
    }

    void remove(UUID requestID) {
        this.collectors.remove(requestID);
    }

    synchronized void close(@Nullable StatusRuntimeException error) {
        collectors.values().forEach(collector -> collector.close(error));
    }

    List<StatusRuntimeException> getErrors() {
        List<StatusRuntimeException> errors = new ArrayList<>();
        collectors.values().forEach(collector -> collector.getError().ifPresent(errors::add));
        return errors;
    }

    public static class Queue<R> {

        private final BlockingQueue<Either<Response<R>, Done>> responseQueue;
        private StatusRuntimeException error;

        Queue() {
            // TODO: switch LinkedTransferQueue to LinkedBlockingQueue once issue #351 is fixed
            responseQueue = new LinkedTransferQueue<>();
            error = null;
        }

        public R take() {
            try {
                Either<Response<R>, Done> response = responseQueue.take();
                if (response.isFirst()) return response.first().message();
                else if (this.error != null) throw new TypeDBClientException(TRANSACTION_CLOSED_WITH_ERRORS, error);
                else throw new TypeDBClientException(TRANSACTION_CLOSED);
            } catch (InterruptedException e) {
                throw new TypeDBClientException(UNEXPECTED_INTERRUPTION);
            }
        }

        public Optional<StatusRuntimeException> getError() {
            return Optional.ofNullable(error);
        }

        public void put(R response) {
            responseQueue.add(Either.first(new Response<>(response)));
        }

        public void close(@Nullable StatusRuntimeException error) {
            this.error = error;
            responseQueue.add(Either.second(new Done()));
        }

        private static class Response<R> {

            @Nullable
            private final R value;

            private Response(@Nullable R value) {
                this.value = value;
            }

            @Nullable
            private R message() {
                return value;
            }
        }

        private static class Done {
            private Done() {
            }
        }
    }
}
