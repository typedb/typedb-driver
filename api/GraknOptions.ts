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

import {Options} from "grakn-protocol/common/options_pb";

namespace Opts {
    export class Core {
        infer?: boolean;
        traceInference?: boolean;
        explain?: boolean;
        parallel?: boolean;
        batchSize?: number;
        prefetch?: boolean;
        sessionIdleTimeoutMillis?: number;
        schemaLockAcquireTimeoutMillis?: number;
    }

    export class Cluster extends Core {
        readAnyReplica?: boolean;
    }

    export function proto(options: Opts.Core | Opts.Cluster): Options {
        const optionsProto = new Options();
        if (options) {
            if (options.infer != null) optionsProto.setInfer(options.infer);
            if (options.traceInference != null) optionsProto.setTraceInference(options.traceInference);
            if (options.explain != null) optionsProto.setExplain(options.explain);
            if (options.parallel != null) optionsProto.setParallel(options.parallel);
            if (options.batchSize != null) optionsProto.setBatchSize(options.batchSize);
            if (options.prefetch != null) optionsProto.setPrefetch(options.prefetch);
            if (options.sessionIdleTimeoutMillis != null) optionsProto.setSessionIdleTimeoutMillis(options.sessionIdleTimeoutMillis);
            if (options.schemaLockAcquireTimeoutMillis != null) optionsProto.setSchemaLockAcquireTimeoutMillis(options.schemaLockAcquireTimeoutMillis);
            if (options instanceof Opts.Cluster) {
                if ((options as Opts.Cluster).readAnyReplica != null) optionsProto.setReadAnyReplica((options as Opts.Cluster).readAnyReplica);
            }
        }
        return optionsProto;
    }
}

export class GraknOptions extends Opts.Core {

    constructor(obj: { [K in keyof Opts.Core]: Opts.Core[K] } = {}) {
        super();
        Object.assign(this, obj);
    }

    public isCluster(): boolean {
        return false;
    }

    proto(): Options {
        return Opts.proto(this);
    }
}

export class GraknClusterOptions extends Opts.Cluster {

    constructor(obj: { [K in keyof Opts.Cluster]: Opts.Cluster[K] } = {}) {
        super();
        Object.assign(this, obj);
    }

    public isCluster(): boolean {
        return true;
    }

    proto(): Options {
        return Opts.proto(this);
    }
}

export namespace GraknOptions {

    export function core(options: { [K in keyof Opts.Core]: Opts.Core[K] } = {}) {
        return new GraknOptions(options);
    }

    export function cluster(options: { [K in keyof Opts.Cluster]: Opts.Cluster[K] } = {}) {
        return new GraknClusterOptions(options);
    }
}
