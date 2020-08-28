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

import grabl.tracing.client.GrablTracingThreadStatic;
import grakn.client.GraknOptions;
import grakn.client.GraknOptions.BatchSize;
import grakn.client.common.exception.GraknException;
import grakn.protocol.OptionsProto;
import grakn.protocol.TransactionProto;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static grabl.tracing.client.GrablTracingThreadStatic.currentThreadTrace;
import static grabl.tracing.client.GrablTracingThreadStatic.isTracingEnabled;
import static grakn.client.common.exception.ErrorMessage.Connection.NEGATIVE_BATCH_SIZE;

abstract class RPCProtoBuilder {

    static OptionsProto.Options options(final GraknOptions options) {
        final OptionsProto.Options.Builder builder = OptionsProto.Options.newBuilder();
        if (options.explain() != null) {
            builder.setExplain(options.explain());
        }
        if (options.infer() != null) {
            builder.setInfer(options.infer());
        }
        if (options.batchSize() != null) {
            builder.setBatchSize(options.batchSize().getSize());
        }
        return builder.build();
    }

    static TransactionProto.Transaction.Iter.Req.Options batchSize(final BatchSize batchSize) {
        if (batchSize.isAll()) {
            return TransactionProto.Transaction.Iter.Req.Options.newBuilder().setAll(true).build();
        }

        if (batchSize.getSize() < 1) {
            throw new GraknException(NEGATIVE_BATCH_SIZE.message(batchSize.getSize()));
        }

        return TransactionProto.Transaction.Iter.Req.Options.newBuilder().setNumber(batchSize.getSize()).build();
    }

    static Map<String, String> tracingData() {
        if (isTracingEnabled()) {
            final GrablTracingThreadStatic.ThreadTrace threadTrace = currentThreadTrace();
            if (threadTrace == null) {
                return Collections.emptyMap();
            }

            if (threadTrace.getId() == null || threadTrace.getRootId() == null) {
                return Collections.emptyMap();
            }

            final Map<String, String> metadata = new HashMap<>(2);
            metadata.put("traceParentId", threadTrace.getId().toString());
            metadata.put("traceRootId", threadTrace.getRootId().toString());
            return metadata;
        } else {
            return Collections.emptyMap();
        }
    }
}
