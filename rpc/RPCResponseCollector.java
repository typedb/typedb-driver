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

import grakn.protocol.TransactionProto;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

abstract class RPCResponseCollector {
    private final AtomicBoolean isDone;
    private final BlockingQueue<RPCResponse> received = new LinkedBlockingQueue<>();

    RPCResponseCollector() {
        isDone = new AtomicBoolean(false);
    }

    void onResponse(final RPCResponse rpcResponse) {
        received.add(rpcResponse);
        if (!(rpcResponse instanceof RPCResponse.Ok) || isLastResponse(rpcResponse.read())) isDone.compareAndSet(false, true);
    }

    boolean isDone() {
        return isDone.get();
    }

    TransactionProto.Transaction.Res take() throws InterruptedException {
        return received.take().read();
    }

    TransactionProto.Transaction.Res take(long timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
        final RPCResponse response = received.poll(timeout, unit);
        if (response != null) return response.read();
        else throw new TimeoutException();
    }

    abstract boolean isLastResponse(final TransactionProto.Transaction.Res response);

    static class Single extends RPCResponseCollector {

        @Override
        boolean isLastResponse(final TransactionProto.Transaction.Res response) {
            return true;
        }
    }

    // TODO: Maybe we can pull the RPCIterator into this thing?
    static class Multiple extends RPCResponseCollector {

        @Override
        boolean isLastResponse(final TransactionProto.Transaction.Res response) {
            return response.getDone();
        }
    }
}
