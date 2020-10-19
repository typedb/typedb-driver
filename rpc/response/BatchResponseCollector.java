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

package grakn.client.rpc.response;

import grakn.protocol.TransactionProto;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class BatchResponseCollector implements ResponseCollector {
    private volatile boolean started;
    private final BlockingQueue<Response> received = new LinkedBlockingQueue<>();

    @Override
    public boolean onResponse(Response response) {
        started = true;
        received.add(response);
        TransactionProto.Transaction.Res res = response.response();
        return res == null || isLastResponse(res);
    }

    public TransactionProto.Transaction.Res take() throws InterruptedException {
        return received.take().ok();
    }

    public TransactionProto.Transaction.Res take(long timeout, TimeUnit unit) throws InterruptedException {
        return received.poll(timeout, unit).ok();
    }

    public boolean isStarted() {
        return started;
    }

    boolean isLastResponse(final TransactionProto.Transaction.Res response) {
        final TransactionProto.Transaction.Iter.Res iterRes = response.getIterRes();
        return iterRes.getIteratorID() != 0 || iterRes.getDone();
    }
}
