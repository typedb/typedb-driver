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

package grakn.client.common.proto;

import grakn.client.GraknOptions;
import grakn.protocol.OptionsProto;

public abstract class OptionsProtoBuilder {

    public static OptionsProto.Options options(GraknOptions options) {
        final OptionsProto.Options.Builder builder = OptionsProto.Options.newBuilder();
        options.infer().ifPresent(builder::setInfer);
        options.explain().ifPresent(builder::setExplain);
        options.batchSize().ifPresent(builder::setBatchSize);
        options.prefetch().ifPresent(builder::setPrefetch);
        options.sessionIdleTimeout().ifPresent(builder::setSessionIdleTimeoutMillis);
        options.schemaLockAcquireTimeout().ifPresent(builder::setSchemaLockAcquireTimeoutMillis);
        if (options.isCluster()) {
            options.asCluster().readAnyReplica().ifPresent(builder::setReadAnyReplica);
        }

        return builder.build();
    }
}
