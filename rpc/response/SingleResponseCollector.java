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
